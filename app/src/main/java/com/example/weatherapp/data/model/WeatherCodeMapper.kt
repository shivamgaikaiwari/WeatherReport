package com.example.weatherapp.data.model

/** Maps WMO weather interpretation codes (used by Open-Meteo) to text + icon. */
object WeatherCodeMapper {
    fun map(code: Int): WeatherCondition {
        val (description, icon) = when (code) {
            0 -> "Clear sky" to "☀️"
            1 -> "Mainly clear" to "🌤️"
            2 -> "Partly cloudy" to "⛅"
            3 -> "Overcast" to "☁️"
            45, 48 -> "Fog" to "🌫️"
            51, 53, 55 -> "Drizzle" to "🌦️"
            56, 57 -> "Freezing drizzle" to "🌧️"
            61 -> "Light rain" to "🌧️"
            63 -> "Moderate rain" to "🌧️"
            65 -> "Heavy rain" to "🌧️"
            66, 67 -> "Freezing rain" to "🌧️"
            71 -> "Light snow" to "🌨️"
            73 -> "Moderate snow" to "🌨️"
            75 -> "Heavy snow" to "❄️"
            77 -> "Snow grains" to "🌨️"
            80, 81, 82 -> "Rain showers" to "🌦️"
            85, 86 -> "Snow showers" to "🌨️"
            95 -> "Thunderstorm" to "⛈️"
            96, 99 -> "Thunderstorm with hail" to "⛈️"
            else -> "Unknown" to "❔"
        }
        return WeatherCondition(code, description, icon)
    }
}
