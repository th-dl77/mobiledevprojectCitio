package edu.ap.citioios.ui.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import edu.ap.citioios.models.Location
import edu.ap.citioios.models.User
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

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

class LocationViewModel(application: Application) : AndroidViewModel(application) {
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

        if (!currentState.selectedCategory.isNullOrBlank()) {
            filtered = filtered.filter { location ->
                location.category.equals(currentState.selectedCategory, ignoreCase = true)
            }
        }

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
                    val currentUser = FirebaseRepository.getCurrentUser()

                    val rawAddress = "$street $number, $cityName, $country"
                    val formattedAddress = rawAddress.trim().replace(Regex("^[ ,]+|[ ,]+$"), "")

                    Log.d("LocationViewModel", "Preparing to add location with address: $formattedAddress")

                    if (imageUri != null) {
                        viewModelScope.launch {
                            try {
                                val imageUrl = FirebaseRepository.compressImageToBase64(getApplication(), imageUri)
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
                        viewModelScope.launch {
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

    private suspend fun createAndSaveLocation(
        name: String,
        category: String,
        formattedAddress: String,
        cityId: String,
        currentUser: User?,
        imageUrl: String,
        onSuccess: () -> Unit
    ) {
        val coordinates = getCoordinatesFromNominatim(formattedAddress)

        if (coordinates.latitude == 0.0 && coordinates.longitude == 0.0) {
            Log.e("LocationViewModel", "WARNING: Coordinates are (0,0). Geocoding failed for address: $formattedAddress")
        } else {
            Log.d("LocationViewModel", "Geocoding Success: ${coordinates.latitude}, ${coordinates.longitude}")
        }

        val newLocation = Location(
            name = name,
            category = category,
            address = formattedAddress,
            cityId = cityId,
            addedByUserId = currentUser?.uid ?: "",
            geoPoint = coordinates,
            averageRating = 0.0,
            reviewCount = 0,
            imageUrl = imageUrl,
            description = ""
        )

        FirebaseRepository.saveNewLocationToFirestore(
            location = newLocation,
            onSuccess = {
                FirebaseRepository.incrementCityRestaurantCount(
                    cityId = cityId,
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        loadLocationsForCity(cityId)
                        onSuccess()
                    },
                    onError = { e ->
                        Log.e("LocationViewModel", "Failed to increment count", e)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        loadLocationsForCity(cityId)
                        onSuccess()
                    }
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fout bij het opslaan van de locatie: ${exception.message}"
                )
            }
        )
    }

    private suspend fun getCoordinatesFromNominatim(address: String): GeoPoint {
        return withContext(Dispatchers.IO) {
            try {
                if (address.isBlank()) {
                    Log.e("LocationViewModel", "Address is empty, skipping geocoding.")
                    return@withContext GeoPoint(0.0, 0.0)
                }

                val cleanedAddress = address.trim()

                val encodedAddress = URLEncoder.encode(cleanedAddress, "UTF-8")
                val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedAddress&format=json&limit=1"

                Log.d("LocationViewModel", "Requesting URL: $urlString")

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                connection.setRequestProperty("User-Agent", "CitioIOS/1.0 (edu.ap.citioios)")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Accept-Language", "en")
                connection.setRequestProperty("From", "s151582@ap.be")

                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                val responseCode = connection.responseCode
                Log.d("LocationViewModel", "HTTP response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    Log.d("LocationViewModel", "Nominatim Response: $responseText")

                    val jsonArray = JSONArray(responseText)

                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        val lat = firstResult.getString("lat").toDouble()
                        val lon = firstResult.getString("lon").toDouble()
                        return@withContext GeoPoint(lat, lon)
                    } else {
                        Log.e("LocationViewModel", "Nominatim returned NO results for: $cleanedAddress")
                    }
                } else {
                    val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("LocationViewModel", "Nominatim HTTP Error $responseCode: $errorMsg")
                }

            } catch (e: Exception) {
                Log.e("LocationViewModel", "Nominatim Geocoding Exception", e)
            }

            return@withContext GeoPoint(0.0, 0.0)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
