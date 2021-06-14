package com.sachin.realtimesensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.socket.client.Socket

class SensorsViewModelFactory(private val repository: SensorsActivityRepository, private val socket: Socket) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensorsListViewModel::class.java)) {
            return SensorsListViewModel(repository, socket) as T
        } else {
            throw IllegalArgumentException("view model not available")
        }
    }
}