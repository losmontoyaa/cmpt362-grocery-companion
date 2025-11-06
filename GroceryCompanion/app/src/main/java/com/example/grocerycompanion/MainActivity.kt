package com.example.grocerycompanion

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.SearchInput
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}


@Composable
private fun AppRoot() {
    val ctx = LocalContext.current
    GroceryCompanionTheme {

        // overall app state
        var isLoggedIn by remember { mutableStateOf(false) }
        var showLogin by remember { mutableStateOf(true) }   // true = LoginScreen, false = SignUpPage

        if (isLoggedIn) {
            // User successfully logged in → go to StartUpScreen
            StartUpScreen(
                onSearch = { input ->
                    when (input) {
                        is SearchInput.ProductName -> {
                            Log.i("StartupScreen", "User searched product name: ${input.value}")
                            Toast.makeText(ctx, "Search: ${input.value}", Toast.LENGTH_SHORT).show()
                        }
                        is SearchInput.Barcode -> {
                            Log.i("StartupScreen", "User searched barcode: ${input.digits}")
                            Toast.makeText(ctx, "Barcode: ${input.digits}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onScanBarcodeClick = {
                    Log.i("StartupScreen", "User pressed Scan Barcode button")
                    Toast.makeText(ctx, "Open scanner screen", Toast.LENGTH_SHORT).show()
                }
            )

        } else {

            // small animated scale effect during screen change
            val scale by animateFloatAsState(
                targetValue = if (showLogin) 1f else 0.96f,
                animationSpec = tween(500)
            )

            Crossfade(
                targetState = showLogin,
                animationSpec = tween(500),
                label = "authCrossfade"
            ) { isLoginVisible ->

                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .fillMaxSize()
                ) {
                    if (isLoginVisible) {
                        LoginScreen(
                            onLogin = { email, password ->
                                if (email == "test@grocery.com" && password == "123456") {
                                    isLoggedIn = true
                                } else {
                                    Toast.makeText(ctx, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onGoToSignUp = { showLogin = false }
                        )
                    } else {
                        // using your existing clickable text inside SignUpPage to go back
                        SignUpPage(
                            onReturnToLogin = { showLogin = true }
                        )
                    }
                }
            }
        }
    }
}

