package com.sachin.realtimesensors

import java.util.*
import kotlin.collections.ArrayList

data class SensorDate(
    val mRecentValues: ArrayList<Pair<Date, Int>> = arrayListOf(),
    val mMinuteValues: ArrayList<Pair<Date, Int>> = arrayListOf(),
)
