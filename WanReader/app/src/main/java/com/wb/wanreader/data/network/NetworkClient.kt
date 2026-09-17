package com.wb.wanreader.data.network

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

// 不依赖 Android，测试和生产使用同一配置入口；实例生命周期由 Hilt 管理。
object NetworkClient {
    fun create(baseUrl: String, debug: Boolean, logger: HttpLoggingInterceptor.Logger): WanApi {
        val json = Json { ignoreUnknownKeys = true }
        val logging = HttpLoggingInterceptor(logger).apply {
            level = if (debug) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
            redactHeader("Authorization")
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // 公共参数集中处理；服务端没有约定的 query 不擅自添加。
                val request = chain.request().newBuilder()
                    .header("Accept", "application/json").build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WanApi::class.java)
    }
}
