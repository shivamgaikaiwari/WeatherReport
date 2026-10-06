package com.example.weatherapp.data

import com.example.weatherapp.data.model.MalformedWeatherDataException
import com.example.weatherapp.data.model.WeatherMapper
import com.example.weatherapp.data.remote.dto.DailyDto
import com.example.weatherapp.data.remote.dto.ForecastResponse
import com.example.weatherapp.data.remote.dto.LocationDto
import com.example.weatherapp.util.TestData
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherMapperTest {
    private val location = LocationDto(1, "London", 51.5, -0.12, "United Kingdom", "England", "Europe/London")
    private val forecast = Gson().fromJson(TestData.readResource("forecast_london.json"), ForecastResponse::class.java)

    @Test
    fun `maps dto to domain report`() {
        val report = WeatherMapper.toReport(location, forecast)

        assertEquals("London", report.cityName)
        assertEquals("United Kingdom", report.country)
        assertEquals(15.4, report.current.temperatureC, 0.001)
        assertEquals("Overcast", report.current.condition.description)
        assertEquals(3, report.daily.size)
        assertEquals("Light rain", report.daily[1].condition.description)
        assertEquals(18.0, report.daily[2].maxC, 0.001)
    }

    @Test
    fun `mismatched daily arrays are truncated to shortest length`() {
        val broken = forecast.copy(
            daily = DailyDto(
                time = listOf("2026-10-05", "2026-10-06"),
                weatherCode = listOf(0),
                temperatureMax = listOf(20.0, 21.0),
                temperatureMin = listOf(10.0, 11.0)
            )
        )
        assertEquals(1, WeatherMapper.toReport(location, broken).daily.size)
    }

    @Test(expected = MalformedWeatherDataException::class)
    fun `missing current block throws`() {
        WeatherMapper.toReport(location, forecast.copy(current = null))
    }
}
