package com.example.grocerycompanion.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerycompanion.util.Receipt


// Carlos Added: Completely Functional now!.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptDisplay(receipt : Receipt?, onAddItems: () -> Unit, onClose: () -> Unit){

        Scaffold (
            topBar = {
                CenterAlignedTopAppBar(
                    title = {Text("Item Confirmation",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                    }
                )
            }

        ) { paddingValues ->

            Column(
                modifier = Modifier.padding(paddingValues).padding(20.dp).fillMaxSize()
            ) {

                Text("${receipt?.storeName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)

                Text("${receipt?.address}",
                    style = MaterialTheme.typography.bodySmall,)

                Spacer(modifier = Modifier.padding(15.dp))

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(15.dp)){
                    receipt?.items?.forEach{ item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier.wrapContentHeight().fillMaxWidth()){
                            Text(item.name, style = MaterialTheme.typography.titleMedium)
                            Text(item.price, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Button(onClick = onAddItems) {
                        Text("Add New Items")
                    }

                    Spacer(modifier = Modifier.padding(10.dp))

                    Button(onClick = onClose) {
                        Text("Cancel")
                    }

                }
            }
        }
    }