package com.example.grocerycompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import androidx.compose.animation.core.tween

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var showLogin by remember { mutableStateOf(true) }

            /*if (showLogin) {
                // show login screen
                LoginScreen(
                    onGoToSignUp = { showLogin = false} // Switch to Sign up Screen
                )
            } else {
                // show sign up screen
                SignUpPage(
                    onReturnToLogin = { showLogin = true} // Switch back to Login
                )
            }*/
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
            }
        }
    }
}

