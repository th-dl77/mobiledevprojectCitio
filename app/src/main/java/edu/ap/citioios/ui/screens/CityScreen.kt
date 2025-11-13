package edu.ap.citioios.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.ap.citioios.models.City
import edu.ap.citioios.models.Location
import edu.ap.citioios.ui.theme.CitioIOSTheme
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

val sampleCities = listOf(
    Location("1","Zilte", geoPoint = GeoPoint(51.22905675588554, 4.404830141179329)),
    Location("2","Jane", GeoPoint(51.234323945479154, 4.4047789960964)),
    Location("3","Dome", GeoPoint(51.20601577414562, 4.426980821041683)),
    Location("4","McDonalds Meir", GeoPoint(51.217996125955594, 4.407443527458958)),
    Location("5","The Breakfast Club", GeoPoint(51.21672457372679, 4.399149929103938))
)

@Composable
fun CityScreen(
    city: City,
    onBackClick: () -> Unit
) {
    val apGuesthouse = GeoPoint(51.230167, 4.416129)
    var center by remember { mutableStateOf(apGuesthouse) }
    var zoom by remember { mutableStateOf(18.0) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(city.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // City information card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = city.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (city.description.isNotBlank()) {
                        Text(
                            text = city.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "${city.restaurantCount} restaurants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
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
                    locations = sampleCities,
                    onMapViewCreated = {
                        mapViewInstance = it
                    }
                )
            }

        }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(sampleCities) { location ->
                    CityListItem(
                        cityName = location.name,
                        onClick = {
                            center = location.geoPoint
                            zoom = 18.0
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CityListItem(
    cityName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = cityName,
            style = MaterialTheme.typography.bodyLarge
        )
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
