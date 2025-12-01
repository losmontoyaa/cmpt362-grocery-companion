package com.example.grocerycompanion.ui.rating

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Screen for users to submit a rating with an optional comment.
@Composable
fun NewRatingScreen(
    itemId: String,
    stars: Int,
    onSubmit: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Row {
            for (i in 1..5) {
                val tint = if (i <= stars) Color.Yellow else Color.Gray
                Icon(
                    imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = tint
                )
            }
        }

        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Optional comment") }
        )

        Button(onClick = {
            onSubmit(comment)
        }) {
            Text("Submit Rating")
        }
    }
}