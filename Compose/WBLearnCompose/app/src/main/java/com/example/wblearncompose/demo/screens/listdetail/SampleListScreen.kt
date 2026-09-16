package com.example.wblearncompose.demo.screens.listdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 「列表-详情」实战之列表页。
 *
 * 点击某条文章时，通过 onArticleClick(id) 把文章 id 交给上层去导航到详情页。
 * 把“导航动作”留给上层（NavHost 所在处），页面本身只关心“显示列表 + 点击回调”，
 * 这样页面更纯粹、更好复用与预览。
 */
@Composable
fun SampleListScreen(
    onBack: () -> Unit,
    onArticleClick: (id: Int) -> Unit,
) {
    DemoScaffold(title = "文章列表", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            items(sampleArticles) { article ->
                Card(
                    onClick = { onArticleClick(article.id) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(article.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            article.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleListScreenPreview() {
    WBLearnComposeTheme {
        SampleListScreen(onBack = {}, onArticleClick = {})
    }
}
