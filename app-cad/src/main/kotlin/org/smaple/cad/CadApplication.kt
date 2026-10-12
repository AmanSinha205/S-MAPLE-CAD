package org.smaple.cad

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for S-MAPLE CAD (Healthcare Professional / Reader app).
 * Initialized with Hilt for dependency injection.
 */
@HiltAndroidApp
class CadApplication : Application()
