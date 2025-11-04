package edu.ap.citioios.models

import org.osmdroid.util.GeoPoint

data class City(
    val name: String,
    val location: GeoPoint
)