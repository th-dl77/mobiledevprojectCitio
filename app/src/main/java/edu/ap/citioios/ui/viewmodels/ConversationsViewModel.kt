package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.citioios.models.Conversation
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentUserId: String = ""
)

class ConversationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val currentUser = FirebaseRepository.getCurrentUser()
        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "User not logged in"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            currentUserId = currentUser.uid
        )

        FirebaseRepository.fetchConversations(
            userId = currentUser.uid,
            onSuccess = { conversations ->
                _uiState.value = _uiState.value.copy(
                    conversations = conversations,
                    isLoading = false,
                    errorMessage = null
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Failed to load conversations"
                )
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun getOtherUserEmail(conversation: Conversation, currentUserId: String): String {
        val otherUserId = conversation.participants.firstOrNull { it != currentUserId }
        return conversation.participantEmails[otherUserId] ?: "Unknown"
    }
}
