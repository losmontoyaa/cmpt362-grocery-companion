package com.example.grocerycompanion.model

import com.google.gson.annotations.SerializedName

/* Data classes used to hold the response from querying the Google API for directions */

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline
)

data class OverviewPolyline(
    val points: String
)