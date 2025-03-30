package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import kotlinx.serialization.Serializable

@Serializable
object Welcome

@Composable
fun WelcomeScreen(onNavigateToPlay: () -> Unit, onNavigateToInstructions: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Simon Says")
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            Button(onClick = onNavigateToPlay) {
                Text("Play")
            }
            Button(onClick = onNavigateToInstructions) {
                Text("Instructions")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomePreview() {
    AshaTheme {
        PlayScreen(hiltViewModel<PlayViewModel>())
    }
}
