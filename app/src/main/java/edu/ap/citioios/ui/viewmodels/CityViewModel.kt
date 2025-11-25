package edu.ap.citioios.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.citioios.models.City
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.util.GeoPoint
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class CityUiState(
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedCountry: String? = null, // null means "All Countries"
    val availableCountries: List<String> = emptyList(),
    val errorMessage: String = ""
)

class CityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    init {
        fetchCities()
    }

    fun fetchCities() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")
    
        FirebaseRepository.fetchCities(
            onSuccess = { cities ->
                val uniqueCountries = cities.map { it.country }.distinct().sorted()
                _uiState.value = _uiState.value.copy(
                    cities = cities,
                    availableCountries = uniqueCountries,
                    filteredCities = filterCities(cities, _uiState.value.searchQuery, _uiState.value.selectedCountry),
                    isLoading = false
                )
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fout bij het laden van steden: ${exception.message}"
                )
            }
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredCities = filterCities(_uiState.value.cities, query, _uiState.value.selectedCountry)
        )
    }

    fun updateCountryFilter(country: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCountry = country,
            filteredCities = filterCities(_uiState.value.cities, _uiState.value.searchQuery, country)
        )
    }

    private fun filterCities(cities: List<City>, query: String, country: String?): List<City> {
        var filtered = cities
        
        if (country != null) {
            filtered = filtered.filter { it.country == country }
        }
        
        if (query.isNotBlank()) {
            filtered = filtered.filter { city ->
                city.name.contains(query, ignoreCase = true) ||
                city.description.contains(query, ignoreCase = true)
            }
        }
        return filtered
    }

    fun refreshCities() {
        fetchCities()
    }

    fun addCity(name: String, description: String, country: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        FirebaseRepository.checkCityExists(
            cityName = name,
            onResult = { exists ->
                if (exists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Een stad met deze naam bestaat al"
                    )
                } else {
                    viewModelScope.launch {
                        val geoPoint = geocodeCity(name, country)
                        if (geoPoint != null) {
                            val newCity = City(
                                name = name,
                                description = description,
                                country = country,
                                latitude = geoPoint.latitude,
                                longitude = geoPoint.longitude,
                                restaurantCount = 0
                            )

                            FirebaseRepository.saveCityToFirestore(
                                city = newCity,
                                onSuccess = {
                                    _uiState.value = _uiState.value.copy(isLoading = false)
                                    fetchCities()
                                    onSuccess()
                                },
                                onError = { exception ->
                                    _uiState.value = _uiState.value.copy(
                                        isLoading = false,
                                        errorMessage = "Fout bij het opslaan van de stad: ${exception.message}"
                                    )
                                }
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Kon coördinaten niet vinden voor deze stad"
                            )
                        }
                    }
                }
            },
            onError = { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fout bij het controleren van de stad: ${exception.message}"
                )
            }
        )
    }


    suspend fun geocodeCity(name: String, country: String): GeoPoint? =
        withContext(Dispatchers.IO) {
            try {
                val query = URLEncoder.encode("${name.trim()}, ${country.trim()}", "UTF-8")
                val urlString = "https://nominatim.openstreetmap.org/search?q=$query&format=json&limit=1"

                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                connection.setRequestProperty("User-Agent", "CitioIOS/1.0 (edu.ap.citioios)")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Accept-Language", "en")
                connection.setRequestProperty("From", "s151582@ap.be")

                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = JSONArray(response)

                    if (json.length() > 0) {
                        val item = json.getJSONObject(0)
                        val lat = item.getString("lat").toDouble()
                        val lon = item.getString("lon").toDouble()
                        return@withContext GeoPoint(lat, lon)
                    } else {
                        Log.e("CityViewModel", "Nominatim returned no results for $name, $country")
                    }
                } else {
                    Log.e("CityViewModel", "Nominatim HTTP Error: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                Log.e("CityViewModel", "Geocoding exception for $name, $country", e)
            }

            return@withContext null
        }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
