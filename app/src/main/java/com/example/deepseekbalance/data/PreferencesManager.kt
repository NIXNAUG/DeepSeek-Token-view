package com.example.deepseekbalance.data

import android.content.Context
import android.content.SharedPreferences

/**
 * 使用 SharedPreferences 管理 API Key 的存储
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 保存 API Key
     */
    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    /**
     * 读取已保存的 API Key，未保存时返回 null
     */
    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    /**
     * 检查是否已配置 API Key
     */
    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }

    /**
     * 清除 API Key
     */
    fun clearApiKey() {
        prefs.edit().remove(KEY_API_KEY).apply()
    }

    companion object {
        private const val PREFS_NAME = "deepseek_balance_prefs"
        private const val KEY_API_KEY = "api_key"
    }
}
