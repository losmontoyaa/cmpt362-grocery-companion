package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.grocerycompanion.model.RatingViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Overview of the ratings and comments on a product. When a star is pressed on the Interactive star bar, it triggers the NewRatingScreen to appear.
@Composable
fun RatingSection(
    itemId: String,
    viewModel: RatingViewModel = viewModel(),
    onStarPress: (Int) -> Unit
) {
    LaunchedEffect(itemId) {
        viewModel.loadRatings(itemId)
    }
    val ratings = viewModel.ratings
    val average = viewModel.averageRating

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AverageRatingDisplay(average)
        InteractiveStarBar(onStarSelect = onStarPress)
        Text("Reviews:")
        RatingPreviewList(ratings)
    }
}
