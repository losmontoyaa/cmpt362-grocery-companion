package com.example.grocerycompanion

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalContext
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.SearchInput
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.example.grocerycompanion.ui.screens.StartUpScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()


            /*var showLogin by remember { mutableStateOf(true) }

            Crossfade(
                targetState = showLogin,
                animationSpec = tween(durationMillis = 1000), // <-- goes here
                label = "fade"
            ) { login ->
                if (login) {
                    LoginScreen(onGoToSignUp = { showLogin = false })
                } else {
                    SignUpPage(onReturnToLogin = { showLogin = true })
                }
            }*/
        }
    }
}

@Composable
private fun AppRoot() {
    val ctx = LocalContext.current
    GroceryCompanionTheme {
        StartUpScreen(
            onSearch = { input: SearchInput ->
                when (input) {
                    is SearchInput.ProductName -> {
                        Log.i("StartupScreen", "User searched product name: ${input.value}")
                        Toast.makeText(ctx, "Search name: ${input.value}", Toast.LENGTH_SHORT).show()
                    }
                    is SearchInput.Barcode -> {
                        Log.i("StartupScreen", "User searched barcode: ${input.digits}")
                        Toast.makeText(ctx, "Search barcode: ${input.digits}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onScanBarcodeClick = {
                Log.i("StartupScreen", "User pressed Scan Barcode button")
                Toast.makeText(ctx, "Open scanner screen", Toast.LENGTH_SHORT).show()
            }
        )
    }
}