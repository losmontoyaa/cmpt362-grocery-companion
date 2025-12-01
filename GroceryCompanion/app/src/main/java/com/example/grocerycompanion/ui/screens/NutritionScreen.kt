// NutritionScreen.kt
package com.example.grocerycompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.data.usda.FoodNutrient
import com.example.grocerycompanion.ui.nutrition.NutritionViewModel
import androidx.compose.ui.text.style.TextAlign


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    itemName: String,
    viewModel: NutritionViewModel = viewModel(),
    onBack: () -> Unit
) {
    LaunchedEffect(itemName) {
        viewModel.fetchNutritionForGroceryItem(itemName)
    }

    val uiState by viewModel.nutritionData
    val mainNutrients = uiState.mainNutrients
    val extraNutrients = uiState.extraNutrients

    Scaffold(
        topBar = {
            // same style as ItemDetailScreen custom row
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Text(
                        text = "Nutritional info: $itemName",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1f)              // use remaining width
                            .padding(end = 40.dp)    // small buffer on the right
                    )
                }
            }
        }
    ) { padding ->
        if (mainNutrients.isEmpty() && extraNutrients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading or no data found for $itemName...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                item {
                    Text(
                        text = "Per 100g approximate",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                    HorizontalDivider()
                }

                // Main “macro” group
                items(mainNutrients) { nutrient ->
                    NutrientRow(nutrient = nutrient)
                    HorizontalDivider()
                }

                // Extras header + items
                if (extraNutrients.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "More nutrients",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        HorizontalDivider()
                    }

                    items(extraNutrients) { nutrient ->
                        NutrientRow(nutrient = nutrient)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun NutrientRow(
    nutrient: FoodNutrient,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = nutrient.nutrientName,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "${"%.1f".format(nutrient.amount)} ${nutrient.unitName}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}