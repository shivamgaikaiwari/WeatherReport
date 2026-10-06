package com.example.weatherapp.ui

import com.example.weatherapp.data.model.WeatherReport

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val report: WeatherReport) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}
