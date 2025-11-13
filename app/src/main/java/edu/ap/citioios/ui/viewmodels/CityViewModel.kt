package edu.ap.citioios.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.citioios.models.City
import edu.ap.citioios.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CityUiState(
    val cities: List<City> = emptyList(),
    val filteredCities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class CityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    init {
        fetchCities()
    }

    fun fetchCities() {
        _uiState.value = _uiState.value.copy(isLoading = true)
    
        FirebaseRepository.fetchCities(
            onSuccess = { cities ->
                _uiState.value = _uiState.value.copy(
                    cities = cities,
                    filteredCities = filterCities(cities, _uiState.value.searchQuery),
                    isLoading = false
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
}
