package com.wb.wanreader.ui.home

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.wb.wanreader.BuildConfig
import com.wb.wanreader.data.ArticleRepository
import com.wb.wanreader.data.network.NetError
import com.wb.wanreader.data.network.NetResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.wb.wanreader.data.AppInfoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

// @HiltViewModel + @Inject constructor：Hilt 接管 ViewModel 的创建，
// 构造函数里要什么依赖就写什么，UI 层完全不知道 Repository 的存在。
// iOS 对照：≈ MVVM 里 ViewModel 的 init 注入，只是实例化交给了框架（配合 hiltViewModel()）。
// S2 先显示一次请求摘要，S3 再演进为分页列表。
@HiltViewModel
class HomeViewModel @Inject constructor(
    appInfoRepository: AppInfoRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {
    val greeting: String = appInfoRepository.appDescription()
    private val _requestState = MutableStateFlow(HomeRequestState())
    val requestState = _requestState.asStateFlow()
    private var requestJob: Job? = null

    init { loadArticles() }

    fun loadArticles() {
        if (requestJob?.isActive == true) return
        requestJob = viewModelScope.launch {
            _requestState.value = HomeRequestState(loading = true, message = "正在加载首页文章…")
            when (val result = articleRepository.firstPage()) {
                is NetResult.Success -> {
                    val articles = result.value.articles
                    _requestState.value = HomeRequestState(message =
                        "已获取 ${articles.size} 篇文章\n" + (articles.firstOrNull()?.title ?: "暂无文章"))
                    if (BuildConfig.DEBUG) {
                        Log.d("WanNetwork", "首页请求成功：${articles.size} 篇")
                        articles.take(3).forEach { Log.d("WanNetwork", "id=${it.id}, title=${it.title}") }
                    }
                }
                is NetResult.Failure -> {
                    val message = when (val error = result.error) {
                        is NetError.Business -> if (error.code == -1001) "登录已失效，请重新登录"
                            else "业务请求失败（${error.code}）"
                        is NetError.Http -> "服务响应异常（HTTP ${error.code}），请稍后重试"
                        NetError.Network -> "网络连接失败或超时，请检查网络后重试"
                        NetError.Decode -> "服务数据格式异常，请稍后重试"
                        NetError.EmptyData -> "服务未返回所需数据，请稍后重试"
                    }
                    _requestState.value = HomeRequestState(message = message)
                    if (BuildConfig.DEBUG) Log.d("WanNetwork", message)
                }
            }
        }
    }
}

data class HomeRequestState(val loading: Boolean = false, val message: String = "准备加载")
