package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
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

@Composable
fun CallsScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = null,
                tint = NeonBlue,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Crystal Clear Calls",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = PureWhite)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your recent calls will appear here.",
                style = MaterialTheme.typography.bodyLarge,
                color = SoftGray
            )
        }
    }
}
