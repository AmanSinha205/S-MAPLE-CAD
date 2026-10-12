package org.smaple.mcard

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for S-MAPLE mCard (Patient app).
 * Initialized with Hilt for dependency injection.
 */
@HiltAndroidApp
class MCardApplication : Application()
