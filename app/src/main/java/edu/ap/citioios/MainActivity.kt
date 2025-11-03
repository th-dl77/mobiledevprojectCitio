package edu.ap.citioios

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.repository.FirebaseRepository

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitioIOSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthApp(
                        modifier = Modifier.padding(innerPadding),
                        context = this
                    )
                }
            }
        }
    }
}

enum class AuthScreen {
    START, LOGIN, REGISTER, HOME
}

@Composable
fun AuthApp(
    modifier: Modifier = Modifier,
    context: Context
) {
    val currentUser = Firebase.auth.currentUser
    var currentScreen by remember { mutableStateOf(if (currentUser == null) AuthScreen.START else AuthScreen.HOME) }

    when (currentScreen) {
        AuthScreen.START -> {
            StartScreen(
                onLoginClick = { currentScreen = AuthScreen.LOGIN },
                onRegisterClick = { currentScreen = AuthScreen.REGISTER }
            )
        }
        AuthScreen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { 
                    currentScreen = AuthScreen.HOME 
                    Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                },
                onLoginError = { error ->
                    Toast.makeText(context, "Login failed: $error", Toast.LENGTH_SHORT).show()
                },
                onBackClick = { currentScreen = AuthScreen.START },
                onRegisterClick = { currentScreen = AuthScreen.REGISTER }
            )
        }
        AuthScreen.REGISTER -> {
            RegisterScreen(
                onRegisterSuccess = { 
                    currentScreen = AuthScreen.HOME 
                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                },
                onRegisterError = { error ->
                    Toast.makeText(context, "Registration failed: $error", Toast.LENGTH_SHORT).show()
                },
                onBackClick = { currentScreen = AuthScreen.START },
                onLoginClick = { currentScreen = AuthScreen.LOGIN }
            )
        }
        AuthScreen.HOME -> {
            val user = Firebase.auth.currentUser
            HomeScreen(
                userEmail = user?.email ?: "Unknown",
                onLogout = {
                    FirebaseRepository.logoutUser()
                    currentScreen = AuthScreen.START
                }
            )
        }
    }
}

@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App logo/title
        Text(
            text = "Citio",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // App description
        Text(
            text = "Ontdek en deel de beste plekken in jouw stad",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Login button
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleMedium
            )
        }
        
        // Register button
        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onLoginError: (String) -> Unit,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
            onClick = {
                isLoading = true
                FirebaseRepository.loginUser(
                    email = email,
                    password = password,
                    onSuccess = {
                        isLoading = false
                        onLoginSuccess()
                    },
                    onError = { error ->
                        isLoading = false
                        onLoginError(error.message ?: "Unknown error")
                    }
                )
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onRegisterError: (String) -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
            onClick = {
                isLoading = true
                FirebaseRepository.registerUser(
                    email = email,
                    password = password,
                    displayName = displayName,
                    onSuccess = {
                        isLoading = false
                        onRegisterSuccess()
                    },
                    onError = { error ->
                        isLoading = false
                        onRegisterError(error.message ?: "Unknown error")
                    }
                )
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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

@Composable  
fun HomeScreen(userEmail: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welkom bij Citio!",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Je bent ingelogd als:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Uitloggen")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    CitioIOSTheme {
        StartScreen(
            onLoginClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CitioIOSTheme {
        LoginScreen(
            onLoginSuccess = {},
            onLoginError = {},
            onBackClick = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CitioIOSTheme {
        RegisterScreen(
            onRegisterSuccess = {},
            onRegisterError = {},
            onBackClick = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CitioIOSTheme {
        HomeScreen(
            userEmail = "user@example.com",
            onLogout = {}
        )
    }
}
