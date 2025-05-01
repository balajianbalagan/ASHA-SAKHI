package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.DetailScaffold

@Composable
fun DietSuggestionsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(
        title = "Diet Suggestions",
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Diet Suggestions for Patient ID: $patientId",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This is a placeholder screen. Actual diet suggestions will be implemented here.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 