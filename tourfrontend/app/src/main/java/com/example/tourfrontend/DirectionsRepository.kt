package com.example.tourfrontend

import com.google.android.gms.maps.model.LatLng
import retrofit2.Response

class DirectionsRepository(private val api: DirectionsApiService, private val apiKey: String) {
    suspend fun getRoutePolyline(
        origin: String,
        destination: String,
        waypoints: String? = null
    ): List<LatLng>? {
        val response: Response<DirectionsResponse> = api.getDirections(origin, destination, waypoints, apiKey = apiKey, alternatives = false)
        if (response.isSuccessful) {
            val body = response.body()
            val polyline = body?.routes?.firstOrNull()?.overview_polyline?.points
            if (polyline != null) {
                return PolylineUtils.decode(polyline)
            }
        }
        return null
    }
}
