package com.example.grocerycompanion.model

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/*

ViewModel for retrieving a user’s location. Used to create a route to stores.
Uses FusedLocationProviderClient to get the device’s location and exposes it as a StateFlow

 */

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(application)

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location

    @SuppressLint("MissingPermission")
    fun loadLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                if (result != null) {
                    _location.value = LatLng(result.latitude, result.longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}