package com.example.grocerycompanion.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import com.example.grocerycompanion.R
import androidx.compose.ui.geometry.Offset

@Composable
fun AppStartPage(
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val darkGreen = Color(0xFF00521B)
    val midGreen = Color(0xFF07A71C)
    val lightGreen = Color(0xFF20D45A)

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(lightGreen, midGreen, darkGreen),
                        center = Offset.Zero,
                        radius = 900f
                    )
                )
        ) {
            // Fake “floating shapes” with soft circles
            FloatingCircles()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // Logo card
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(18.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.grocery_basket),
                        contentDescription = "Grocery Companion logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "GroceryCompanion",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Compare Prices, Track Nutrition,\nand Make Smarter Grocery Trips.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Login button with shadow
                ShadowButton(
                    text = "Login",
                    onClick = onLoginClick,
                    contentColor = midGreen
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Create account button with shadow
                ShadowButton(
                    text = "Create an Account",
                    onClick = onCreateAccountClick,
                    contentColor = midGreen
                )
            }
        }
    }
}

@Composable
private fun ShadowButton(
    text: String,
    onClick: () -> Unit,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(50.dp))
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = contentColor
            )
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FloatingCircles() {
    // Just a few translucent circles to give depth
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(70.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(55.dp))
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(40.dp))
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .alpha(0.9f)
        )
    }
}
