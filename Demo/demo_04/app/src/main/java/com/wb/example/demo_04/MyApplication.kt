package com.wb.example.demo_04

import android.app.Application
import com.wb.example.demo_04.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Application = iOS 的 AppDelegate（进程级入口）。在此初始化全局依赖容器 Koin。
// 已在 AndroidManifest.xml 的 android:name=".MyApplication" 注册。
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}
