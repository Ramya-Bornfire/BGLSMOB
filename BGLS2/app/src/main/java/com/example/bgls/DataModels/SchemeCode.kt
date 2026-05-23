package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class SchemeCode(
    @SerializedName("product") val product: String? = null,
    @SerializedName("id") val id: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("productType") val productType: String? = null,
    @SerializedName("productCategory") val productCategory: String? = null,
    @SerializedName("productDescription") val productDescription: String? = null,
    @SerializedName("availableTo") val availableTo: String? = null,
    @SerializedName("branches") val branches: String? = null,
    @SerializedName("entityFlg") val entityFlg: String? = null,
    @SerializedName("delFlg") val delFlg: String? = null,
    @SerializedName("modifyFlg") val modifyFlg: String? = null,
    val category: String? = null,
    val type: String? = null,
    val description: String? = null
)

data class SchemeResponse(
    val formmode: String? = null,
    val lms_schemes: List<SchemeCode>? = null
)
