package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppStartupSignupGuestTest {

    @Test
    fun testColdLaunchAndGuestMode() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Clear prefs to simulate cold fresh launch
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("fivelight_guest_perms", Context.MODE_PRIVATE).edit().clear().commit()

        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity = controller.create().start().resume().visible().get()
        assertNotNull(activity)

        // Advance looper to process compose frames and splash
        ShadowLooper.idleMainLooper(3, TimeUnit.SECONDS)

        println("COLD_LAUNCH_SUCCESS")
    }
}
