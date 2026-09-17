package com.wb.wanreader.data

import com.wb.wanreader.data.model.Article
import com.wb.wanreader.data.model.ArticlePage
import com.wb.wanreader.data.network.NetResult
import com.wb.wanreader.data.network.WanApi
import com.wb.wanreader.data.network.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(private val api: WanApi) {
    suspend fun firstPage(): NetResult<ArticlePage> = when (val result = apiCall { api.articles(0) }) {
        is NetResult.Failure -> result
        is NetResult.Success -> NetResult.Success(ArticlePage(
            articles = result.value.datas.map { dto ->
                Article(dto.id, dto.title, dto.link,
                    dto.author?.takeIf { it.isNotBlank() }
                        ?: dto.shareUser?.takeIf { it.isNotBlank() } ?: "佚名")
            },
            currentPage = result.value.curPage,
            isLastPage = result.value.over
        ))
    }
}
