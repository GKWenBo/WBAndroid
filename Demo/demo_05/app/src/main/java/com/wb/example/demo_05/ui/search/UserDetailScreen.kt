package com.wb.example.demo_05.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@Composable
fun UserDetailScreen(
    userId: Long,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val user = users.firstOrNull { it.id == userId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("用户详情") }) }
    ) { padding ->
        if (user == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text("未找到 / 数据未加载") }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = user.login,
                    modifier = Modifier.size(96.dp),
                )
                Text(user.login, style = MaterialTheme.typography.headlineSmall)
                Text("类型：${user.type}")
                Text("主页：${user.htmlUrl}")
            }
        }
    }
}
