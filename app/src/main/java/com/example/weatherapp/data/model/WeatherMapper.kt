package com.example.weatherapp.data.model

import com.example.weatherapp.data.remote.dto.ForecastResponse
import com.example.weatherapp.data.remote.dto.LocationDto

/** Converts parsed JSON DTOs into the domain [WeatherReport]. */
object WeatherMapper {
    fun toReport(location: LocationDto, forecast: ForecastResponse): WeatherReport {
        val current = forecast.current
            ?: throw MalformedWeatherDataException("Missing 'current' block")

        val daily = forecast.daily?.let { d ->
            val days = minOf(d.time.size, d.weatherCode.size, d.temperatureMax.size, d.temperatureMin.size)
            (0 until days).map { i ->
                DailyForecast(
                    date = d.time[i],
                    maxC = d.temperatureMax[i],
                    minC = d.temperatureMin[i],
                    condition = WeatherCodeMapper.map(d.weatherCode[i])
                )
            }
        }.orEmpty()

        return WeatherReport(
            cityName = location.name,
            region = location.region,
            country = location.country,
            current = CurrentWeather(
                time = current.time,
                temperatureC = current.temperature,
                feelsLikeC = current.apparentTemperature,
                humidityPercent = current.humidity,
                windSpeedKmh = current.windSpeed,
                condition = WeatherCodeMapper.map(current.weatherCode)
            ),
            daily = daily
        )
    }
}

class MalformedWeatherDataException(message: String) : Exception(message)
