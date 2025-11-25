package edu.ap.citioios.ui.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import edu.ap.citioios.R
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.toOsmGeoPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


private fun scaleBitmap(bitmap: android.graphics.Bitmap, width: Int, height: Int): android.graphics.Bitmap {
    return android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true)
}

private fun addMarkers(
    mapView: MapView,
    locations: List<Location>,
    onMarkerClick: (Location) -> Unit
) {
    val customBitmap = ContextCompat.getDrawable(mapView.context, R.drawable.ic_custom_pin)
        ?.let { drawable ->
            val raw = android.graphics.Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(raw)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            scaleBitmap(raw, 64, 64)
        }

    val customIcon = customBitmap?.let { bitmap ->
        android.graphics.drawable.BitmapDrawable(mapView.resources, bitmap)
    }

    locations.forEach { location ->
        if (location.geoPoint.latitude == 0.0 &&
            location.geoPoint.longitude == 0.0) {

            Log.w("OsmMapView", "Skipping ${location.name}: Coordinates are (0,0)")
            return@forEach
        }

        val marker = Marker(mapView).apply {
            position = location.geoPoint.toOsmGeoPoint()
            icon = customIcon
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            title = location.name
            snippet = location.categories.joinToString(", ")

            setOnMarkerClickListener { m, _ ->
                onMarkerClick(location)
                m.showInfoWindow()
                true
            }
        }

        mapView.overlays.add(marker)
    }

    mapView.invalidate()
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
                setMultiTouchControls(true)

                val locationOverlay = MyLocationNewOverlay(
                    GpsMyLocationProvider(context),
                    this
                )
                locationOverlay.enableMyLocation()

                controller.setZoom(zoom)
                controller.setCenter(center)

                overlays.add(locationOverlay)

                addMarkers(this, locations, onMarkerClick)

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
