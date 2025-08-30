package com.example.tourfrontend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import coil.compose.AsyncImage

@Composable
fun CityWeatherCard(weatherViewModel: WeatherViewModel, cityName: String) {
    val weather by weatherViewModel.cityWeather.collectAsState()
    val loading by weatherViewModel.loading.collectAsState()

    // Fetch weather when composable is shown
    androidx.compose.runtime.LaunchedEffect(cityName) {
        weatherViewModel.fetchCityWeather(cityName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Weather in $cityName", style = MaterialTheme.typography.titleMedium)
            if (loading) {
                CircularProgressIndicator()
            } else if (weather != null) {
                Text(text = "${weather!!.current.temp_c}°C, ${weather!!.current.condition.text}")
                Text(text = "Wind: ${weather!!.current.wind_kph} kph, Humidity: ${weather!!.current.humidity}%")
                AsyncImage(
                    model = "https:${weather!!.current.condition.icon}",
                    contentDescription = "Weather icon",
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Text(text = "No weather data available.")
            }
        }
    }
}
