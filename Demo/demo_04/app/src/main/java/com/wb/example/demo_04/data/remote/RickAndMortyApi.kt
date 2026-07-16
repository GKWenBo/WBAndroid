package com.wb.example.demo_04.data.remote

import com.wb.example.demo_04.data.remote.model.CharacterResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit 接口 = iOS 的 URLSession + Decodable 封装，方法即 endpoint
interface RickAndMortyApi {
    // suspend 函数 = Swift 的 async/await；Retrofit 自动在后台线程执行，不阻塞主线程
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int = 1): CharacterResponseDto
}
