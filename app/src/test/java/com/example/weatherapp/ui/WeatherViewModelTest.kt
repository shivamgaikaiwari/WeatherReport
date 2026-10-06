package com.example.weatherapp.ui

import com.example.weatherapp.data.FakeWeatherRepository
import com.example.weatherapp.data.repository.CityNotFoundException
import com.example.weatherapp.util.MainDispatcherRule
import com.example.weatherapp.util.TestData
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeWeatherRepository
    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setUp() {
        repository = FakeWeatherRepository()
        viewModel = WeatherViewModel(repository)
    }

    @Test
    fun `initial state is Idle with empty query`() {
        assertEquals(WeatherUiState.Idle, viewModel.uiState.value)
        assertEquals("", viewModel.query.value)
    }

    @Test
    fun `onQueryChange updates query`() {
        viewModel.onQueryChange("Paris")
        assertEquals("Paris", viewModel.query.value)
    }

    @Test
    fun `blank query shows error without calling repository`() = runTest {
        viewModel.onQueryChange("   ")
        viewModel.search()
        advanceUntilIdle()

        assertEquals(WeatherUiState.Error(WeatherViewModel.EMPTY_QUERY_MESSAGE), viewModel.uiState.value)
        assertTrue(repository.requestedCities.isEmpty())
    }

    @Test
    fun `successful search emits Loading then Success`() = runTest {
        repository.result = Result.success(TestData.londonReport)
        viewModel.onQueryChange("  London ")

        viewModel.search()
        advanceUntilIdle()

        assertEquals(WeatherUiState.Success(TestData.londonReport), viewModel.uiState.value)
        assertEquals(listOf("London"), repository.requestedCities)
    }

    @Test
    fun `state is Loading while request is in flight`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        repository.result = Result.success(TestData.londonReport)
        viewModel.onQueryChange("London")

        viewModel.search()
        runCurrent() // start the coroutine; it suspends on the gate

        assertEquals(WeatherUiState.Loading, viewModel.uiState.value)

        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun `city not found maps to friendly error`() = runTest {
        repository.result = Result.failure(CityNotFoundException("Nowhere"))
        viewModel.onQueryChange("Nowhere")

        viewModel.search()
        advanceUntilIdle()

        assertEquals(WeatherUiState.Error("No results found for \"Nowhere\""), viewModel.uiState.value)
    }

    @Test
    fun `network failure maps to network error message`() = runTest {
        repository.result = Result.failure(IOException("timeout"))
        viewModel.onQueryChange("London")

        viewModel.search()
        advanceUntilIdle()

        assertEquals(WeatherUiState.Error(WeatherViewModel.NETWORK_ERROR_MESSAGE), viewModel.uiState.value)
    }
}
