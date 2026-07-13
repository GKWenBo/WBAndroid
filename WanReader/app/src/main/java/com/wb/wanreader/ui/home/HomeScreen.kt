package com.wb.wanreader.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

// hiltViewModel()：从 Hilt 容器里按当前导航目的地的生命周期取/建 ViewModel。
// iOS 对照：≈ SwiftUI 的 @StateObject 交给 DI 容器来 new。
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "首页", style = MaterialTheme.typography.titleLarge)
        Text(
            text = viewModel.greeting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
