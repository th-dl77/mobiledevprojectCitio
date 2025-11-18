package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.citioios.models.Location
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import android.util.Log

data class LocationUiState(
    val locations: List<Location> = emptyList(),
    val filteredLocations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val availableCategories: List<String> = emptyList()
)

class LocationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState: StateFlow<LocationUiState> = _uiState

    fun loadLocationsForCity(cityId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        FirebaseRepository.fetchLocationsByCityId(
            cityId = cityId,
            onSuccess = { locations ->
                Log.d("LocationViewModel", "Loaded ${locations.size} locations for city: $cityId")
                
                val categories = locations.map { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .sorted()
                
                _uiState.value = _uiState.value.copy(
                    locations = locations,
                    availableCategories = categories,
                    isLoading = false,
                    error = null
                )
                updateFilteredLocations()
            },
            onError = { exception ->
                Log.e("LocationViewModel", "Error loading locations for city: $cityId", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Error loading locations"
                )
            }
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredLocations()
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        updateFilteredLocations()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = ""
        )
        updateFilteredLocations()
    }

    private fun updateFilteredLocations() {
        val currentState = _uiState.value
        var filtered = currentState.locations

        // Filter by category
        if (!currentState.selectedCategory.isNullOrBlank()) {
            filtered = filtered.filter { location ->
                location.category.equals(currentState.selectedCategory, ignoreCase = true)
            }
        }

        // Filter by search query
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { location ->
                location.name.lowercase().contains(query) ||
                location.category.lowercase().contains(query)
            }
        }

        _uiState.value = currentState.copy(filteredLocations = filtered)
    }

    fun refreshLocations(cityId: String) {
        loadLocationsForCity(cityId)
    }
}
