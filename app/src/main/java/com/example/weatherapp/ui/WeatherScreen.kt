package com.example.weatherapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.data.model.DailyForecast
import com.example.weatherapp.data.model.WeatherReport
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(topBar = { TopAppBar(title = { Text("Weather Report") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = {
                    focusManager.clearFocus()
                    viewModel.search()
                }
            )
            Spacer(Modifier.height(16.dp))

            when (val state = uiState) {
                WeatherUiState.Idle -> CenteredMessage("Search for a city to see its weather")
                WeatherUiState.Loading -> Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
                is WeatherUiState.Error -> CenteredMessage(state.message, isError = true)
                is WeatherUiState.Success -> WeatherContent(state.report)
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onSearch: () -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Enter city name, e.g. London") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() })
    )
}

@Composable
private fun CenteredMessage(text: String, isError: Boolean = false) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeatherContent(report: WeatherReport) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { CurrentWeatherCard(report) }
        item {
            Text("7-day forecast", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(report.daily, key = { it.date }) { day -> DailyRow(day) }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CurrentWeatherCard(report: WeatherReport) {
    val current = report.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(report.cityName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val subtitle = listOfNotNull(report.region, report.country).joinToString(", ")
            if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodyMedium)

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Text(current.condition.icon, fontSize = 48.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${current.temperatureC.roundToInt()}°F", fontSize = 44.sp, fontWeight = FontWeight.Light)
                    Text(current.condition.description, style = MaterialTheme.typography.titleMedium)
                }
            }
            HorizontalDivider()
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Stat("Feels like", "${current.feelsLikeC.roundToInt()}°F")
                Stat("Humidity", "${current.humidityPercent}%")
                Stat("Wind", "${current.windSpeedKmh.roundToInt()} mph")
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DailyRow(day: DailyForecast) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(dayLabel(day.date), modifier = Modifier.width(56.dp), fontWeight = FontWeight.Medium)
            Text(day.condition.icon, fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
            Text(day.condition.description, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("${day.maxC.roundToInt()}° / ${day.minC.roundToInt()}°", fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun dayLabel(isoDate: String): String = runCatching {
    LocalDate.parse(isoDate).dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}.getOrDefault(isoDate)
