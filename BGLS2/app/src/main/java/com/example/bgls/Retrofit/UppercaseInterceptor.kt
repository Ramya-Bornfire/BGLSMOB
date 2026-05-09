package com.example.bgls.Retrofit

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

class UppercaseInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.isSuccessful) {
            val body = response.body
            if (body != null) {
                val contentType = body.contentType()
                if (contentType?.subtype == "json") {
                    val jsonString = body.string()
                    val uppercasedJson = uppercaseJson(jsonString)
                    return response.newBuilder()
                        .body(uppercasedJson.toResponseBody(contentType))
                        .build()
                }
            }
        }
        return response
    }

    private fun uppercaseJson(json: String): String {
        return try {
            val tokener = JSONTokener(json)
            val value = tokener.nextValue()
            val uppercasedValue = uppercaseElement(value)
            uppercasedValue.toString()
        } catch (e: JSONException) {
            json
        }
    }

    private fun uppercaseElement(element: Any?): Any? {
        return when (element) {
            is JSONObject -> {
                val newObj = JSONObject()
                val keys = element.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    newObj.put(key, uppercaseElement(element.get(key)))
                }
                newObj
            }
            is JSONArray -> {
                val newArr = JSONArray()
                for (i in 0 until element.length()) {
                    newArr.put(uppercaseElement(element.get(i)))
                }
                newArr
            }
            is String -> {
                element.uppercase(java.util.Locale.getDefault())
            }
            else -> element
        }
    }
}
