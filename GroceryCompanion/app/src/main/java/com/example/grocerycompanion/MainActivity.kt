package com.example.grocerycompanion

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.grocerycompanion.ui.item.ItemDetailScreen
import com.example.grocerycompanion.ui.item.ItemListScreen
import com.example.grocerycompanion.ui.list.ShoppingListScreen
import com.example.grocerycompanion.ui.screens.ForgotPassword
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.Profile
import com.example.grocerycompanion.ui.screens.SearchInput
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.google.firebase.auth.FirebaseAuth

// Auth screens for the login flow
private enum class AuthScreen { Login, SignUp, Forgot }

// Tabs for Goku's shopping UI
private enum class BottomTab { ITEMS, LIST }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val ctx = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }

    GroceryCompanionTheme {

        var isLoggedIn by remember { mutableStateOf(false) }
        var authScreen by remember { mutableStateOf(AuthScreen.Login) }

        // For the "home" after login
        var showGokuFlow by remember { mutableStateOf(false) }
        var showProfile by remember { mutableStateOf(false) }

        // Auto-enter if already signed in with Firebase
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) isLoggedIn = true
        }

        if (isLoggedIn) {
            when {
                showProfile -> {
                    Profile(
                        onBack = { showProfile = false },
                        onSignOut = {
                            auth.signOut()
                            isLoggedIn = false
                            showProfile = false
                            showGokuFlow = false
                        }
                    )
                }

                showGokuFlow -> {
                    // Goku's compose-based item/list experience
                    GroceryApp()
                }

                else -> {
                    StartUpScreen(
                        onSearch = { _: SearchInput -> },
                        onScanBarcodeClick = { /* TODO */ },
                        onOpenItemList = { showGokuFlow = true },
                        onOpenProfile = { showProfile = true }
                    )
                }
            }
        } else {
            // Slight scale for a nice Crossfade feel
            val scale by animateFloatAsState(
                targetValue = if (authScreen == AuthScreen.Login) 1f else 0.97f,
                animationSpec = tween(250),
                label = "authScale"
            )

            Crossfade(
                targetState = authScreen,
                animationSpec = tween(350),
                label = "authXfade"
            ) { screen ->
                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .fillMaxSize()
                ) {
                    when (screen) {
                        AuthScreen.Login -> {
                            LoginScreen(
                                onLogin = { email, password ->
                                    Log.i("AUTH_FLOW", "Login attempt with email: $email")
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener {
                                            Log.i(
                                                "AUTH_FLOW",
                                                "LOGIN SUCCESS for user: ${auth.currentUser?.uid}"
                                            )
                                            isLoggedIn = true
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                ctx,
                                                "Invalid credentials. Try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                },
                                onGoToSignUp = { authScreen = AuthScreen.SignUp },
                                onForgotPassword = { authScreen = AuthScreen.Forgot }
                            )
                        }

                        AuthScreen.SignUp -> {
                            SignUpPage(
                                onReturnToLogin = { authScreen = AuthScreen.Login },
                                onSignUpComplete = { name, email, password ->
                                    Log.i(
                                        "AUTH_FLOW",
                                        "Sign up attempt for email: $email (Name: $name)"
                                    )
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener {
                                            Log.i(
                                                "AUTH_FLOW",
                                                "SIGN UP SUCCESS - user: ${auth.currentUser?.uid}"
                                            )
                                            Toast.makeText(
                                                ctx,
                                                "Account created. Please log in.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            authScreen = AuthScreen.Login
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(
                                                ctx,
                                                it.localizedMessage ?: "Sign up failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            )
                        }

                        AuthScreen.Forgot -> {
                            ForgotPassword(
                                onReturnToLogin = { authScreen = AuthScreen.Login }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Goku's compose bottom-nav shopping UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryApp() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.ITEMS) }
    var detailItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val title = when {
        detailItemId != null -> "Item details"
        selectedTab == BottomTab.ITEMS -> "Browse items"
        selectedTab == BottomTab.LIST -> "Shopping list"
        else -> ""
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

