package com.wb.example.demo_04.data.remote

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.JsonConverterFactory

// 网络客户端单例。对应 iOS 的 URLSession.shared 封装。
// 教学目的用 object 单例；真实项目可交给 Koin 注入，便于测试替换。
object RetrofitClient {
    // ignoreUnknownKeys：后端字段比模型多时忽略，对应 Swift Decodable 的 .ignoreUnknownKeys
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                // 生产环境应降级为 NONE/BASIC，避免泄露请求体
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    val api: RickAndMortyApi = Retrofit.Builder()
        .baseUrl("https://rickandmortyapi.com/api/")
        .client(okHttpClient)
        .addConverterFactory(JsonConverterFactory.create(json))
        .build()
        .create(RickAndMortyApi::class.java)
}
