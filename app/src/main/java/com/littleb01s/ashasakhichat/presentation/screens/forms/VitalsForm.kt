package com.littleb01s.ashasakhichat.presentation.screens.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.littleb01s.ashasakhichat.presentation.AddCheckupViewModel
import com.littleb01s.ashasakhichat.presentation.CheckupFormState
import kotlin.random.Random

private val CustomBlue = Color(0xFF0174B3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsForm(viewModel: AddCheckupViewModel, state: CheckupFormState) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.bloodPressure,
            onValueChange = { viewModel.updateBloodPressure(it) },
            label = { Text("Blood Pressure (mmHg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.oxygen,
            onValueChange = { viewModel.updateOxygen(it) },
            label = { Text("Oxygen Level (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.weight,
            onValueChange = { viewModel.updateWeight(it) },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.temperature,
            onValueChange = { viewModel.updateTemperature(it) },
            label = { Text("Temperature (°C)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.sugarLevel,
            onValueChange = { viewModel.updateSugarLevel(it) },
            label = { Text("Blood Sugar Level") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.bmi,
            onValueChange = { viewModel.updateBMI(it) },
            label = { Text("BMI") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
        OutlinedTextField(
            value = state.haemoglobin,
            onValueChange = { viewModel.updateHaemoglobin(it) },
            label = { Text("Haemoglobin") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CustomBlue,
                unfocusedBorderColor = CustomBlue.copy(alpha = 0.5f)
            )
        )
    }
}

object VitalsForm {
    fun onActionButtonClick(viewModel: AddCheckupViewModel, state: CheckupFormState) {
        // Generate random vital signs within normal ranges
        val systolic = Random.nextInt(100, 140)
        val diastolic = Random.nextInt(60, 90)
        val bloodPressure = "$systolic/$diastolic"
        
        val oxygen = Random.nextInt(95, 100)
        val weight = Random.nextInt(45, 100)
        val temperature = String.format("%.1f", Random.nextDouble(36.5, 37.5))
        val sugarLevel = Random.nextInt(70, 140)
        val haemoglobin = String.format("%.1f", Random.nextDouble(11.0, 15.0))
        
        // Calculate BMI based on weight (assuming average height of 1.6m)
        val height = 1.6
        val bmi = String.format("%.1f", weight / (height * height))
        
        // Update all vital signs
        viewModel.updateBloodPressure(bloodPressure)
        viewModel.updateOxygen(oxygen.toString())
        viewModel.updateWeight(weight.toString())
        viewModel.updateTemperature(temperature)
        viewModel.updateSugarLevel(sugarLevel.toString())
        viewModel.updateBMI(bmi)
        viewModel.updateHaemoglobin(haemoglobin)
    }
} 