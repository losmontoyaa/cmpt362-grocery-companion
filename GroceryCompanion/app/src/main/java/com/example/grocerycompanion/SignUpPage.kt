package com.example.grocerycompanion

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpPage(
    onReturnToLogin: () -> Unit = {}
) {

    var email by remember {
        mutableStateOf( "")
    }

    var password by remember {
        mutableStateOf( "")
    }

    var name by remember {
        mutableStateOf( "")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // generated image from chatGPT - nice one eh?
        Image(painter = painterResource(id = R.drawable.login_image), contentDescription = "Login Image",
            modifier = Modifier.size(200.dp))

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Create Your Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = {name = it}, label = {
            Text(text = "Enter your Name")
        })

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = {email = it}, label = {
            Text(text = "Enter Your Email Address")
        })

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = {password = it}, label = {
            Text(text = "Enter Your Password")

            // hides the password as dots
        }, visualTransformation = PasswordVisualTransformation())


        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {

            // this the logic for the login - we will use firebase authentication for it

            Log.i("Credential", "Name : $name Email : $email Password : $password")
        }) {
            Text(text = "Save")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(text = "Return To Login Page", modifier = Modifier.clickable {
            onReturnToLogin()
        })
    }

}