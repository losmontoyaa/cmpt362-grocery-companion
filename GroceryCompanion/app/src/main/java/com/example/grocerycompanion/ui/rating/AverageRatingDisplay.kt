package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.floor

// Displays the average rating with stars and text
@Composable
fun AverageRatingDisplay(average: Double = 3.5) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        val numFullStars = floor(average).toInt()
        val hasHalfStar = average - numFullStars in 0.25..<0.75

        for (i in 1..5) {
            val icon = when {
                i <= numFullStars -> Icons.Filled.Star
                i == numFullStars + 1 && hasHalfStar -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.Star
            }

            val tint = when {
                i <= numFullStars || (i == numFullStars + 1 && hasHalfStar) -> Color.Yellow
                else -> Color.Gray
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.width(22.dp)
            )
        }

        Spacer(Modifier.width(6.dp))
        Text(String.format("%.1f", average), style = MaterialTheme.typography.bodyMedium)
    }
}