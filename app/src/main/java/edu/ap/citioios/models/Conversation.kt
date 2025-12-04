package edu.ap.citioios.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Conversation(
    @DocumentId
    val id: String = "",
    val participants: List<String> = emptyList(), // [userId1, userId2]
    val participantEmails: Map<String, String> = emptyMap(), // userId -> email
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    @ServerTimestamp
    val lastMessageTime: Date? = null
)
