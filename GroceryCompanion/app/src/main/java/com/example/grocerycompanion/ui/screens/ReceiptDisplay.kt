package com.example.grocerycompanion.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerycompanion.util.Receipt


// Carlos Added: Actually does stuff now. Needs refinement.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDisplay(receipt : Receipt?, onAddItems: () -> Unit, onClose: () -> Unit){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Column {

            Text(
                "Receipt",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.padding(10.dp))

            Text("Store: ${receipt?.storeName}", style = MaterialTheme.typography.titleMedium)
            Text("Address: ${receipt?.address}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.padding(10.dp))

            Text(
                "Items:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.padding(6.dp))

            receipt?.items?.forEach { item ->
                Text(
                    "- ${item.name}: ${item.price}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.padding(20.dp))

            androidx.compose.material3.Button(onClick = onAddItems) {
                Text("Add Items to Database")
            }

            Spacer(modifier = Modifier.padding(10.dp))

            androidx.compose.material3.Button(onClick = onClose) {
                Text("Close")
            }
        }
    }
}