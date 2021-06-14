package com.sachin.realtimesensors

import kotlin.String

class BaseUrl(val value: String) {

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is String -> {
                value == other
            }
            is BaseUrl -> {
                value == other.value
            }
            else -> super.equals(other)
        }
    }
}