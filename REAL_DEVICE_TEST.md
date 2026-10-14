# S-MAPLE Phone-to-Phone NFC Real Device Testing Guide

This guide describes the complete procedure for testing contactless patient transfer between two physical Android phones:
- **Phone A (CAD):** Healthcare Professional Reader app (`app-cad`).
- **Phone B (mCard):** Patient Host Card Emulation app (`app-mcard`).

---

## 1. Device Requirements

| Role | App | Minimum Android Version | Hardware Requirement |
|---|---|---|---|
| **Reader (PCD)** | S-MAPLE CAD (`app-cad`) | Android 8.0 (API 26+) | Physical NFC controller with ISO 14443-4 Reader Mode support |
| **Emulated Card (PICC)** | S-MAPLE mCard (`app-mcard`) | Android 8.0 (API 26+) | Physical NFC controller with Host-based Card Emulation (HCE) support |

> [!IMPORTANT]
> Emulators cannot emulate physical NFC radio waves or HCE phone-to-phone contact. Testing this contactless flow requires two physical Android devices.

---

## 2. Prerequisites & Building APKs

### Step 2.1: Build Debug APKs
Run from the repository root:
```powershell
./gradlew assembleDebug
```
This generates the two APKs:
- `app-cad/build/outputs/apk/debug/app-cad-debug.apk`
- `app-mcard/build/outputs/apk/debug/app-mcard-debug.apk`

### Step 2.2: Verify Connected Devices
Connect both devices via USB (or wireless ADB) and check their serial IDs:
```powershell
adb devices
```
*Example output:*
```
List of devices attached
DEVICE_CAD_SERIAL       device
DEVICE_MCARD_SERIAL     device
```

### Step 2.3: Install APKs

Install **CAD** on Phone A:
```powershell
adb -s <DEVICE_CAD_SERIAL> install -r app-cad/build/outputs/apk/debug/app-cad-debug.apk
```

Install **mCard** on Phone B:
```powershell
adb -s <DEVICE_MCARD_SERIAL> install -r app-mcard/build/outputs/apk/debug/app-mcard-debug.apk
```

---

## 3. Device Configuration (How to Enable NFC)

On **both** devices:
1. Open **Settings** -> **Connected devices** -> **Connection preferences** -> **NFC**.
2. Toggle **Use NFC** to **ON**.
3. Under **Contactless payments** / **Default payment service**:
   - Ensure NFC payments or non-payment HCE is enabled.
   - Set "Use default" to **Except when another payment app is open** or ensure foreground apps can take precedence.
4. **Unlock both screens**: Android NFC antennas are powered off when the screen is locked. Keep both phone displays awake and unlocked during the test.

---

## 4. End-to-End Test Steps

1. **Launch mCard on Phone B (Patient):**
   - Open **S-MAPLE mCard**.
   - Observe the home screen showing:
     - Token state: **Ready** (green accent)
     - Headline: **Ready**
     - Instruction: **"Hold against doctor's phone"**
   - Keep this screen visible in the foreground.

2. **Launch CAD on Phone A (Doctor):**
   - Open **S-MAPLE CAD**.
   - From the Home screen, tap **"Tap patient's mCard"** (or the scan trigger).
   - The CAD app navigates to the **Scan** screen.
   - The token indicates: **"Hold phone flat against the patient's card or phone"**.
   - `AndroidNfcCardReaderManager` activates `NfcAdapter.enableReaderMode` with `FLAG_READER_NFC_A` and `FLAG_READER_SKIP_NDEF_CHECK`.

3. **Contactless Tap:**
   - Hold the backs of Phone A and Phone B flat against each other.
   - Gently adjust the position until the NFC coils align (usually near the top camera bump or center back).
   - Phone A detects the contactless tag and initiates the ISO 7816-4 APDU handshake.

4. **Automatic Navigation:**
   - Upon successful receipt and decoding of the basic info block, Phone A automatically navigates to **`PatientFoundScreen`**.

