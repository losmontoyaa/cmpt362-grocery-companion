package com.example.grocerycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.FirebaseApp
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.grocerycompanion.ui.item.ItemDetailScreen
import com.example.grocerycompanion.ui.item.ItemListScreen
import com.example.grocerycompanion.ui.list.ShoppingListScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        // Fire-and-forget anonymous sign-in.
        // It will be very fast if the user was already signed-in before.
        FirebaseAuth.getInstance().signInAnonymously()

        setContent {
            GroceryCompanionTheme {
                GroceryApp()
            }
        }
    }
}


private enum class BottomTab { ITEMS, LIST }

// MainActivity.kt (only the GroceryApp() part)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryApp() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.ITEMS) }
    var detailItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val title = when {
        detailItemId != null -> "Item details"
        selectedTab == BottomTab.ITEMS -> "Browse items"
        selectedTab == BottomTab.LIST -> "Shopping list"
        else -> "" // Just in case future tabs are added
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.ITEMS,
                    onClick = {
                        selectedTab = BottomTab.ITEMS
                        detailItemId = null
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Items") },
                    label = { Text("Items") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.LIST,
                    onClick = {
                        selectedTab = BottomTab.LIST
                        detailItemId = null
                    },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "List") },
                    label = { Text("List") }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {
            if (detailItemId != null) {
                ItemDetailScreen(
                    itemId = detailItemId!!,
                    onBack = { detailItemId = null }
                )
            } else {
                when (selectedTab) {
                    BottomTab.ITEMS -> ItemListScreen(onItemClick = { id -> detailItemId = id })
                    BottomTab.LIST -> ShoppingListScreen()
                }
            }
        }
    }
}
