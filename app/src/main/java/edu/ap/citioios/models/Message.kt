package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    @DocumentId
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderEmail: String = "",
    val receiverId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

fun String.toDisplayName(): String = this.substringBefore("@")
