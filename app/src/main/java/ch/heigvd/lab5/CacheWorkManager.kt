package ch.heigvd.lab5

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CacheWorkManager(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val files = applicationContext.cacheDir.listFiles()
        for (file in files) {
            file.delete()
        }
        return Result.success()
    }
}