package com.wb.example.demo_04.data.repository

import com.wb.example.demo_04.data.remote.model.Character
import kotlinx.coroutines.flow.Flow

// Repository 接口 = iOS 的 UseCase/Repository 协议，隔离数据源，UI 不直接碰网络/数据库。
interface CharacterRepository {
    // 观察本地缓存（Room 作为单一数据源）。断网也能读到上次的数据。
    fun observeCharacters(): Flow<List<Character>>

    // 从网络拉取并写入本地；用 Result 包装成功/失败，供 UI 展示加载/错误状态。
    suspend fun refresh(page: Int = 1): Result<List<Character>>

    // 详情：本地已缓存则直接返回。
    suspend fun getCharacter(id: Int): Character?
}
