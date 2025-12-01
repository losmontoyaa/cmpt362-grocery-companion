package com.example.grocerycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.grocerycompanion.model.ProductSearchViewModel
import com.example.grocerycompanion.ui.map.MapScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme

class MapTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GroceryCompanionTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "list"
                ) {

                    composable("list") {

                        val viewModel: ProductSearchViewModel = viewModel()
                        val itemName = "2% Milk"
                        val brand = "Dairyland"
                        // TODO: Implement location services
                        // Hard-code SFU for user location for now
                        val userLat = 49.27897658366635
                        val userLng = -122.92046918164297
                        val products by viewModel.products.collectAsState()

                        LaunchedEffect(Unit) {
                            viewModel.searchProducts(itemName, brand, userLat, userLng)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                items(products) { (product, distance) ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                            .clickable {
                                                navController.navigate(
                                                    "map/$userLat/$userLng/${product.location?.latitude}/" +
                                                            "${product.location?.longitude}/${product.store_name}"
                                                )
                                            }
                                    ) {
                                        Text(
                                            text = "Store: ${product.store_name}",
                                            lineHeight = 14.sp
                                        )
                                        Text(
                                            text = "Price: ${product.total_price}",
                                            lineHeight = 14.sp
                                        )
                                        Text(
                                            text = "${"%.1f".format(distance)} km",
                                            fontSize = 12.sp,
                                            color = Color.DarkGray,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    composable(
                        route = "map/{userLat}/{userLng}/{storeLat}/{storeLng}/{storeName}",
                        arguments = listOf(
                            navArgument("userLat") { type = NavType.StringType },
                            navArgument("userLng") { type = NavType.StringType },
                            navArgument("storeLat") { type = NavType.StringType },
                            navArgument("storeLng") { type = NavType.StringType },
                            navArgument("storeName") { type = NavType.StringType }
                        )
                    ) { backStack ->

                        val userLat = backStack.arguments?.getString("userLat")!!.toDouble()
                        val userLng = backStack.arguments?.getString("userLng")!!.toDouble()
                        val storeLat = backStack.arguments?.getString("storeLat")!!.toDouble()
                        val storeLng = backStack.arguments?.getString("storeLng")!!.toDouble()
                        val storeName = backStack.arguments?.getString("storeName")!!

                        MapScreen(
                            userLat = userLat,
                            userLng = userLng,
                            storeLat = storeLat,
                            storeLng = storeLng,
                            storeName = storeName
                        )
                    }
                }
            }
        }
    }
}