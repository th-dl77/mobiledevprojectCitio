package edu.ap.citioios.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.citioios.models.Location
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import android.util.Log
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

data class LocationUiState(
    val locations: List<Location> = emptyList(),
    val filteredLocations: List<Location> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val availableCategories: List<String> = emptyList(),
    val errorMessage: String = ""
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

    fun addLocation(
        name: String,
        category: String,
        street: String,
        number: String,
        cityId: String,
        cityName: String,
        country: String,
        imageUri: Uri? = null,
        onSuccess: () -> Unit
    ) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        // Check if location already exists in this city
        FirebaseRepository.checkLocationExists(
            locationName = name,
            cityId = cityId,
            onResult = { exists ->
                if (exists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Een locatie met deze naam bestaat al in deze stad"
                    )
                } else {
                    // doesn't exist, create it
                    val currentUser = FirebaseRepository.getCurrentUser()
                    val formattedAddress = "$country, $cityName, $street $number"
                    
                    // Gen temp location ID for image upload
                    val tempLocationId = "${cityId}_${System.currentTimeMillis()}"
                    
                    // Handle image upload 
                    if (imageUri != null) {
                        viewModelScope.launch {
                            try {
                                // Upload image and get download URL
                                val imageUrl = FirebaseRepository.uploadLocationImage(imageUri, tempLocationId)
                                
                                // Create location with image URL
                                createAndSaveLocation(
                                    name = name,
                                    category = category,
                                    formattedAddress = formattedAddress,
                                    cityId = cityId,
                                    currentUser = currentUser,
                                    imageUrl = imageUrl,
                                    onSuccess = onSuccess
                                )
                            } catch (e: Exception) {
                                _uiState.value = _uiState.value.copy(
                                    isLoading = false,
                                    errorMessage = "Fout bij het uploaden van de afbeelding: ${e.message}"
                                )
                            }
                        }
                    } else {
                        // Create location without image
                        createAndSaveLocation(
                            name = name,
                            category = category,
                            formattedAddress = formattedAddress,
                            cityId = cityId,
                            currentUser = currentUser,
                            imageUrl = "",
                            onSuccess = onSuccess
                        )
                    }
                }
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fout bij het controleren van de locatie: ${exception.message}"
                )
            }
        )
    }

    private fun createAndSaveLocation(
        name: String,
        category: String,
        formattedAddress: String,
        cityId: String,
        currentUser: edu.ap.citioios.models.User?,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val newLocation = Location(
            name = name,
            category = category,
            address = formattedAddress,
            cityId = cityId,
            addedByUserId = currentUser?.uid ?: "",
            geoPoint = GeoPoint(0.0, 0.0), // Default coordinates for now, get from osm api?
            averageRating = 0.0,
            reviewCount = 0,
            imageUrl = imageUrl,
            description = ""
        )
        
        FirebaseRepository.saveNewLocationToFirestore(
            location = newLocation,
            onSuccess = {
                _uiState.value = _uiState.value.copy(isLoading = false)
                loadLocationsForCity(cityId) // Refresh
                onSuccess()
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fout bij het opslaan van de locatie: ${exception.message}"
                )
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
