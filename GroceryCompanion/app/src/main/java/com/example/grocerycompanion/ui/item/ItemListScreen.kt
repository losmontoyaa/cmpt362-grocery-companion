package com.example.grocerycompanion.ui.item

import android.location.Geocoder
import com.example.grocerycompanion.ui.common.PopCard
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.util.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

// Carlos Added: Search Query
@Composable
fun ItemListScreen(
    onItemClick: (String) -> Unit,
    searchQuery: String?
) {
    val vm: ItemListViewModel = viewModel(
        factory = ViewModelFactory {
            ItemListViewModel(
                itemRepo = FirebaseItemRepo()
            )
        }
    )

    val items by vm.items.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if(searchQuery != null && searchQuery != query){
            query = searchQuery
        }
    }

    val filtered = remember(query, items) {
        if (query.isBlank()) items
        else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.brand.contains(query, ignoreCase = true) ||
                    it.barcode.contains(query, ignoreCase = true)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search or scan barcode") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Loading items…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Couldn't load items",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = errorMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = { vm.retry() }) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }

            else {
                // Animated empty state
                AnimatedVisibility(
                    visible = filtered.isEmpty() && query.isNotBlank(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 })
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Oops, we couldn't find that item.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Would you like to add it?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("Add item")
                            }
                        }
                    }
                }

                // Animated list state
                AnimatedVisibility(
                    visible = filtered.isNotEmpty() || query.isBlank(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 8 })
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            ItemRow(
                                item = item,
                                onClick = { onItemClick(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            initialName = query,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, brand, barcode, category, storeName, lat, lng ->
                vm.addItem(
                    name = name,
                    brand = brand,
                    barcode = barcode,
                    category = category,
                    storeName = storeName,
                    latitude = lat,
                    longitude = lng,
                    price = 0.00
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ItemRow(
    item: Item,
    onClick: () -> Unit
) {
    PopCard(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.brand.isNotBlank()) {
                    Text(
                        text = item.brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.category.isNotBlank()) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddItemDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        brand: String,
        barcode: String,
        category: String,
        storeName: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initialName) }
    var brand by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var isResolving by remember { mutableStateOf(false) }
    var geoError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add item") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store name (e.g. Real Canadian Superstore)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = storeAddress,
                    onValueChange = { storeAddress = it },
                    label = { Text("Store address (e.g. 4700 Kingsway, Burnaby, BC)") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (geoError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = geoError ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isResolving,
                onClick = {
                    if (name.isBlank()) return@TextButton

                    val queryText = listOf(storeName, storeAddress)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")

                    if (queryText.isBlank()) {
                        // No location info – add item without coordinates
                        onConfirm(
                            name.trim(),
                            brand.trim(),
                            barcode.trim(),
                            category.trim(),
                            storeName.trim(),
                            null,
                            null
                        )
                        return@TextButton
                    }

                    // Resolve address -> lat/lng using Geocoder (off main thread)
                    isResolving = true
                    geoError = null

                    scope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val results = geocoder.getFromLocationName(queryText, 1)

                            val (lat, lng) =
                                if (!results.isNullOrEmpty()) {
                                    results[0].latitude to results[0].longitude
                                } else {
                                    null to null
                                }

                            withContext(Dispatchers.Main) {
                                isResolving = false
                                if (lat == null || lng == null) {
                                    geoError = "Couldn't resolve location. Item will be saved without map location."
                                    // You can decide: either block save, or still save without coords.
                                    onConfirm(
                                        name.trim(),
                                        brand.trim(),
                                        barcode.trim(),
                                        category.trim(),
                                        storeName.trim(),
                                        null,
                                        null
                                    )
                                } else {
                                    onConfirm(
                                        name.trim(),
                                        brand.trim(),
                                        barcode.trim(),
                                        category.trim(),
                                        storeName.trim(),
                                        lat,
                                        lng
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isResolving = false
                                geoError = "Error resolving location: ${e.localizedMessage ?: "Unknown error"}"
                                // still save without coordinates if you want:
                                onConfirm(
                                    name.trim(),
                                    brand.trim(),
                                    barcode.trim(),
                                    category.trim(),
                                    storeName.trim(),
                                    null,
                                    null
                                )
                            }
                        }
                    }
                }
            ) {
                Text(if (isResolving) "Resolving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
