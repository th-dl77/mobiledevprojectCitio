package edu.ap.citioios.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import edu.ap.citioios.models.City
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import edu.ap.citioios.ui.viewmodels.LocationViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    city: City,
    onBackClick: () -> Unit,
    onAddLocationClick: () -> Unit = {},
    locationViewModel: LocationViewModel = viewModel(),
    onDetailScreenClick: (Location) -> Unit = {}
) {
    val context = LocalContext.current
    val locationUiState by locationViewModel.uiState.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = GeoPoint(location.latitude, location.longitude)
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Location permission needed to calculate distance", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                }
            }
        }
    }

    fun calculateAndShowDistance(targetLocation: Location) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        if (userLocation == null) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = GeoPoint(location.latitude, location.longitude)
                    val targetGeo = targetLocation.geoPoint.toOsmGeoPoint()
                    val distanceMeters = userLocation!!.distanceToAsDouble(targetGeo)
                    val distanceText = if (distanceMeters > 1000) {
                        String.format("%.2f km", distanceMeters / 1000)
                    } else {
                        String.format("%.0f meters", distanceMeters)
                    }
                    Toast.makeText(context, "Distance to ${targetLocation.name}: $distanceText", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Searching for your location... please wait.", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }

        userLocation?.let { currentUser ->
            val targetGeo = targetLocation.geoPoint.toOsmGeoPoint()
            val distanceMeters = currentUser.distanceToAsDouble(targetGeo)

            val distanceText = if (distanceMeters > 1000) {
                String.format("%.2f km", distanceMeters / 1000)
            } else {
                String.format("%.0f meters", distanceMeters)
            }

            Toast.makeText(context, "Distance to ${targetLocation.name}: $distanceText", Toast.LENGTH_LONG).show()
        }
    }

    var center by remember {
        mutableStateOf(
            GeoPoint(
                city.latitude.takeIf { it != 0.0 } ?: 51.230167,
                city.longitude.takeIf { it != 0.0 } ?: 4.416129
            )
        )
    }
    var zoom by remember { mutableDoubleStateOf(14.0) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(city.id) {
        if (city.id.isNotBlank()) {
            locationViewModel.loadLocationsForCity(city.id)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(city.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLocationClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new location")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Search bar
            OutlinedTextField(
                value = locationUiState.searchQuery,
                onValueChange = { locationViewModel.updateSearchQuery(it) },
                label = { Text("Search locations...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                singleLine = true
            )

            // Category filter chips
            if (locationUiState.availableCategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { locationViewModel.selectCategory(null) },
                            label = { Text("All") },
                            selected = locationUiState.selectedCategory == null
                        )
                    }
                    items(locationUiState.availableCategories) { category ->
                        FilterChip(
                            onClick = { locationViewModel.selectCategory(category) },
                            label = { Text(category) },
                            selected = locationUiState.selectedCategory == category
                        )
                    }
                }
            }

            // Map view
            if (LocalInspectionMode.current) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) { Text(text = "Map Preview Unavailable") }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clipToBounds()
                ) {
                    OsmMapView(
                        modifier = Modifier.fillMaxSize(),
                        center = center,
                        zoom = zoom,
                        locations = locationUiState.filteredLocations,
                        onMapViewCreated = {
                            mapViewInstance = it
                        },
                        onMarkerClick = { location ->
                            calculateAndShowDistance(location)
                        }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Loading indicator
            if (locationUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Error message
            locationUiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = { locationViewModel.refreshLocations(city.id) },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }

            // Locations list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(locationUiState.filteredLocations) { location ->
                    LocationListItem(
                        location = location,
                        onClick = {
                            center = location.geoPoint.toOsmGeoPoint()
                            zoom = 18.0

                            calculateAndShowDistance(location)
                        },
                        onDetailScreenClick = onDetailScreenClick
                    )
                }

                if (locationUiState.filteredLocations.isEmpty() && !locationUiState.isLoading && locationUiState.error == null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (locationUiState.searchQuery.isBlank() && locationUiState.selectedCategory == null)
                                        "No locations found for this city"
                                    else
                                        "No locations match your filters",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (locationUiState.searchQuery.isNotBlank() || locationUiState.selectedCategory != null) {
                                    Button(
                                        onClick = { locationViewModel.clearFilters() },
                                        modifier = Modifier.padding(top = 16.dp)
                                    ) {
                                        Text("Clear filters")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationListItem(
    location: Location,
    onClick: () -> Unit,
    onDetailScreenClick: (Location) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (location.category.isNotBlank()) {
                    Text(
                        text = location.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(onClick = { onDetailScreenClick(location) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Details"
                )
            }
        }
    }
}
