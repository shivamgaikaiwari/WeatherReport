package com.example.weatherapp.data.model

/** Domain models used by the UI — independent of the JSON shape. */
data class WeatherReport(
    val cityName: String,
    val region: String?,
    val country: String?,
    val current: CurrentWeather,
    val daily: List<DailyForecast>
)

data class CurrentWeather(
    val time: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val condition: WeatherCondition
)

data class DailyForecast(
    val date: String,
    val maxC: Double,
    val minC: Double,
    val condition: WeatherCondition
)

data class WeatherCondition(
    val code: Int,
    val description: String,
    val icon: String
)
