package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Users click/press on a star to submit a rating. This opens up a NewRatingScreen, passing in the ratings/stars selected.
@Composable
fun InteractiveStarBar(
    starSize: Dp = 30.dp,
    onStarSelect: (Int) -> Unit
) {
    var selectedStars by remember { mutableStateOf(0) }

    Row {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= selectedStars) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Rating $i",
                modifier = Modifier
                    .size(starSize)
                    .clickable {
                        selectedStars = i
                        onStarSelect(i)
                    },
                tint = if (i <= selectedStars) Color.Yellow else Color.Gray
            )
        }
    }
}