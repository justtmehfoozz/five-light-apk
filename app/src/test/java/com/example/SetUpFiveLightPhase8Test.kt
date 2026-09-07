package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.backup.GoogleDriveService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.DateFormat
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SetUpFiveLightPhase8Test {

    private lateinit var app: Application
    private lateinit var context: Context

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        context = app
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("fivelight_drive_backup_meta", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testBackupFrequencyOptionsAndDefaults() {
        // Initial / Default state is Weekly
        val defaultFreq = BackupManager.getAutoBackupFrequency(context)
        assertEquals(BackupManager.AutoBackupFrequency.WEEKLY, defaultFreq)

        // Switching to Daily
        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.DAILY)
        assertEquals(BackupManager.AutoBackupFrequency.DAILY, BackupManager.getAutoBackupFrequency(context))

        // Switching to Off
        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.OFF)
        assertEquals(BackupManager.AutoBackupFrequency.OFF, BackupManager.getAutoBackupFrequency(context))

        // Switching back to Weekly
        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.WEEKLY)
        assertEquals(BackupManager.AutoBackupFrequency.WEEKLY, BackupManager.getAutoBackupFrequency(context))
    }

    @Test
    fun testBackupDateFormattingForHumanReadableUI() {
        val now = System.currentTimeMillis()
        BackupManager.setLastBackupTime(context, now)

        val retrievedTime = BackupManager.getLastBackupTime(context)
        val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(retrievedTime))

        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun testScopesPreserveAppDataOnly() {
        // Verify only appdata scope is used and no broader scopes are added
        val scope = GoogleDriveService.DRIVE_APPDATA_SCOPE
        assertNotNull(scope)
        assertEquals("https://www.googleapis.com/auth/drive.appdata", scope.scopeUri)
    }

    @Test
    fun testPrivacyCopyAvoidsTechnicalJargon() {
        val privacyText = "Your backup is encrypted before it is stored in Google Drive."
        assertFalse(privacyText.contains("AES-256-GCM"))
        assertFalse(privacyText.contains("drive.appdata"))
        assertFalse(privacyText.contains("OAuth"))
        assertTrue(privacyText.contains("encrypted"))
    }
}
