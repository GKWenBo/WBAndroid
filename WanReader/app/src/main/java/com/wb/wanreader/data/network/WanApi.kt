package com.wb.wanreader.data.network

import com.wb.wanreader.data.network.dto.ArticlePageDto
import retrofit2.http.GET
import retrofit2.http.Path

interface WanApi {
    // 请求从第 0 页开始，响应 curPage 从 1 开始，不能混用。
    @GET("article/list/{page}/json")
    suspend fun articles(@Path("page") page: Int): BaseResponse<ArticlePageDto>
}
