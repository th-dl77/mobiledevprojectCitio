package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import edu.ap.citioios.models.Message
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String = "",
    val currentUserEmail: String = ""
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val currentUser = FirebaseRepository.getCurrentUser()
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                currentUserId = currentUser.uid,
                currentUserEmail = currentUser.email
            )
        }
    }

    fun loadMessages(conversationId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        FirebaseRepository.fetchMessages(
            conversationId = conversationId,
            onSuccess = { messages ->
                _uiState.value = _uiState.value.copy(
                    messages = messages,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load messages"
                )
            }
        )
    }

    fun sendMessage(conversationId: String, receiverId: String, text: String) {
        if (text.isBlank()) return

        val currentUser = FirebaseRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isSending = true)

        val message = Message(
            conversationId = conversationId,
            senderId = currentUser.uid,
            senderEmail = currentUser.email,
            receiverId = receiverId,
            text = text.trim()
        )

        FirebaseRepository.sendMessage(
            conversationId = conversationId,
            message = message,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = null
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    errorMessage = exception.message ?: "Failed to send message"
                )
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
