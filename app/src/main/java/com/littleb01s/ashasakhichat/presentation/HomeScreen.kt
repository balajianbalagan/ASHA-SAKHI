package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import kotlinx.serialization.Serializable

@Serializable
object Home

data class DashboardButton(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPatients: () -> Unit,
    onNavigateToTraining: () -> Unit,
    onNavigateToRiskAnalysis: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val buttonColor = Color(0xFF0BB066)
    val textColor = Color(0xFF432C81)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Namaste, Pragati!",
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Profile action */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = textColor
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, "Patients") },
                    label = { Text("Patients") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, "Chat") },
                    label = { Text("Chat") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val dashboardButtons = listOf(
                DashboardButton("Your Patients", Icons.Filled.Home, onNavigateToPatients),
                DashboardButton("ASHA Training", Icons.Filled.Home, onNavigateToTraining),
                DashboardButton("Risk Analysis", Icons.Filled.Home, onNavigateToRiskAnalysis),
                DashboardButton("AI Sakhi Chat", Icons.Filled.Home, onNavigateToChat),
                DashboardButton("Regional Map", Icons.Filled.Home, onNavigateToMap)
            )

            dashboardButtons.forEach { button ->
                DashboardButtonItem(
                    text = button.text,
                    icon = button.icon,
                    onClick = button.onClick,
                    buttonColor = buttonColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
fun DashboardButtonItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    buttonColor: Color,
    textColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AshaTheme {
        HomeScreen(
            onNavigateToPatients = {},
            onNavigateToTraining = {},
            onNavigateToRiskAnalysis = {},
            onNavigateToChat = {},
            onNavigateToMap = {}
        )
    }
} 