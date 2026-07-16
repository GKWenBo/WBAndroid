package com.wb.example.demo_05.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wb.example.demo_05.data.remote.model.GitHubUser

// 缓存搜索到的用户，断网也能浏览。对应 iOS 的 Core Data 缓存表。
@Entity(tableName = "cached_users")
data class CachedUserEntity(
    @PrimaryKey val id: Long,
    val login: String,
    val avatarUrl: String,
    val type: String,
    val htmlUrl: String,
    val cachedAt: Long = System.currentTimeMillis(),
)

fun CachedUserEntity.toDomain(): GitHubUser = GitHubUser(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    type = type,
    htmlUrl = htmlUrl,
)

fun GitHubUser.toEntity(): CachedUserEntity = CachedUserEntity(
    id = id,
    login = login,
    avatarUrl = avatarUrl,
    type = type,
    htmlUrl = htmlUrl,
)
