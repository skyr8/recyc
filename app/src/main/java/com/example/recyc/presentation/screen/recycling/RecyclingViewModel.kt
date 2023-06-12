package com.example.recyc.presentation.screen.recycling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetRecyclerUseCase
import com.example.recyc.presentation.widget.updateWidget
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecyclingViewModel @Inject constructor(
    private val getRecyclerUseCase: GetRecyclerUseCase,
    private val getCurrentDayUseCase: GetCurrentDayUseCase
) : ViewModel() {

    val _recyclingState: MutableStateFlow<RecyclingState> = MutableStateFlow(RecyclingState())
    val recyclingDays = _recyclingState.asLiveData()

    init {
        _recyclingState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getRecyclerUseCase().let { recyclingDays ->
                _recyclingState.update {
                    it.copy(
                        recyclingDays = recyclingDays,
                        currentDay = getCurrentDayUseCase(),
                        isLoading = false
                    )
                }
            }
        }
    }

    data class RecyclingState(
        val recyclingDays: List<RecyclingDayModel>? = null,
        val currentDay: DayEnum? = null,
        val isLoading: Boolean = false
    )

}
