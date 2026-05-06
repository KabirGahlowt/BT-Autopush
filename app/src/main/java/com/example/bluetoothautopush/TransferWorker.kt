package com.example.bluetoothautopush

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransferWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return Result.failure()
        if (!adapter.isEnabled) return Result.retry()

        val targetAddress = inputData.getString("device_address")
        val candidates = adapter.bondedDevices.filter { it.isLikelyComputer() }
            .filter { targetAddress == null || it.address == targetAddress }

        if (candidates.isEmpty()) return Result.retry()

        val payloadUris = buildPayloadUris(applicationContext)
        if (payloadUris.isEmpty()) return Result.failure()

        val success = sendViaBluetoothOpp(applicationContext, payloadUris)
        val retries = Prefs.getRetryCount(applicationContext)
        return when {
            success -> {
                Prefs.setRetryCount(applicationContext, 0)
                Result.success()
            }
            retries >= 5 -> {
                Prefs.setRetryCount(applicationContext, 0)
                Result.failure()
            }
            else -> {
                Prefs.setRetryCount(applicationContext, retries + 1)
                Result.retry()
            }
        }
    }

    private fun BluetoothDevice.isLikelyComputer(): Boolean {
        val major = bluetoothClass?.majorDeviceClass
        return major == BluetoothClass.Device.Major.COMPUTER || name?.contains("ubuntu", true) == true
    }

    private fun buildPayloadUris(context: Context): List<Uri> {
        val out = mutableListOf<Uri>()
        val queueDir = File(context.filesDir, "queue").apply { mkdirs() }

        val generated = File(queueDir, "auto_text_${timestamp()}.txt")
        generated.writeText(Prefs.getTemplateText(context))
        out.add(fileUri(context, generated))

        queueDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase(Locale.US) in setOf("pdf", "txt", "jpg", "png") }
            ?.forEach { out.add(fileUri(context, it)) }

        return out
    }

    private fun fileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun sendViaBluetoothOpp(context: Context, uris: List<Uri>): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "*/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.android.bluetooth")
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun timestamp(): String {
        return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    }
}
