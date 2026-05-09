package com.example.bgls.Retrofit

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class GsonStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(`in`: JsonReader): String? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val value = `in`.nextString()
        return formatIfDate(value)
    }

    private fun formatIfDate(value: String): String {
        try {
            // Match yyyy-MM-dd...
            val isoRegex = Regex("^(\\d{4})-(\\d{2})-(\\d{2})(?:T|\\s|$)")
            val isoMatch = isoRegex.find(value)
            if (isoMatch != null) {
                val year = isoMatch.groupValues[1]
                val month = isoMatch.groupValues[2]
                val day = isoMatch.groupValues[3]
                if (year.toInt() in 1900..2100) {
                    return "$day-$month-$year"
                }
            }

            // Match yyyy/MM/dd...
            val isoSlashRegex = Regex("^(\\d{4})/(\\d{2})/(\\d{2})(?:T|\\s|$)")
            val isoSlashMatch = isoSlashRegex.find(value)
            if (isoSlashMatch != null) {
                val year = isoSlashMatch.groupValues[1]
                val month = isoSlashMatch.groupValues[2]
                val day = isoSlashMatch.groupValues[3]
                if (year.toInt() in 1900..2100) {
                    return "$day-$month-$year"
                }
            }
        } catch (e: Exception) {
            // Ignore format exceptions
        }
        return value
    }
}
