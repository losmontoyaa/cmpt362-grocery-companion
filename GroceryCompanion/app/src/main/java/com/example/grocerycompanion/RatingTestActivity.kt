package com.example.grocerycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.model.RatingViewModel
import com.example.grocerycompanion.ui.rating.NewRatingScreen
import com.example.grocerycompanion.ui.rating.RatingSection
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme

class RatingTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GroceryCompanionTheme {
                val viewModel = viewModel<RatingViewModel>()
                val testItemId = "LD01"   // test ID

                var selectedStars by remember { mutableStateOf<Int?>(null) }

                if (selectedStars == null) {
                    RatingSection(
                        itemId = testItemId,
                        viewModel = viewModel,
                        onStarPress = { stars ->
                            selectedStars = stars
                        }
                    )
                } else {
                    NewRatingScreen(
                        itemId = testItemId,
                        stars = selectedStars!!,
                        onSubmit = { comment ->
                            viewModel.submitRating(
                                itemId = testItemId,
                                stars = selectedStars!!,
                                comment = comment,
                                onSuccess = {
                                    println("Rating Submitted")
                                    selectedStars = null
                                },
                                onFailure = { e ->
                                    println("Failed to submit: ${e.message}")
                                }
                            )
                        }
                    )
                }
            }
        }
    }

}