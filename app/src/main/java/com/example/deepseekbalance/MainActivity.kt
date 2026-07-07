package com.example.deepseekbalance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.deepseekbalance.ui.screen.BalanceScreen
import com.example.deepseekbalance.ui.theme.DeepSeekBalanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DeepSeekBalanceTheme {
                BalanceScreen()
            }
        }
    }
}
