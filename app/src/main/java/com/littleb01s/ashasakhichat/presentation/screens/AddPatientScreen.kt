package com.littleb01s.ashasakhichat.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.littleb01s.ashasakhichat.data.api.PatientData
import com.littleb01s.ashasakhichat.data.api.VitalsData
import com.littleb01s.ashasakhichat.presentation.PatientsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPatientScreen(
    onNavigateBack: () -> Unit,
    viewModel: PatientsViewModel = hiltViewModel()
) {
    // Mock data lists
    val religions = listOf("Hindu", "Islam", "Christian", "Sikh", "Buddhist", "Jain")

    // Names by religion
    val hinduFirstNames = listOf(
        "Aaradhya", "Diya", "Saanvi", "Aanya", "Aadhya", "Ananya", "Pari", "Anika",
        "Riya", "Prisha", "Zara", "Myra", "Shanaya", "Kiara", "Advika", "Anvi",
        "Ridhi", "Nisha", "Avni", "Mira", "Meera", "Siya", "Aria", "Ahana"
    )
    val hinduLastNames = listOf(
        "Sharma", "Verma", "Patel", "Kumar", "Singh", "Rao", "Reddy", "Nair",
        "Menon", "Iyer", "Pillai", "Shah", "Desai", "Mehta", "Joshi", "Malhotra",
        "Kapoor", "Gupta", "Sinha", "Chopra", "Bhat", "Yadav", "Chauhan", "Agarwal"
    )

    val islamicFirstNames = listOf(
        "Fatima", "Aisha", "Zara", "Amira", "Sana", "Hafsa", "Maryam", "Ayesha",
        "Khadija", "Sumaiya", "Noor", "Rahma", "Yasmin", "Lubna", "Safiya", "Asma"
    )
    val islamicLastNames = listOf(
        "Khan", "Shaikh", "Patel", "Ansari", "Qureshi", "Siddiqui", "Momin", "Sayyed",
        "Shaikh", "Mansoori", "Kazi", "Mulla", "Chauhan", "Pathan", "Sheikh", "Momin"
    )

    val christianFirstNames = listOf(
        "Maria", "Grace", "Sarah", "Elizabeth", "Rachel", "Rebecca", "Esther", "Ruth",
        "Hannah", "Mary", "Martha", "Lydia", "Dorothy", "Catherine", "Margaret", "Joan"
    )
    val christianLastNames = listOf(
        "Fernandes", "D'Souza", "Pereira", "Rodrigues", "Dias", "Costa", "Menezes", "Silva",
        "Thomas", "George", "Joseph", "Philip", "Mathew", "Kurian", "Varghese", "Antony"
    )

    val sikhFirstNames = listOf(
        "Gurleen", "Harleen", "Manpreet", "Jasleen", "Navleen", "Prabhleen", "Simran", "Kiran",
        "Rajinder", "Balwinder", "Harinder", "Jaswinder", "Kulwinder", "Maninder", "Parminder"
    )
    val sikhLastNames = listOf(
        "Singh", "Kaur", "Gill", "Dhillon", "Sidhu", "Brar", "Mann", "Randhawa",
        "Sandhu", "Bhatia", "Chahal", "Grewal", "Malhotra", "Sohal", "Toor", "Virk"
    )

    val buddhistFirstNames = listOf(
        "Dhamma", "Sangha", "Buddhi", "Karuna", "Maitri", "Prajna", "Shanti", "Tara",
        "Ananda", "Bodhi", "Chandana", "Dipa", "Esha", "Gita", "Hema", "Indu"
    )
    val buddhistLastNames = listOf(
        "Das", "Dutta", "Bose", "Banerjee", "Chatterjee", "Mukherjee", "Sen", "Ghosh",
        "Roy", "Mitra", "Guha", "Saha", "Pal", "Mandal", "Sarkar", "Chakraborty"
    )

    val jainFirstNames = listOf(
        "Ananya", "Diya", "Kavya", "Maya", "Nitya", "Priya", "Riya", "Sanya",
        "Tanvi", "Uma", "Vanya", "Yashika", "Zara", "Aaradhya", "Bhavya", "Chaya"
    )
    val jainLastNames = listOf(
        "Jain", "Shah", "Mehta", "Bhandari", "Gandhi", "Kothari", "Lad", "Mangal",
        "Nahar", "Oswal", "Parekh", "Rathi", "Sanghvi", "Tater", "Vora", "Zaveri"
    )

    val indianStatesAndLanguages = mapOf(
        "Karnataka" to "Kannada",
        "Maharashtra" to "Marathi",
        "Tamil Nadu" to "Tamil",
        "Kerala" to "Malayalam",
        "Andhra Pradesh" to "Telugu",
        "Gujarat" to "Gujarati",
        "Punjab" to "Punjabi",
        "West Bengal" to "Bengali",
        "Uttar Pradesh" to "Hindi",
        "Rajasthan" to "Hindi"
    )

    // Cities by state
    val karnatakaCities = listOf("Bangalore", "Mysore", "Hubli", "Mangalore", "Belgaum", "Gulbarga", "Davanagere", "Bellary")
    val maharashtraCities = listOf("Mumbai", "Pune", "Nagpur", "Thane", "Nashik", "Aurangabad", "Solapur", "Kolhapur")
    val tamilNaduCities = listOf("Chennai", "Coimbatore", "Madurai", "Salem", "Tiruchirappalli", "Tiruppur", "Erode", "Vellore")
    val keralaCities = listOf("Thiruvananthapuram", "Kochi", "Kozhikode", "Thrissur", "Kollam", "Palakkad", "Malappuram", "Kannur")
    val andhraPradeshCities = listOf("Visakhapatnam", "Vijayawada", "Guntur", "Nellore", "Tirupati", "Kurnool", "Kakinada", "Rajahmundry")
    val gujaratCities = listOf("Ahmedabad", "Surat", "Vadodara", "Rajkot", "Bhavnagar", "Jamnagar", "Gandhinagar", "Junagadh")
    val punjabCities = listOf("Chandigarh", "Ludhiana", "Amritsar", "Jalandhar", "Patiala", "Bathinda", "Mohali", "Pathankot")
    val westBengalCities = listOf("Kolkata", "Howrah", "Asansol", "Siliguri", "Durgapur", "Bardhaman", "Malda", "Darjeeling")
    val uttarPradeshCities = listOf("Lucknow", "Kanpur", "Agra", "Prayagraj", "Varanasi", "Ghaziabad", "Meerut", "Bareilly")
    val rajasthanCities = listOf("Jaipur", "Jodhpur", "Kota", "Bikaner", "Ajmer", "Udaipur", "Alwar", "Bhilwara")

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val educationLevels = listOf("Primary", "Secondary", "Graduate", "Illiterate")
    val employmentStatuses = listOf("Employed", "Unemployed", "Self-employed", "Homemaker")
    val castes = listOf("General", "OBC", "SC", "ST")

    // State variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var employmentStatus by remember { mutableStateOf("") }
    var religion by remember { mutableStateOf("") }
    var caste by remember { mutableStateOf("") }
    var languagePreference by remember { mutableStateOf("") }
    var lmp by remember { mutableStateOf("") }
    var previousIllness by remember { mutableStateOf("") }

    // Loading and error states
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Function to get cities for a state
    fun getCitiesForState(state: String): List<String> {
        return when (state) {
            "Karnataka" -> karnatakaCities
            "Maharashtra" -> maharashtraCities
            "Tamil Nadu" -> tamilNaduCities
            "Kerala" -> keralaCities
            "Andhra Pradesh" -> andhraPradeshCities
            "Gujarat" -> gujaratCities
            "Punjab" -> punjabCities
            "West Bengal" -> westBengalCities
            "Uttar Pradesh" -> uttarPradeshCities
            "Rajasthan" -> rajasthanCities
            else -> emptyList()
        }
    }

    // Function to generate random mock data
    fun generateRandomData() {
        // First select religion
        val selectedReligion = religions.random()
        
        // Select name based on religion
        val (firstNameList, lastNameList) = when (selectedReligion) {
            "Hindu" -> hinduFirstNames to hinduLastNames
            "Islam" -> islamicFirstNames to islamicLastNames
            "Christian" -> christianFirstNames to christianLastNames
            "Sikh" -> sikhFirstNames to sikhLastNames
            "Buddhist" -> buddhistFirstNames to buddhistLastNames
            "Jain" -> jainFirstNames to jainLastNames
            else -> hinduFirstNames to hinduLastNames // Default case
        }
        
        firstName = firstNameList.random()
        lastName = lastNameList.random()

        // Generate random phone number
        mobileNumber = buildString {
            append("9") // Start with 9 for mobile numbers
            repeat(9) { append(Random.nextInt(0, 10)) }
        }

        // Generate random state and matching language
        state = indianStatesAndLanguages.keys.random()
        languagePreference = indianStatesAndLanguages[state]!!
        
        // Generate random city for the selected state
        city = getCitiesForState(state).random()

        // Generate random DOB between 1985 and 2000
        val startDate = Calendar.getInstance().apply { set(1985, 0, 1) }.timeInMillis
        val endDate = Calendar.getInstance().apply { set(2000, 11, 31) }.timeInMillis
        val randomDOB = Date(Random.nextLong(startDate, endDate))
        dateOfBirth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(randomDOB)

        // Generate random LMP between March and April 2025
        val lmpStartDate = Calendar.getInstance().apply { set(2025, 2, 1) }.timeInMillis // March 1, 2025
        val lmpEndDate = Calendar.getInstance().apply { set(2025, 3, 30) }.timeInMillis // April 30, 2025
        val randomLMP = Date(Random.nextLong(lmpStartDate, lmpEndDate))
        lmp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(randomLMP)

        // Set other random values
        bloodGroup = bloodGroups.random()
        education = educationLevels.random()
        employmentStatus = employmentStatuses.random()
        religion = selectedReligion
        caste = castes.random()
        previousIllness = listOf("None", "Diabetes", "Hypertension", "Thyroid", "Asthma").random()
    }

    // Validation states
    var firstNameError by remember { mutableStateOf<String?>(null) }
    var mobileNumberError by remember { mutableStateOf<String?>(null) }
    var stateError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var dateOfBirthError by remember { mutableStateOf<String?>(null) }
    var lmpError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showLMPPicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Function to calculate delivery date from LMP
    fun calculateDeliveryDate(lmpDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = lmpDate
        calendar.add(Calendar.DAY_OF_YEAR, 280) // Adding 280 days to LMP
        return calendar.time
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Date(it)
                            dateOfBirth = dateFormat.format(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLMPPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showLMPPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val date = Date(it)
                            lmp = dateFormat.format(date)
                        }
                        showLMPPicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLMPPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "An unknown error occurred") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text(successMessage ?: "Patient added successfully") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Patient") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Information section with Generate Random button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Basic Information", style = MaterialTheme.typography.titleMedium)
                Button(
                    onClick = { generateRandomData() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    enabled = !isLoading
                ) {
                    Text("Generate Random")
                }
            }

            OutlinedTextField(
                value = firstName,
                onValueChange = { 
                    firstName = it
                    firstNameError = if (it.isBlank()) "First name is required" else null
                },
                label = { Text("First Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = firstNameError != null,
                supportingText = { firstNameError?.let { Text(it) } },
                enabled = !isLoading
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { 
                    mobileNumber = it
                    mobileNumberError = when {
                        it.isBlank() -> "Mobile number is required"
                        !it.matches(Regex("^[0-9]{10}$")) -> "Please enter a valid 10-digit mobile number"
                        else -> null
                    }
                },
                label = { Text("Mobile Number*") },
                modifier = Modifier.fillMaxWidth(),
                isError = mobileNumberError != null,
                supportingText = { mobileNumberError?.let { Text(it) } },
                enabled = !isLoading
            )

            // Location Information
            Text("Location", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = state,
                onValueChange = { 
                    state = it
                    stateError = if (it.isBlank()) "State is required" else null
                },
                label = { Text("State*") },
                modifier = Modifier.fillMaxWidth(),
                isError = stateError != null,
                supportingText = { stateError?.let { Text(it) } },
                enabled = !isLoading
            )
            OutlinedTextField(
                value = city,
                onValueChange = { 
                    city = it
                    cityError = if (it.isBlank()) "City/Town/Village is required" else null
                },
                label = { Text("City/Town/Village*") },
                modifier = Modifier.fillMaxWidth(),
                isError = cityError != null,
                supportingText = { cityError?.let { Text(it) } },
                enabled = !isLoading
            )

            // Personal Information
            Text("Personal Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = if (dateOfBirth.isNotEmpty()) displayDateFormat.format(dateFormat.parse(dateOfBirth)!!) else "",
                onValueChange = { },
                label = { Text("Date of Birth*") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                isError = dateOfBirthError != null,
                supportingText = { dateOfBirthError?.let { Text(it) } },
                enabled = !isLoading
            )
            OutlinedTextField(
                value = bloodGroup,
                onValueChange = { bloodGroup = it },
                label = { Text("Blood Group") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            // Language and LMP
            Text("Additional Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = languagePreference,
                onValueChange = { languagePreference = it },
                label = { Text("Language Preference") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = if (lmp.isNotEmpty()) displayDateFormat.format(dateFormat.parse(lmp)!!) else "",
                onValueChange = { },
                label = { Text("Last Menstrual Period (LMP)*") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { showLMPPicker = true },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                isError = lmpError != null,
                supportingText = { 
                    if (lmpError != null) {
                        Text(lmpError!!)
                    } else if (lmp.isNotEmpty()) {
                        val lmpDate = dateFormat.parse(lmp)!!
                        val deliveryDate = calculateDeliveryDate(lmpDate)
                        Text("Expected Delivery Date: ${displayDateFormat.format(deliveryDate)}")
                    }
                },
                enabled = !isLoading
            )

            // Social Information
            Text("Social Information", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = education,
                onValueChange = { education = it },
                label = { Text("Education") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = employmentStatus,
                onValueChange = { employmentStatus = it },
                label = { Text("Employment Status") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = religion,
                onValueChange = { religion = it },
                label = { Text("Religion") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = caste,
                onValueChange = { caste = it },
                label = { Text("Caste") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = previousIllness,
                onValueChange = { previousIllness = it },
                label = { Text("Previous Illness") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validate required fields
                    var isValid = true
                    if (firstName.isBlank()) {
                        firstNameError = "First name is required"
                        isValid = false
                    }
                    if (mobileNumber.isBlank() || !mobileNumber.matches(Regex("^[0-9]{10}$"))) {
                        mobileNumberError = if (mobileNumber.isBlank()) "Mobile number is required" else "Please enter a valid 10-digit mobile number"
                        isValid = false
                    }
                    if (state.isBlank()) {
                        stateError = "State is required"
                        isValid = false
                    }
                    if (city.isBlank()) {
                        cityError = "City/Town/Village is required"
                        isValid = false
                    }
                    if (dateOfBirth.isBlank()) {
                        dateOfBirthError = "Date of birth is required"
                        isValid = false
                    }
                    if (lmp.isBlank()) {
                        lmpError = "LMP is required"
                        isValid = false
                    }

                    if (!isValid) {
                        errorMessage = "Please fill in all required fields correctly"
                        showErrorDialog = true
                        return@Button
                    }

                    // Set loading state
                    isLoading = true

                    // Calculate delivery date
                    val lmpDate = dateFormat.parse(lmp)!!
                    val deliveryDate = calculateDeliveryDate(lmpDate)

                    // Create patient data
                    val patientData = PatientData(
                        firstName = firstName,
                        lastName = lastName.takeIf { it.isNotBlank() },
                        state = state,
                        city = city,
                        languagePreference = languagePreference.takeIf { it.isNotBlank() },
                        dateOfBirth = dateFormat.parse(dateOfBirth)!!,
                        deliveryDate = deliveryDate,
                        mobileNumber = mobileNumber,
                        employmentStatus = employmentStatus.takeIf { it.isNotBlank() },
                        religion = religion.takeIf { it.isNotBlank() },
                        education = education.takeIf { it.isNotBlank() },
                        caste = caste.takeIf { it.isNotBlank() },
                        bloodGroup = bloodGroup.takeIf { it.isNotBlank() },
                        previousIllness = previousIllness.takeIf { it.isNotBlank() },
                        lmp = lmpDate
                    )

                    // Add patient
                    viewModel.addNewPatient(
                        patientData = patientData,
                        onSuccess = { isSynced ->
                            isLoading = false
                            successMessage = if (isSynced) {
                                "Patient added successfully and synced with server"
                            } else {
                                "Patient added successfully to local storage. Will be synced when internet is available"
                            }
                            showSuccessDialog = true
                        },
                        onError = { message ->
                            isLoading = false
                            if (message.contains("app issue", ignoreCase = true)) {
                                errorMessage = message.replace("app issue:", "").trim()
                                showErrorDialog = true
                            } else {
                                // For non-app issues (like network), save locally
                                successMessage = "Patient added successfully to local storage. Will be synced when internet is available"
                                showSuccessDialog = true
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Patient Details")
                }
            }
        }
    }
} 