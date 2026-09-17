package com.wb.wanreader.data.model

// 页面不依赖序列化注解和服务端的 datas/shareUser 等字段名。
data class Article(val id: Int, val title: String, val link: String, val author: String)
data class ArticlePage(val articles: List<Article>, val currentPage: Int, val isLastPage: Boolean)
