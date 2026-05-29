package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class CalendarApiModel(
    val year: Int?,
    val month: String?
)

data class HolidayApiModel(
    val year: String?,
    val month: String?,
    val date: String?,
    val description: String?,
    val remarks: String?
)

data class CalendarResponse(
    val calender_list: List<CalendarApiModel> = emptyList(),
    val holidays_list: List<HolidayApiModel> = emptyList()
)

data class AddHolidayMasterRequest(
    @SerializedName("orgn")
    val orgn: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("cal_year")
    val calYear: String,
    @SerializedName("cal_month")
    val calMonth: String,
    @SerializedName("record_date")
    val recordDate: String,
    @SerializedName("holiday_desc")
    val holidayDesc: String,
    @SerializedName("holiday_remarks")
    val holidayRemarks: String,
    @SerializedName("holiday_flg")
    val holidayFlg: String
)

data class HolidayMasterListResponse(
    @SerializedName("Listofvalues")
    val listOfValues: List<HolidayMasterListItem> = emptyList()
)

data class HolidayMasterListItem(
    @SerializedName("cal_year")
    val calYear: String? = null,
    @SerializedName("cal_month")
    val calMonth: String? = null,
    @SerializedName("record_date")
    val recordDate: String? = null,
    @SerializedName("holiday_desc")
    val holidayDesc: String? = null,
    @SerializedName("holiday_remarks")
    val holidayRemarks: String? = null
)

