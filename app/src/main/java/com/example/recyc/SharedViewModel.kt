package com.example.recyc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _confirmationState = MutableStateFlow<Boolean?>(null)
    val confirmationState = _confirmationState.asLiveData()

    fun setDayConfirmation(date: Boolean) {
        viewModelScope.launch {
            _confirmationState.value = date
        }
    }
}
