package edu.ap.citioios.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import edu.ap.citioios.models.Location
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private fun addMarkers(mapView: MapView, locations: List<Location>) {
    val markerIcon = mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default)

    locations.forEach { location ->
        val marker = Marker(mapView)
        marker.position = location.geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = markerIcon

        marker.title = location.name

        mapView.overlays.add(marker)
    }
}

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double,
    locations: List<Location> = emptyList(),
    onMapViewCreated: (MapView) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(zoom)
                controller.setCenter(center)
                setMultiTouchControls(true)

                addMarkers(this, locations)

                onMapViewCreated(this)
            }
        },
        update = { view ->
            if (view.zoomLevelDouble != zoom) {
                view.controller.setZoom(zoom)
            }
            if (view.mapCenter.latitude != center.latitude ||
                view.mapCenter.longitude != center.longitude) {
                view.controller.setCenter(center)
            }

            view.overlays.removeAll { it is Marker }

            addMarkers(view, locations)

            view.invalidate()
        }
    )
}