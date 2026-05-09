package com.example.bgls.Retrofit

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class ResponseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isSuccessful) {
            val body = response.body
            if (body != null) {
                val contentType = body.contentType()
                if (contentType?.subtype == "json") {
                    val jsonString = body.string()
                    val processedJson = processJson(jsonString)
                    return response.newBuilder()
                        .body(processedJson.toResponseBody(contentType))
                        .build()
                }
            }
        }
        return response
    }

    private fun processJson(json: String): String {
        return try {
            val tokener = JSONTokener(json)
            val value = tokener.nextValue()
            val processedValue = processElement(value)
            processedValue.toString()
        } catch (e: JSONException) {
            json
        }
    }

    private fun processElement(element: Any?): Any? {
        return when (element) {
            is JSONObject -> {
                val newObj = JSONObject()
                val keys = element.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    newObj.put(key, processElement(element.get(key)))
                }
                newObj
            }
            is JSONArray -> {
                val newArr = JSONArray()
                for (i in 0 until element.length()) {
                    newArr.put(processElement(element.get(i)))
                }
                newArr
            }
            is String -> {
                formatIfDate(element)
            }
            else -> element
        }
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

            // Match dd-MM-yyyy...
            val reverseRegex = Regex("^(\\d{2})-(\\d{2})-(\\d{4})(?:T|\\s|$)")
            val reverseMatch = reverseRegex.find(value)
            if (reverseMatch != null) {
                val day = reverseMatch.groupValues[1]
                val month = reverseMatch.groupValues[2]
                val year = reverseMatch.groupValues[3]
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

            // Match dd/MM/yyyy...
            val slashRegex = Regex("^(\\d{2})/(\\d{2})/(\\d{4})(?:T|\\s|$)")
            val slashMatch = slashRegex.find(value)
            if (slashMatch != null) {
                val day = slashMatch.groupValues[1]
                val month = slashMatch.groupValues[2]
                val year = slashMatch.groupValues[3]
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
