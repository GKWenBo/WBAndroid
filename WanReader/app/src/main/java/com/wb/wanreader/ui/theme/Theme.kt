package com.wb.wanreader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// App 级主题包装。iOS 对照：≈ SwiftUI 根视图上统一注入的 environment 配色。
// S0 先用 Material3 默认配色跑通深浅色切换，S1 再定制品牌色。
@Composable
fun WanReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
