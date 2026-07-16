package com.wb.example.demo_05.data.repository

import com.wb.example.demo_05.data.remote.model.GitHubUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // 观察本地缓存的用户（断网可见）
    fun observeCachedUsers(): Flow<List<GitHubUser>>

    // 搜索并写入缓存；返回结果用于 UI 状态
    suspend fun search(query: String, page: Int = 1): Result<List<GitHubUser>>
}
