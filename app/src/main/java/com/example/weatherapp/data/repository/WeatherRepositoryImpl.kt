package com.example.weatherapp.data.repository

import com.example.weatherapp.data.model.WeatherMapper
import com.example.weatherapp.data.model.WeatherReport
import com.example.weatherapp.data.remote.GeocodingApi
import com.example.weatherapp.data.remote.WeatherApi
import kotlinx.coroutines.CancellationException

/**
 * 1. Resolves the city name to coordinates (Open-Meteo Geocoding API)
 * 2. Fetches the forecast for those coordinates (Open-Meteo Forecast API)
 * 3. Maps the parsed JSON into a [WeatherReport]
 */
class WeatherRepositoryImpl(
    private val geocodingApi: GeocodingApi,
    private val weatherApi: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(city: String): Result<WeatherReport> = try {
        val location = geocodingApi.searchCity(city).results?.firstOrNull()
            ?: throw CityNotFoundException(city)
        val forecast = weatherApi.getForecast(location.latitude, location.longitude)
        Result.success(WeatherMapper.toReport(location, forecast))
    } catch (e: CancellationException) {
        throw e // never swallow coroutine cancellation
    } catch (e: Exception) {
        Result.failure(e)
    }
}
