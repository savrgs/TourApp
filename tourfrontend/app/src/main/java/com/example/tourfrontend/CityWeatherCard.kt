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

import androidx.navigation.NavController
import androidx.compose.foundation.clickable
@Composable
fun CityWeatherCard(
    weatherViewModel: WeatherViewModel,
    cityName: String,
    navController: NavController
) {
    val weather by weatherViewModel.cityWeather.collectAsState()
    val loading by weatherViewModel.loading.collectAsState()

    // Fetch weather when composable is shown
    androidx.compose.runtime.LaunchedEffect(cityName) {
        // Use English city name as expected by WeatherAPI.com
        weatherViewModel.fetchCityWeather(cityName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate("weatherDetail/${cityName}")
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Weather in $cityName",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable {
                    // TODO: Replace with correct navigation logic
                }
            )
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
                val error = weatherViewModel.lastError
                if (error != null) {
                    Text(text = error)
                } else {
                    Text(text = "No weather data available. Make sure the city name is valid and in English as expected by WeatherAPI.com.")
                }
            }
        }
    }
}
