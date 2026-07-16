package com.wb.example.demo_04.ui.characters

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

// 详情页：数据来自 ViewModel 观察的 Room 缓存（首屏已 refresh 写入），
// 对应 iOS 的详情页直接从本地存储取值。
@Composable
fun CharacterDetailScreen(
    characterId: Int,
    viewModel: CharactersViewModel = koinViewModel(),
) {
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val character = characters.firstOrNull { it.id == characterId }

    Scaffold(
        topBar = { TopAppBar(title = { Text("角色详情") }) }
    ) { padding ->
        if (character == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { Text("未找到或数据尚未加载") }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    modifier = Modifier.size(120.dp),
                )
                Text(character.name, style = MaterialTheme.typography.headlineSmall)
                Text("状态：${character.status}")
                Text("物种：${character.species}")
                Text("性别：${character.gender}")
                Text("起源：${character.originName}")
            }
        }
    }
}
