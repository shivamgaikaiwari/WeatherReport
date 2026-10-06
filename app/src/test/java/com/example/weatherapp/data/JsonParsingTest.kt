package com.example.weatherapp.data

import com.example.weatherapp.data.remote.dto.ForecastResponse
import com.example.weatherapp.data.remote.dto.GeocodingResponse
import com.example.weatherapp.util.TestData
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonParsingTest {
    private val gson = Gson()

    @Test
    fun `geocoding json is parsed into LocationDto`() {
        val response = gson.fromJson(TestData.readResource("geocoding_london.json"), GeocodingResponse::class.java)

        val location = response.results!!.single()
        assertEquals("London", location.name)
        assertEquals(51.50853, location.latitude, 0.0001)
        assertEquals(-0.12574, location.longitude, 0.0001)
        assertEquals("United Kingdom", location.country)
        assertEquals("England", location.region)
    }

    @Test
    fun `geocoding json without results has null results`() {
        val response = gson.fromJson(TestData.readResource("geocoding_empty.json"), GeocodingResponse::class.java)
        assertNull(response.results)
    }

    @Test
    fun `forecast json is parsed into ForecastResponse`() {
        val response = gson.fromJson(TestData.readResource("forecast_london.json"), ForecastResponse::class.java)

        val current = response.current!!
        assertEquals(15.4, current.temperature, 0.001)
        assertEquals(14.1, current.apparentTemperature, 0.001)
        assertEquals(72, current.humidity)
        assertEquals(3, current.weatherCode)
        assertEquals(12.6, current.windSpeed, 0.001)

        val daily = response.daily!!
        assertEquals(listOf("2026-10-05", "2026-10-06", "2026-10-07"), daily.time)
        assertEquals(listOf(3, 61, 0), daily.weatherCode)
        assertEquals(listOf(16.2, 14.8, 18.0), daily.temperatureMax)
        assertEquals(listOf(9.1, 10.3, 8.7), daily.temperatureMin)
    }
}
