package edu.ap.citioios.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider

private fun addMarkers(mapView: MapView, locations: List<Location>,  onMarkerClick: (Location) -> Unit) {
    val markerIcon = mapView.context.getDrawable(org.osmdroid.library.R.drawable.marker_default)

    locations.forEach { location ->
        val marker = Marker(mapView)
        marker.position = location.geoPoint.toOsmGeoPoint()
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = markerIcon

        marker.title = location.name

        marker.setOnMarkerClickListener { _, _ ->
            onMarkerClick(location)
            marker.showInfoWindow()
            true
        }

        mapView.overlays.add(marker)
    }
}

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double,
    locations: List<Location> = emptyList(),
    onMapViewCreated: (MapView) -> Unit = {},
    onMarkerClick: (Location) -> Unit = {}
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                controller.setZoom(zoom)
                controller.setCenter(center)
                setMultiTouchControls(true)

                locationOverlay.enableMyLocation()
                addMarkers(this, locations, onMarkerClick)

                this.overlays.add(locationOverlay)
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

            addMarkers(view, locations, onMarkerClick)

            view.invalidate()
        }
    )
}