package com.example.greatingkmp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _greetinnngList = MutableStateFlow<List<String>>(listOf())
    val greetingList: StateFlow<List<String>> get() = _greetinnngList

    init {
        viewModelScope.launch {
            Greeting().greet().collect { phrase ->
                _greetinnngList.update { list -> list + phrase }
            }
        }
    }
}