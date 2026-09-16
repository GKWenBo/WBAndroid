package com.wb.wanreader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 强类型包一层，避免直接注入裸 String（多个 String 依赖会撞车，这是 DI 的通用规范）
// DI 边界使用普通包装类，避免 value class 的 JVM 名称改编影响 Hilt 代码生成。
data class AppVersionName(val value: String)

// @Module：告诉 Hilt "这里有一批依赖的制造方法"。
// @InstallIn(SingletonComponent::class)：装进全 App 生命周期的容器（≈ 全局单例作用域）。
// 什么时候用 @Provides？——当依赖【不是你写的类】（系统 API、三方库、需要加工才能得到）
// 时，没法在它构造函数上标 @Inject，就在 Module 里写个方法教 Hilt 怎么造。
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // @ApplicationContext：Hilt 内置的限定符，注入全局 Context（而非某个 Activity 的）
    @Provides
    @Singleton
    fun provideAppVersionName(@ApplicationContext context: Context): AppVersionName {
        val versionName = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName ?: "unknown"
        return AppVersionName(versionName)
    }
}
