package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val textColor = Color(0xFF432C81)
    val userName by viewModel.userName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome_greeting, userName),
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile),
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                    icon = { Icon(Icons.Default.Home, stringResource(R.string.home), Modifier.size(24.dp), Color(red = 253, green = 90, blue = 86)) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = currentRoute == Screen.Home.route,
                    onClick = { onNavigate(Screen.Home.route) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.baseline_pregnant_woman_24),
                            contentDescription = stringResource(R.string.patients),
                            tint = Color(red = 253, green = 90, blue = 86),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.patients)) },
                    selected = currentRoute == Screen.Patients.route,
                    onClick = { onNavigate(Screen.Patients.route) }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            painterResource(id = R.drawable.baseline_notifications_24),
                            contentDescription = stringResource(R.string.notifications),
                            tint = Color(red = 253, green = 90, blue = 86),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.notifications)) },
                    selected = currentRoute == Screen.Notifications.route,
                    onClick = { onNavigate(Screen.Notifications.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, stringResource(R.string.settings), Modifier.size(24.dp), Color(red = 253, green = 90, blue = 86)) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { onNavigate(Screen.Settings.route) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            content()
        }
    }
} 