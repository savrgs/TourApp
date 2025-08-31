package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RatingViewModel(private val onRatingSubmitted: (() -> Unit)? = null) : ViewModel() {
    var submitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submitRating(placeId: Long, rating: Double?) {
        if (rating == null || rating < 1.0 || rating > 5.0) {
            errorMessage = "Enter a valid rating between 1 and 5"
            return
        }
        submitting = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val api = retrofit.create(RatingApiService::class.java)
                api.ratePlace(placeId, rating)
                submitting = false
                errorMessage = null
                onRatingSubmitted?.invoke()
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                submitting = false
            }
        }
    }
}
