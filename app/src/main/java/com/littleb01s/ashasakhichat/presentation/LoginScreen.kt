package com.littleb01s.ashasakhichat.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.littleb01s.R
import com.littleb01s.ashasakhichat.ui.theme.AshaTheme
import com.littleb01s.ashasakhichat.presentation.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    
    // State for dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Handle login state
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                dialogMessage = (loginState as LoginState.Success).message
                isError = false
                showDialog = true
                
                // Navigate after showing success message
                onLoginSuccess()
            }
            is LoginState.Error -> {
                dialogMessage = (loginState as LoginState.Error).message
                isError = true
                showDialog = true
            }
            else -> {}
        }
    }

    LoginScreenContent(
        onLoginClick = { phone, password ->
            viewModel.login(phone, password)
        },
        isLoading = loginState is LoginState.Loading
    )

    // Show dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                viewModel.resetState()
            },
            title = { Text(if (isError) "Error" else "Success") },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.resetState()
                }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = if (isError) Color.Red else Color.Green,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenContent(
    onLoginClick: (String, String) -> Unit,
    isLoading: Boolean
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val passwordFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF0078D4)
        )
        
        Text(
            text = "सखी",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFFF4B4B)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFFE1F5FE))
        ) {
            Image(
                painter = painterResource(id = R.drawable.login_avatar),
                contentDescription = "Avatar",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text(stringResource(R.string.phone_number), fontWeight = FontWeight.Bold) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password), fontWeight = FontWeight.Bold) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onLoginClick(phoneNumber, password) }
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(passwordFocusRequester)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLoginClick(phoneNumber, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D4)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    stringResource(R.string.login),
                    fontSize = 22.sp,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Screen Light")
@Composable
private fun LoginScreenPreview() {
    AshaTheme {
        LoginScreenContent(
            onLoginClick = { _, _ -> },
            isLoading = false
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Dark")
@Composable
private fun LoginScreenDarkPreview() {
    AshaTheme(darkTheme = true) {
        LoginScreenContent(
            onLoginClick = { _, _ -> },
            isLoading = false
        )
    }
} 