---

## 5. Expected Patient Data on CAD Screen

On Phone A (**`PatientFoundScreen`**), verify the decoded patient profile:

| Field | Expected Display Value | Source |
|---|---|---|
| **Patient ID** | `AV-2041` | Spec test vector |
| **Name** | `Asha Verma` | Spec test vector |
| **Age** | Current age calculated from `1972-05-20` (e.g. `54` in 2026) | Computed from `dateOfBirth` |
| **Gender** | `Female` | Code `1` |
| **Medical Summary** | `Blood Group: B+` | Code `3` (`B+`) |
| **Risk Status** | `Low` | Default initialization |

---

## 6. Live Logcat Verification

To observe the APDU handshake in real-time, open two terminal windows.

### Terminal 1 (CAD Reader Logs):
```powershell
adb -s <DEVICE_CAD_SERIAL> logcat -s SMaple-CAD
```

**Expected Log Output on Phone A:**
```text
I/SMaple-CAD: NFC Reader Mode scanning started. Waiting for contactless card/phone...
D/SMaple-CAD: Calling NfcAdapter.enableReaderMode on MainActivity
I/SMaple-CAD: NFC tag detected! ID=..., Techs=IsoDep, NfcA
D/SMaple-CAD: Connecting to IsoDep target...
I/SMaple-CAD: IsoDep connected successfully. MaxTransceiveLength=..., Timeout=3000ms
I/SMaple-CAD: SELECT S-MAPLE AID sent: 00A4040006F01234567821
I/SMaple-CAD: SELECT response received: SW=0x9000 (Success=true)
I/SMaple-CAD: GET_BASIC_INFO sent: 8002000000
I/SMaple-CAD: GET_BASIC_INFO response received: SW=0x9000, DataLength=23 bytes (104C67450741562D323034310A41736861205665726D61)
I/SMaple-CAD: Decoded BasicInfo: ID=AV-2041, Name='Asha Verma', Gender=FEMALE, BloodGroup=B+, DOB=1972-05-20, Age=54
D/SMaple-CAD: IsoDep connection closed cleanly
```

---

### Terminal 2 (mCard HCE Logs):
```powershell
adb -s <DEVICE_MCARD_SERIAL> logcat -s SMaple-mCard
```

**Expected Log Output on Phone B:**
```text
I/SMaple-mCard: APDU received: 00A4040006F01234567821 (11 bytes)
I/SMaple-mCard: Command type: SELECT APPLICATION (AID)
I/SMaple-mCard: S-MAPLE applet selected successfully (AID: F01234567821)
I/SMaple-mCard: Response generated: SW=0x9000, TotalBytes=2 (9000)
I/SMaple-mCard: APDU received: 8002000000 (5 bytes)
I/SMaple-mCard: Command type: GET_BASIC_INFO (0x02)
I/SMaple-mCard: Encoded BasicInfo for patient 'Asha Verma' (23 bytes: 104C67450741562D323034310A41736861205665726D61)
I/SMaple-mCard: Response generated: SW=0x9000, TotalBytes=25 (104C67450741562D323034310A41736861205665726D619000)
I/SMaple-mCard: HCE contactless session deactivated: LINK_LOSS
```

---

## 7. Troubleshooting

| Symptom | Cause | Solution |
|---|---|---|
| No reaction when phones touch | Screen locked or NFC off | Unlock both phones; check NFC toggle in Settings. |
| Tag detected, but SW `6A88` on SELECT | Wrong AID selected | Verify `F01234567821` in both `CardCommands.SMAPLE_AID` and `apduservice.xml`. |
| `TagLostException` or transceive timeout | Phones separated too quickly | Keep phones held together until the CAD screen navigates. |
| `IsoDep` null on tag discovery | Target device NFC chip didn't enter HCE mode | Ensure `app-mcard` is in the foreground on Phone B. |
