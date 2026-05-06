package com.example.bluetoothautopush

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var templateText: EditText
    private lateinit var statusText: TextView
    private val pickDocs =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            val count = importIntoQueue(uris)
            statusText.text = if (count > 0) "Added $count file(s) to queue." else "No files added."
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val title = TextView(this).apply {
            text = "Bluetooth Auto Push (Android -> Ubuntu)"
            textSize = 20f
        }
        val desc = TextView(this).apply {
            text =
                "When a PC pairs/connects, app enqueues predefined text and files and retries automatically."
        }
        templateText = EditText(this).apply {
            hint = "Default text to send"
            setText(Prefs.getTemplateText(this@MainActivity))
        }
        val saveTextButton = Button(this).apply {
            text = "Save Text Payload"
            setOnClickListener {
                Prefs.setTemplateText(this@MainActivity, templateText.text.toString())
                statusText.text = "Saved text payload."
            }
        }
        val addFilesButton = Button(this).apply {
            text = "Add PDF/files to queue"
            setOnClickListener {
                pickDocs.launch(arrayOf("application/pdf", "text/plain", "image/*"))
            }
        }
        val requestPermsButton = Button(this).apply {
            text = "Grant Bluetooth permissions"
            setOnClickListener { requestPermissionsIfNeeded() }
        }
        val testButton = Button(this).apply {
            text = "Queue test transfer now"
            setOnClickListener {
                TransferScheduler.enqueueNow(this@MainActivity, null)
                statusText.text = "Queued test transfer attempt."
            }
        }
        statusText = TextView(this).apply {
            text = "Ready."
        }

        root.addView(title)
        root.addView(desc)
        root.addView(templateText)
        root.addView(saveTextButton)
        root.addView(addFilesButton)
        root.addView(requestPermsButton)
        root.addView(testButton)
        root.addView(statusText)
        setContentView(root)

        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val needed = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                needed.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
                needed.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (Build.VERSION.SDK_INT >= 33 && !hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun importIntoQueue(uris: List<Uri>): Int {
        val queue = File(filesDir, "queue").apply { mkdirs() }
        var added = 0
        uris.forEachIndexed { index, uri ->
            val ext = contentResolver.guessExtension(uri)
            val target = File(queue, "user_${System.currentTimeMillis()}_${index}.$ext")
            kotlin.runCatching {
                contentResolver.openInputStream(uri).use { input ->
                    target.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                }
                added++
            }
        }
        return added
    }

    private fun ContentResolver.guessExtension(uri: Uri): String {
        val mime = getType(uri) ?: return "bin"
        return when {
            mime.contains("pdf") -> "pdf"
            mime.contains("text") -> "txt"
            mime.contains("jpeg") -> "jpg"
            mime.contains("png") -> "png"
            else -> "bin"
        }
    }
}
