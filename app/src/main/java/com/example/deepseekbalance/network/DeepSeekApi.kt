package com.example.deepseekbalance.network

import com.example.deepseekbalance.model.BalanceInfo
import com.example.deepseekbalance.model.BalanceResponse
import com.example.deepseekbalance.model.BalanceUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.IOException

/**
 * DeepSeek API 调用封装
 */
object DeepSeekApi {

    /**
     * 查询用户余额
     * @param apiKey DeepSeek API Key
     * @return BalanceUiState 余额展示数据
     * @throws IOException 网络异常
     * @throws AuthException API Key 无效
     */
    suspend fun getBalance(apiKey: String): BalanceUiState = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("${ApiClient.BASE_URL}/user/balance")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .get()
            .build()

        val response = ApiClient.httpClient.newCall(request).execute()

        when (response.code) {
            200 -> {
                val body = response.body?.string()
                    ?: throw IOException("Empty response body")

                val balanceResponse = ApiClient.json.decodeFromString<BalanceResponse>(body)
                parseBalanceResponse(balanceResponse)
            }
            401 -> throw AuthException("API Key 无效")
            403 -> throw AuthException("API Key 无权限")
            else -> {
                val errorBody = response.body?.string().orEmpty()
                throw IOException("请求失败 (${response.code}): $errorBody")
            }
        }
    }

    /**
     * 将 API 响应转换为 UI 展示模型
     */
    private fun parseBalanceResponse(response: BalanceResponse): BalanceUiState {
        if (!response.isAvailable || response.balanceInfos.isEmpty()) {
            return BalanceUiState()
        }

        // 优先找 CNY，否则取第一个
        val info = response.balanceInfos.firstOrNull { it.currency == "CNY" }
            ?: response.balanceInfos.first()

        val total = info.totalBalance.toDoubleOrNull() ?: 0.0
        val granted = info.grantedBalance.toDoubleOrNull() ?: 0.0
        val toppedUp = info.toppedUpBalance.toDoubleOrNull() ?: 0.0
        val used = maxOf(0.0, total - granted - toppedUp)

        return BalanceUiState(
            totalBalance = formatAmount(total),
            usedBalance = formatAmount(used),
            remainingBalance = formatAmount(granted + toppedUp),
            grantedBalance = formatAmount(granted),
            toppedUpBalance = formatAmount(toppedUp),
            currency = info.currency
        )
    }

    private fun formatAmount(amount: Double): String {
        return String.format("%.2f", amount)
    }
}

/**
 * 认证异常
 */
class AuthException(message: String) : Exception(message)