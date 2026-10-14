package org.smaple.cad.reader

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.smaple.cad.CadApplication
import org.smaple.core.cardtransport.CardCommands
import org.smaple.core.cardtransport.CardTransportException
import org.smaple.core.cardtransport.IsoDepCardTransport
import org.smaple.core.cardtransport.NfcDebugConfig
import org.smaple.core.codec.CodecResult
import org.smaple.core.codec.basicinfo.BasicInfoCodec
import org.smaple.core.codec.util.ByteBuffers
import org.smaple.core.model.BasicInfo
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production Android NFC implementation of [CardReaderManager].
 *
 * Utilizes [NfcAdapter.enableReaderMode] configured with:
 * - [NfcAdapter.FLAG_READER_NFC_A]
 * - [NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK]
 *
 * Communicates via ISO 14443-4 [IsoDep] to:
 * 1. SELECT the S-MAPLE AID (00 A4 04 00 06 F0 12 34 56 78 21).
 * 2. Execute GET_BASIC_INFO (80 02 00 00 00).
 * 3. Decode the returned basic info payload into [BasicInfo].
 *
 * ## Lifecycle
 * - [startScanning] must be called when the Scan screen becomes visible.
 * - [cancelScanning] must be called in the Scan screen's `onDispose` / `onStop`.
 * - Internally uses a [SupervisorJob] so individual tag-handling failures
 *   never cancel the parent scanning coroutine.
 *
 * ## Error Handling
 * Failures are surfaced through [CardDiscoveryState.Error] with a specific
 * [NfcErrorReason] so the UI layer can show an actionable message.
 *
 * ## Debug Logging
 * Controlled by [NfcDebugConfig.ENABLED]. When disabled, APDU hex bytes
 * and patient PII are NOT written to logcat.
 */
