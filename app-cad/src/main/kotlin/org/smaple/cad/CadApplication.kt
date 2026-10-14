package org.smaple.cad

import android.app.Activity
import android.app.Application
import android.os.Bundle
import dagger.hilt.android.HiltAndroidApp
import java.lang.ref.WeakReference

/**
 * Base Application class for S-MAPLE CAD (Healthcare Professional / Reader app).
 * Initialized with Hilt for dependency injection.
 * Tracks foreground Activity lifecycle to enable NFC Reader Mode.
 */
@HiltAndroidApp
class CadApplication : Application() {

    companion object {
        var currentActivity: WeakReference<Activity>? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                if (currentActivity?.get() === activity) {
                    currentActivity = null
                }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                currentActivity = WeakReference(activity)
            }

            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (currentActivity?.get() === activity) {
                    currentActivity = null
                }
            }
        })
    }
}
