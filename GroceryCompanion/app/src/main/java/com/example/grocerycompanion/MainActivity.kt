package com.example.grocerycompanion

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.grocerycompanion.ui.item.ItemDetailScreen
import com.example.grocerycompanion.ui.item.ItemListScreen
import com.example.grocerycompanion.ui.list.ShoppingListScreen
import com.example.grocerycompanion.ui.screens.*
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.grocerycompanion.ui.start.AppStartPage


private enum class AuthScreen { Login, SignUp, Forgot }
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

        // Home flows
        var showGokuFlow by remember { mutableStateOf(false) }
        var showProfile by remember { mutableStateOf(false) }

        // Camera flows
        var showBarcodeCamera by remember { mutableStateOf(false) }
        var showReceiptCamera by remember { mutableStateOf(false) }
        var receiptPhotoUri by remember { mutableStateOf<Uri?>(null) }
        var parsedReceipt by remember { mutableStateOf<String?>(null) }
        var showReceiptInfo by remember { mutableStateOf(false) }

        val hasCameraPermission = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // Receipt photo launcher
        val receiptCameraLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && receiptPhotoUri != null) {
                    extractReceipt(ctx, receiptPhotoUri!!) { receiptText ->
                        parsedReceipt = receiptText
                        showReceiptInfo = true
                    }
                }
                showReceiptCamera = false
            }

        // Auto login
        LaunchedEffect(Unit) {
            if (auth.currentUser != null) isLoggedIn = true
        }

        if (isLoggedIn) {

            // Permission request
            if (!hasCameraPermission.value) {
                LaunchedEffect(Unit) {
                    ActivityCompat.requestPermissions(
                        ctx as MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        1
                    )
                }
            }

            when {
                showGokuFlow -> {
                    GroceryApp(
                        onBackToHome = {
                            // back to StartUpScreen
                            showGokuFlow = false
                        }
                    )
                }

                // -------- BARCODE CAMERA --------
                showBarcodeCamera && hasCameraPermission.value -> {
                    CameraScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeScanned = { code ->
                            showBarcodeCamera = false
                            Log.d("BARCODE_SCAN", "Scanned: $code")

                            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, code)
                            }
                            ctx.startActivity(searchIntent)
                        },
                        onClose = { showBarcodeCamera = false }
                    )
                }

                // -------- RECEIPT CAMERA --------
                showReceiptCamera && hasCameraPermission.value -> {
                    LaunchedEffect(Unit) {
                        val photoFile = File(
                            ctx.cacheDir,
                            "receipt_${System.currentTimeMillis()}.jpg"
                        )
                        val photoUri = FileProvider.getUriForFile(
                            ctx,
                            "com.example.grocerycompanion.fileprovider",
                            photoFile
                        )
                        receiptPhotoUri = photoUri
                        receiptCameraLauncher.launch(photoUri)
                    }
                }

                // -------- RECEIPT DISPLAY --------
                showReceiptInfo -> {
                    ReceiptDisplay(
                        receiptText = parsedReceipt ?: "No receipt text found.",
                        onClose = {
                            parsedReceipt = null
                            showReceiptInfo = false
                        }
                    )
                }

                // -------- PROFILE --------
                showProfile -> {
                    Profile(
                        onBack = { showProfile = false },
                        onSignOut = {
                            auth.signOut()
                            isLoggedIn = false
                            showProfile = false
                            showGokuFlow = false
                            showBarcodeCamera = false
                            showReceiptCamera = false
                            showReceiptInfo = false
                        }
                    )
                }

                // -------- HOME SCREEN --------
                else -> {
                    StartUpScreen(
                        onSearch = { _: SearchInput -> },
                        onScanBarcodeClick = { showBarcodeCamera = true },
                        onScanReceiptClick = { showReceiptCamera = true },
                        onOpenItemList = { showGokuFlow = true },
                        onOpenProfile = { showProfile = true }
                    )
                }
            }

        } else {

            var showStart by remember { mutableStateOf(true) }

            if (showStart) {
                AppStartPage(
                    onLoginClick = {
                        showStart = false
                        authScreen = AuthScreen.Login
                    },
                    onCreateAccountClick = {
                        showStart = false
                        authScreen = AuthScreen.SignUp
                    }
                )
            } else {


                // ---------- AUTH FLOW ----------
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
                        Modifier
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .fillMaxSize()
                    ) {
                        when (screen) {
                            AuthScreen.Login -> LoginScreen(
                                onLogin = { email, password ->
                                    auth.signInWithEmailAndPassword(email, password)
                                        .addOnSuccessListener { isLoggedIn = true }
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

                            AuthScreen.SignUp -> SignUpPage(
                                onReturnToLogin = { authScreen = AuthScreen.Login },
                                onSignUpComplete = { _, email, password ->
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnSuccessListener {
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

                            AuthScreen.Forgot -> ForgotPassword(
                                onReturnToLogin = { authScreen = AuthScreen.Login }
                            )
                        }
                    }
                }
            }
        }
    }
}


/* ---------- GOKU LIST UI ---------- */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroceryApp(
    onBackToHome: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.ITEMS) }
    var detailItemId by rememberSaveable { mutableStateOf<String?>(null) }

    var nutritionItemName by rememberSaveable { mutableStateOf<String?>(null) }

    val pagerState = rememberPagerState(
        initialPage = if (selectedTab == BottomTab.ITEMS) 0 else 1,
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()

    // Keep pager in sync when bottom tab changes
    LaunchedEffect(selectedTab) {
        val target = if (selectedTab == BottomTab.ITEMS) 0 else 1
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)
        }
    }

    // Keep bottom tab in sync when user swipes pager
    LaunchedEffect(pagerState.currentPage) {
        val newTab = if (pagerState.currentPage == 0) BottomTab.ITEMS else BottomTab.LIST
        if (selectedTab != newTab) {
            selectedTab = newTab
            detailItemId = null
        }
    }

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
                navigationIcon = {
                    // Only show Home button when not inside detail screen
                    if (detailItemId == null) {
                        TextButton(onClick = onBackToHome) {
                            Text("Home")
                        }
                    }
                },
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
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    icon = { Icon(Icons.Filled.List, contentDescription = "Items") },
                    label = { Text("Items") }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.LIST,
                    onClick = {
                        selectedTab = BottomTab.LIST
                        detailItemId = null
                        scope.launch { pagerState.animateScrollToPage(1) }
                    },
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "List") },
                    label = { Text("List") }
                )
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colorScheme.background
        ) {

            when {
                // nutrition screen
                nutritionItemName != null -> {
                    NutritionScreen(
                        itemName = nutritionItemName!!,
                        onBack = { nutritionItemName = null }
                    )
                }


                detailItemId != null -> {
                    ItemDetailScreen(
                        itemId = detailItemId!!,
                        onBack = { detailItemId = null },

                        // callback to itemDetail screen
                        onSeeNutrition = { itemName ->
                            nutritionItemName = itemName
                        }
                    )
                }
                else -> {
                    HorizontalPager(state = pagerState) { page ->
                        when (page) {
                            0 -> ItemListScreen(onItemClick = { id ->
                                detailItemId = id
                            })

                            1 -> ShoppingListScreen()
                        }
                    }
                }
            }
        }
    }
}

