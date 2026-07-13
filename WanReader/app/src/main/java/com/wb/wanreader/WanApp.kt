package com.wb.wanreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// @HiltAndroidApp：Hilt 的总开关——编译期生成全 App 的依赖容器（SingletonComponent），
// 并让 Application 持有它。iOS 对照：≈ 在 AppDelegate/App 入口手工组装的"依赖工厂"，
// 差别是 Hilt 在【编译期】生成这套工厂代码，写错了直接编译失败而不是运行时崩。
@HiltAndroidApp
class WanApp : Application()
