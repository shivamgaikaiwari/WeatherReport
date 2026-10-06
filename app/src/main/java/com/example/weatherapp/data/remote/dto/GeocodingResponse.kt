package com.example.weatherapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/** JSON returned by https://geocoding-api.open-meteo.com/v1/search */
data class GeocodingResponse(
    @SerializedName("results") val results: List<LocationDto>? = null
)

data class LocationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String? = null,
    @SerializedName("admin1") val region: String? = null,
    @SerializedName("timezone") val timezone: String? = null
)
