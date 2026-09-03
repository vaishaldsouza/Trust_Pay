package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.engine.SyncEngine
import java.util.concurrent.TimeUnit

class SupabaseSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SupabaseSyncWorker", "Starting background sync job with Supabase...")
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val syncEngine = SyncEngine(db)
            val result = syncEngine.runSyncPipeline()
            Log.d("SupabaseSyncWorker", "Sync complete. Settled: ${result.settledCount}, Flagged: ${result.reviewRequiredCount}")
            Result.success()
        } catch (e: Exception) {
            Log.e("SupabaseSyncWorker", "Sync worker error: ${e.message}")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "trustpay_supabase_sync"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SupabaseSyncWorker>()
                .setConstraints(constraints)
                .setInitialDelay(500, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}
