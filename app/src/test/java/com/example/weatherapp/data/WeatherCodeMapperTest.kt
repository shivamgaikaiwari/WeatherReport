package com.example.weatherapp.data

import com.example.weatherapp.data.model.WeatherCodeMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherCodeMapperTest {
    @Test
    fun `known codes map to descriptions`() {
        assertEquals("Clear sky", WeatherCodeMapper.map(0).description)
        assertEquals("Overcast", WeatherCodeMapper.map(3).description)
        assertEquals("Light rain", WeatherCodeMapper.map(61).description)
        assertEquals("Thunderstorm", WeatherCodeMapper.map(95).description)
    }

    @Test
    fun `unknown code maps to Unknown and keeps code`() {
        val condition = WeatherCodeMapper.map(1234)
        assertEquals("Unknown", condition.description)
        assertEquals(1234, condition.code)
    }
}
