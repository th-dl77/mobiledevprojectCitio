package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Review(
    @DocumentId
    val id: String = "",

    val userId: String = "",

    val username: String = "",

    val rating: Double = 0.0,
    val comment: String = "",

    @ServerTimestamp
    val createdAt: Date? = null
)