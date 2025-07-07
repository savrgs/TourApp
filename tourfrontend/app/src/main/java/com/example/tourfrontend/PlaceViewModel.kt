package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlaceViewModel(private val repository: CityRepository) : ViewModel() {
    private val _places = MutableStateFlow<List<PlaceDto>?>(null)
    val places: StateFlow<List<PlaceDto>?> = _places

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun fetchPlacesForCity(cityId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _places.value = repository.getPlacesByCityId(cityId)
            } catch (e: Exception) {
                _places.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
} 