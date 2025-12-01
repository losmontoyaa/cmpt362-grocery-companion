package com.example.grocerycompanion.ui.item

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grocerycompanion.model.Price
import com.example.grocerycompanion.model.Store
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo
import com.example.grocerycompanion.util.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    itemId: String,
    onSeeNutrition: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "demoUser"

    val vm: ItemViewModel = viewModel(
        key = "item-$itemId-$userId",
        factory = ViewModelFactory {
            ItemViewModel(
                itemId = itemId,
                itemRepo = FirebaseItemRepo(),
                priceRepo = FirebasePriceRepo(),
                storeRepo = FirebaseStoreRepo(),
                listRepo = FirebaseShoppingListRepo(userId = userId)
            )
        }
    )

    val item by vm.item.observeAsState()
    val storePrices by vm.storePrices.observeAsState(emptyList())

    LaunchedEffect(itemId) {
        vm.load()
    }

    var qty by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    // Back button aligned left
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    // Centered product name
                    Text(
                        text = item?.name ?: "Item details",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }


    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item?.let { it ->
                // Image card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        AsyncImage(
                            model = it.imgUrl,
                            contentDescription = it.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                // Info on left, See nutrition button on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.headlineSmall,
                        )

                        if (it.brand.isNotBlank()) {
                            Text(
                                text = it.brand,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${"%.1f".format(it.avgRating)} ★ (${it.ratingsCount} ratings)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSeeNutrition(it.name) },
                        modifier = Modifier.widthIn(min = 120.dp)
                    ) {
                        Text("See nutrition")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Qty + Add to list row
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Qty",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = { if (qty > 1) qty-- }) {
                    Text("-")
                }

                Text(
                    text = qty.toString(),
                    modifier = Modifier
                        .width(32.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )

                OutlinedButton(onClick = { if (qty < 99) qty++ }) {
                    Text("+")
                }

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            vm.addToList(qty)
                            Toast.makeText(
                                context,
                                "Added $qty to list",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add to List")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Prices by Store",
                style = MaterialTheme.typography.titleMedium
            )

            if (storePrices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No price data yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    items(storePrices) { (price, store, isCheapest) ->
                        StorePriceRow(
                            price = price,
                            store = store,
                            isCheapest = isCheapest,
                            onQuickAdd = {
                                scope.launch {
                                    vm.addToList(1)
                                    Toast.makeText(
                                        context,
                                        "Added to list",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorePriceRow(
    price: Price,
    store: Store,
    isCheapest: Boolean,
    onQuickAdd: () -> Unit
) {
    val bgColor =
        if (isCheapest) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.surface

    Surface(
        color = bgColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (isCheapest) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$${"%.2f".format(price.price)} ${price.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isCheapest) {
                    Text(
                        text = "Cheapest here",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onQuickAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick add"
                )
            }
        }
    }
}

