package com.example.deepseekbalance.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.deepseekbalance.data.PreferencesManager
import com.example.deepseekbalance.data.WidgetDataStore
import com.example.deepseekbalance.model.BalanceUiState
import com.example.deepseekbalance.network.AuthException
import com.example.deepseekbalance.network.DeepSeekApi
import com.example.deepseekbalance.widget.BalanceWidgetReceiver
import kotlinx.coroutines.launch
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

// ═══════════════════════════════════════════
// 主界面
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var apiKey by remember { mutableStateOf(prefsManager.getApiKey() ?: "") }
    var apiKeyVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var balanceState by remember { mutableStateOf<BalanceUiState?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isConfigured by remember { mutableStateOf(prefsManager.hasApiKey()) }

    // 如果已配置 API Key，自动查询余额
    LaunchedEffect(isConfigured) {
        if (isConfigured && prefsManager.hasApiKey()) {
            val savedKey = prefsManager.getApiKey() ?: return@LaunchedEffect
            apiKey = savedKey
            queryBalance(
                apiKey = savedKey,
                context = context,
                scope = scope,
                onLoading = { isLoading = it },
                onSuccess = {
                    balanceState = it
                    errorMessage = null
                },
                onError = { errorMessage = it }
            )
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.small,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        },
        topBar = {
            BalanceTopBar(
                onRefresh = {
                    val key = prefsManager.getApiKey()
                    if (key != null) {
                        queryBalance(
                            apiKey = key,
                            context = context,
                            scope = scope,
                            onLoading = { isLoading = it },
                            onSuccess = {
                                balanceState = it
                                errorMessage = null
                            },
                            onError = { errorMessage = it }
                        )
                    }
                },
                isConfigured = isConfigured
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ═══ API Key 配置区域 ═══
            ApiKeySection(
                apiKey = apiKey,
                apiKeyVisible = apiKeyVisible,
                isConfigured = isConfigured,
                onApiKeyChange = { apiKey = it },
                onToggleVisibility = { apiKeyVisible = !apiKeyVisible },
                onSave = {
                    focusManager.clearFocus()
                    prefsManager.saveApiKey(apiKey)
                    isConfigured = true
                    queryBalance(
                        apiKey = apiKey,
                        context = context,
                        scope = scope,
                        onLoading = { isLoading = it },
                        onSuccess = {
                            balanceState = it
                            errorMessage = null
                        },
                        onError = { errorMessage = it }
                    )
                },
                onEdit = {
                    isConfigured = false
                    balanceState = null
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ 主内容区域 - 带动画过渡 ═══
            AnimatedContent(
                targetState = when {
                    isLoading -> ContentState.LOADING
                    errorMessage != null -> ContentState.ERROR
                    balanceState != null -> ContentState.BALANCE
                    !isConfigured -> ContentState.HOW_TO_GET_KEY
                    else -> ContentState.WAITING
                },
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInVertically(
                        animationSpec = tween(300),
                        initialOffsetY = { it / 8 }
                    )) togetherWith
                        (fadeOut(animationSpec = tween(200)) + slideOutVertically(
                            animationSpec = tween(200),
                            targetOffsetY = { -it / 8 }
                        ))
                },
                label = "content_state"
            ) { state ->
                when (state) {
                    ContentState.LOADING -> ShimmerLoadingSection()
                    ContentState.ERROR -> ErrorSection(
                        message = errorMessage!!,
                        onRetry = {
                            val key = prefsManager.getApiKey()
                            if (key != null) {
                                queryBalance(
                                    apiKey = key,
                                    context = context,
                                    scope = scope,
                                    onLoading = { isLoading = it },
                                    onSuccess = {
                                        balanceState = it
                                        errorMessage = null
                                    },
                                    onError = { errorMessage = it }
                                )
                            }
                        }
                    )
                    ContentState.BALANCE -> BalanceCard(
                        balance = balanceState!!,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "balance",
                                "总额: ¥${balanceState!!.totalBalance}  已用: ¥${balanceState!!.usedBalance}  剩余: ¥${balanceState!!.remainingBalance}"
                            )
                            clipboard.setPrimaryClip(clip)
                        }
                    )
                    ContentState.HOW_TO_GET_KEY -> HowToGetKeySection()
                    ContentState.WAITING -> {}
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════
// 内容状态枚举
// ═══════════════════════════════════════════
private enum class ContentState { LOADING, ERROR, BALANCE, HOW_TO_GET_KEY, WAITING }

// ═══════════════════════════════════════════
// 顶部栏
// ═══════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BalanceTopBar(
    onRefresh: () -> Unit,
    isConfigured: Boolean
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 品牌小圆点
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DeepSeek 余额",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        actions = {
            if (isConfigured) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Outlined.Refresh,
                        contentDescription = "刷新",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

// ═══════════════════════════════════════════
// API Key 区域
// ═══════════════════════════════════════════

@Composable
private fun ApiKeySection(
    apiKey: String,
    apiKeyVisible: Boolean,
    isConfigured: Boolean,
    onApiKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Outlined.Key,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "API Key",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isConfigured) {
                    // 已配置状态指示
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    ) {
                        Text(
                            text = "● 已配置",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isConfigured) {
                // 已配置状态：显示掩码 + 编辑按钮
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = maskApiKey(apiKey),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalIconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "修改",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                // 未配置状态：输入框
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "请输入 API Key",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    visualTransformation = if (apiKeyVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSave() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                if (apiKeyVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                contentDescription = "切换可见性",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = apiKey.isNotBlank(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    )
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "保存并查询",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// Shimmer 加载动画
// ═══════════════════════════════════════════

@Composable
private fun ShimmerLoadingSection() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX = transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX.value - 200f, 0f),
        end = Offset(translateX.value, 0f),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 总余额 shimmer
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // 进度条 shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(20.dp))
            // 双列 shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 错误展示
// ═══════════════════════════════════════════

@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "查询失败",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            FilledTonalButton(
                onClick = onRetry,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    contentColor = MaterialTheme.colorScheme.error,
                )
            ) {
                Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

// ═══════════════════════════════════════════
// 余额卡片 - 核心展示
// ═══════════════════════════════════════════

@Composable
private fun BalanceCard(
    balance: BalanceUiState,
    onCopy: () -> Unit,
) {
    val usagePercent = remember(balance.totalBalance, balance.usedBalance) {
        calculateUsagePercent(balance.totalBalance, balance.usedBalance)
    }

    // 使用进度比例动画
    val animatedProgress by animateFloatAsState(
        targetValue = usagePercent / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ═══ 标题行 + 复制按钮 ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "账户余额",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContentCopy,
                        contentDescription = "复制",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ═══ 总余额 - 大字展示 ═══
            Text(
                text = "¥${balance.totalBalance}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = balance.currency,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ═══ 使用进度条 ═══
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已使用",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "${usagePercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                // 自定义进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (usagePercent > 80) {
                                        listOf(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.error
                                        )
                                    } else {
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══ 已用 / 剩余 双卡片 ═══
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiniBalanceCard(
                    icon = Icons.Outlined.TrendingUp,
                    label = "已用",
                    amount = balance.usedBalance,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f),
                )
                MiniBalanceCard(
                    icon = Icons.Outlined.Savings,
                    label = "剩余",
                    amount = balance.remainingBalance,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ═══ 已获得(赠送)额度 ═══
            if (balance.grantedBalance.isNotEmpty() && balance.grantedBalance != "0.00") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Redeem,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "赠送额度",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "¥${balance.grantedBalance}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ═══ 充值按钮 ═══
            RechargeButton()
        }
    }
}

@Composable
private fun RechargeButton() {
    val context = LocalContext.current
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.deepseek.com/top_up"))
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(
            Icons.Outlined.AddCard,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "充值",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun MiniBalanceCard(
    icon: ImageVector,
    label: String,
    amount: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "¥$amount",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
            )
        }
    }
}

// ═══════════════════════════════════════════
// 获取 API Key 引导
// ═══════════════════════════════════════════

@Composable
private fun HowToGetKeySection() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图标
            Surface(
                modifier = Modifier.size(72.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "还没有 API Key？",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "在 DeepSeek 开放平台注册并创建 API Key\n即可查询您的账户余额和使用情况",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 步骤提示
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                StepDot("1", "注册")
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
                StepDot("2", "创建 Key")
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                )
                StepDot("3", "查询余额")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.deepseek.com/api_keys"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "前往获取 API Key",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun StepDot(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

// ═══════════════════════════════════════════
// 工具函数
// ═══════════════════════════════════════════

/**
 * 查询余额（同时将结果同步到桌面小组件）
 */
private fun queryBalance(
    apiKey: String,
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onLoading: (Boolean) -> Unit,
    onSuccess: (BalanceUiState) -> Unit,
    onError: (String) -> Unit
) {
    scope.launch {
        onLoading(true)
        try {
            val result = DeepSeekApi.getBalance(apiKey)
            // 同步余额到桌面卡片小组件
            WidgetDataStore(context).saveBalance(result)
            try {
                BalanceWidgetReceiver.updateAllWidgets(context)
            } catch (_: Exception) {
                // 小组件尚未添加时更新会失败，忽略即可
            }
            onSuccess(result)
        } catch (e: AuthException) {
            onError("API Key 无效，请检查后重试")
        } catch (e: IOException) {
            onError("网络连接失败，请检查网络后重试")
        } catch (e: Exception) {
            onError("查询失败: ${e.message ?: "未知错误"}")
        } finally {
            onLoading(false)
        }
    }
}

/**
 * 将 API Key 掩码显示
 */
private fun maskApiKey(key: String): String {
    if (key.length <= 8) return "****"
    return "${key.take(4)}${"*".repeat(maxOf(0, key.length - 8))}${key.takeLast(4)}"
}

/**
 * 计算使用百分比
 */
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
