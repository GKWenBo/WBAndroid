package com.wb.example.demo_05.data.remote

import com.wb.example.demo_05.data.remote.model.SearchUsersResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// GitHub 公开搜索 API（未认证 60 次/小时，教学足够）。
interface GitHubApi {
    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
    ): SearchUsersResponseDto
}
