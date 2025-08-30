package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherForecastViewModel(private val api: WeatherApiService, private val apiKey: String) : ViewModel() {
    private val _forecastDays = MutableStateFlow<List<ForecastDay>?>(null)
    val forecastDays: StateFlow<List<ForecastDay>?> = _forecastDays

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchForecast(cityName: String, days: Int = 5) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response: Response<ForecastResponse> = api.getForecast(apiKey, cityName, days)
                if (response.isSuccessful) {
                    _forecastDays.value = response.body()?.forecast?.forecastday
                } else {
                    _error.value = "Failed to fetch forecast: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.localizedMessage}"
            }
            _loading.value = false
        }
    }
}
