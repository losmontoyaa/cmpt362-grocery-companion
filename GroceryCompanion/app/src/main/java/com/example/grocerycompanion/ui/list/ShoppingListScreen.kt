package com.example.grocerycompanion.ui.list

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.foundation.shape.RoundedCornerShape


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.model.ShoppingListItem
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import com.example.grocerycompanion.util.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen() {
    // Use Firebase user (or demo fallback)
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demoUser"

    val vm: ShoppingListViewModel = viewModel(
        factory = ViewModelFactory {
            ShoppingListViewModel(
                listRepo = FirebaseShoppingListRepo(userId = userId),
                priceRepo = FirebasePriceRepo(),
                storeRepo = FirebaseStoreRepo(),
                itemRepo = FirebaseItemRepo()
            )
        }
    )

    val items by vm.list.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val productsById by vm.productsById.collectAsState()

    var storeTotals by remember { mutableStateOf<List<StoreTotal>>(emptyList()) }
    var perItemRecs by remember { mutableStateOf<Map<String, ItemPickUi>>(emptyMap()) }

    val estimatedTotal = remember(items, perItemRecs) {
        items.sumOf { sli ->
            val rec = perItemRecs[sli.itemId]
            (rec?.price ?: 0.0) * sli.qty
        }
    }
    val scope = rememberCoroutineScope()

    // Auto recompute totals & per-item recommendations whenever list changes
    LaunchedEffect(items) {
        if (items.isNotEmpty()) {
            val totals = vm.computePerStoreTotals()
            storeTotals = totals
            perItemRecs = vm.computePerItemCheapestUi()
        } else {
            storeTotals = emptyList()
            perItemRecs = emptyMap()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My list",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.weight(1f))
                if (items.isNotEmpty()) {
                    Text(
                        text = "${items.size} items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Loading / error states
            if (isLoading && items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Hero "Best overall store"
            val bestStore = storeTotals.firstOrNull()

            AnimatedVisibility(visible = bestStore != null) {
                bestStore?.let { best ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Text(
                                text = "Best overall store",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = best.storeName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Estimated total: $" + "%.2f".format(best.total),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            best.distanceKm?.let { d ->
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Approx. ${"%.1f".format(d)} km away",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Other store options
            if (storeTotals.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Other options",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        storeTotals.drop(1).forEach { total ->
                            val distStr = total.distanceKm?.let { d ->
                                " • ${"%.1f".format(d)} km"
                            } ?: ""
                            Text(
                                text = "${total.storeName}: $" +
                                        "%.2f".format(total.total) + distStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MAIN CONTENT AREA (takes remaining height)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (items.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your shopping list is empty.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.itemId }) { sli ->
                            val pickUi = perItemRecs[sli.itemId]
                            val product = productsById[sli.itemId]


                            ShoppingListRow(
                                item = sli,
                                productName = product?.name,
                                brand = product?.brand,
                                recommendation = pickUi,
                                onRemove = { id ->
                                    scope.launch { vm.remove(id) }
                                },
                                onQtyChange = { id, qty ->
                                    scope.launch { vm.setQty(id, qty) }
                                }
                            )
                        }
                    }
                }
            }

            // TOTAL PRICE SECTION AT BOTTOM
            if (items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estimated total (cheapest per item)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // If you still want to mention the "best overall store" when it exists:
                            bestStore?.let { best ->
                                Text(
                                    text = "Best overall store: ${best.storeName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "$" + "%.2f".format(estimatedTotal),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

        }
    }
}



@Composable
private fun ShoppingListRow(
    item: ShoppingListItem,
    productName: String?,
    brand: String?,
    recommendation: ItemPickUi?,
    onRemove: (String) -> Unit,
    onQtyChange: (String, Int) -> Unit
) {
    val displayName = productName ?: item.itemId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!brand.isNullOrBlank()) {
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (recommendation != null) {
                    Text(
                        text = "Best: ${recommendation.storeName} — $${"%.2f".format(recommendation.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            IconButton(
                onClick = { if (item.qty > 1) onQtyChange(item.itemId, item.qty - 1) },
                enabled = item.qty > 1
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease")
            }

            Text(
                text = item.qty.toString(),
                modifier = Modifier.width(32.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(
                onClick = { onQtyChange(item.itemId, item.qty + 1) }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Increase")
            }

            IconButton(
                onClick = { onRemove(item.itemId) }
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove")
            }
        }
    }
}
