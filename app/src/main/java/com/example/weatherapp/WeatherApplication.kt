package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.di.AppContainer

class WeatherApplication : Application() {
    val container: AppContainer by lazy { AppContainer() }
}
