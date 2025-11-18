package edu.ap.citioios.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    val locationId: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    @ServerTimestamp
    val createdAt: Date = Date()
)
