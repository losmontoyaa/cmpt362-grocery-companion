package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.model.RatingViewModel

@Composable
fun RatingDialog(
    itemId: String,
    productName: String,
    brand: String
) {
    val viewModel = viewModel<RatingViewModel>()
    var showDialog by remember { mutableStateOf(false) }
    var selectedStars by remember { mutableStateOf(0) }

    Column {
        InteractiveStarBar(onStarSelect = { stars ->
            selectedStars = stars
            showDialog = true
        })

        if (showDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    selectedStars = 0
                },
                confirmButton = {},
                text = {
                    NewRatingScreen(
                        stars = selectedStars,
                        onSubmit = { comment ->
                            viewModel.submitRating(
                                itemId = itemId,
                                productName = productName,
                                brand = brand,
                                stars = selectedStars,
                                comment = comment,
                                onSuccess = {
                                    println("Rating Submitted")
                                    selectedStars = 0
                                    showDialog = false
                                },
                                onFailure = { e ->
                                    println("Failed to submit: ${e.message}")
                                }
                            )
                        }
                    )
                }
            )
        }
    }
}