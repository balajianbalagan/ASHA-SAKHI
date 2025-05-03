package com.littleb01s.ashasakhichat.presentation.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import kotlin.random.Random

private val CustomBlue = Color(0xFF0174B3)
private val CustomOrange = Color(0xFFFF5151)

// List of common medical notes
private val commonNotes = listOf(
    "Patient appears to be in good health",
    "Follow-up appointment recommended in 2 weeks",
    "Patient advised to maintain current medication regimen",
    "Dietary changes suggested for better health outcomes",
    "Patient showing positive response to treatment",
    "Additional tests may be required for further diagnosis",
    "Patient needs to increase physical activity",
    "Sleep pattern needs improvement",
    "Stress management techniques discussed",
    "Patient education provided on self-care"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    var note by remember { mutableStateOf(state.checkupData) }
    var author by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = note ?: "",
            onValueChange = {
                note = it
                viewModel.updateCheckupData(it)
            },
            label = { Text("Note") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        if (error != null) {
            Text(error!!, color = CustomOrange)
        }
    }
}

object NotesForm {
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random number of notes (between 1 and 3)
        val numNotes = Random.nextInt(1, 4)
        
        // Randomly select notes
        val selectedNotes = commonNotes.shuffled().take(numNotes)
        
        // Format the notes with author and timestamp
        val author = listOf("Dr. Smith", "Nurse Johnson", "Dr. Patel", "Dr. Kumar").random()
        val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        
        val formattedNotes = selectedNotes.joinToString("\n\n")
        val result = "Author: $author\nDate: $timestamp\n\n$formattedNotes"
        
        // Update the form state
        viewModel.updateCheckupData(result)
        viewModel.updateAuthor(author)
    }
} 