package com.wb.wanreader.ui.home

import androidx.lifecycle.ViewModel
import com.wb.wanreader.data.AppInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// @HiltViewModel + @Inject constructor：Hilt 接管 ViewModel 的创建，
// 构造函数里要什么依赖就写什么，UI 层完全不知道 Repository 的存在。
// iOS 对照：≈ MVVM 里 ViewModel 的 init 注入，只是实例化交给了框架（配合 hiltViewModel()）。
// S1 先返回静态文案验证链路；S3 起这里会变成 StateFlow + 分页数据流。
@HiltViewModel
class HomeViewModel @Inject constructor(
    appInfoRepository: AppInfoRepository
) : ViewModel() {
    val greeting: String = appInfoRepository.appDescription()
}
