package com.example.bluetoothautopush

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TransferScheduler {
    private const val KEY_DEVICE_ADDRESS = "device_address"

    fun enqueueNow(context: Context, deviceAddress: String?) {
        val input = Data.Builder()
            .putString(KEY_DEVICE_ADDRESS, deviceAddress)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = OneTimeWorkRequestBuilder<TransferWorker>()
            .setInputData(input)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
