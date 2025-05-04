package com.littleb01s.ashasakhichat.presentation.screens.forms

import android.util.Log
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
    "Follow-up appointment recommended",
    "Medication prescribed as needed",
    "Patient advised to rest and hydrate",
    "Vital signs within normal range",
    "Patient showing signs of improvement",
    "Dietary recommendations provided",
    "Exercise routine suggested",
    "Patient needs regular monitoring",
    "Symptoms are being managed well"
)

private val commonAuthors = listOf(
    "Dr. Smith",
    "Nurse Johnson",
    "Dr. Williams",
    "Nurse Brown",
    "Dr. Davis",
    "Nurse Miller",
    "Dr. Wilson",
    "Nurse Moore",
    "Dr. Taylor",
    "Nurse Anderson"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    var note by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = note,
            onValueChange = { 
                note = it
                viewModel.updateNote(it)
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
            onValueChange = { 
                author = it
                viewModel.updateAuthor(it)
            },
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
    fun validateForm(state: CheckupFormState): Boolean {
        if (state.note.isEmpty()) {
            println("Validation Error: Note cannot be empty")
            return false
        }
        return true
    }

    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random note content
        val noteContent = listOf(
            "Patient reported feeling better after medication",
            "Follow-up scheduled for next week",
            "Patient advised to maintain regular exercise",
            "Dietary recommendations provided",
            "Patient showing good progress"
        ).random()
        
        // Generate random author
        val author = listOf("Dr. Smith", "Nurse Johnson", "Dr. Patel", "Nurse Williams").random()
        
        // Update the form fields
        viewModel.updateNote(noteContent)
        viewModel.updateAuthor(author)
        
        // Format the data for display
        val formattedData = """
            Note: $noteContent
            Author: $author
        """.trimIndent()
        
        viewModel.updateCheckupData(formattedData)
    }
} 