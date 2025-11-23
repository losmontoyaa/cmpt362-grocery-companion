package com.example.grocerycompanion.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.example.grocerycompanion.model.Item
import com.example.grocerycompanion.model.ShoppingListItem
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import com.example.grocerycompanion.util.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun ShoppingListScreen() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demoUser"

    val vm: ShoppingListViewModel = viewModel(
        factory = ViewModelFactory {
            ShoppingListViewModel(
                listRepo = FirebaseShoppingListRepo(userId = userId),
                priceRepo = FirebasePriceRepo(),
                storeRepo = FirebaseStoreRepo()
            )
        }
    )

    // 🔹 All shopping list items (itemId + qty)
    val items by vm.list.collectAsState()

    // 🔹 All item metadata from Firestore (for name/brand/img/category)
    val itemRepo = remember { FirebaseItemRepo() }
    val allItems by itemRepo.streamAll().collectAsState(initial = emptyList())
    val itemMap = remember(allItems) { allItems.associateBy { it.id } }

    // 🔹 Store totals + per-item cheapest (computed in VM)
    var storeTotals by remember { mutableStateOf<List<StoreTotal>>(emptyList()) }
    var perItemRecs by remember { mutableStateOf<Map<String, ItemPickUi>>(emptyMap()) }

    val scope = rememberCoroutineScope()

    // 🔁 Recompute whenever the shopping list changes
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
            // ---------- HEADER ----------
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

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- REFRESH BUTTON (manual) ----------
            FilledTonalButton(
                onClick = {
                    scope.launch {
                        val totals = vm.computePerStoreTotals()
                        storeTotals = totals
                        perItemRecs = vm.computePerItemCheapestUi()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = items.isNotEmpty()
            ) {
                Text("Refresh totals")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ---------- BEST OVERALL STORE HERO CARD ----------
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

            // ---------- OTHER STORE OPTIONS ----------
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

            // ---------- LIST CONTENT ----------
            if (items.isEmpty()) {
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
                        val meta = itemMap[sli.itemId]
                        val pickUi = perItemRecs[sli.itemId]

                        ShoppingListRow(
                            item = sli,
                            meta = meta,
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
    }
}

@Composable
private fun ShoppingListRow(
    item: ShoppingListItem,
    meta: Item?,
    recommendation: ItemPickUi?,
    onRemove: (String) -> Unit,
    onQtyChange: (String, Int) -> Unit
) {
    val displayName = meta?.name ?: item.itemId
    val brand = meta?.brand.orEmpty()
    val category = meta?.category.orEmpty()
    val imgUrl = meta?.imgUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(), // smooth resize on qty changes
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            AsyncImage(
                model = imgUrl,
                contentDescription = displayName,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (brand.isNotBlank()) {
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (category.isNotBlank()) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (recommendation != null) {
                    Text(
                        text = "Best at ${recommendation.storeName} — $${"%.2f".format(recommendation.price)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Qty controls + delete
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (item.qty > 1) onQtyChange(item.itemId, item.qty - 1) },
                        enabled = item.qty > 1
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = item.qty.toString(),
                        modifier = Modifier.width(24.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(
                        onClick = { onQtyChange(item.itemId, item.qty + 1) }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase")
                    }
                }

                IconButton(
                    onClick = { onRemove(item.itemId) }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                }
            }
        }
    }
}

