package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Panorama
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SoftGray
import com.example.ui.theme.PinkGradient

@Composable
fun StoriesScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Panorama,
                contentDescription = null,
                tint = PinkGradient,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Futuristic Stories",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "See what your friends are up to.",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftGray
            )
        }
    }
}
