package com.littleb01s.ashasakhichat.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.littleb01s.ashasakhichat.data.api.ModelDownloadState
import kotlin.math.roundToInt

@Composable
fun ModelDownloadDialog(
    downloadState: ModelDownloadState,
    onDismissRequest: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(
        onDismissRequest = { 
            if (!downloadState.isDownloading) {
                onDismissRequest()
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI Model Download",
                    style = MaterialTheme.typography.titleLarge
                )

                when {
                    downloadState.error != null -> {
                        Text(
                            text = "Error: ${downloadState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                    downloadState.isDownloading -> {
                        Text(
                            text = "Downloading AI model...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        LinearProgressIndicator(
                            progress = { downloadState.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "${(downloadState.progress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    downloadState.isComplete -> {
                        Text(
                            text = "Download complete!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = onDismissRequest) {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
} 