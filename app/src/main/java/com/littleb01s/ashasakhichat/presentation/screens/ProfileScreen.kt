package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.DetailScaffold
import com.littleb01s.ashasakhichat.presentation.ProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val showSignOutDialog by viewModel.showSignOutDialog.collectAsState()

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideSignOutDialog() },
            title = { Text("Success") },
            text = { Text("You have been successfully signed out.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.hideSignOutDialog()
                        onSignOut()
                    }
                ) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }

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
                text = "${userProfile.firstName} ${userProfile.lastName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ASHA Worker ID: ${userProfile.workerId}",
                fontSize = 16.sp
            )

            // Personal Information
            ProfileSection(
                title = "Personal Information",
                items = listOf(
                    "Profile ID: ${userProfile.profileId}",
                    "Worker ID: ${userProfile.workerId}",
                    "Specialization: ${userProfile.specialization}",
                    "Language Preference: ${userProfile.languagePreference}"
                )
            )

            // Location Information
            ProfileSection(
                title = "Location Details",
                items = listOf(
                    "State: ${userProfile.state}",
                    "City: ${userProfile.city}"
                )
            )

            // Account Information
            ProfileSection(
                title = "Account Information",
                items = listOf(
                    "Created: ${userProfile.createdAt}",
                    "Last Updated: ${userProfile.updatedAt}"
                )
            )

            // Sign Out Button
            Button(
                onClick = { viewModel.signOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out")
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