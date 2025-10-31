package edu.ap.citioios

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import edu.ap.citioios.models.Location
import edu.ap.citioios.repository.FirebaseRepository
import edu.ap.citioios.ui.theme.CitioIOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CitioIOSTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welkom bij Citio!"
        )

        Button(onClick = {
            val testLocation = Location(
                name = "Test Café",
                latitude = 51.2194,
                longitude = 4.4025,
                category = "horeca",
                addedByUserId = "dummyUser123",
                cityName = "Antwerpen"
            )

            FirebaseRepository.saveNewLocationToFirestore(
                location = testLocation,
                onSuccess = {
                    Log.d("MainScreen", "Opslaan gelukt")
                    Toast.makeText(context, "Locatie opgeslagen", Toast.LENGTH_SHORT).show()
                },
                onError = { exception ->
                    Log.w("MainScreen", "Opslaan mislukt", exception)
                    Toast.makeText(context, "Fout: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            )
        }) {
            Text("Test: Locatie Opslaan")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CitioIOSTheme {
        MainScreen()
    }
}