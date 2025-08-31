package com.example.tourfrontend

data class ForecastResponse(
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val condition: Condition,
    val maxwind_kph: Double,
    val avghumidity: Double
)

data class Condition(
    val text: String,
    val icon: String
)
