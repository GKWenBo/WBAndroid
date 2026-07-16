package com.wb.example.demo_04.data.remote.model

import kotlinx.serialization.Serializable

// ── DTO（网络层，Retrofit 用 kotlinx.serialization 解析）──
// 对应 iOS 的 Codable struct；@Serializable 等价于 Swift 的 Codable 自动合成
@Serializable
data class InfoDto(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?,
)

@Serializable
data class OriginDto(val name: String)

@Serializable
data class CharacterDto(
    val id: Int,
    val name: String,
    val status: String,      // "Alive" / "Dead" / "unknown"
    val species: String,
    val gender: String,
    val image: String,       // 头像 URL（交给 Coil 加载）
    val origin: OriginDto,
)

@Serializable
data class CharacterResponseDto(
    val info: InfoDto,
    val results: List<CharacterDto>,
)

// ── Domain 模型（业务层，UI 只认它，不认 DTO）──
// 对应 iOS 的纯 Model / 领域模型，与传输格式解耦
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val imageUrl: String,
    val originName: String,
)

// ── Mapper：DTO -> Domain（手动映射，对应 Swift 的 Decodable 后转换）──
fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    imageUrl = image,
    originName = origin.name,
)
