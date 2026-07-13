package com.wb.wanreader.data

import com.wb.wanreader.di.AppVersionName
import javax.inject.Inject
import javax.inject.Singleton

// 构造注入：依赖【是你自己写的类】时，直接在构造函数标 @Inject，
// Hilt 就知道"要造 AppInfoRepository，先造它构造函数要的东西"——不需要写 Module。
// 这是 Hilt 的主力用法，Module 的 @Provides 只兜底造不了的。
// iOS 对照：≈ init(versionName:) 手工传参，只是"谁来调 init"从你变成了 Hilt。
@Singleton
class AppInfoRepository @Inject constructor(
    private val versionName: AppVersionName
) {
    fun appDescription(): String = "WanReader v${versionName.value} · Hilt 注入链路已打通"
}
