package com.wb.example.demo_05.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wb.example.demo_05.data.remote.model.GitHubUser
import com.wb.example.demo_05.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
)

// 搜索页 ViewModel：持有搜索词、结果列表（观察 Room 缓存）、一次性 UI 状态。
class SearchViewModel(
    private val repository: UserRepository,
) : ViewModel() {

    // 搜索词（TextField 双向绑定，对应 Swift 的 @State / @Binding）
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    // 缓存用户：断网也能浏览（ViewModel + Room 双重保活，旋转屏幕不丢）
    val users: StateFlow<List<GitHubUser>> = repository.observeCachedUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun onQueryChange(q: String) { _query.value = q }

    fun search() {
        val q = _query.value.trim()
        if (q.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "请输入搜索词")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, hasSearched = true)
            repository.search(q, 1)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(isLoading = false, error = e.message) }
        }
    }
}
