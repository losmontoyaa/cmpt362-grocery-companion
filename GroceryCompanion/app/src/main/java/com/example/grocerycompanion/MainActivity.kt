package com.example.grocerycompanion

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.grocerycompanion.ui.screens.ForgotPassword
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.SearchInput
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.FragmentActivity
import com.example.grocerycompanion.ui.screens.XmlGokuHostScreen
import com.google.firebase.BuildConfig

// Simple 3-state auth flow
private enum class AuthScreen { Login, SignUp, Forgot }

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val ctx = LocalContext.current
    GroceryCompanionTheme {

        var isLoggedIn by remember { mutableStateOf(false) }
        var authScreen by remember { mutableStateOf(AuthScreen.Login) }
        val auth = remember { FirebaseAuth.getInstance() }

        // TODO: Remove
        isLoggedIn = true

        // Auto-enter if already signed in
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) isLoggedIn = true
        }

        if (isLoggedIn) {

            // NEW toggle state
            var showGokuFlow by remember { mutableStateOf(false) }

            if (showGokuFlow) {
                XmlGokuHostScreen(onExit = { showGokuFlow = false })
            } else {
                StartUpScreen(
                    onSearch = { _: SearchInput -> },
                    onScanBarcodeClick = { },
                    onOpenItemList = { showGokuFlow = true }   // your button triggers this
                )
            }

        } else {
            // Slight scale for a nice Crossfade feel
            val scale by animateFloatAsState(
                targetValue = if (authScreen == AuthScreen.Login) 1f else 0.97f,
                animationSpec = tween(250), label = "authScale"
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
                                    // Create the auth user (save profile later if needed)
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

