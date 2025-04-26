package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.DetailScaffold

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    DetailScaffold(
        title = stringResource(R.string.profile),
        onNavigateBack = onNavigateBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = "Pragati",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ASHA Worker ID: ASH123456",
                fontSize = 16.sp
            )

            // Profile sections
            ProfileSection(
                title = "Personal Information",
                items = listOf(
                    "Phone: +91 98765 43210",
                    "Email: pragati@asha.org",
                    "Location: Mumbai, Maharashtra"
                )
            )

            ProfileSection(
                title = "Work Statistics",
                items = listOf(
                    "Active Patients: 45",
                    "Years of Service: 5",
                    "Area Coverage: 3 km²"
                )
            )

            ProfileSection(
                title = "Certifications",
                items = listOf(
                    "ASHA Basic Training",
                    "Maternal Care Specialist",
                    "Digital Health Training"
                )
            )

            // Edit Profile Button
            Button(
                onClick = { /* TODO: Edit profile */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            items.forEach { item ->
                Text(
                    text = item,
                    fontSize = 14.sp
                )
            }
        }
    }
} 