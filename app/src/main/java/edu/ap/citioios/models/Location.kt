package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Location(
    @DocumentId
    val id: String = "",

    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val imageUrl: String = "",

    val category: String = "",

    val addedByUserId: String = "",

    @ServerTimestamp
    val addedAt: Date? = null,

    val cityName: String = "",

    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
)