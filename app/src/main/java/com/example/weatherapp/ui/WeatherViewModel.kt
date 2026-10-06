package com.example.weatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.weatherapp.WeatherApplication
import com.example.weatherapp.data.model.MalformedWeatherDataException
import com.example.weatherapp.data.repository.CityNotFoundException
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        val city = _query.value.trim()
        if (city.isEmpty()) {
            _uiState.value = WeatherUiState.Error(EMPTY_QUERY_MESSAGE)
            return
        }
        searchJob?.cancel() // a newer search replaces an in-flight one
        searchJob = viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            _uiState.value = repository.getWeather(city).fold(
                onSuccess = { WeatherUiState.Success(it) },
                onFailure = { WeatherUiState.Error(it.toUserMessage()) }
            )
        }
    }

    private fun Throwable.toUserMessage(): String = when (this) {
        is CityNotFoundException -> "No results found for \"$city\""
        is IOException -> NETWORK_ERROR_MESSAGE
        is HttpException -> "Server error (${code()}). Please try again."
        is MalformedWeatherDataException -> "Received invalid weather data."
        else -> message ?: "Something went wrong"
    }

    companion object {
        const val EMPTY_QUERY_MESSAGE = "Please enter a city name"
        const val NETWORK_ERROR_MESSAGE = "Network error. Check your internet connection."

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WeatherApplication
                WeatherViewModel(app.container.weatherRepository)
            }
        }
    }
}
