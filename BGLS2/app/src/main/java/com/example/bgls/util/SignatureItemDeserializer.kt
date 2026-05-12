package com.example.bgls.util

import com.example.bgls.DataModels.SignatureItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class SignatureItemDeserializer : JsonDeserializer<SignatureItem> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): SignatureItem {
        val obj = json.asJsonObject
        val keyword = if (obj.has("keyword")) obj.get("keyword").asString else null
        // The problematic 'sign' field is ignored completely
        return SignatureItem(keyword = keyword)
    }
}