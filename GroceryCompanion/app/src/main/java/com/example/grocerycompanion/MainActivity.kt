package com.example.grocerycompanion

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.grocerycompanion.ui.screens.ForgotPassword
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.SearchInput
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.FragmentActivity
import com.example.grocerycompanion.ui.screens.CameraScreen
import com.example.grocerycompanion.ui.screens.XmlGokuHostScreen

// Simple 3-state auth flow
private enum class AuthScreen { Login, SignUp, Forgot }

class MainActivity : FragmentActivity() {

    private val cameraPermission = Manifest.permission.CAMERA

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

        var showCamera by remember { mutableStateOf(false) }
        var showGokuFlow by remember { mutableStateOf(false) }

        // Auto login if Firebase already has a user
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) isLoggedIn = true
        }

        if (isLoggedIn) {

            // ---- CAMERA PERMISSION CHECK ----
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        ctx, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            if (!hasPermission) {
                LaunchedEffect(Unit) {
                    ActivityCompat.requestPermissions(
                        ctx as MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        1
                    )
                }
            }

            // ---- NAVIGATION FLOW ----
            when {
                showGokuFlow -> {
                    XmlGokuHostScreen(onExit = { showGokuFlow = false })
                }

                showCamera && hasPermission -> {
                    CameraScreen(
                        Modifier.fillMaxSize(),
                        onBarcodeScanned = { code ->
                            showCamera = false
                            println("DEBUG: Barcode scanned: $code")
                            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, code)
                            }
                            ctx.startActivity(searchIntent)

                        },
                        onClose = { showCamera = false }
                    )
                }

                else -> {
                    StartUpScreen(
                        onSearch = { },
                        onScanBarcodeClick = { showCamera = true },
                        onOpenItemList = { showGokuFlow = true }
                    )
                }
            }
        }

        else {
            // your auth flow unchanged...
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
                        AuthScreen.Login -> LoginScreen(
                            onLogin = { email, password -> /* unchanged */ },
                            onGoToSignUp = { authScreen = AuthScreen.SignUp },
                            onForgotPassword = { authScreen = AuthScreen.Forgot }
                        )

                        AuthScreen.SignUp -> SignUpPage(
                            onReturnToLogin = { authScreen = AuthScreen.Login },
                            onSignUpComplete = { _, email, password -> /* unchanged */ }
                        )

                        AuthScreen.Forgot -> ForgotPassword(
                            onReturnToLogin = { authScreen = AuthScreen.Login }
                        )
                    }
                }
            }
        }
    }
}

