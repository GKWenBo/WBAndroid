package com.wb.wanreader.ui.square

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// S1 占位页；S3 后接入项目/广场列表
@Composable
fun SquareScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "广场（S3 接入列表）", style = MaterialTheme.typography.titleLarge)
    }
}
