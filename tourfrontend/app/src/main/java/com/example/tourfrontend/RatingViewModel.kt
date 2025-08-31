package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class RatingViewModel : ViewModel() {
    var submitting by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submitRating(placeId: Long, rating: Double?) {
        if (rating == null || rating < 0.0 || rating > 5.0) {
            errorMessage = "Enter a valid rating between 0 and 5"
            return
        }
        submitting = true
        errorMessage = null
        viewModelScope.launch {
            try {
                // Simulate network call
                kotlinx.coroutines.delay(1000)
                // TODO: Replace with actual API call
                submitting = false
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                submitting = false
            }
        }
    }
}
