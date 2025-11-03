package edu.ap.citioios.ui.viewmodels

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val userEmail: String = ""
)
