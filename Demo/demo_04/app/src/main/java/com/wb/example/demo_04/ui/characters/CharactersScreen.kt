package com.wb.example.demo_04.ui.characters

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.wb.example.demo_04.data.remote.model.Character
import org.koin.androidx.compose.koinViewModel

@Composable
fun CharactersScreen(
    viewModel: CharactersViewModel = koinViewModel(),
    onCharacterClick: (Int) -> Unit,
) {
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rick & Morty 角色") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                // 首次加载且无缓存：整页转圈
                uiState.isLoading && characters.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // 首次加载失败且无缓存：错误 + 重试
                uiState.error != null && characters.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("加载失败：${uiState.error}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::retry) { Text("重试") }
                    }
                }
                // 有缓存（可能正在后台刷新）：展示列表
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(characters, key = { it.id }) { character ->
                            CharacterRow(character, onCharacterClick)
                        }
                    }
                    // 后台刷新中的小菊花
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterRow(character: Character, onCharacterClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCharacterClick(character.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // Coil 异步加载网络图片，对应 iOS 的 SDWebImage / Kingfisher
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(character.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${character.status} · ${character.species}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
