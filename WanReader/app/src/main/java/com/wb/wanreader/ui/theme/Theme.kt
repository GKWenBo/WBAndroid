package com.wb.wanreader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// App 级主题包装。iOS 对照：≈ SwiftUI 根视图上统一注入的 environment 配色。
// S1 升级：接入品牌配色与字体档；深浅色两套 scheme 跟随系统切换。
private val LightColors = lightColorScheme(
    primary = WanBlue,
    primaryContainer = WanBlueContainer
)

private val DarkColors = darkColorScheme(
    primary = WanBlueDark,
    primaryContainer = WanBlueContainerDark
)

@Composable
fun WanReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = WanTypography,
        content = content
    )
}
