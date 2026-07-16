package com.wb.example.demo_04.ui.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wb.example.demo_04.data.remote.model.Character
import com.wb.example.demo_04.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// UI 状态（一次性 UI 事件/标志，不进列表流）
data class CharactersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
)

// ViewModel = iOS 的 ObservableObject + @MainActor 持有 @Published 状态。
// 用 StateFlow 暴露不可变状态，对应 Swift 的 @Published / CurrentValueSubject。
class CharactersViewModel(
    private val repository: CharacterRepository,
) : ViewModel() {

    // 列表数据：直接观察 Room（数据库作为单一数据源，断网也能显示已缓存数据）。
    val characters: StateFlow<List<Character>> = repository.observeCharacters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(CharactersUiState())
    val uiState: StateFlow<CharactersUiState> = _uiState.asStateFlow()

    init { loadFirstPage() }

    // 对应 iOS 的 Task { try await api.load() } 拉取首屏
    fun loadFirstPage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.refresh(1)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false) }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun retry() = loadFirstPage()
}
