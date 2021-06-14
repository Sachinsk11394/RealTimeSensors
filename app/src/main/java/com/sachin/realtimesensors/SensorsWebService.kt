package com.sachin.realtimesensors

import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET
import kotlin.String

interface SensorsWebService {
    @GET("sensornames")
    suspend fun getSensors(): Response<List<String>>

    @GET("config")
    suspend fun getSensorConfigs(): Response<JSONObject>
}