package com.example.weatherapp.data

import com.example.weatherapp.data.remote.GeocodingApi
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.repository.CityNotFoundException
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import com.example.weatherapp.util.TestData
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Exercises Retrofit + Gson parsing end-to-end against a local mock server. */
class WeatherRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: WeatherRepositoryImpl

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        repository = WeatherRepositoryImpl(
            geocodingApi = retrofit.create(GeocodingApi::class.java),
            weatherApi = retrofit.create(WeatherApi::class.java)
        )
    }

    @After
    fun tearDown() = server.shutdown()

    private fun enqueueJson(file: String, code: Int = 200) =
        server.enqueue(MockResponse().setResponseCode(code).setBody(TestData.readResource(file)))

    @Test
    fun `returns weather report for a valid city`() = runTest {
        enqueueJson("geocoding_london.json")
        enqueueJson("forecast_london.json")

        val report = repository.getWeather("London").getOrThrow()

        assertEquals("London", report.cityName)
        assertEquals(15.4, report.current.temperatureC, 0.001)
        assertEquals(3, report.daily.size)

        val geoRequest = server.takeRequest()
        assertTrue(geoRequest.path!!.startsWith("/v1/search?name=London"))
        val forecastRequest = server.takeRequest()
        assertTrue(forecastRequest.path!!.startsWith("/v1/forecast?latitude=51.50853&longitude=-0.12574"))
    }

    @Test
    fun `returns CityNotFoundException when geocoding has no results`() = runTest {
        enqueueJson("geocoding_empty.json")

        val error = repository.getWeather("Atlantisxyz").exceptionOrNull()

        assertTrue(error is CityNotFoundException)
        assertEquals(1, server.requestCount) // forecast is never requested
    }

    @Test
    fun `returns HttpException on server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val error = repository.getWeather("London").exceptionOrNull()

        assertTrue(error is HttpException)
        assertEquals(500, (error as HttpException).code())
    }

    @Test
    fun `returns failure on malformed json`() = runTest {
        server.enqueue(MockResponse().setBody("{ not valid json"))

        assertTrue(repository.getWeather("London").isFailure)
    }
}
