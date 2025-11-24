package edu.ap.citioios.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.ap.citioios.models.City
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.ui.viewmodels.LocationViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationScreen(
    city: City,
    onBackClick: () -> Unit,
    onLocationAdded: () -> Unit,
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationUiState by locationViewModel.uiState.collectAsState()
    
    var locationName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf("") }
    var categoryError by remember { mutableStateOf("") }
    var streetError by remember { mutableStateOf("") }
    var numberError by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    var showPermissionDeniedMessage by remember { mutableStateOf(false) }

    fun createImageFile(): File {
        val storageDir = context.cacheDir.resolve("images").apply { mkdirs() }
        return File(storageDir, "location_${System.currentTimeMillis()}.jpg")
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoFile != null) {
            capturedImageUri = Uri.fromFile(photoFile)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = createImageFile()
            photoFile = file
            val uri = FileProvider.getUriForFile(
                context,
                "edu.ap.citioios.fileprovider",
                file
            )
            takePictureLauncher.launch(uri)
        } else {
            showPermissionDeniedMessage = true
        }
    }

    val predefinedCategories = listOf(
        "Restaurant", "Bar", "Café", "Museum", "Park", "Shopping",
        "Hotel", "Attraction", "Entertainment", "Sports", "Healthcare", "Education"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nieuwe locatie toevoegen") },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Locatie voor:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (city.country.isNotBlank()) {
                        Text(
                            text = city.country,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                text = "Vul de details van de nieuwe locatie in",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            
            OutlinedTextField(
                value = locationName,
                onValueChange = { 
                    locationName = it
                    nameError = ""
                },
                label = { Text("Locatienaam *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError.isNotEmpty(),
                supportingText = if (nameError.isNotEmpty()) {
                    { Text(nameError, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
        
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categorie *") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = categoryError.isNotEmpty(),
                    supportingText = if (categoryError.isNotEmpty()) {
                        { Text(categoryError, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
                
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    predefinedCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                categoryError = ""
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Adres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedTextField(
                value = street,
                onValueChange = { 
                    street = it
                    streetError = ""
                },
                label = { Text("Straatnaam *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = streetError.isNotEmpty(),
                supportingText = if (streetError.isNotEmpty()) {
                    { Text(streetError, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            OutlinedTextField(
                value = number,
                onValueChange = { 
                    number = it
                    numberError = ""
                },
                label = { Text("Huisnummer *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = numberError.isNotEmpty(),
                supportingText = if (numberError.isNotEmpty()) {
                    { Text(numberError, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            // Address preview
            if (street.isNotBlank() || number.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Adres voorvertoning:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${city.country.ifBlank { "Land" }}, ${city.name}, ${street.ifBlank { "Straat" }} ${number.ifBlank { "Nr" }}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Photo Section
            Text(
                text = "Foto (optioneel)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            if (capturedImageUri != null) {
                // Photo preview 
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Toegevoegde foto",
                                style = MaterialTheme.typography.labelMedium
                            )
                            IconButton(
                                onClick = {
                                    capturedImageUri = null
                                    photoFile = null
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Verwijder foto"
                                )
                            }
                        }
                        
                        AsyncImage(
                            model = capturedImageUri,
                            contentDescription = "Locatiefoto",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                        
                        OutlinedButton(
                            onClick = {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nieuwe foto maken")
                        }
                    }
                }
            } else {
                // Add photo button
                OutlinedButton(
                    onClick = {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Foto toevoegen")
                }
            }

            if (showPermissionDeniedMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Camera toestemming is geweigerd. Ga naar de app-instellingen om deze toe te staan.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            if (locationUiState.errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = locationUiState.errorMessage,
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
                        // Validation
                        var hasError = false
                        
                        if (locationName.trim().length < 2) {
                            nameError = "Locatienaam moet minstens 2 karakters bevatten"
                            hasError = true
                        }
                        
                        if (selectedCategory.isBlank()) {
                            categoryError = "Selecteer een categorie"
                            hasError = true
                        }
                        
                        if (street.trim().isBlank()) {
                            streetError = "Straatnaam is verplicht"
                            hasError = true
                        }
                        
                        if (number.trim().isBlank()) {
                            numberError = "Huisnummer is verplicht"
                            hasError = true
                        }
                        
                        if (!hasError) {
                            locationViewModel.addLocation(
                                name = locationName.trim(),
                                category = selectedCategory,
                                street = street.trim(),
                                number = number.trim(),
                                cityId = city.id,
                                cityName = city.name,
                                country = city.country.ifBlank { "Unknown" },
                                imageUri = capturedImageUri,
                                onSuccess = onLocationAdded
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !locationUiState.isLoading
                ) {
                    if (locationUiState.isLoading) {
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

@Preview(showBackground = true)
@Composable
fun AddLocationScreenPreview() {
    CitioIOSTheme {
        AddLocationScreen(
            city = City(
                id = "1",
                name = "Antwerp",
                country = "Belgium",
                description = "Beautiful historic city"
            ),
            onBackClick = {},
            onLocationAdded = {}
        )
    }
}
