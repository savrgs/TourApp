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

    private val _selectedPlaces = MutableStateFlow<List<PlaceDto>>(emptyList())
    val selectedPlaces: StateFlow<List<PlaceDto>> = _selectedPlaces

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

    fun selectPlace(place: PlaceDto) {
        if (!_selectedPlaces.value.contains(place)) {
            _selectedPlaces.value = _selectedPlaces.value + place
        }
    }

    fun unselectPlace(place: PlaceDto) {
        if (_selectedPlaces.value.contains(place)) {
            _selectedPlaces.value = _selectedPlaces.value - place
        }
    }

    fun togglePlaceSelection(place: PlaceDto, isSelected: Boolean) {
        if (isSelected) selectPlace(place) else unselectPlace(place)
    }

    fun fetchPlacesForCity(cityId: Long) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val fetchedPlaces = repository.getPlacesByCityId(cityId)
                _places.value = fetchedPlaces
                _selectedPlaces.value = emptyList() // Reset selection on city change
            } catch (e: Exception) {
                _places.value = emptyList()
                _selectedPlaces.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun planRoute(userLocation: LatLng?) {
        if (directionsRepository == null || userLocation == null) return
        val selected = _selectedPlaces.value
        if (selected.size < 2) {
            _routeError.value = "Please select at least two places to plan a route"
            _routePolyline.value = null
            return
        }
        routeJob?.cancel()
        routeJob = viewModelScope.launch {
            routeMutex.withLock {
                _routeLoading.value = true
                _routeError.value = null
                try {
                    delay(500) // debounce
                    val sortedPlaces = selected.sortedBy { place ->
                        val placeLocation = LatLng(place.latitude, place.longitude)
                        val userLoc = userLocation
                        Math.abs(userLoc.latitude - placeLocation.latitude) + Math.abs(userLoc.longitude - placeLocation.longitude)
                    }
                    val originStr = "${userLocation.latitude},${userLocation.longitude}"
                    val destinationStr = "${sortedPlaces.last().latitude},${sortedPlaces.last().longitude}"
                    val waypoints = sortedPlaces.dropLast(1).joinToString("|") { "${it.latitude},${it.longitude}" }
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