package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class City(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val restaurantCount: Int = 0,
    val country: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)
