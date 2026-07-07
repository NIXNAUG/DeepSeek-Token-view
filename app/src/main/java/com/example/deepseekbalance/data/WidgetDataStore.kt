package com.example.deepseekbalance.data

import android.content.Context
import com.example.deepseekbalance.model.BalanceUiState

/**
 * 小组件专用数据存储 — 持久化最近一次余额快照
 *
 * 小组件无法直接调用 API，所以主 App 每次查询余额成功后
 * 将结果同步写入这里，小组件刷新时从此读取。
 */
class WidgetDataStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** 保存余额快照供小组件读取 */
    fun saveBalance(balance: BalanceUiState) {
        prefs.edit()
            .putString(KEY_TOTAL, balance.totalBalance)
            .putString(KEY_USED, balance.usedBalance)
            .putString(KEY_REMAINING, balance.remainingBalance)
            .putString(KEY_GRANTED, balance.grantedBalance)
            .putString(KEY_CURRENCY, balance.currency)
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    /** 读取最近一次余额快照，无数据返回 null */
    fun loadBalance(): BalanceUiState? {
        val total = prefs.getString(KEY_TOTAL, null) ?: return null
        return BalanceUiState(
            totalBalance = total,
            usedBalance = prefs.getString(KEY_USED, "0.00")!!,
            remainingBalance = prefs.getString(KEY_REMAINING, "0.00")!!,
            grantedBalance = prefs.getString(KEY_GRANTED, "0.00")!!,
            currency = prefs.getString(KEY_CURRENCY, "CNY")!!,
        )
    }

    /** 上次更新时间戳，0 表示从未更新 */
    fun lastUpdatedAt(): Long = prefs.getLong(KEY_UPDATED_AT, 0L)

    /** 是否有缓存数据 */
    fun hasCachedData(): Boolean = prefs.contains(KEY_TOTAL)

    companion object {
        private const val PREFS_NAME = "deepseek_balance_widget"
        private const val KEY_TOTAL = "total"
        private const val KEY_USED = "used"
        private const val KEY_REMAINING = "remaining"
        private const val KEY_GRANTED = "granted"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_UPDATED_AT = "updated_at"
    }
}