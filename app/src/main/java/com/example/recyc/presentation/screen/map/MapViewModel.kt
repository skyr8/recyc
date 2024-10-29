package com.example.recyc.presentation.screen.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recyc.domain.usecase.PreferenceUseCase
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val preferenceUseCase: PreferenceUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        viewModelScope.launch {
            val homeLocation = preferenceUseCase.getHomeLocation()
            _uiState.value = _uiState.value.copy(homeLocation = homeLocation, selectedPosition = homeLocation)
        }
    }

    fun saveGeofenceLocation(latLng: LatLng) {
        viewModelScope.launch {
            _uiState.update { it.copy(homeLocation = latLng) }
            preferenceUseCase.setHomeLocation(latLng)
        }
    }

    fun updateSelectedPosition(latLng: LatLng) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedPosition = latLng) }
        }
    }

    fun updateUserLocation(latLng: LatLng) {
        viewModelScope.launch {
            _uiState.update { it.copy(userLocation = latLng) }
        }
    }
}

data class MapUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val homeLocation: LatLng? = null,
    val selectedPosition: LatLng? = null,
    val userLocation: LatLng? = null,
)
