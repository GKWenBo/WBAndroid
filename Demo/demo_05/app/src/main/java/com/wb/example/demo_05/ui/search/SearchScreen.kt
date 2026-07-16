package com.wb.example.demo_05.ui.search

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wb.example.demo_05.data.remote.model.GitHubUser
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onUserClick: (Long) -> Unit,
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 运行时权限请求演示：对应 iOS 的 UNUserNotificationCenter.requestAuthorization。
    // 注意：minSdk 24 上该权限不适用，系统会立即回调拒绝——正好演示“被拒绝”分支。
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Toast.makeText(
            context,
            if (granted) "通知权限已授予" else "通知权限被拒绝",
            Toast.LENGTH_SHORT,
        ).show()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("GitHub 用户搜索") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 搜索框 + 按钮
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text("输入用户名，如 torvalds") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
                )
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Button(onClick = viewModel::search) { Text("搜索") }
            }
            Spacer(Modifier.height(8.dp))

            // 运行时权限演示按钮
            OutlinedButton(
                onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
            ) { Text("请求通知权限（演示运行时权限）") }
            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                uiState.error != null -> Text(
                    "错误：${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(users, key = { it.id }) { user ->
                    UserRow(user, onUserClick)
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: GitHubUser, onUserClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onUserClick(user.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = user.login,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(user.login, style = MaterialTheme.typography.titleMedium)
                Text(user.type, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
