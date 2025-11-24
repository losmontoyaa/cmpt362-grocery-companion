package com.example.grocerycompanion.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.grocerycompanion.R


@Composable
fun StartUpScreen(
    modifier: Modifier = Modifier,
    onSearch: (SearchInput) -> Unit,
    onScanBarcodeClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    onOpenItemList: () -> Unit = {},
    onOpenProfile: () -> Unit = {}
) {

    val focusManager = LocalFocusManager.current
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Column(
            modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.profile_button_icon),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onOpenProfile() },
                    tint = Color.Unspecified
                )
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = "Find Groceries",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 20.dp),
                textAlign = TextAlign.Center,
                fontSize = 32.sp
            )

            Spacer(Modifier.height(20.dp))

            // make the search bar appear like pill shaped
            TextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Search Product Name or Barcode") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (query.isNotBlank()) onSearch(parseInput(query))
                    }
                ),

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )

            )


            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "OR", modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onScanBarcodeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Scan Barcode Lookup")
            }

            Spacer(Modifier.height(16.dp))


            Button(
                onClick = onScanReceiptClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text("Scan Receipt Lookup")
            }
        }


        // ── Bottom section ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),   // when keyboard up
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))
            Image(
                painter = painterResource(id = R.drawable.grocery_basket),
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Compare prices instantly",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))

            Button(onClick = onOpenItemList) {
                Text("Open Item List")
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun parseInput(raw: String): SearchInput {

    val trimmed = raw.trim()
    val isDigits = trimmed.all { it.isDigit() }
    val looksLikeBarcode = isDigits && trimmed.length in 8..14
    return if (looksLikeBarcode) SearchInput.Barcode(trimmed) else SearchInput.ProductName(trimmed)
}

sealed interface SearchInput {

    @JvmInline value class ProductName(val value: String) : SearchInput
    @JvmInline value class Barcode(val digits: String) : SearchInput
}








































