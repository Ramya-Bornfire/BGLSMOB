package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName


data class UserActivityItem(
    val audit_date: String?,           // received as "dd-MM-yyyy"
    val audit_table: String?,
    val func_code: String?,
    val entry_user: String?,
    val entry_time: String?,            // "HH:mm a"
    val auth_user: String?,
    val auth_time: String?,
    val remarks: String?
)

data class BusinessActivityItem(
    val audit_date: String?,
    val audit_table: String?,
    val func_code: String?,
    val entry_user: String?,
    val entry_time: String?,
    val auth_user: String?,
    val auth_time: String?,
    val fieldName: List<String>?,
    val oldvalue: List<String>?,
    val newvalue: List<String>?,
    val remarks: String?
)
data class BusinessActivityResponse(
    @SerializedName("AuditList") val auditList: List<BusinessActivityItem>?,
    @SerializedName("formmode") val formmode: String?,
    @SerializedName("Fromdate") val fromDate: String?,
    @SerializedName("menuname") val menuName: String?,
    @SerializedName("auditflag") val auditFlag: String?
)