package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grocerycompanion.model.Rating
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Displays a couple reviews for the product showing the rating in stars and the comment.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingPreviewList(
    ratings: List<Rating>,
) {
    val commentedRatings = ratings.filter { it.comment.isNotBlank() }
    val sortedRatings = commentedRatings.sortedByDescending { it.timestamp }

    var showSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("All Reviews", style = MaterialTheme.typography.headlineSmall)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    items(sortedRatings) { rating ->
                        ReviewRow(rating)
                    }
                }
            }
        }
    }

    // Show only recent 2 reviews for now
    Column {
        sortedRatings.take(2).forEach { rating ->
            ReviewRow(rating)
        }

        if (ratings.size > 2) {
            Text(
                text = "More reviews",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable { showSheet = true }
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ReviewRow(rating: Rating) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row {
            for (i in 1..5) {
                val tint = if (i <= rating.stars) Color(0xFFFFBF00) else Color.Gray
                Icon(
                    imageVector = if (i <= rating.stars) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.width(16.dp),
                    tint = tint
                )
            }
        }

        if (rating.comment.isNotBlank()) {
            Text(text = rating.comment, style = MaterialTheme.typography.bodyMedium)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    }
}