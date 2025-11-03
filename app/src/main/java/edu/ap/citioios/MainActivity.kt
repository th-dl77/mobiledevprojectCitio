package edu.ap.citioios

import android.os.Bundle
import android.widget.Toast
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.ui.theme.CitioIOSTheme

class MainActivity : ComponentActivity() {
    
    // FirebaseUI launcher
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitioIOSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthApp(
                        modifier = Modifier.padding(innerPadding),
                        onStartSignIn = { startSignInFlow() }
                    )
                }
            }
        }
    }

    private fun startSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Welkom!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Inloggen geannuleerd", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun AuthApp(
    modifier: Modifier = Modifier,
    onStartSignIn: () -> Unit
) {
    val currentUser = Firebase.auth.currentUser
    
    if (currentUser == null) {
        // Show polished welcome screen
        WelcomeScreen(onGetStarted = onStartSignIn)
    } else {
        // Show home screen after login
        HomeScreen(
            userEmail = currentUser.email ?: "Onbekend",
            onLogout = {
                Firebase.auth.signOut()
            }
        )
    }
}

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
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

        // Get started button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Get started",
                style = MaterialTheme.typography.titleMedium
            )
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
fun WelcomeScreenPreview() {
    CitioIOSTheme {
        WelcomeScreen(onGetStarted = {})
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
