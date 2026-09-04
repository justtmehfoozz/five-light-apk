package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.updater.AppUpdateManager
import com.example.data.updater.ReleaseInfo
import com.example.data.updater.UpdateState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppUpdateManagerTest {

    private lateinit var context: Context
    private lateinit var updateManager: AppUpdateManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        updateManager = AppUpdateManager(context)
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(UpdateState.Idle, updateManager.updateState.value)
    }

    @Test
    fun testCurrentVersionProperties() {
        assertEquals("1.7", updateManager.currentVersionName)
        assertEquals(5L, updateManager.currentVersionCode)
    }

    @Test
    fun testApkValidationRejectsEmptyFile() {
        val tempApk = File(context.cacheDir, "test_empty.apk").apply {
            createNewFile()
        }

        val dummyRelease = ReleaseInfo(
            versionName = "1.7",
            versionCode = 5,
            tagName = "v1.7",
            name = "FiveLight v1.7",
            body = "New features",
            publishedAt = "2026-09-04T00:00:00Z",
            apkDownloadUrl = "https://github.com/mehfoozzshaikhh/FiveLight/releases/download/v1.7/fivelight.apk",
            apkFileName = "fivelight.apk",
            apkSize = 1024L
        )

        val result = updateManager.validateApk(tempApk, dummyRelease)
        assertTrue(result.isFailure)
        tempApk.delete()
    }

    @Test
    fun testInstallPermissionIntent() {
        val intent = updateManager.getInstallPermissionIntent()
        assertNotNull(intent)
        assertNotNull(intent.action)
    }
}
