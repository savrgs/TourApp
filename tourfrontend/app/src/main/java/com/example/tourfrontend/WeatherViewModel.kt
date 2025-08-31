package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel(private val api: WeatherApiService, private val apiKey: String) : ViewModel() {
    private val _cityWeather = MutableStateFlow<WeatherResponse?>(null)
    val cityWeather: StateFlow<WeatherResponse?> = _cityWeather

    private val _placeWeather = MutableStateFlow<Map<Long, WeatherResponse>>(emptyMap())
    val placeWeather: StateFlow<Map<Long, WeatherResponse>> = _placeWeather

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    var lastError: String? = null

    fun fetchCityWeather(cityName: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response: Response<WeatherResponse> = api.getCurrentWeather(apiKey, cityName)
                if (response.isSuccessful) {
                    _cityWeather.value = response.body()
                    lastError = null
                } else {
                    _cityWeather.value = null
                    lastError = "Error: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _cityWeather.value = null
                lastError = "Exception: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchPlaceWeather(placeId: Long, lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val query = "$lat,$lon"
                val response: Response<WeatherResponse> = api.getCurrentWeather(apiKey, query)
                if (response.isSuccessful) {
                    val weather = response.body()
                    if (weather != null) {
                        _placeWeather.value = _placeWeather.value + (placeId to weather)
                    }
                }
            } catch (_: Exception) {}
        }
    }
}