/* ---------- RECEIPT OCR + AI PARSING ---------- */
// Uses ML Kit OCR to extract raw text from the receipt, then sends it to OpenAI
// to be interpreted into a structured format. -- Carlos
private fun extractReceipt(context: Context, uri: Uri, onResult: (String) -> Unit) {
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer
        .process(image)
        .addOnSuccessListener { visionText ->
            val resultText = visionText.text
            Log.d("RECEIPT_OCR", "Raw receipt text: $resultText")

            analyzeReceiptText(resultText) { parsedText ->
                onResult(parsedText)
            }
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
            onResult("Error: ${e.message}")
        }
}

// Using OpenAI, the content of the receipt is interpreted and put into a usable format.
private fun analyzeReceiptText(extractedText: String, onResult: (String) -> Unit) {
    // ⚠️ IMPORTANT: Do NOT commit your real API key.
    // Recommended: expose it via BuildConfig and local.properties:
    // val client = OpenAI(BuildConfig.OPENAI_API_KEY)

    val client = OpenAI("[INSERT OPENAI API KEY HERE")  // or temporarily: OpenAI("CARLOS_KEY_HERE")

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val request = ChatCompletionRequest(
                model = ModelId("gpt-4o-mini"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = """
                            You are an AI designed only to parse information from text extracted from shopping receipts.
                            Your task is to discern and extract the following information from the provided text string and
                            return in strictly this format:

                            {
                                "store_name": "",
                                "address": "",
                                "items": [
                                    {"name": "", "price": ""}
                                ]
                            }

                            Do NOT include comments or explanations.
                        """.trimIndent()
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = extractedText
                    )
                )
            )

            val completion = client.chatCompletion(request)
            val output = completion.choices.first().message?.content ?: "NO RESULT"
            Log.d("RECEIPT_AI", "GPT receipt analysis: $output")

            // Switch back to main so we can safely update Compose state
            withContext(Dispatchers.Main) {
                onResult(output)
            }
        } catch (e: Exception) {
            Log.e("RECEIPT_AI", "Error calling OpenAI", e)
            withContext(Dispatchers.Main) {
                onResult("ERROR: ${e.message}")
            }
        }
    }
}


