package com.example.grocerycompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.ui.nutrition.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    itemName: String,
    onBack: () -> Unit,
    viewModel: NutritionViewModel = viewModel()
) {
    LaunchedEffect(itemName) {
        viewModel.fetchNutritionForGroceryItem(itemName)
    }

    val nutrients by viewModel.nutritionData

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nutritional info: $itemName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (nutrients.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading or no data found for $itemName...")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                item {
                    Text(
                        "Per 100g approximate",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()
                }
                items(nutrients) { nutrient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(nutrient.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${"%.1f".format(nutrient.amount)} ${nutrient.unitName}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
