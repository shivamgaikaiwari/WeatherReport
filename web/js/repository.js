// Mirrors GeocodingApi.kt + WeatherApi.kt + WeatherRepositoryImpl.kt.
import { toReport } from "./mapper.js";

const GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";
const FORECAST_URL = "https://api.open-meteo.com/v1/forecast";

export class CityNotFoundError extends Error {}
export class HttpError extends Error {
  constructor(status) {
    super(`HTTP ${status}`);
    this.status = status;
  }
}

async function getJson(url, params, fetchImpl) {
  const res = await fetchImpl(`${url}?${new URLSearchParams(params)}`);
  if (!res.ok) throw new HttpError(res.status);
  return res.json();
}

/**
 * Look up a city, then fetch its current conditions and 7-day forecast.
 * units: "imperial" (°F, mph) or "metric" (°C, km/h).
 */
export async function getWeather(city, { units = "imperial", signal, fetchImpl = fetch } = {}) {
  const f = (url, params) => getJson(url, params, (u) => fetchImpl(u, { signal }));

  const geo = await f(GEOCODING_URL, { name: city, count: 1, language: "en", format: "json" });
  const location = geo.results?.[0];
  if (!location) throw new CityNotFoundError(city);

  const metric = units === "metric";
  const forecast = await f(FORECAST_URL, {
    latitude: location.latitude,
    longitude: location.longitude,
    current: "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
    daily: "weather_code,temperature_2m_max,temperature_2m_min",
    temperature_unit: metric ? "celsius" : "fahrenheit",
    wind_speed_unit: metric ? "kmh" : "mph",
    timezone: "auto",
    forecast_days: 7,
  });
  return toReport(location, forecast);
}
