package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import edu.ap.citioios.models.City
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CityUiState(
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
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
                _uiState.value = _uiState.value.copy(
                    cities = cities,
                    filteredCities = filterCities(cities, _uiState.value.searchQuery),
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
            filteredCities = filterCities(_uiState.value.cities, query)
        )
    }

    private fun filterCities(cities: List<City>, query: String): List<City> {
        if (query.isBlank()) {
            return cities
        }
        return cities.filter { city ->
            city.name.contains(query, ignoreCase = true) ||
            city.description.contains(query, ignoreCase = true)
        }
    }

    fun refreshCities() {
        fetchCities()
    }

    fun addCity(name: String, description: String, country: String, onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

        //check if city already exists
        FirebaseRepository.checkCityExists(
            cityName = name,
            onResult = { exists ->
                if (exists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Een stad met deze naam bestaat al"
                    )
                } else {
                    // City doesn't exist,, save it
                    val newCity = City(
                        name = name,
                        description = description,
                        country = country,
                        restaurantCount = 0 // Default  0, will be calculated later from locations in firebase conntected to ctiy
                    )
                    
                    FirebaseRepository.saveCityToFirestore(
                        city = newCity,
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            fetchCities() // Refresh the cities list
                            onSuccess()
                        },
                        onError = { exception ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Fout bij het opslaan van de stad: ${exception.message}"
                            )
                        }
                    )
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = "")
    }
}
