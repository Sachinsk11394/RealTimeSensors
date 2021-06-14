package com.sachin.realtimesensors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.socket.emitter.Emitter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class SensorsListViewModel(
    private val sensorsRepository: SensorsActivityRepository,
    private val socket: io.socket.client.Socket
) : ViewModel() {

    var mSensorName = MutableLiveData<String>()
    val mSensorsList = MutableLiveData<ArrayList<String>>()
    val mSensorsConfig: HashMap<String, Pair<Int, Int>> = hashMapOf()
    val mSensorsData = MutableLiveData<HashMap<String, SensorDate>>(hashMapOf())
    private var mSubscribed = false
    val error = MutableLiveData<String>()
    private val onNewMessage = Emitter.Listener { args ->
        val data = args[0] as JSONObject
        var type = ""
        try {
            type = data.optString("type")
        } catch (e: JSONException) {
            error.value = e.message
        }
        if (type == "init") {
            initialiseData(data)
        } else {
            updateData(type, data)
        }
    }

    init {
        socket.on("data", onNewMessage);
        socket.connect()
    }

    private fun initialiseData(data: JSONObject) {
        val recent = data.getJSONArray("recent")
        val recentValue: ArrayList<Pair<Date, Int>> = arrayListOf()
        for (i in 0 until recent.length()) {
            val item = recent.getJSONObject(i)
            recentValue.add(Pair(Date(item.getLong("key") * 1000), item.getInt("val")))
        }
        recentValue.sortBy { pair -> pair.first }
        val minute = data.getJSONArray("minute")
        val minuteValues: ArrayList<Pair<Date, Int>> = arrayListOf()
        for (i in 0 until minute.length()) {
            val item = minute.getJSONObject(i)
            minuteValues.add(Pair(Date(item.getLong("key") * 1000), item.getInt("val")))
        }
        minuteValues.sortBy { pair -> pair.first }
        mSensorsData.value?.set(mSensorName.value!!, SensorDate(recentValue, minuteValues))
        mSensorsData.notifyObserver()
    }

    private fun updateData(type: String, data: JSONObject) {
        val sensorDate = mSensorsData.value?.get(mSensorName.value)
        sensorDate?.let {
            val key = data.getLong("key")
            val value = data.optInt("val")
            if (type == "update") {
                if (data.getString("scale") == "recent") {
                    if (it.mRecentValues.isNotEmpty() && it.mRecentValues.size > 50) it.mRecentValues.removeAt(
                        0
                    )
                    it.mRecentValues.add(Pair(Date(key * 1000), value))
                } else {
                    if (it.mMinuteValues.isNotEmpty() && it.mMinuteValues.size > 20) it.mMinuteValues.removeAt(
                        0
                    )
                    it.mMinuteValues.add(Pair(Date(key * 1000), value))
                }
            }
        }
    }

    override fun onCleared() {
        socket.disconnect();
        super.onCleared()
    }

    fun getSensorsList(): LiveData<ArrayList<String>> {
        viewModelScope.launch {
            when (val sensorsResponse = sensorsRepository.getSensors()) {
                is NetworkResponse.Success -> {
                    mSensorsList.value = sensorsResponse.data as ArrayList<String>
                    getSensorsConfig()
                }
                is NetworkResponse.Failure -> {
                    error.postValue("Invalid user name")
                }
            }
        }
        return mSensorsList
    }

    private suspend fun getSensorsConfig() {
        when (val sensorConfigsResponse = sensorsRepository.getSensorConfigs()) {
            is NetworkResponse.Success -> {
                mSensorsList.value?.forEach { sensor ->
                    val sensorObject = sensorConfigsResponse.data.getJSONObject(sensor)
                    val min = sensorObject.getInt("min")
                    val max = sensorObject.getInt("max")
                    mSensorsConfig[sensor] = Pair(min, max)
                }
                mSensorName.notifyObserver()
            }
            is NetworkResponse.Failure -> {
                error.postValue("Invalid user name")
            }
        }
    }

    fun subscribeSensor(sensorName: String) {
        unSubscribeSensor()
        mSensorName.value = sensorName
        socket.emit("subscribe", sensorName);
        mSubscribed = true

        viewModelScope.launch {
            while (mSubscribed) {
                mSensorsData.notifyObserver()
                delay(2000L)
            }
        }
    }

    private fun unSubscribeSensor() {
        mSensorName.value = ""
        mSensorsData.value?.clear()
        socket.emit("unsubscribe", mSensorName.value);
        mSubscribed = false
    }
}

fun <T> MutableLiveData<T>.notifyObserver() {
    this.postValue(this.value)
}