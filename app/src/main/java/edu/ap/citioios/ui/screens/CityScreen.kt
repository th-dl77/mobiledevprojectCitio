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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.ap.citioios.models.City
import edu.ap.citioios.ui.theme.CitioIOSTheme
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

val sampleCities = listOf(
    City("Antwerp", GeoPoint(51.2194, 4.4025)),
    City("Paris", GeoPoint(48.8566, 2.3522)),
    City("London", GeoPoint(51.5074, -0.1278)),
    City("New York", GeoPoint(40.7128, -74.0060)),
    City("Tokyo", GeoPoint(35.6762, 139.6503))
)

@Composable
fun CityScreen() {
    val apGuesthouse = GeoPoint(51.230167, 4.416129)
    var center by remember { mutableStateOf(apGuesthouse) }
    var zoom by remember { mutableStateOf(12.0) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "City Map Viewer", // 👈 Renamed title
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (LocalInspectionMode.current) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) { Text(text = "Map Preview Unavailable") }
        } else {
            OsmMapView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                center = center,
                zoom = zoom,
                onMapViewCreated = {
                    mapViewInstance = it
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(sampleCities) { city ->
                CityListItem(
                    cityName = city.name,
                    onClick = {
                        center = city.location
                        zoom = 12.0
                    }
                )
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
            .clickable { onClick() } // 👈 Makes the row clickable
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
        CityScreen()
    }
}