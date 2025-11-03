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
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onErrorDismissed: () -> Unit
) {
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
            text = "Login",
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
            onClick = { onLoginClick(email, password) },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
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
                Text("Login")
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("Back")
            }
            TextButton(onClick = onRegisterClick) {
                Text("Need account? Register")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CitioIOSTheme {
        LoginScreen(
            isLoading = false,
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onBackClick = {},
            onRegisterClick = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    CitioIOSTheme {
        LoginScreen(
            isLoading = true,
            errorMessage = null,
            onLoginClick = { _, _ -> },
            onBackClick = {},
            onRegisterClick = {},
            onErrorDismissed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenErrorPreview() {
    CitioIOSTheme {
        LoginScreen(
            isLoading = false,
            errorMessage = "Invalid email or password",
            onLoginClick = { _, _ -> },
            onBackClick = {},
            onRegisterClick = {},
            onErrorDismissed = {}
        )
    }
}
