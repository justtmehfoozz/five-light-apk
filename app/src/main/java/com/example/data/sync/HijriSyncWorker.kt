package com.example.data.sync

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
import com.example.data.repository.IslamicDateRepository
import com.example.data.util.HijriCalc
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker responsible for synchronizing verified Hijri date mappings.
 *
 * Characteristics:
 * - Strictly constrained to active network connectivity (NetworkType.CONNECTED).
 * - Offloads completely to background IO dispatcher (no UI thread impact).
 * - Handles exponential backoff retry on temporary network failure.
 * - Idempotent and safe to run multiple times without duplicating database records.
 * - Decoupled from sunset/Maghrib boundary calculation.
 */
class HijriSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "HijriSyncWorker"
        private const val UNIQUE_ONE_TIME_WORK_NAME = "hijri_date_sync_one_time"
        private const val UNIQUE_PERIODIC_WORK_NAME = "hijri_date_sync_periodic"
        private const val KEY_FORCE_SYNC = "key_force_sync"

        /**
         * Enqueues a one-time background synchronization request with network constraints
         * and exponential backoff retry.
         */
        fun enqueueOneTime(context: Context, force: Boolean = false) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<HijriSyncWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(KEY_FORCE_SYNC to force))
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
                Log.d(TAG, "Enqueued one-time Hijri date sync worker (force=$force)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue one-time sync worker: ${e.message}")
            }
        }

        /**
         * Enqueues daily periodic background sync with network constraints.
         * Runs at most once every 24 hours without aggressive polling.
         */
        fun enqueuePeriodic(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicRequest = PeriodicWorkRequestBuilder<HijriSyncWorker>(
                    24, TimeUnit.HOURS,
                    4, TimeUnit.HOURS // flex interval
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
                Log.d(TAG, "Enqueued periodic 24h Hijri date sync worker")
            } catch (e: Exception) {
                Log.w(TAG, "Could not enqueue periodic sync worker: ${e.message}")
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val force = inputData.getBoolean(KEY_FORCE_SYNC, false)
        Log.d(TAG, "Executing Hijri date sync work (runAttemptCount=$runAttemptCount, force=$force)")

        try {
            val repository = IslamicDateRepository.getInstance(applicationContext)
            val currentState = repository.islamicDateState.value

            val syncSuccess = HijriCalc.syncWithInternet(
                context = applicationContext,
                date = currentState.gregorianDate,
                method = currentState.method,
                cityName = currentState.cityName,
                latitude = currentState.latitude,
                longitude = currentState.longitude,
                force = force
            )

            if (syncSuccess) {
                Log.d(TAG, "Hijri date background sync successful. Refreshing IslamicDateRepository.")
                repository.refresh()
                Result.success()
            } else {
                if (runAttemptCount < 3) {
                    Log.w(TAG, "Hijri date sync unsuccessful on attempt $runAttemptCount. Retrying with exponential backoff.")
                    Result.retry()
                } else {
                    Log.w(TAG, "Hijri date sync max attempts reached. Falling back gracefully to verified cached baseline.")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Hijri date sync worker execution: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
