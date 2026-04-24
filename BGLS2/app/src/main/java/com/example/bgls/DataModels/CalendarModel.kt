package com.example.bgls.DataModels

data class CalendarModel(
    val year: String,
    val month: String,
    var isSelected: Boolean = false
)