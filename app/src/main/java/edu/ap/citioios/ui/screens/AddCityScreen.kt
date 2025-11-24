package edu.ap.citioios.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.citioios.ui.viewmodels.CityViewModel
import edu.ap.citioios.utils.Countries

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
    var showCountryDropdown by remember { mutableStateOf(false) }
    
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
            
            ExposedDropdownMenuBox(
                expanded = showCountryDropdown,
                onExpandedChange = { showCountryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCountry,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Land *") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = countryError.isNotEmpty(),
                    supportingText = if (countryError.isNotEmpty()) {
                        { Text(countryError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                ExposedDropdownMenu(
                    expanded = showCountryDropdown,
                    onDismissRequest = { showCountryDropdown = false }
                ) {
                    Countries.allCountries.forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                selectedCountry = country
                                countryError = ""
                                showCountryDropdown = false
                            }
                        )
                    }
                }
            }
            
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
