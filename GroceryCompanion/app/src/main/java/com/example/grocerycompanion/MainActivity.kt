package com.example.grocerycompanion

import android.Manifest
import android.content.Context
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grocerycompanion.repo.FirebaseItemRepo
import com.example.grocerycompanion.ui.item.ItemDetailScreen
import com.example.grocerycompanion.ui.item.ItemListScreen
import com.example.grocerycompanion.ui.item.ItemListViewModel
import com.example.grocerycompanion.ui.list.ShoppingListScreen
import com.example.grocerycompanion.ui.screens.*
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.example.grocerycompanion.util.Receipt
import com.example.grocerycompanion.util.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

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
        var parsedReceipt by remember { mutableStateOf<Receipt?>(null) }
        var showReceiptInfo by remember { mutableStateOf(false) }

        // Carlos Added: -- pending search for barcode scanning (could also be used for search bar!).
        var pendingSearch by remember {mutableStateOf<String?>(null)}

        // Carlos Added -- View model to add receipt items to db
        val vm: ItemListViewModel = viewModel(
            factory = ViewModelFactory {
                ItemListViewModel(
                    itemRepo = FirebaseItemRepo()
                )
            }
        )

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
                    extractReceipt(ctx, receiptPhotoUri!!) { parsedText ->
                        parsedReceipt = parsedText
                        showReceiptInfo = true
                    }
                }
                showReceiptCamera = false
            }

        val hasLocationPermission = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val locationPermissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                hasLocationPermission.value = granted
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

            LaunchedEffect(Unit) {
                if (!hasLocationPermission.value) {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }


            // ---- NAVIGATION FLOW ----
            when {
                showGokuFlow -> {
                    GroceryApp(
                        onBackToHome = {
                            // back to StartUpScreen
                            showGokuFlow = false
                        },
                        searchQuery = pendingSearch
                    )
                }

                // -------- BARCODE CAMERA --------
                showBarcodeCamera && hasCameraPermission.value -> {
                    CameraScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBarcodeScanned = { code ->
                            showBarcodeCamera = false
                            Log.d("BARCODE_SCAN", "Scanned: $code")

                            //Carlos Added
                            pendingSearch = code
                            showGokuFlow = true

                            // jason branch addition:



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

                // Carlos Added: -------- RECEIPT DISPLAY --------
                showReceiptInfo -> {
                    ReceiptDisplay(
                        receipt = parsedReceipt,
                        onAddItems = {
                            val receiptItems = parsedReceipt?.items

                            if (receiptItems != null) {
                                val storeName = parsedReceipt!!.storeName

                                for (item in receiptItems) {
                                    vm.addItem(
                                        name = item.name,
                                        brand = storeName,
                                        barcode = "",
                                        category = "",
                                        storeName = storeName,
                                        latitude = null,
                                        longitude = null,
                                        price = item.price.toDouble()
                                    )
                                }

                                Toast.makeText(
                                    ctx,
                                    "Items added to search! Thank you for contributing!",
                                    Toast.LENGTH_LONG
                                ).show()
                                parsedReceipt = null
                                showReceiptInfo = false
                            }

                        },
                        onClose = {
                            Toast.makeText(
                                ctx,
                                "Receipt Scan Cancelled.",
                                Toast.LENGTH_SHORT
                            ).show()
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
                    // Carlos Added: Fixed search bar.
                    StartUpScreen(
                        onSearch = { input: SearchInput ->

                            pendingSearch = when (input) {
                                is SearchInput.ProductName -> {
                                    input.value
                                }

                                is SearchInput.Barcode -> {
                                    input.digits
                                }
                            }

                            showGokuFlow = true
                        },
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
    onBackToHome: () -> Unit,
    searchQuery: String?
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
                        IconButton(onClick = onBackToHome) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,        // green background
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,   // text color (white)
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                                detailItemId = id },
                                searchQuery = searchQuery
                            )
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
// to be interpreted into a structured format.
private fun extractReceipt(context: Context, uri: Uri, onResult: (Receipt?) -> Unit) {
    val img = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(img).addOnSuccessListener {
            scannedText ->
        val text = scannedText.text
        Log.d("OCR Output", "Extracted text: $text")

        analyzeReceiptText(text){
                scannedReceipt ->
            onResult(scannedReceipt)
        }
    }.addOnFailureListener { e ->
        e.printStackTrace()
        onResult(null) }
}

// Using OpenAI, the content of the receipt is interpreted and put into a usable format.
private fun analyzeReceiptText(extractedText: String, onResult: (Receipt?) -> Unit) {

    val client = OpenAI("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")

    //Carlos Added: last line of prompt changed slightly.
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
                            return in strictly this JSON format:

                            {
                                "store_name": "",
                                "address": "",
                                "items": [
                                    {"item_name": "", "price": ""}
                                ]
                            }

                            Do NOT include comments or explanations. Do NOT include the $ when returning price.
                        """.trimIndent()
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = extractedText
                    )
                )
            )

            val completion = client.chatCompletion(request)
            val output = completion.choices.first().message?.content ?: "{}"
            val json = Json {
                ignoreUnknownKeys = true
                allowStructuredMapKeys = true
                prettyPrint = true
                isLenient = true
                coerceInputValues = true
            }

            Log.d("RECEIPT_AI", "GPT receipt analysis: $output")

            val parsedReceipt = try{
                json.decodeFromString<Receipt>(output)

            }catch (e: Exception){
                Log.d("Json Parsing","Failed to Parse Generated JSON!")
                null
            }

            // Switch back to main so we can safely update Compose state
            withContext(Dispatchers.Main) {
                onResult(parsedReceipt)
            }

        } catch (e: Exception) {
            Log.e("RECEIPT_AI", "Error calling OpenAI", e)
            withContext(Dispatchers.Main) {
                onResult(null)
            }
        }
    }
}


