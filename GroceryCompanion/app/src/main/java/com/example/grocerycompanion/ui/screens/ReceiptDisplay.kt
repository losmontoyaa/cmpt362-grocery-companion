package com.example.grocerycompanion.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


// Very simple receipt display for now. -- Carlos
@Composable
fun ReceiptDisplay(receiptText: String, onClose: () -> Unit){

    Box(modifier = Modifier.fillMaxSize().padding(20.dp)){

        Column {
            Text("Receipt Information: ", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.padding(10.dp))

            Text(receiptText, style = MaterialTheme.typography.bodyMedium)

        }

    }

}