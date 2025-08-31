package com.example.tourfrontend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastScreen(cityName: String, forecast: List<ForecastDay>?, loading: Boolean, error: String?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forecast for $cityName", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(error, color = Color.Red, fontSize = 18.sp, modifier = Modifier.padding(24.dp))
                }
                forecast != null -> {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(forecast.size) { idx ->
                            val day = forecast[idx]
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.width(180.dp).height(220.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(day.date, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    AsyncImage(
                                        model = "https:${day.day.condition.icon}",
                                        contentDescription = day.day.condition.text,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text("${day.day.maxtemp_c}°C / ${day.day.mintemp_c}°C", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text(day.day.condition.text, fontSize = 14.sp)
                                    Text("Wind: ${day.day.maxwind_kph} kph", fontSize = 12.sp)
                                    Text("Humidity: ${day.day.avghumidity}%", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
