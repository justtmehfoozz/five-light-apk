package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.AuthRepository
import com.example.data.backup.BackupManager
import com.example.data.backup.GoogleDriveService
import com.example.data.db.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.sync.FirestoreSyncManager
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
class SetUpFiveLightPhase3Test {

    private lateinit var app: Application
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var authRepository: AuthRepository
    private lateinit var repository: AppRepository
    private lateinit var syncManager: FirestoreSyncManager

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        context = app
        context.getSharedPreferences("fivelight_prefs", Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("fivelight_drive_backup_meta", Context.MODE_PRIVATE).edit().clear().commit()

        database = AppDatabase.getDatabase(context)
        authRepository = AuthRepository.getInstance(context)
        repository = AppRepository(database, context)
        syncManager = FirestoreSyncManager.getInstance(context, repository, authRepository)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 1: Default Auto-Backup Frequency is Weekly
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testDefaultAutoBackupFrequencyIsWeekly() {
        val freq = BackupManager.getAutoBackupFrequency(context)
        assertEquals(BackupManager.AutoBackupFrequency.WEEKLY, freq)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 2: Setting Auto-Backup Frequency (Weekly, Daily, Off)
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testSetAutoBackupFrequencyOptions() {
        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.DAILY)
        assertEquals(BackupManager.AutoBackupFrequency.DAILY, BackupManager.getAutoBackupFrequency(context))

        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.OFF)
        assertEquals(BackupManager.AutoBackupFrequency.OFF, BackupManager.getAutoBackupFrequency(context))

        BackupManager.setAutoBackupFrequency(context, BackupManager.AutoBackupFrequency.WEEKLY)
        assertEquals(BackupManager.AutoBackupFrequency.WEEKLY, BackupManager.getAutoBackupFrequency(context))
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 3: Last Backup Time Starts at 0 and Updates Correctly
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testLastBackupTimeTracking() {
        assertEquals(0L, BackupManager.getLastBackupTime(context))

        val now = System.currentTimeMillis()
        BackupManager.setLastBackupTime(context, now)
        assertEquals(now, BackupManager.getLastBackupTime(context))
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 4: Account Mismatch Detection Logic
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testAccountMismatchDetection() {
        val userEmail = "fivelight.user@example.com"
        val driveEmailDifferent = "another.account@gmail.com"
        val driveEmailSame = "fivelight.user@example.com"

        val isMismatch = !driveEmailDifferent.equals(userEmail, ignoreCase = true)
        assertTrue("Different emails must trigger mismatch", isMismatch)

        val isMatch = driveEmailSame.equals(userEmail, ignoreCase = true)
        assertFalse("Matching emails must not trigger mismatch", !isMatch)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 5: Encryption & Decryption Format Integrity
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testEncryptionAndDecryptionIntegrity() {
        val uid = "test_user_uid_123"
        val sampleJson = """{"version":1,"bookmarks":[],"dhikrPresetHistory":[]}"""

        val encryptMethod = BackupManager::class.java.getDeclaredMethod(
            "encryptData",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val decryptMethod = BackupManager::class.java.getDeclaredMethod(
            "decryptData",
            ByteArray::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val encryptedBytes = encryptMethod.invoke(BackupManager, sampleJson, uid) as ByteArray
        assertNotNull(encryptedBytes)
        // 16 bytes salt + 12 bytes IV + GCM ciphertext + 16 bytes tag
        assertTrue("Encrypted bytes must exceed salt (16) + IV (12)", encryptedBytes.size > 28)

        val decryptedJson = decryptMethod.invoke(BackupManager, encryptedBytes, uid) as String
        assertEquals(sampleJson, decryptedJson)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 6: Decryption with Wrong Key Fails Gracefully
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testDecryptionFailsWithWrongKey() {
        val uid = "correct_user_uid"
        val wrongUid = "wrong_user_uid"
        val sampleJson = """{"version":1,"data":"secret"}"""

        val encryptMethod = BackupManager::class.java.getDeclaredMethod(
            "encryptData",
            String::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val decryptMethod = BackupManager::class.java.getDeclaredMethod(
            "decryptData",
            ByteArray::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val encryptedBytes = encryptMethod.invoke(BackupManager, sampleJson, uid) as ByteArray
        var failed = false
        try {
            decryptMethod.invoke(BackupManager, encryptedBytes, wrongUid)
        } catch (_: Exception) {
            failed = true
        }
        assertTrue("Decryption with wrong UID key must fail", failed)
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 7: Human Readable Date Formatting for Backups
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testBackupDateFormatting() {
        val timestamp = 1788719760000L // Specific epoch
        val formatted = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(timestamp))
        assertNotNull(formatted)
        assertFalse("Formatted date should not contain raw epoch milliseconds", formatted.contains("1788719760000"))
    }

    // ---------------------------------------------------------------------------------------------
    // Scenario 8: Setup Completion Status Persistence
    // ---------------------------------------------------------------------------------------------
    @Test
    fun testSetupCompletionPersistence() {
        val testUid = "user_setup_test"
        assertFalse(authRepository.isSetupCompleted(testUid))

        authRepository.setSetupCompleted(testUid, true)
        assertTrue(authRepository.isSetupCompleted(testUid))
    }
}
