package com.example.deepseekbalance.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DeepSeek 用户余额响应
 */
@Serializable
data class BalanceResponse(
    @SerialName("is_available")
    val isAvailable: Boolean,
    @SerialName("balance_infos")
    val balanceInfos: List<BalanceInfo> = emptyList()
)

/**
 * 单个币种余额信息
 */
@Serializable
data class BalanceInfo(
    val currency: String = "CNY",
    @SerialName("total_balance")
    val totalBalance: String = "0.00",
    @SerialName("granted_balance")
    val grantedBalance: String = "0.00",
    @SerialName("topped_up_balance")
    val toppedUpBalance: String = "0.00"
)

/**
 * UI 层使用的余额展示模型
 */
data class BalanceUiState(
    val totalBalance: String = "0.00",
    val usedBalance: String = "0.00",
    val remainingBalance: String = "0.00",
    val grantedBalance: String = "0.00",
    val toppedUpBalance: String = "0.00",
    val currency: String = "CNY"
)
