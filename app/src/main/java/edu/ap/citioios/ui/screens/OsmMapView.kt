package edu.ap.citioios.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    center: GeoPoint,
    zoom: Double,
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
        }
    )
}