package com.example.bluetoothautopush

import android.content.Context

object Prefs {
    private const val NAME = "bt_auto_push"
    private const val KEY_TEXT = "template_text"
    private const val KEY_RETRY = "retry_count"

    fun getTemplateText(context: Context): String {
        val p = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        return p.getString(KEY_TEXT, "Auto message from Android via Bluetooth.") ?: ""
    }

    fun setTemplateText(context: Context, value: String) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TEXT, value)
            .apply()
    }

    fun getRetryCount(context: Context): Int {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getInt(KEY_RETRY, 0)
    }

    fun setRetryCount(context: Context, value: Int) {
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_RETRY, value)
            .apply()
    }
}
