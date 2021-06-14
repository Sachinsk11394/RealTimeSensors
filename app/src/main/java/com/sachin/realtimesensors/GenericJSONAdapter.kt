package com.sachin.realtimesensors

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import org.json.JSONObject

class JsonAdapter : JsonAdapter<JSONObject>() {

    override fun fromJson(reader: JsonReader): JSONObject {
        return JSONObject(reader.readJsonValue() as Map<*, *>)
    }

    override fun toJson(writer: JsonWriter, value: JSONObject?) {
        // Empty Implementation
    }
}