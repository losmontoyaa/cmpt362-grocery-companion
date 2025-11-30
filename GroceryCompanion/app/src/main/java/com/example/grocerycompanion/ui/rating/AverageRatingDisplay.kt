package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
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
fun AverageRatingDisplay(average: Double = 3.5, ratingCount: Int = 0) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        val numFullStars = floor(average).toInt()
        val hasHalfStar = average - numFullStars in 0.25..<1.0

        for (i in 1..5) {
            val icon = when {
                i <= numFullStars -> Icons.Filled.Star
                i == numFullStars + 1 && hasHalfStar -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.Star
            }

            val tint = when {
                i <= numFullStars || (i == numFullStars + 1 && hasHalfStar) -> Color(0xFFFFBF00)
                else -> Color.Gray
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.width(16.dp)
            )
        }

        Text(
            text = String.format("%.1f (%d)", average, ratingCount),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.offset(x = 2.dp, y = 1.dp)
            )
    }
}