package com.wb.example.demo_03.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Material 3 提供默认浅色/深色配色，无需自己配色即可演示深色模式切换。
private val DarkColorScheme = darkColorScheme()
private val LightColorScheme = lightColorScheme()

/**
 * 统一的 App 主题。
 * @param darkTheme 是否使用深色模式（由上层状态控制，可手动切换，也可跟随系统）。
 *
 * 对比 iOS/SwiftUI：SwiftUI 用 `.preferredColorScheme(.dark)`，
 * Compose 则通过传入不同的 colorScheme 给 MaterialTheme 实现。
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
