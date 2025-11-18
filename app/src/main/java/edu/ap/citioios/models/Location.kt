package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.GeoPoint as FirebaseGeoPoint
import org.osmdroid.util.GeoPoint as OsmGeoPoint
import java.util.Date

data class Location(
    @DocumentId
    val id: String = "",

    val name: String = "",
    val geoPoint: FirebaseGeoPoint = FirebaseGeoPoint(0.0, 0.0),
    val imageUrl: String = "",

    val category: String = "",

    val addedByUserId: String = "",

    @ServerTimestamp
    val addedAt: Date? = null,

    val cityId: String = "",

    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val address: String = "",
    val description: String = ""
)

// helper function for GeoPoint conversion
fun FirebaseGeoPoint.toOsmGeoPoint(): OsmGeoPoint = OsmGeoPoint(this.latitude, this.longitude)

fun OsmGeoPoint.toFirebaseGeoPoint(): FirebaseGeoPoint = FirebaseGeoPoint(this.latitude, this.longitude)
