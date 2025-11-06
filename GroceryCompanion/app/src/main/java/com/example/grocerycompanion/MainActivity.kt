package com.example.grocerycompanion

import android.os.Bundle
import android.util.*
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
import com.google.firebase.auth.FirebaseAuth


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

        var isLoggedIn by remember { mutableStateOf(false) }
        var showLogin by remember { mutableStateOf(true) }
        val auth = remember { FirebaseAuth.getInstance() }

        // Auto-enter if already signed in
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) isLoggedIn = true
        }

        if (isLoggedIn) {
            StartUpScreen(
                onSearch = { _: SearchInput -> /* hook up later */ },
                onScanBarcodeClick = { /* scanner later */ }
            )
        } else {
            // Slight scale for a nice Crossfade feel
            val scale by animateFloatAsState(
                targetValue = if (showLogin) 1f else 0.97f,
                animationSpec = tween(250), label = "authScale"
            )

            Crossfade(targetState = showLogin, animationSpec = tween(350), label = "authXfade") { loginVisible ->
                Box(
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .fillMaxSize()
                ) {
                    if (loginVisible) {
                        LoginScreen(
                            onLogin = { email, password ->

                                Log.i("AUTH_FLOW", "Login attempt with email: $email")

                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        Log.i("AUTH_FLOW", "LOGIN SUCCESS for user: ${auth.currentUser?.uid}")
                                        isLoggedIn = true }
                                    .addOnFailureListener {
                                        Toast.makeText(ctx, "Invalid credentials. Try again.", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onGoToSignUp = { showLogin = false }
                        )
                    } else {
                        SignUpPage(
                            onReturnToLogin = { showLogin = true },
                            onSignUpComplete = { name, email, password ->

                                Log.i("AUTH_FLOW", "Sign up attempt for email: $email (Name: $name)")
                                // name can be saved later to Firestore/Profile; for now we only create the auth user
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener {
                                        Log.i("AUTH_FLOW", "SIGN UP SUCCESS - user created: ${auth.currentUser?.uid}")
                                        Toast.makeText(ctx, "Account created. Please log in.", Toast.LENGTH_SHORT).show()
                                        showLogin = true
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(ctx, it.localizedMessage ?: "Sign up failed", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

