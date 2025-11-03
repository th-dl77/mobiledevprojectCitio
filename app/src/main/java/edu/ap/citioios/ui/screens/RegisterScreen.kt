package edu.ap.citioios.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.ap.citioios.ui.theme.CitioIOSTheme

@Composable
fun RegisterScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRegisterClick: (String, String, String) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onErrorDismissed: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(3000)
                onErrorDismissed()
            }
        }

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Button(
            onClick = { onRegisterClick(displayName, email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("Back")
            }
            TextButton(onClick = onLoginClick) {
                Text("Have account? Login")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CitioIOSTheme {
        RegisterScreen(
            isLoading = false,
            errorMessage = null,
            onRegisterClick = { _, _, _ -> },
            onBackClick = {},
            onLoginClick = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenLoadingPreview() {
    CitioIOSTheme {
        RegisterScreen(
            isLoading = true,
            errorMessage = null,
            onRegisterClick = { _, _, _ -> },
            onBackClick = {},
            onLoginClick = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenErrorPreview() {
    CitioIOSTheme {
        RegisterScreen(
            isLoading = false,
            errorMessage = "Registration failed. Please try again.",
            onRegisterClick = { _, _, _ -> },
            onBackClick = {},
            onLoginClick = {},
            onErrorDismissed = {}
        )
    }
}
