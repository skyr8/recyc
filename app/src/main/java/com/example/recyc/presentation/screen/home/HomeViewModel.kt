package com.example.recyc.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.recyc.data.model.CheckedState
import com.example.recyc.data.model.DayEnum
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetCurrentRecyclerDayUseCase
import com.example.recyc.domain.usecase.GetRecyclerUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.example.recyc.domain.usecase.UpdateWidgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecyclerUseCase: GetRecyclerUseCase,
    private val getCurrentDayUseCase: GetCurrentDayUseCase,
    private val preferenceUseCase: PreferenceUseCase,
    private val currentRecyclingDayModel: GetCurrentRecyclerDayUseCase,
    private val updateWidgetUseCase: UpdateWidgetUseCase,
) : ViewModel() {

    val _recyclingState: MutableStateFlow<RecyclingState> = MutableStateFlow(RecyclingState())
    val recyclingDays = _recyclingState.asLiveData()

    init {
        refresh()
    }

    fun refresh() {
        _recyclingState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getRecyclerUseCase().let { recyclingDays ->
                val isCurrentDayConfirmed =
                    preferenceUseCase.isCurrentDayDone(currentRecyclingDayModel()?.day)
                _recyclingState.update {
                    it.copy(
                        recyclingDays = recyclingDays,
                        currentDay = getCurrentDayUseCase(),
                        isLoading = false,
                        isCurrentDayConfirmed = isCurrentDayConfirmed,
                        isCurrentDaySkipped = preferenceUseCase.isDaySkipped(currentRecyclingDayModel()?.day)
                    )
                }
            }
        }
    }

    fun setCurrentDayConfirmation(checkedState: CheckedState) {
        viewModelScope.launch {
            val currentDay = getCurrentDayUseCase()
            when (checkedState) {
                CheckedState.CONFIRM -> {
                    preferenceUseCase.setDayConfirmation(currentDay!!.name)
                    preferenceUseCase.skipDay(currentDay,false)
                }
                CheckedState.UNCHECKED -> preferenceUseCase.clearConfirmationDay()
                else -> {}
            }
            updateWidgetUseCase()
            _recyclingState.update { it.copy(isCurrentDayConfirmed = checkedState == CheckedState.CONFIRM) }
        }
    }

    data class RecyclingState(
        val recyclingDays: List<RecyclingDayModel>? = null,
        val currentDay: DayEnum? = null,
        val isLoading: Boolean = false,
        val isCurrentDayConfirmed: Boolean = false,
        val isCurrentDaySkipped: Boolean = false,
    )

}
