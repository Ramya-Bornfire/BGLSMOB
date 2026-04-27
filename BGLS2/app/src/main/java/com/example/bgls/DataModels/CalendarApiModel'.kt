package com.example.bgls.DataModels

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

