/*package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


// Displays a couple reviews for the product showing the rating in stars and the comment.
@Composable
fun RatingPreviewList(
    ratings: List<android.media.Rating>,
    onViewMore: () -> Unit = {}
) {
    // Just display ratings that have comments
    val commentedRatings = ratings.filter { it.comment.isNotBlank() }
    val sortedRatings = commentedRatings.sortedByDescending { it.timestamp }
    Column {
        sortedRatings.take(2).forEach { rating ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    for (i in 1..5) {
                        val tint = if (i <= rating.stars) Color.Yellow else Color.Gray
                        Icon(
                            imageVector = if (i <= rating.stars) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                }

                Text(text = rating.comment, style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider()
            }
        }

        // TODO: Implement another screen that shows more reviews
        if (ratings.size > 2) {
            Text(
                text = "More reviews",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onViewMore() }
                    .fillMaxWidth()
            )
        }
    }
}*/

package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.grocerycompanion.model.Rating

// Displays a couple reviews for the product showing the rating in stars and the comment.
@Composable
fun RatingPreviewList(
    ratings: List<Rating>,
    onViewMore: () -> Unit = {}
) {
    // Just display ratings that have comments
    val commentedRatings = ratings.filter { it.comment.isNotBlank() }
    val sortedRatings = commentedRatings.sortedByDescending { it.timestamp }
    Column {
        sortedRatings.take(2).forEach { rating ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row {
                    for (i in 1..5) {
                        val tint = if (i <= rating.stars) Color.Yellow else Color.Gray
                        Icon(
                            imageVector = if (i <= rating.stars) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            tint = tint
                        )
                    }
                }

                Text(text = rating.comment, style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider()
            }
        }

        // TODO: Implement another screen that shows more reviews
        if (ratings.size > 2) {
            Text(
                text = "More reviews",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onViewMore() }
                    .fillMaxWidth()
            )
        }
    }
}