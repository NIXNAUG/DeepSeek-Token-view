package com.example.deepseekbalance.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.deepseekbalance.MainActivity
import com.example.deepseekbalance.R
import com.example.deepseekbalance.data.WidgetDataStore
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 桌面小组件广播接收器 — 传统 RemoteViews 方案
 *
 * 支持 vivo OriginOS / Funtouch OS 卡片原子组件体系。
 * 长按桌面 → 添加卡片 → 搜索 Token 即可添加。
 * 主 App 查询余额后会调用 [updateAllWidgets] 刷新所有卡片。
 */
class BalanceWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {

        /** 主 App 查询余额成功后调用，刷新所有桌面卡片 */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BalanceWidgetReceiver::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (widgetId in widgetIds) {
                updateWidget(context, appWidgetManager, widgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_balance)
            val dataStore = WidgetDataStore(context)
            val balance = dataStore.loadBalance()
            val updatedAt = dataStore.lastUpdatedAt()

            // 整个卡片点击 → 打开 App
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetId, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            if (balance != null) {
                views.setTextViewText(R.id.widget_balance, "¥${balance.totalBalance}")
                views.setTextViewText(R.id.widget_used, "¥${balance.usedBalance}")
                views.setTextViewText(R.id.widget_remaining, "¥${balance.remainingBalance}")

                val percent = calculateUsagePercent(balance.totalBalance, balance.usedBalance)
                views.setTextViewText(R.id.widget_usage, "已使用 $percent%")

                if (updatedAt > 0) {
                    views.setTextViewText(R.id.widget_time, formatRelativeTime(updatedAt))
                }
                views.setTextViewText(R.id.widget_hint, "点击刷新")
            } else {
                views.setTextViewText(R.id.widget_balance, "¥ —.—")
                views.setTextViewText(R.id.widget_used, "¥0.00")
                views.setTextViewText(R.id.widget_remaining, "¥0.00")
                views.setTextViewText(R.id.widget_usage, "")
                views.setTextViewText(R.id.widget_time, "")
                views.setTextViewText(R.id.widget_hint, "打开 App 设置 API Key")
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        private fun calculateUsagePercent(total: String, used: String): Int {
            return try {
                val t = BigDecimal(total)
                val u = BigDecimal(used)
                if (t.compareTo(BigDecimal.ZERO) <= 0) return 0
                u.divide(t, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal(100))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toInt()
                    .coerceIn(0, 100)
            } catch (_: Exception) {
                0
            }
        }

        private fun formatRelativeTime(timestamp: Long): String {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60_000 -> "刚刚"
                diff < 3_600_000 -> "${diff / 60_000}分钟前"
                diff < 86_400_000 -> "${diff / 3_600_000}小时前"
                else -> "${diff / 86_400_000}天前"
            }
        }
    }
}