package edu.ap.citioios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.citioios.models.City
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.ui.viewmodels.LocationViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    city: City,
    onBackClick: () -> Unit,
    onAddLocationClick: () -> Unit = {},
    locationViewModel: LocationViewModel = viewModel(),
    onDetailScreenClick: (Location) -> Unit = {}
) {
    val locationUiState by locationViewModel.uiState.collectAsState()
    
    // Default center for map, center city geo?
    val defaultCenter = GeoPoint(51.230167, 4.416129)
    var center by remember { mutableStateOf(defaultCenter) }
    var zoom by remember { mutableDoubleStateOf(15.0) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Load locations when screen opens
    LaunchedEffect(city.id) {
        if (city.id.isNotBlank()) {
            locationViewModel.loadLocationsForCity(city.id)
        }
    }

    Scaffold(
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
                ) {
                    OsmMapView(
                        modifier = Modifier.fillMaxSize(),
                        center = center,
                        zoom = zoom,
                        locations = locationUiState.filteredLocations,
                        onMapViewCreated = {
                            mapViewInstance = it
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
                modifier = Modifier.weight(1f).padding(end = 8.dp)
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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
                if (location.averageRating > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = String.format(Locale.getDefault(),"%.1f", location.averageRating),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                        if (location.reviewCount > 0) {
                            Text(
                                text = " (${location.reviewCount})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = { onDetailScreenClick(location) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CityScreenPreview() {
    CitioIOSTheme {
        CityScreen(
            city = City(
                id = "1",
                name = "Antwerp",
                description = "Beautiful historic city with great restaurants",
                restaurantCount = 25
            ),
            onBackClick = {}
        )
    }
}
