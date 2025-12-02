package com.example.grocerycompanion.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.grocerycompanion.repo.DirectionRepo
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

// Composable to display a Google map showing a marker for the user’s location, a
// marker at the store’s location, and a polyline showing the directions between the two

@Composable
fun MapScreen(
    userLat: Double,
    userLng: Double,
    storeLat: Double,
    storeLng: Double,
    storeName: String
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(userLat, userLng), 12f)
    }
    val userMarkerState = remember {
        MarkerState(LatLng(userLat, userLng))
    }
    val storeMarkerState = remember {
        MarkerState(position = LatLng(storeLat, storeLng))
    }

    val routePoints = remember { mutableStateListOf<LatLng>() }
    val scope = rememberCoroutineScope()

    // Fetch polyline on launch
    LaunchedEffect(Unit) {
        scope.launch {
            val polyline = DirectionRepo.getRoute(
                origin = "$userLat,$userLng",
                destination = "$storeLat,$storeLng"
            )
            routePoints.clear()
            routePoints.addAll(polyline)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = userMarkerState,
            title = "You"
        )

        Marker(
            state = storeMarkerState,
            title = storeName
        )

        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                width = 12f
            )
        }
    }
}