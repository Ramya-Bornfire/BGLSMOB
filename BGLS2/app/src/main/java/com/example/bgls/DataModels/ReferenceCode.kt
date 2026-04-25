package com.example.bgls.DataModels


data class ReferenceCode(
    val ref_type: String,
    val ref_type_desc: String,
    val ref_id: String,
    val ref_id_desc: String,
    val module_id: String
)
data class RefResponse(
    val formmode: String,
    val refList: List<ReferenceCode>?,
    val refType: List<ReferenceCode>?
)