@Singleton
class AndroidNfcCardReaderManager(
    private val context: Context,
    private val basicInfoCodec: BasicInfoCodec,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CardReaderManager, NfcAdapter.ReaderCallback {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        basicInfoCodec: BasicInfoCodec
    ) : this(context, basicInfoCodec, Dispatchers.IO)

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _discoveryState = MutableStateFlow<CardDiscoveryState>(CardDiscoveryState.Idle)
    override val discoveryState: StateFlow<CardDiscoveryState> = _discoveryState.asStateFlow()

    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(context)
    }

    private var pendingBasicInfoDeferred: CompletableDeferred<BasicInfo>? = null
    private var activeTransport: IsoDepCardTransport? = null

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    override suspend fun startScanning() {
        if (_discoveryState.value == CardDiscoveryState.Scanning) {
            Log.d(TAG, "startScanning() called but already in Scanning state — ignoring")
            return
        }

        val adapter = nfcAdapter
        if (adapter == null) {
            val msg = "NFC hardware adapter not available on this device"
            Log.e(TAG, "Cannot start scanning: $msg")
            transitionState(CardDiscoveryState.Error(NfcErrorReason.NO_NFC_HARDWARE, msg))
            return
        }
        if (!adapter.isEnabled) {
            val msg = "NFC is disabled in device settings — please enable it and retry"
            Log.w(TAG, "Cannot start scanning: $msg")
            transitionState(CardDiscoveryState.Error(NfcErrorReason.NFC_DISABLED, msg))
            return
        }

        pendingBasicInfoDeferred = CompletableDeferred()
        transitionState(CardDiscoveryState.Scanning)
        Log.i(TAG, "NFC Reader Mode scanning started — waiting for contactless card/phone...")

        val activity = CadApplication.currentActivity?.get()
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            enableNfcReaderMode(activity)
        } else {
            Log.w(TAG, "No resumed foreground Activity available to attach enableReaderMode — " +
                    "reader mode will be enabled when Activity becomes available")
        }
    }

    override fun cancelScanning() {
        Log.i(TAG, "Cancelling NFC scanning session")
        pendingBasicInfoDeferred?.cancel()
        pendingBasicInfoDeferred = null

        val activity = CadApplication.currentActivity?.get()
        if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
            disableNfcReaderMode(activity)
        } else {
            Log.d(TAG, "cancelScanning(): no foreground Activity — disableReaderMode skipped")
        }

        scope.launch {
            try {
                activeTransport?.close()
            } catch (e: Exception) {
                Log.d(TAG, "Exception while closing active transport during cancel: ${e.message}")
            } finally {
                activeTransport = null
            }
        }

        transitionState(CardDiscoveryState.Idle)
    }

    override suspend fun getBasicInfo(): BasicInfo {
        val deferred = pendingBasicInfoDeferred
            ?: throw IllegalStateException("Scanning has not been started. Call startScanning() first.")
        return deferred.await()
    }

    // -------------------------------------------------------------------------
    // NfcAdapter.ReaderCallback
    // -------------------------------------------------------------------------

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) {
            Log.w(TAG, "onTagDiscovered() called with null tag — ignoring")
            return
        }
        Log.i(TAG, "NFC tag detected! ID=${ByteBuffers.toHexString(tag.id)}, " +
                "Techs=${tag.techList.joinToString { it.substringAfterLast('.') }}")
        scope.launch {
            handleTagDiscovered(tag)
        }
    }

    // -------------------------------------------------------------------------
    // APDU communication sequence (internal, package-private for unit tests)
    // -------------------------------------------------------------------------

    /**
     * Executes the full APDU handshake sequence on a discovered tag:
     * 1. Connect to IsoDep
     * 2. SELECT S-MAPLE AID → expect 90 00
     * 3. GET_BASIC_INFO → decode payload with BasicInfoCodec
     *
     * All errors result in a structured [CardDiscoveryState.Error] state and
     * an exceptional completion of [pendingBasicInfoDeferred].
     */
    internal suspend fun handleTagDiscovered(tag: Tag) {
        val isoDep = IsoDep.get(tag)
        if (isoDep == null) {
            val msg = "Detected NFC tag does not support ISO 14443-4 (IsoDep). " +
                    "Technologies present: ${tag.techList.joinToString()}"
            Log.e(TAG, msg)
            failWithError(NfcErrorReason.NOT_ISO_DEP, msg,
                CardTransportException(msg))
            return
        }

        val transport = IsoDepCardTransport(isoDep)
        activeTransport = transport

        try {
            // ── Step 1: Connect ──────────────────────────────────────────────
            Log.d(TAG, "Connecting to IsoDep target...")
            try {
                transport.connect()
            } catch (e: TagLostException) {
                val msg = "Card was removed before IsoDep connection could be established"
                Log.e(TAG, msg)
                failWithError(NfcErrorReason.TAG_LOST, msg, e)
                return
            } catch (e: Exception) {
                val msg = "IsoDep connection failed: ${e.message}"
                Log.e(TAG, msg, e)
                failWithError(NfcErrorReason.CONNECTION_FAILED, msg, e)
                return
            }
            Log.i(TAG, "IsoDep connected. MaxTransceiveLength=${isoDep.maxTransceiveLength}, Timeout=${isoDep.timeout}ms")
            transitionState(CardDiscoveryState.CardDetected)

            // ── Step 2: SELECT S-MAPLE AID ────────────────────────────────────
            val selectCommand = CardCommands.selectApplication(CardCommands.SMAPLE_AID)
            val selectBytes = selectCommand.toByteArray()
            if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_APDU_BYTES) {
                Log.d(TAG, "TX SELECT AID: ${ByteBuffers.toHexString(selectBytes)}")
            } else {
                Log.i(TAG, "SELECT S-MAPLE AID sent (${selectBytes.size} bytes)")
            }

            val selectResponse = try {
                transport.transceiveApdu(selectCommand)
            } catch (e: TagLostException) {
                val msg = "Card lost during SELECT AID exchange"
                Log.e(TAG, msg)
                failWithError(NfcErrorReason.TAG_LOST, msg, e)
                return
            } catch (e: CardTransportException) {
                val msg = "Transport error during SELECT AID: ${e.message}"
                Log.e(TAG, msg, e)
                failWithError(NfcErrorReason.CONNECTION_FAILED, msg, e)
                return
            }

            if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_RESPONSE_BYTES) {
                Log.d(TAG, "RX SELECT AID: SW=0x${selectResponse.sw.toString(16).uppercase().padStart(4,'0')} " +
                        "data=${ByteBuffers.toHexString(selectResponse.data)}")
            } else {
                Log.i(TAG, "SELECT response: SW=0x${selectResponse.sw.toString(16).uppercase().padStart(4,'0')} " +
                        "(Success=${selectResponse.isSuccess})")
            }

            if (!selectResponse.isSuccess) {
                val msg = "S-MAPLE applet not found on card — " +
                        "SW=0x${selectResponse.sw.toString(16).uppercase()} " +
                        "(expected 9000). Is mCard installed and NFC enabled?"
                Log.e(TAG, msg)
                failWithError(NfcErrorReason.WRONG_AID_RESPONSE, msg,
                    CardTransportException(msg))
                return
            }
            Log.i(TAG, "S-MAPLE AID selected successfully")

            // ── Step 3: GET_BASIC_INFO ────────────────────────────────────────
            val getBasicInfoCommand = CardCommands.getBasicInfo()
            val getBasicInfoBytes = getBasicInfoCommand.toByteArray()
            if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_APDU_BYTES) {
                Log.d(TAG, "TX GET_BASIC_INFO: ${ByteBuffers.toHexString(getBasicInfoBytes)}")
            } else {
                Log.i(TAG, "GET_BASIC_INFO sent (${getBasicInfoBytes.size} bytes)")
            }

            val basicInfoResponse = try {
                transport.transceiveApdu(getBasicInfoCommand)
            } catch (e: TagLostException) {
                val msg = "Card lost during GET_BASIC_INFO exchange"
                Log.e(TAG, msg)
                failWithError(NfcErrorReason.TAG_LOST, msg, e)
                return
            } catch (e: CardTransportException) {
                val msg = "Transport error during GET_BASIC_INFO: ${e.message}"
                Log.e(TAG, msg, e)
                failWithError(NfcErrorReason.CONNECTION_FAILED, msg, e)
                return
            }

            if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_RESPONSE_BYTES) {
                Log.d(TAG, "RX GET_BASIC_INFO: SW=0x${basicInfoResponse.sw.toString(16).uppercase().padStart(4,'0')} " +
                        "DataLen=${basicInfoResponse.data.size} bytes ${ByteBuffers.toHexString(basicInfoResponse.data)}")
            } else {
                Log.i(TAG, "GET_BASIC_INFO response: SW=0x${basicInfoResponse.sw.toString(16).uppercase().padStart(4,'0')} " +
                        "DataLen=${basicInfoResponse.data.size} bytes")
            }

            if (!basicInfoResponse.isSuccess) {
                val msg = "GET_BASIC_INFO returned error: " +
                        "SW=0x${basicInfoResponse.sw.toString(16).uppercase()} " +
                        "(expected 9000)"
                Log.e(TAG, msg)
                failWithError(NfcErrorReason.GET_BASIC_INFO_FAILED, msg,
                    CardTransportException(msg))
                return
            }

            // ── Step 4: Decode with BasicInfoCodec ───────────────────────────
            val decodeResult = basicInfoCodec.decode(basicInfoResponse.data)
            when (decodeResult) {
                is CodecResult.Success -> {
                    val info = decodeResult.value
                    Log.i(TAG, "BasicInfo decoded successfully: " +
                            "ID=${info.patientId}, Name='${info.name}', " +
                            "Gender=${info.gender}, BloodGroup=${info.bloodGroup.displayLabel}, " +
                            "DOB=${info.dateOfBirth}, Age=${info.calculateAge()}")
                    pendingBasicInfoDeferred?.complete(info)
                }
                is CodecResult.Error -> {
                    val msg = "BasicInfoCodec failed to decode response: ${decodeResult.message}"
                    Log.e(TAG, msg, decodeResult.cause)
                    failWithError(NfcErrorReason.DECODE_FAILED, msg,
                        CardTransportException(msg, decodeResult.cause))
                }
            }

        } catch (e: Exception) {
            val msg = "Unexpected error during NFC communication: ${e.message}"
            Log.e(TAG, msg, e)
            failWithError(NfcErrorReason.UNKNOWN, msg, e)
        } finally {
            try {
                transport.close()
                Log.d(TAG, "IsoDep connection closed cleanly")
            } catch (_: Exception) {}
            if (activeTransport === transport) {
                activeTransport = null
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Transitions [_discoveryState] to [newState] and optionally logs the transition.
     */
    private fun transitionState(newState: CardDiscoveryState) {
        val old = _discoveryState.value
        _discoveryState.value = newState
        if (NfcDebugConfig.ENABLED && NfcDebugConfig.LOG_STATE_TRANSITIONS) {
            Log.d(TAG, "State: ${old::class.simpleName} → ${newState::class.simpleName}" +
                    if (newState is CardDiscoveryState.Error) " [${newState.reason}] ${newState.detail ?: ""}" else "")
        }
    }

    /**
     * Convenience method that sets an [CardDiscoveryState.Error] state and
     * completes the pending deferred exceptionally.
     */
    private fun failWithError(reason: NfcErrorReason, detail: String, cause: Exception) {
        transitionState(CardDiscoveryState.Error(reason, detail))
        pendingBasicInfoDeferred?.completeExceptionally(cause)
    }

    private fun enableNfcReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        activity.runOnUiThread {
            try {
                val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                val options = Bundle().apply {
                    putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
                }
                if (NfcDebugConfig.ENABLED) {
                    Log.d(TAG, "NfcAdapter.enableReaderMode() on ${activity.localClassName} " +
                            "flags=FLAG_READER_NFC_A|FLAG_READER_SKIP_NDEF_CHECK presenceDelay=250ms")
                }
                adapter.enableReaderMode(activity, this, flags, options)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable NFC reader mode: ${e.message}", e)
                transitionState(CardDiscoveryState.Error(NfcErrorReason.CONNECTION_FAILED,
                    "enableReaderMode() threw: ${e.message}"))
            }
        }
    }

    private fun disableNfcReaderMode(activity: Activity) {
        val adapter = nfcAdapter ?: return
        activity.runOnUiThread {
            try {
                if (NfcDebugConfig.ENABLED) {
                    Log.d(TAG, "NfcAdapter.disableReaderMode() on ${activity.localClassName}")
                }
                adapter.disableReaderMode(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to disable NFC reader mode: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val TAG = "SMaple-CAD"
    }
}
