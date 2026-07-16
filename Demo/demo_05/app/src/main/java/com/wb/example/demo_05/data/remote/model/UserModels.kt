package com.wb.example.demo_05.data.remote.model

import kotlinx.serialization.Serializable

// ── DTO（GitHub Search API，字段为 snake_case，与 JSON 精确匹配）──
@Serializable
data class GitHubUserDto(
    val id: Long,
    val login: String,
    val avatar_url: String? = null,
    val type: String? = null,
    val html_url: String? = null,
)

@Serializable
data class SearchUsersResponseDto(
    val total_count: Int,
    val incomplete_results: Boolean,
    val items: List<GitHubUserDto>,
)

// ── Domain 模型（camelCase，Kotlin 习惯，UI 只认它）──
data class GitHubUser(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val type: String,
    val htmlUrl: String,
)

fun GitHubUserDto.toDomain(): GitHubUser = GitHubUser(
    id = id,
    login = login,
    avatarUrl = avatar_url ?: "",
    type = type ?: "User",
    htmlUrl = html_url ?: "",
)
