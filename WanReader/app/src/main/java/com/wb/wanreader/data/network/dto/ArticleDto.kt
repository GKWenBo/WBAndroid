package com.wb.wanreader.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArticlePageDto(
    val curPage: Int,
    val over: Boolean,
    val datas: List<ArticleDto>
)

@Serializable
data class ArticleDto(
    val id: Int,
    val title: String,
    val link: String,
    // 可空和有默认值分别解决显式 null 与字段缺失，含义不同。
    val author: String? = null,
    val shareUser: String? = null
)
