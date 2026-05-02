package com.example.bgls.DataModels

data class SchemeCode(
    val product: String? = null,
    val id: String? = null,
    val category: String? = null,
    val type: String? = null,
    val description: String? = null,
    val status: String? = null,
    val state: String? = null,
    val productType: String? = null,
    val productCategory: String? = null,
    val productDescription: String? = null,
    val availableTo: String? = null,
    val branches: String? = null,
    val entity_flg: String? = null,
    val del_flg: String? = null,
    val modify_flg: String? = null
)

data class SchemeResponse(
    val formmode: String? = null,
    val lms_schemes: List<SchemeCode>? = null
)
