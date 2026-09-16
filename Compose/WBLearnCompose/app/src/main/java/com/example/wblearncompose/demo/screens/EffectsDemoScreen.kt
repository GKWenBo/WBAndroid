package com.example.wblearncompose.demo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoList
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.demo.components.DemoSection
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * UI 效果与 Material 组件详情页。
 *
 * 说明：主题里 dynamicColor = true，Android 12+ 会用系统壁纸取色覆盖默认紫色调色板，
 * 所以你在真机看到的主色可能和预览不同。想固定配色可在入口传 WBLearnComposeTheme(dynamicColor = false)。
 */
@Composable
fun EffectsDemoScreen(onBack: () -> Unit) {
    DemoScaffold(title = "UI 效果与 Material 组件", onBack = onBack) { padding ->
        DemoList(contentPadding = padding) {
            item { CardSection() }
            item { GradientSection() }
            item { DividerSection() }
            item { BadgeSection() }
            item { DialogSection() }
        }
    }
}

@Composable
private fun CardSection() = DemoSection(
    title = "卡片 Card：圆角 + 阴影",
    description = "Card 自带圆角与阴影（elevation）。阴影越大层级感越强。",
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("这是一张卡片", style = MaterialTheme.typography.titleMedium)
            Text("圆角 16dp，阴影 6dp", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun GradientSection() = DemoSection(
    title = "渐变 Brush",
    description = "用 Brush.linearGradient / horizontalGradient 做背景渐变，配合 clip 裁成圆角。",
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text("线性渐变背景", color = Color.White)
    }
}

@Composable
private fun DividerSection() = DemoSection(
    title = "分割线 HorizontalDivider",
    description = "列表/分组之间的细线（Material3 里叫 HorizontalDivider）。",
) {
    Column {
        Text("上面一行")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("下面一行")
    }
}

@Composable
private fun BadgeSection() = DemoSection(
    title = "角标 BadgedBox + Badge",
    description = "给图标叠加未读红点/数字，常用于消息、购物车。",
) {
    BadgedBox(badge = { Badge { Text("8") } }) {
        Icon(Icons.Filled.Notifications, contentDescription = "通知")
    }
}

@Composable
private fun DialogSection() = DemoSection(
    title = "对话框 AlertDialog",
    description = "用一个布尔状态控制显示/隐藏：需要时才把 Dialog 放进组合树（≈ SwiftUI .alert）。",
) {
    var showDialog by remember { mutableStateOf(false) }
    Button(onClick = { showDialog = true }) { Text("弹出对话框") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("提示") },
            text = { Text("这是一个 Material3 AlertDialog 示例。") },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EffectsDemoScreenPreview() {
    WBLearnComposeTheme {
        EffectsDemoScreen(onBack = {})
    }
}
