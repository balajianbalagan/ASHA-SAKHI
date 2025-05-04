package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.DetailScaffold

@Composable
fun AppointmentsScreen(
    patientId: Int,
    onNavigateBack: () -> Unit
) {
    DetailScaffold(
        title = "Appointments",
        onNavigateBack = onNavigateBack
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "This is the appointments page for patient ID: $patientId",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
} 