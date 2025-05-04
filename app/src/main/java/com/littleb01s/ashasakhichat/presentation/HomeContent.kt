package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.R
import com.littleb01s.ashasakhichat.presentation.components.ModelDownloadDialog

data class DashboardButton(
    val text: String,
    val drawableResId: Int,
    val onClick: () -> Unit
)

@Composable
fun HomeContent(
    onNavigateToTraining: () -> Unit,
    onNavigateToRiskAnalysis: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToPatients: () -> Unit = {},
    onNavigateToSpeecher: () -> Unit = {},
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val isLLMInitialized by viewModel.isLLMInitialized.collectAsState()
    val showDownloadDialog by viewModel.showDownloadDialog.collectAsState()
    val modelDownloadState by viewModel.modelDownloadState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (isInitializing || !isLLMInitialized) {
            // Show loading screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isInitializing) "Initializing ASHA Sakhi..." else "Loading AI Model...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Show dashboard buttons
            val buttonColor = Color(0xFF84D5B1)
            val textColor = Color(0xFF432C81)

            val dashboardButtons = listOf(
                DashboardButton(stringResource(R.string.your_patients), R.drawable.your_patients_icon, onNavigateToPatients),
                DashboardButton(stringResource(R.string.asha_training), R.drawable.asha_training_icon, onNavigateToTraining),
                DashboardButton(stringResource(R.string.risk_analysis), R.drawable.risk_analysis_icon, onNavigateToRiskAnalysis),
                DashboardButton(stringResource(R.string.ai_sakhi_chat), R.drawable.ai_sakhi_chat_icon, onNavigateToChat),
                DashboardButton(stringResource(R.string.regional_map), R.drawable.regional_maps_icon, onNavigateToMap),
                DashboardButton(stringResource(R.string.profile), R.drawable.regional_maps_icon, onNavigateToSpeecher)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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

        // Show download dialog if needed
        if (showDownloadDialog) {
            ModelDownloadDialog(
                downloadState = modelDownloadState,
                onDismissRequest = viewModel::dismissDownloadDialog,
                onRetry = viewModel::retryModelDownload
            )
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