package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

/**
 * Response model for the `api/loanSchedule?formmode=listschedule` endpoint.
 */
data class LoanScheduleListResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("list") val list: List<LoanScheduleListModel>? = null
)

/**
 * Response model for the `api/loanSchedule?formmode=viewloanschedule1` endpoint.
 * Note: backend returns Object[] and List<Object[]> for details and dues.
 */
data class LoanScheduleViewResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("loanDetails") val loanDetails: List<Any?>? = null,
    @SerializedName("dues") val dues: List<List<Any?>>? = null,
    @SerializedName("user") val user: String? = null
)
