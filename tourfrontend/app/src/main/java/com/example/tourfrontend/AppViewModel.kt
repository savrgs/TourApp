package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class AppViewModel : ViewModel() {
    var loggedInUser = mutableStateOf<User?>(null)
    var showRegister = mutableStateOf(false)
}