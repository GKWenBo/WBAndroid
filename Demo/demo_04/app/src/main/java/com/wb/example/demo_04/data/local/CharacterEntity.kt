package com.wb.example.demo_04.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wb.example.demo_04.data.remote.model.Character

// Room 实体 = iOS 的 Core Data Entity / 数据库表行。
// @Entity 标注表；@PrimaryKey 标注主键（对应 Swift 的 @Attribute ID）。
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val imageUrl: String,
    val originName: String,
)

// Mapper：Entity <-> Domain
fun CharacterEntity.toDomain(): Character = Character(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    imageUrl = imageUrl,
    originName = originName,
)

fun Character.toEntity(): CharacterEntity = CharacterEntity(
    id = id,
    name = name,
    status = status,
    species = species,
    gender = gender,
    imageUrl = imageUrl,
    originName = originName,
)
