package com.sachin.realtimesensors

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.String

open class SensorsActivityRepository(private val webService: SensorsWebService,
                                     private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) {
    suspend fun getSensors(): NetworkResponse<List<String>> {
        return withContext(ioDispatcher){
            val response = webService.getSensors()
            if (response.isSuccessful) {
                return@withContext NetworkResponse.success(response.body()!!)
            } else {
                return@withContext NetworkResponse.failure<List<String>>(Throwable("Something went wrong, Please try again later."))
            }
        }
    }

    suspend fun getSensorConfigs(): NetworkResponse<JSONObject> {
        return withContext(ioDispatcher){
            val response = webService.getSensorConfigs()
            if (response.isSuccessful) {
                return@withContext NetworkResponse.success(response.body()!!)
            } else {
                return@withContext NetworkResponse.failure<JSONObject>(Throwable("Something went wrong, Please try again later."))
            }
        }
    }
}