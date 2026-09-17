package com.wb.wanreader.di

import android.util.Log
import com.wb.wanreader.BuildConfig
import com.wb.wanreader.data.network.NetworkClient
import com.wb.wanreader.data.network.WanApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideWanApi(): WanApi = NetworkClient.create(
        baseUrl = "https://wanandroid.com/",
        debug = BuildConfig.DEBUG,
        logger = HttpLoggingInterceptor.Logger { Log.d("WanHttp", it) }
    )
}
