package edu.ap.citioios.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import edu.ap.citioios.ui.theme.CitioIOSTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    location: Location,
    onBackClick: () -> Unit
) {
    val osmGeoPoint = location.geoPoint.toOsmGeoPoint()

    val center by remember { mutableDoubleStateOf(osmGeoPoint.latitude) }
    val zoom by remember { mutableDoubleStateOf(16.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        location.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (location.averageRating > 0.0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f", location.averageRating),
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            if (location.reviewCount > 0) {
                                Text(
                                    text = " (${location.reviewCount} reviews)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    if (location.category.isNotBlank()) {
                        DetailItem(
                            label = "Category",
                            value = location.category
                        )
                    }
                    if (location.address.isNotBlank()) {
                        DetailItem(
                            label = "Address",
                            value = location.address
                        )
                    }
                    if (location.description.isNotBlank()) {
                        DetailItem(
                            label = "Description",
                            value = location.description
                        )
                    }

                    DetailItem(
                        label = "Coordinates (OSM)",
                        value = "Lat: ${String.format(Locale.getDefault(), "%.4f", osmGeoPoint.latitude)}, Lon: ${String.format(Locale.getDefault(), "%.4f", osmGeoPoint.longitude)}"
                    )

                    if (location.addedByUserId.isNotBlank()) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Added by: ${location.addedByUserId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // todo: Add a section for reviews and comments here

        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationDetailScreenPreview() {
    CitioIOSTheme {
        LocationDetailScreen(
            location = Location(
                id = "loc1",
                cityId = "city1",
                name = "MAS Museum",
                category = "Museum",
                imageUrl = "test",
                averageRating = 4.5,
                reviewCount = 128,
                addedByUserId = "user123"
            ),
            onBackClick = {}
        )
    }
}