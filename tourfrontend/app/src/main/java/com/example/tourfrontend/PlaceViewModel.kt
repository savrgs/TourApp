package com.example.tourfrontend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlaceViewModel(
    private val repository: CityRepository,
    private val directionsRepository: DirectionsRepository? = null
) : ViewModel() {
    private val _places = MutableStateFlow<List<PlaceDto>?>(null)
    val places: StateFlow<List<PlaceDto>?> = _places

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _routePolyline = MutableStateFlow<List<LatLng>?>(null)
    val routePolyline: StateFlow<List<LatLng>?> = _routePolyline
    private val _routeLoading = MutableStateFlow(false)
    val routeLoading: StateFlow<Boolean> = _routeLoading
    private val _routeError = MutableStateFlow<String?>(null)
    val routeError: StateFlow<String?> = _routeError
    private var routeJob: Job? = null
    private val routeMutex = Mutex()

    fun fetchPlacesForCity(cityId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _places.value = repository.getPlacesByCityId(cityId)
            } catch (e: Exception) {
                _places.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchRoute(origin: LatLng, destinations: List<LatLng>) {
        if (directionsRepository == null) return
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            routeMutex.withLock {
                _routeLoading.value = true
                _routeError.value = null
                try {
                    delay(500) // debounce
                    if (destinations.size < 2) {
                        _routePolyline.value = null
                        _routeLoading.value = false
                        return@launch
                    }
                    val originStr = "${origin.latitude},${origin.longitude}"
                    val destinationStr = "${destinations.last().latitude},${destinations.last().longitude}"
                    val waypointList = destinations.dropLast(1)
                    val waypoints = if (waypointList.size > 1) {
                        "optimize:true|" + waypointList.joinToString("|") { "${it.latitude},${it.longitude}" }
                    } else {
                        waypointList.joinToString("|") { "${it.latitude},${it.longitude}" }
                    }
                    val polyline = directionsRepository.getRoutePolyline(originStr, destinationStr, if (waypoints.isEmpty()) null else waypoints)
                    _routePolyline.value = polyline
                } catch (e: Exception) {
                    _routeError.value = e.message
                    _routePolyline.value = null
                } finally {
                    _routeLoading.value = false
                }
            }
        }
    }
}