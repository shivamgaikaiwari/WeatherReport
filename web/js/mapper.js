// Mirrors WeatherCodeMapper.kt and WeatherMapper.kt: Open-Meteo JSON -> domain report.

/** WMO weather interpretation codes (used by Open-Meteo) to text + icon. */
export function mapWeatherCode(code) {
  const table = [
    [[0], "Clear sky", "☀️"],
    [[1], "Mainly clear", "🌤️"],
    [[2], "Partly cloudy", "⛅"],
    [[3], "Overcast", "☁️"],
    [[45, 48], "Fog", "🌫️"],
    [[51, 53, 55], "Drizzle", "🌦️"],
    [[56, 57], "Freezing drizzle", "🌧️"],
    [[61], "Light rain", "🌧️"],
    [[63], "Moderate rain", "🌧️"],
    [[65], "Heavy rain", "🌧️"],
    [[66, 67], "Freezing rain", "🌧️"],
    [[71], "Light snow", "🌨️"],
    [[73], "Moderate snow", "🌨️"],
    [[75], "Heavy snow", "❄️"],
    [[77], "Snow grains", "🌨️"],
    [[80, 81, 82], "Rain showers", "🌦️"],
    [[85, 86], "Snow showers", "🌨️"],
    [[95], "Thunderstorm", "⛈️"],
    [[96, 99], "Thunderstorm with hail", "⛈️"],
  ];
  const hit = table.find(([codes]) => codes.includes(code));
  const [description, icon] = hit ? [hit[1], hit[2]] : ["Unknown", "❔"];
  return { code, description, icon };
}

export class MalformedWeatherDataError extends Error {}

/** Combine a geocoding result and a forecast response into one report. */
export function toReport(location, forecast) {
  const current = forecast?.current;
  if (!current) throw new MalformedWeatherDataError("Missing 'current' block");

  const d = forecast.daily;
  const days = d
    ? Math.min(d.time.length, d.weather_code.length, d.temperature_2m_max.length, d.temperature_2m_min.length)
    : 0;
  const daily = Array.from({ length: days }, (_, i) => ({
    date: d.time[i],
    max: d.temperature_2m_max[i],
    min: d.temperature_2m_min[i],
    condition: mapWeatherCode(d.weather_code[i]),
  }));

  return {
    cityName: location.name,
    region: location.admin1 ?? null,
    country: location.country ?? null,
    current: {
      time: current.time,
      temperature: current.temperature_2m,
      feelsLike: current.apparent_temperature,
      humidityPercent: current.relative_humidity_2m,
      windSpeed: current.wind_speed_10m,
      condition: mapWeatherCode(current.weather_code),
    },
    daily,
  };
}
