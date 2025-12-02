package com.example.grocerycompanion.ui.item

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.clickable
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.grocerycompanion.model.LocationViewModel
import com.example.grocerycompanion.model.ProductSearchViewModel
import com.example.grocerycompanion.model.RatingViewModel

import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.repo.FirebasePriceRepo
import com.example.grocerycompanion.repo.FirebaseShoppingListRepo
import com.example.grocerycompanion.repo.FirebaseStoreRepo

import com.example.grocerycompanion.ui.map.MapScreen
import com.example.grocerycompanion.ui.rating.AverageRatingDisplay
import com.example.grocerycompanion.ui.rating.RatingDialog
import com.example.grocerycompanion.ui.rating.RatingPreviewList
import com.example.grocerycompanion.util.LocationViewModelFactory

import com.example.grocerycompanion.util.ViewModelFactory
import kotlinx.coroutines.launch

/*

Displays information about an item based on its ID.
Displays the item’s image, name, brand, average rating, recent reviews, and prices by store.
Contains buttons and clickable text to change the item’s price, view its nutrition content, or add to the user’s shopping list.
Can display a map showing a route from the user’s location to a selected store carrying the item

 */

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

    val ratingVm: RatingViewModel = viewModel()
    val productVm: ProductSearchViewModel = viewModel()
    val locationVm: LocationViewModel = viewModel(
        factory = LocationViewModelFactory(context.applicationContext as Application)
    )

    // products holds a list of products and the distance from user coordinates to the product's store location
    val products by productVm.products.collectAsState()
    val userLocation by locationVm.location.collectAsState()

    LaunchedEffect(Unit) {
        locationVm.loadLocation()
    }

    LaunchedEffect(userLocation, item?.name, item?.brand) {
        if (userLocation != null && item != null) {
            productVm.searchProducts(
                item!!.name,
                item!!.brand,
                userLocation!!.latitude,
                userLocation!!.longitude
            )
        }
    }

    LaunchedEffect(itemId) {
        vm.load()
    }

    var qty by remember { mutableStateOf(1) }
    val scope = rememberCoroutineScope()

    var showMapDialog by remember { mutableStateOf(false) }
    var selectedStoreLat by remember { mutableStateOf(0.0) }
    var selectedStoreLng by remember { mutableStateOf(0.0) }
    var selectedStoreName by remember { mutableStateOf("") }

    var showPriceDialog by remember { mutableStateOf(false) }
    var newPrice by remember { mutableStateOf("") }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
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

                    // Info on left, actions on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: name, brand, rating text
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
                            // shows the average ratings and ratings count based
                            // on the database information
                            AverageRatingDisplay(
                                average = it.avgRating,
                                ratingCount = it.ratingsCount
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // change price + star bar + nutrition button
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Change Price",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .clickable { showPriceDialog = true }
                                    .padding(bottom = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onSeeNutrition(it.name) },
                                modifier = Modifier.widthIn(min = 140.dp)
                            ) {
                                Text("See nutrition")
                            }
                        }
                    }
                }
            }

            // Qty + Add to list row
            item {
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
            }


            // ───────── Ratings section (dialog + preview) ─────────
            item {
                item?.let { itItem ->
                    Text(
                        text = "Leave a review:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    RatingDialog(itItem.id, itItem.name, itItem.brand)

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Recent reviews:",
                        style = MaterialTheme.typography.titleSmall
                    )

                    // Jason's approach: load ratings for this item
                    ratingVm.loadRatings(itItem.name, itItem.brand)
                    val ratings = ratingVm.ratings
                    RatingPreviewList(ratings)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ───────── Prices by store (your Price/Store list) ─────────
            item {
                Text(
                    text = "Prices by Store",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (storePrices.isEmpty()) {
                item {
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
                }
            } else {

                // products is List<Pair<Product, Double>> where Double is distance
                val cheapestPrice = products.minOfOrNull { it.first.total_price } ?: 0.0

                items(products) { (product, distance) ->
                    StorePriceRow(
                        price = product.total_price,
                        size = product.size,
                        store = product.store_name,
                        isCheapest = product.total_price <= cheapestPrice,
                        distance = distance,
                        onQuickAdd = {
                            scope.launch {
                                vm.addToList(1)
                                Toast.makeText(
                                    context,
                                    "Added to list",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onOpenMap = {
                            selectedStoreLat = product.location?.latitude ?: 0.0
                            selectedStoreLng = product.location?.longitude ?: 0.0
                            selectedStoreName = product.store_name
                            showMapDialog = true
                        }
                    )
                }
            }
        }
    }

    // ───────── Map dialog (hook up later when you use products + locations) ─────────
    if (showMapDialog) {
        AlertDialog(
            onDismissRequest = { showMapDialog = false },
            confirmButton = {
                TextButton(onClick = { showMapDialog = false }) {
                    Text("Close")
                }
            },
            text = {
                MapScreen(
                    userLat = userLocation?.latitude ?: 0.0,
                    userLng = userLocation?.longitude ?: 0.0,
                    storeLat = selectedStoreLat,
                    storeLng = selectedStoreLng,
                    storeName = selectedStoreName
                )
            }
        )
    }

    // ───────── Change price dialog () ─────────
    if (showPriceDialog) {
        AlertDialog(
            onDismissRequest = { showPriceDialog = false },
            title = { Text("Update Price") },
            text = {
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { newPrice = it },
                    label = { Text("New Price") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val priceDouble = newPrice.toDoubleOrNull()
                    if (priceDouble != null) {
                        scope.launch {
                            vm.updatePrice(priceDouble)
                            Toast.makeText(
                                context,
                                "Price updated",
                                Toast.LENGTH_SHORT
                            ).show()
                            showPriceDialog = false
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Enter a valid number",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    Text(text = "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPriceDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}


@Composable
private fun StorePriceRow(
    price: Double,
    size: String,
    store: String,
    isCheapest: Boolean,
    distance: Double,
    onQuickAdd: () -> Unit,
    onOpenMap: () -> Unit
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
                    text = store,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$${"%.2f".format(price)} $size",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${"%.1f".format(distance)} km",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 14.sp
                )

                if (isCheapest) {
                    Text(
                        text = "Cheapest here",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = onOpenMap) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Open map"
                )
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

