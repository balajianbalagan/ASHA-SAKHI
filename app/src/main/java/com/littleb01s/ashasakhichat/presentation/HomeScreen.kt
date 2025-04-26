package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littleb01s.R
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import kotlinx.serialization.Serializable

@Serializable
object Home

data class DashboardButton(
    val text: String,
    val drawableResId: Int,
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
    val buttonColor = Color(0xFF84D5B1)
    val textColor = Color(0xFF432C81)
    val dashboardButtons = listOf(
        DashboardButton(stringResource(R.string.your_patients), R.drawable.your_patients_icon, onNavigateToPatients),
        DashboardButton(stringResource(R.string.asha_training), R.drawable.asha_training_icon, onNavigateToTraining),
        DashboardButton(stringResource(R.string.risk_analysis), R.drawable.risk_analysis_icon, onNavigateToRiskAnalysis),
        DashboardButton(stringResource(R.string.ai_sakhi_chat), R.drawable.ai_sakhi_chat_icon, onNavigateToChat),
        DashboardButton(stringResource(R.string.regional_map), R.drawable.regional_maps_icon, onNavigateToMap)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.welcome_greeting, "Pragati"),
                        color = textColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Profile action */ }) {
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
                    icon = { Icon(Icons.Default.Home, stringResource(R.string.home), Modifier.size(24.dp),Color(red = 253, green = 90, blue = 86)) },
                    label = { Text(stringResource(R.string.home)) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = {
                        Icon(painterResource(id = R.drawable.baseline_pregnant_woman_24),
                            contentDescription = stringResource(R.string.patients),
                            tint = Color(red = 253, green = 90, blue = 86),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text(stringResource(R.string.patients)) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = {
                        Icon(painterResource(id = R.drawable.ai_sakhi_chat_icon),
                            contentDescription = stringResource(R.string.chat),
                            tint = Color(red = 253, green = 90, blue = 86),
                            modifier = Modifier.size(24.dp))
                    },
                    label = { Text(stringResource(R.string.chat)) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, stringResource(R.string.settings), Modifier.size(24.dp),Color(red = 253, green = 90, blue = 86)) },
                    label = { Text(stringResource(R.string.settings)) },
                    selected = false,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            items(dashboardButtons) { button ->
                DashboardButtonItem(
                    text = button.text,
                    drawableResId = button.drawableResId,
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
    drawableResId: Int,
    onClick: () -> Unit,
    buttonColor: Color,
    textColor: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
                fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = text,
                modifier = Modifier.size(90.dp)
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