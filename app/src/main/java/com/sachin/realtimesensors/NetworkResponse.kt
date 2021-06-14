package com.sachin.realtimesensors

sealed class NetworkResponse<T> {
    data class Success<T>(var data: T) : NetworkResponse<T>()
    data class Failure<T>(val e: Throwable) : NetworkResponse<T>()

    companion object {
        fun <T> success(data: T): NetworkResponse<T> = Success(data)
        fun <T> failure(e: Throwable): NetworkResponse<T> = Failure(e)
    }
}