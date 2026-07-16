package com.wb.example.demo_04.data.repository

import com.wb.example.demo_04.data.local.CharacterDao
import com.wb.example.demo_04.data.remote.RickAndMortyApi
import com.wb.example.demo_04.data.remote.model.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Repository 实现：组合“网络 + 本地”，是 MVVM 的数据中枢。
// 对应 iOS 中同时持有 APIClient 与 Core Data Stack 的 Repository 实现。
class CharacterRepositoryImpl(
    private val api: RickAndMortyApi,
    private val dao: CharacterDao,
) : CharacterRepository {

    // 列表只从本地观察：网络数据在 refresh() 中写入，UI 永远读缓存，
    // 因此旋转屏幕/断网都不丢数据（对应 iOS 的单一数据源思想）。
    override fun observeCharacters(): Flow<List<Character>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh(page: Int): Result<List<Character>> = runCatching {
        val dtos = api.getCharacters(page).results
        val domains = dtos.map { it.toDomain() }
        dao.insertAll(domains.map { it.toEntity() })  // 写入本地，触发 Flow 重新发射
        domains
    }

    override suspend fun getCharacter(id: Int): Character? =
        dao.getById(id)?.toDomain()
}
