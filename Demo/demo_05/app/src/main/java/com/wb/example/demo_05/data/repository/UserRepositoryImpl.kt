package com.wb.example.demo_05.data.repository

import com.wb.example.demo_05.data.local.CachedUserDao
import com.wb.example.demo_05.data.remote.GitHubApi
import com.wb.example.demo_05.data.remote.model.GitHubUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val api: GitHubApi,
    private val dao: CachedUserDao,
) : UserRepository {

    override fun observeCachedUsers(): Flow<List<GitHubUser>> =
        dao.observeAll().map { it.toDomain() }

    override suspend fun search(query: String, page: Int): Result<List<GitHubUser>> =
        runCatching {
            val domains = api.searchUsers(query, page).items.map { it.toDomain() }
            dao.insertAll(domains.map { it.toEntity() })
            domains
        }
}
