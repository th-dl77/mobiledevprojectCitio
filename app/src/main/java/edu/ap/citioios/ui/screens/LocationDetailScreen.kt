package edu.ap.citioios.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.ui.viewmodels.LocationDetailViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    location: Location,
    onBackClick: () -> Unit,
    viewModel: LocationDetailViewModel = viewModel()
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
            RatingAndCommentSection(
                onReviewSubmit = { rating, comment ->
                    viewModel.submitReview(
                        locationId = location.id,
                        rating = rating,
                        comment = comment
                    )
                }
            )
        }
    }
}
@Composable
fun RatingAndCommentSection(onReviewSubmit: (Int, String) -> Unit) {
    var selectedRating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Laat een review achter",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                (1..5).forEach { starIndex ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Ster $starIndex",
                        tint = if (starIndex <= selectedRating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(horizontal = 2.dp)
                            .clickable { selectedRating = starIndex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Commentaar (optioneel)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedRating > 0) {
                        onReviewSubmit(selectedRating, comment.trim())
                        selectedRating = 0
                        comment = ""
                    }
                },
                enabled = selectedRating > 0,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Verstuur Review")
            }
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