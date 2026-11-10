# Weather Report (Kotlin · MVVM · Jetpack Compose)

Search any city and see current conditions plus a 7-day forecast.

**Try it in your browser:** https://shivamgaikaiwari.github.io/WeatherReport/
**Android APK:** [download the latest build](https://github.com/shivamgaikaiwari/WeatherReport/releases/latest/download/weather-report.apk)

## Web version (`web/`)
A browser port of the app with the same layers, one file each: `mapper.js` (WeatherMapper + WeatherCodeMapper), `repository.js` (Retrofit APIs + repository), `viewmodel.js` (WeatherViewModel with Idle/Loading/Success/Error) and `app.js` (the screen). Plain ES modules, no build step. Tests: `node --test web/test/*.test.js`. GitHub Actions runs them and publishes `web/` to GitHub Pages on every push to `main`; another workflow runs the Android unit tests and attaches a fresh APK to a release.

## Weather API
[Open-Meteo](https://open-meteo.com/) — free, **no API key needed**.
1. Geocoding: `https://geocoding-api.open-meteo.com/v1/search?name=London` → lat/lon
2. Forecast: `https://api.open-meteo.com/v1/forecast?latitude=..&longitude=..&current=..&daily=..`

JSON is parsed with Retrofit + Gson into DTOs, then mapped to domain models.

## Architecture (MVVM)
```
UI (Compose)  WeatherScreen ──observes StateFlow──▶ WeatherViewModel
ViewModel     WeatherViewModel ──calls──▶ WeatherRepository (interface)
Data          WeatherRepositoryImpl ──▶ GeocodingApi + WeatherApi (Retrofit)
                                     └─▶ WeatherMapper (DTO → WeatherReport)
DI            AppContainer (manual DI, created in WeatherApplication)
```
UI states: `Idle`, `Loading`, `Success(report)`, `Error(message)`.

## Unit tests (`app/src/test`)
| Test | Covers |
|---|---|
| `JsonParsingTest` | Gson parsing of real-shaped geocoding/forecast JSON fixtures |
| `WeatherMapperTest` | DTO → domain mapping, malformed data handling |
| `WeatherCodeMapperTest` | WMO weather code → description |
| `WeatherRepositoryImplTest` | Retrofit end-to-end via MockWebServer: success, city not found, HTTP 500, bad JSON |
| `WeatherViewModelTest` | State transitions (Idle → Loading → Success/Error), blank input, error messages |

Run: `./gradlew testDebugUnitTest` (or right-click the `test` folder → Run Tests in Android Studio).

## Setup
1. Open the `WeatherApp` folder in Android Studio (Ladybug or newer), JDK 17+.
2. Let Gradle sync. If `gradlew` is missing, run `gradle wrapper` once or just use Android Studio's sync.
3. Run on an emulator/device (min SDK 26).
