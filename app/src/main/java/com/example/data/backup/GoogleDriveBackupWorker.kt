package com.example.data.backup

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.auth.AuthRepository
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker responsible for executing scheduled automatic backups
 * to the user's private Google Drive appDataFolder.
 *
 * Characteristics:
 * - Constrained to active network connectivity (NetworkType.CONNECTED) and non-low battery.
 * - Fully asynchronous on Dispatchers.IO.
 * - Graceful skip if user is not logged in or Drive is not authorized.
 * - Updates last backup timestamp upon successful completion.
 */
class GoogleDriveBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "GoogleDriveBackupWorker"
        const val UNIQUE_PERIODIC_WORK_NAME = "google_drive_auto_backup_periodic"

        /**
         * Schedules or cancels periodic auto-backup work based on the user's chosen frequency.
         */
        fun schedule(context: Context, frequency: BackupManager.AutoBackupFrequency) {
            val workManager = WorkManager.getInstance(context)
            when (frequency) {
                BackupManager.AutoBackupFrequency.OFF -> {
                    workManager.cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
                    Log.d(TAG, "Cancelled periodic Google Drive auto-backup work.")
                }
                BackupManager.AutoBackupFrequency.DAILY -> {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()

                    val periodicRequest = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(
                        24, TimeUnit.HOURS,
                        4, TimeUnit.HOURS
                    )
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            15,
                            TimeUnit.MINUTES
                        )
                        .build()

                    workManager.enqueueUniquePeriodicWork(
                        UNIQUE_PERIODIC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        periodicRequest
                    )
                    Log.d(TAG, "Enqueued DAILY periodic Google Drive auto-backup work.")
                }
                BackupManager.AutoBackupFrequency.WEEKLY -> {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()

                    val periodicRequest = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(
                        7, TimeUnit.DAYS,
                        12, TimeUnit.HOURS
                    )
                        .setConstraints(constraints)
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            30,
                            TimeUnit.MINUTES
                        )
                        .build()

                    workManager.enqueueUniquePeriodicWork(
                        UNIQUE_PERIODIC_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        periodicRequest
                    )
                    Log.d(TAG, "Enqueued WEEKLY periodic Google Drive auto-backup work.")
                }
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val currentFrequency = BackupManager.getAutoBackupFrequency(applicationContext)
        if (currentFrequency == BackupManager.AutoBackupFrequency.OFF) {
            Log.d(TAG, "Auto-backup frequency is OFF; cancelling scheduled work.")
            WorkManager.getInstance(applicationContext).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
            return@withContext Result.success()
        }

        Log.i(TAG, "Starting automatic Google Drive backup (frequency=$currentFrequency, attempt=$runAttemptCount)...")

        val authRepository = AuthRepository.getInstance(applicationContext)
        val user = authRepository.currentUser.value
        if (user == null) {
            Log.d(TAG, "Skipping auto-backup: user is not signed in.")
            return@withContext Result.success()
        }

        val googleAccount = GoogleDriveService.getAuthorizedAccount(applicationContext)
        if (googleAccount == null) {
            Log.d(TAG, "Skipping auto-backup: Google Drive account is not authorized.")
            return@withContext Result.success()
        }

        val appDataScopeString = "https://www.googleapis.com/auth/drive.appdata"
        val hasScope = googleAccount.grantedScopes.any { it.scopeUri.equals(appDataScopeString, ignoreCase = true) }
        if (!hasScope) {
            Log.w(TAG, "Skipping auto-backup: drive.appdata scope not granted.")
            return@withContext Result.success()
        }

        try {
            val repository = AppRepository.getInstance(applicationContext)
            val backupResult = BackupManager.performBackup(applicationContext, repository, authRepository)

            if (backupResult.isSuccess) {
                val backupTime = backupResult.getOrNull() ?: System.currentTimeMillis()
                Log.i(TAG, "Automatic Google Drive backup completed successfully at timestamp: $backupTime")
                Result.success()
            } else {
                val error = backupResult.exceptionOrNull()
                Log.w(TAG, "Automatic Google Drive backup failed: ${error?.message}")
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected exception during automatic Google Drive backup: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
