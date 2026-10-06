package com.example.weatherapp.util

import com.example.weatherapp.data.model.CurrentWeather
import com.example.weatherapp.data.model.DailyForecast
import com.example.weatherapp.data.model.WeatherCodeMapper
import com.example.weatherapp.data.model.WeatherReport

object TestData {
    fun readResource(name: String): String =
        requireNotNull(javaClass.classLoader?.getResource(name)) { "Missing test resource $name" }.readText()

    val londonReport = WeatherReport(
        cityName = "London",
        region = "England",
        country = "United Kingdom",
        current = CurrentWeather(
            time = "2026-10-05T14:00",
            temperatureC = 15.4,
            feelsLikeC = 14.1,
            humidityPercent = 72,
            windSpeedKmh = 12.6,
            condition = WeatherCodeMapper.map(3)
        ),
        daily = listOf(DailyForecast("2026-10-05", 16.2, 9.1, WeatherCodeMapper.map(3)))
    )
}
