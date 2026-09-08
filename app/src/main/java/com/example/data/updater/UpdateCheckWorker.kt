package com.example.data.updater

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker responsible for checking for official FiveLight app updates.
 *
 * Characteristics:
 * - Strictly constrained to active network connectivity (NetworkType.CONNECTED).
 * - Offloads completely to background IO dispatcher (no UI thread impact).
 * - Queries only the official repository release endpoint.
 * - Idempotent and handles notification deduplication.
 * - Periodic interval of 24 hours.
 */
class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "UpdateCheckWorker"
        private const val UNIQUE_ONE_TIME_WORK_NAME = "fivelight_update_check_one_time"
        private const val UNIQUE_PERIODIC_WORK_NAME = "fivelight_update_check_periodic"
        private const val KEY_FORCE_CHECK = "key_force_check"

        /**
         * Enqueues a one-time background update check with network constraints.
         */
        fun enqueueOneTime(context: Context, force: Boolean = false) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<UpdateCheckWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(KEY_FORCE_CHECK to force))
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        15,
                        TimeUnit.MINUTES
                    )
                    .build()

                val policy = if (force) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP
                WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_ONE_TIME_WORK_NAME,
                    policy,
                    workRequest
                )
                Log.d(TAG, "Enqueued one-time update check worker (force=$force)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue one-time update check worker: ${e.message}")
            }
        }

        /**
         * Enqueues periodic background update check with network constraints.
         * Runs approximately once every 24 hours.
         */
        fun enqueuePeriodic(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                    24, TimeUnit.HOURS,
                    4, TimeUnit.HOURS
                )
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        30,
                        TimeUnit.MINUTES
                    )
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    UNIQUE_PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
                )
                Log.d(TAG, "Enqueued periodic update check worker (interval=24h)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue periodic update check worker: ${e.message}")
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val force = inputData.getBoolean(KEY_FORCE_CHECK, false)
        Log.d(TAG, "UpdateCheckWorker running in background (force=$force)")

        try {
            val updateManager = AppUpdateManager.getInstance(applicationContext)
            val state = updateManager.checkForUpdates(force = force)

            when (state) {
                is UpdateState.UpdateAvailable -> {
                    Log.i(TAG, "Background update check found update: v${state.releaseInfo.versionName} (build ${state.releaseInfo.versionCode})")
                    Result.success()
                }
                is UpdateState.UpToDate -> {
                    Log.d(TAG, "Background update check: App is up to date.")
                    Result.success()
                }
                is UpdateState.Error -> {
                    if (state.isNetworkError && runAttemptCount < 3) {
                        Log.w(TAG, "Background update check encountered network error: ${state.message}. Retrying...")
                        Result.retry()
                    } else {
                        Log.w(TAG, "Background update check encountered error: ${state.message}")
                        Result.success()
                    }
                }
                else -> Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "UpdateCheckWorker execution failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
