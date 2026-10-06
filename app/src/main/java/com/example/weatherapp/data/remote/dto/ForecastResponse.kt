package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/** JSON returned by https://api.open-meteo.com/v1/forecast */
data class ForecastResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("current") val current: CurrentDto? = null,
    @SerializedName("daily") val daily: DailyDto? = null
)

data class CurrentDto(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val apparentTemperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double
)

/** Open-Meteo returns daily data as parallel arrays indexed by day. */
data class DailyDto(
    @SerializedName("time") val time: List<String> = emptyList(),
    @SerializedName("weather_code") val weatherCode: List<Int> = emptyList(),
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double> = emptyList(),
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double> = emptyList()
)
