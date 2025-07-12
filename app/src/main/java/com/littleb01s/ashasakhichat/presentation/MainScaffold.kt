package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.navigation.Screen

data class StatusData(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector,
    val message: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    mainViewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val textColor = Color(0xFF432C81)
    val userName by mainViewModel.userName.collectAsState()
    val syncStatus by mainViewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            Column {
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
                
                // Sync Status Bar (only show when logged in)
                SyncStatusBar(
                    syncStatus = syncStatus,
                    onRetry = { mainViewModel.manualSync() },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
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
                            painterResource(id = R.drawable.baseline_calendar_month_24),
                            contentDescription = stringResource(R.string.Calendar),
                            tint = Color(red = 253, green = 90, blue = 86),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.Calendar)) },
                    selected = currentRoute == Screen.Calendar.route,
                    onClick = { onNavigate(Screen.Calendar.route) }
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

@Composable
fun SyncStatusBar(
    syncStatus: SyncStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, icon, message) = when (syncStatus) {
        SyncStatus.IDLE -> StatusData(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Default.Sync,
            "Ready to sync"
        )
        SyncStatus.SYNCING -> StatusData(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Default.Sync,
            "Syncing data..."
        )
        SyncStatus.SUCCESS -> StatusData(
            Color(0xFF4CAF50),
            Color.White,
            Icons.Default.Sync,
            "Sync completed"
        )
        SyncStatus.FAILED -> StatusData(
            Color(0xFFF44336),
            Color.White,
            Icons.Default.SyncDisabled,
            "Sync failed"
        )
        SyncStatus.NO_INTERNET -> StatusData(
            Color(0xFFFF9800),
            Color.White,
            Icons.Default.SyncDisabled,
            "No internet connection"
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onRetry,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Manual sync",
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Progress indicator for syncing state
        if (syncStatus == SyncStatus.SYNCING) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
} 