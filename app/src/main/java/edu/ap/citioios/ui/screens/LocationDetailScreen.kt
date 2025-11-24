package edu.ap.citioios.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.Review
import edu.ap.citioios.models.toOsmGeoPoint
import edu.ap.citioios.ui.theme.CitioIOSTheme
import edu.ap.citioios.ui.viewmodels.LocationDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    location: Location,
    onBackClick: () -> Unit,
    viewModel: LocationDetailViewModel = viewModel()
) {
    val osmGeoPoint = location.geoPoint.toOsmGeoPoint()

    val reviews by viewModel.reviews.collectAsState()

    LaunchedEffect(location.id) {
        viewModel.fetchReviews(location.id)
    }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                if (location.imageUrl.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        AsyncImage(
                            model = location.imageUrl,
                            contentDescription = "Foto van ${location.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
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
            }
            item {
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

            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp, horizontal = 16.dp))
            }

            item {
                Text(
                    text = "Alle Reviews (${reviews.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        text = "Er zijn nog geen reviews voor deze locatie.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }

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

@Composable
fun ReviewCard(review: Review, modifier: Modifier = Modifier) {
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
    val formattedDate = remember(review.createdAt) {
        formatter.format(review.createdAt)
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { starIndex ->
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (starIndex <= review.rating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${review.rating}/5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Door: ${review.userDisplayName} op $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
