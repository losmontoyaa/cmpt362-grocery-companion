package com.example.grocerycompanion

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import com.example.grocerycompanion.ui.screens.ForgotPassword
import com.example.grocerycompanion.ui.screens.LoginScreen
import com.example.grocerycompanion.ui.screens.SignUpPage
import com.example.grocerycompanion.ui.screens.StartUpScreen
import com.example.grocerycompanion.ui.theme.GroceryCompanionTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.FragmentActivity
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.grocerycompanion.ui.screens.CameraScreen
import com.example.grocerycompanion.ui.screens.XmlGokuHostScreen
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.grocerycompanion.BuildConfig
import com.example.grocerycompanion.ui.screens.ReceiptDisplay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import java.io.File

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

        var showBarcodeCamera by remember { mutableStateOf(false) }

        var showReceiptCamera by remember { mutableStateOf(false) }
        var receiptPhotoUri by remember { mutableStateOf<Uri?>(null) }
        var parsedReceipt by remember { mutableStateOf<String?>(null) }
        var showReceiptInfo by remember { mutableStateOf(false) }

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

            val receiptCameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {
                success ->
                if (success && receiptPhotoUri != null){
                    //Receipt photo taken! Now analyze it.
                    extractReceipt(ctx, receiptPhotoUri!!){
                        receiptText ->
                        parsedReceipt = receiptText
                        showReceiptInfo = true
                    }
                    showReceiptCamera = false
                }
                else
                    showReceiptCamera = false

            }

            // ---- NAVIGATION FLOW ----
            when {
                showGokuFlow -> {
                    XmlGokuHostScreen(onExit = { showGokuFlow = false })
                }

                //Barcode search: right now, looks up barcode number on google. -- Carlos
                showBarcodeCamera && hasPermission -> {
                    CameraScreen(
                        Modifier.fillMaxSize(),
                        onInfoScanned = { code ->
                            showBarcodeCamera = false
                            println("DEBUG: Barcode scanned: $code")
                            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                putExtra(SearchManager.QUERY, code)
                            }
                            ctx.startActivity(searchIntent)
                        },
                        onClose = { showBarcodeCamera = false }
                    )
                }

                showReceiptCamera && hasPermission ->{
                    LaunchedEffect(Unit) {
                        val photoFile = File(ctx.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
                        val photoUri = FileProvider.getUriForFile(ctx, "com.example.grocerycompanion.fileprovider", photoFile)

                        receiptPhotoUri = photoUri
                        receiptCameraLauncher.launch(photoUri)
                    }

                }

                //Displays the receipt information. In the final version it will do more but for now, just shows what was found. -- Carlos
                showReceiptInfo ->{
                    ReceiptDisplay(
                        receiptText = parsedReceipt ?: "No receipt text",
                        onClose = {
                            parsedReceipt = null
                            showReceiptInfo = false
                        }
                    )
                }

                else -> {
                    StartUpScreen(
                        onSearch = { },
                        onScanBarcodeClick = { showBarcodeCamera = true },
                        onScanReceiptClick = { showReceiptCamera = true },
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

//Uses MLKit OCR to extract text from the receipt. -- Carlos
private fun extractReceipt(context: Context, uri: Uri, onResult : (String) -> Unit){
    val image = InputImage.fromFilePath(context, uri)
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image).addOnSuccessListener { visionText ->
        val resultText = visionText.text
        println("DEBUG: Receipt text: $resultText")

        analyzeReceiptText(resultText){parsedText ->
            onResult(parsedText)
        }
    }
    .addOnFailureListener { e ->
        e.printStackTrace()
        onResult("Error: ${e.message}")
    }

}

//Using OpenAI, the content of the receipt is interpreted and put into a usable format. -- Carlos
private fun analyzeReceiptText(extractedText: String, onResult: (String) -> Unit){
    val client = OpenAI("")

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
            println("GPT RECEIPT ANALYSIS: $output")

            onResult(output)

        } catch (e: Exception) {
            e.printStackTrace()
            onResult("ERROR: ${e.message}")
        }
    }

}