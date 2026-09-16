package com.example.wblearncompose.demo.screens.listdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wblearncompose.demo.components.DemoScaffold
import com.example.wblearncompose.ui.theme.WBLearnComposeTheme

/**
 * 「列表-详情」实战之详情页。
 *
 * 详情页只需要一个 articleId：上层从路由参数里取出 id 传进来，
 * 页面据此查数据并展示。找不到时给出兜底文案（真实项目里可能是加载/错误态）。
 *
 * SwiftUI 对照：这相当于 NavigationLink 目的地里根据传入的 value 渲染详情。
 */
@Composable
fun SampleDetailScreen(
    articleId: Int,
    onBack: () -> Unit,
) {
    val article = findArticle(articleId)
    DemoScaffold(title = article?.title ?: "详情", onBack = onBack) { padding ->
        if (article == null) {
            Text(
                text = "未找到 id=$articleId 的文章",
                modifier = Modifier.padding(padding).padding(16.dp),
            )
            return@DemoScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(article.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "作者：${article.author}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(article.content, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SampleDetailScreenPreview() {
    WBLearnComposeTheme {
        SampleDetailScreen(articleId = 1, onBack = {})
    }
}
