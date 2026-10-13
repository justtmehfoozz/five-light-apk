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
class SimpleLaunchTest {

    @Test
    fun testActivityLaunch() {
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val activity = controller.setup().get()
        assertNotNull(activity)
        
        ShadowLooper.idleMainLooper(10, TimeUnit.SECONDS)
        println("LAUNCH_SUCCESS")
    }
}
