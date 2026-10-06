package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.WeatherReport

interface WeatherRepository {
    suspend fun getWeather(city: String): Result<WeatherReport>
}

class CityNotFoundException(val city: String) : Exception("City '$city' not found")
