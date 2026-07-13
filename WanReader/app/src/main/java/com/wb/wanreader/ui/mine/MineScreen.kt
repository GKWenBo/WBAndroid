package com.wb.wanreader.ui.mine

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// S1 占位页；S4 接入登录态与用户信息
@Composable
fun MineScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "我的（S4 接入登录）", style = MaterialTheme.typography.titleLarge)
    }
}
