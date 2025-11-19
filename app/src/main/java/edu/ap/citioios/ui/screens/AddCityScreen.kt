package edu.ap.citioios.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.citioios.ui.components.SearchableCountryDropdown
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.ui.viewmodels.CityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCityScreen(
    onBackClick: () -> Unit,
    onCityAdded: () -> Unit,
    cityViewModel: CityViewModel = viewModel()
) {
    val cityUiState by cityViewModel.uiState.collectAsState()
    
    var cityName by remember { mutableStateOf("") }
    var cityDescription by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var countryError by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nieuwe stad toevoegen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Terug")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Voeg een nieuwe stad toe aan de lijst",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = cityName,
                onValueChange = { 
                    cityName = it
                    nameError = ""
                },
                label = { Text("Stadnaam *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError.isNotEmpty(),
                supportingText = if (nameError.isNotEmpty()) {
                    { Text(nameError, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            SearchableCountryDropdown(
                selectedCountry = selectedCountry,
                onCountrySelected = { 
                    selectedCountry = it
                    countryError = ""
                },
                label = "Land *",
                modifier = Modifier.fillMaxWidth(),
                isError = countryError.isNotEmpty(),
                supportingText = if (countryError.isNotEmpty()) {
                    { Text(countryError, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            OutlinedTextField(
                value = cityDescription,
                onValueChange = { cityDescription = it },
                label = { Text("Beschrijving (optioneel)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            if (cityUiState.errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cityUiState.errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Annuleren")
                }
                
                Button(
                    onClick = {
                        var hasError = false                   
                        if (cityName.trim().length < 2) {
                            nameError = "Stadnaam moet minstens 2 karakters bevatten"
                            hasError = true
                        }
                        
                        if (selectedCountry.isBlank()) {
                            countryError = "Selecteer een land"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            cityViewModel.addCity(
                                name = cityName.trim(),
                                description = cityDescription.trim(),
                                country = selectedCountry,
                                onSuccess = onCityAdded
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !cityUiState.isLoading && cityName.trim().isNotEmpty()
                ) {
                    if (cityUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Opslaan")
                    }
                }
            }
        }
    }
}