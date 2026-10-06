package com.example.weatherapp.di

import com.example.weatherapp.data.remote.GeocodingApi
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.repository.WeatherRepository
import com.example.weatherapp.data.repository.WeatherRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Simple manual dependency injection (swap for Hilt/Koin in larger apps). */
class AppContainer {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(
            geocodingApi = retrofit(GeocodingApi.BASE_URL).create(GeocodingApi::class.java),
            weatherApi = retrofit(WeatherApi.BASE_URL).create(WeatherApi::class.java)
        )
    }
}
