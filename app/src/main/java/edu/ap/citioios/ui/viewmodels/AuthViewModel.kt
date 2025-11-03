package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {


    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                isLoggedIn = true,
                userEmail = currentUser.email ?: "Unknown"
            )
        }
    }

    fun loginUser(email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        FirebaseRepository.loginUser(
            email = email,
            password = password,
            onSuccess = {
                val currentUser = Firebase.auth.currentUser
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userEmail = currentUser?.email ?: "Unknown"
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        )
    }

    fun registerUser(displayName: String, email: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        
        FirebaseRepository.registerUser(
            email = email,
            password = password,
            displayName = displayName,
            onSuccess = {
                val currentUser = Firebase.auth.currentUser
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userEmail = currentUser?.email ?: "Unknown"
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message
                )
            }
        )
    }

    fun logout() {
        FirebaseRepository.logoutUser()
        _uiState.value = AuthUiState() // Reset to initial state
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
