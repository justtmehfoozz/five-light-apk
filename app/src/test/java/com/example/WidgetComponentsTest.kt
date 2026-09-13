package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.widget.ayah.DailyAyahWidget
import com.example.widget.prayer.PrayerArcRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WidgetComponentsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testPrayerArcRendererGeneratesValidBitmap() {
        val bitmapDay = PrayerArcRenderer.generateArcBitmap(
            widthPx = 280,
            heightPx = 70,
            progressFraction = 0.45f,
            isNight = false,
            isDarkMode = true
        )
        assertNotNull(bitmapDay)
        assertEquals(280, bitmapDay.width)
        assertEquals(70, bitmapDay.height)

        val bitmapNight = PrayerArcRenderer.generateArcBitmap(
            widthPx = 200,
            heightPx = 60,
            progressFraction = 1.0f,
            isNight = true,
            isDarkMode = false
        )
        assertNotNull(bitmapNight)
        assertEquals(200, bitmapNight.width)
        assertEquals(60, bitmapNight.height)
    }

    @Test
    fun testDailyAyahWidgetRetrievalAndCycle() {
        val ayah1 = DailyAyahWidget.getDailyAyah(context)
        assertNotNull(ayah1)
        assertTrue(ayah1.arabic.isNotBlank())
        assertTrue(ayah1.translation.isNotBlank())
        assertTrue(ayah1.reference.isNotBlank())

        DailyAyahWidget.cycleNextAyah(context)
        val ayah2 = DailyAyahWidget.getDailyAyah(context)
        assertNotNull(ayah2)
        assertTrue(ayah2.arabic.isNotBlank())
    }
}
