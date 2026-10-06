package com.example.weatherapp.data

import com.example.weatherapp.data.model.WeatherReport
import com.example.weatherapp.data.repository.WeatherRepository
import kotlinx.coroutines.CompletableDeferred

class FakeWeatherRepository : WeatherRepository {
    var result: Result<WeatherReport> = Result.failure(IllegalStateException("not set"))
    val requestedCities = mutableListOf<String>()

    /** When set, getWeather suspends until the gate is completed (lets tests observe Loading). */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getWeather(city: String): Result<WeatherReport> {
        requestedCities += city
        gate?.await()
        return result
    }
}
