package com.example.grocerycompanion.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grocerycompanion.R
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPassword(
    onReturnToLogin: () -> Unit
) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    val auth = remember { FirebaseAuth.getInstance() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_image),
            contentDescription = "Reset Image",
            modifier = Modifier.size(200.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text("Forgot Password", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter your email address") }
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val addr = email.trim()
                if (addr.isEmpty()) {
                    Toast.makeText(ctx, "Enter your email", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                auth.sendPasswordResetEmail(addr)
                    .addOnSuccessListener {
                        Toast.makeText(ctx, "Reset link sent to $addr", Toast.LENGTH_LONG).show()
                        onReturnToLogin()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(ctx, e.localizedMessage ?: "Could not send reset email", Toast.LENGTH_LONG).show()
                    }
            }
        ) {
            Text("Send reset link")
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Return to login page",
            modifier = Modifier.clickable { onReturnToLogin() }
        )
    }
}
