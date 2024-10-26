package com.example.recyc.presentation.screen.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.recyc.data.model.RecyclingType
import com.example.recyc.domain.model.RecyclingDayModel
import com.example.recyc.domain.usecase.GetCurrentDayUseCase
import com.example.recyc.domain.usecase.GetRecyclerDetailUseCase
import com.example.recyc.domain.usecase.PreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val preferenceUseCase: PreferenceUseCase,
    private val getRecyclerDetailUseCase: GetRecyclerDetailUseCase,
    private val getCurrentDayUseCase: GetCurrentDayUseCase,
) : ViewModel() {
    val _detailState: MutableStateFlow<DetailState> = MutableStateFlow(DetailState())
    val detailDays = _detailState.asLiveData()

    fun getDetail(id: Int) {
        viewModelScope.launch {
            val currentDay = getCurrentDayUseCase()
            val recyclingDayModel = getRecyclerDetailUseCase(id)
            _detailState.value = _detailState.value.copy(
                recyclingDayModel = recyclingDayModel,
                isCurrentDay = currentDay == recyclingDayModel?.day,
                isCurrentDayConfirmed = preferenceUseCase.isCurrentDayDone(currentDay)
            )
        }
    }

    fun saveChanges() {
        viewModelScope.launch {
            val recyclingDayModel = _detailState.value.recyclingDayModel
            if (_detailState.value.isCurrentDay) {
                if (_detailState.value.isCurrentDayConfirmed) {
                    preferenceUseCase.setDayConfirmation(recyclingDayModel?.day?.name.orEmpty())
                }else{
                    preferenceUseCase.clearConfirmationDay()
                }
            }
            recyclingDayModel?.let {
                preferenceUseCase.setRecyclerDay(
                    recyclingDayModel.id,
                    recyclingDayModel,
                )
            }
        }
    }

    fun updateDay(list: List<String>) {
        viewModelScope.launch {
            val recyclingDayModel = _detailState.value.recyclingDayModel
            _detailState.update {
                it.copy(recyclingDayModel = recyclingDayModel?.copy(type = list.map {
                    RecyclingType.valueOf(it)
                }))
            }
        }
    }

    fun updateConfirmationDay(isConfirmed: Boolean) {
        viewModelScope.launch {
            _detailState.update { it.copy(isCurrentDayConfirmed = isConfirmed) }
        }
    }

    data class DetailState(
        val recyclingDayModel: RecyclingDayModel? = null,
        val isLoading: Boolean = false,
        val isCurrentDay: Boolean = false,
        val isCurrentDayConfirmed: Boolean = false,
    )
}
