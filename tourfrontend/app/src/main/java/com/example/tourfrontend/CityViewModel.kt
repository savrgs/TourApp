package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CityViewModel(private val repository: CityRepository) : ViewModel() {
    private val _cities = MutableStateFlow<List<CityDto>?>(null)
    val cities: StateFlow<List<CityDto>?> = _cities

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun fetchCities() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _cities.value = repository.getCities()
            } catch (e: Exception) {
                _cities.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
} 