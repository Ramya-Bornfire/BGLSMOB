package com.example.bgls.DataModels

data class ApprovalModel(
    val slNo: Int,
    val custGroup: String,
    val appRefNo: String,
    val accountType: String,
    val customerName: String,
    val nationalId: String,
    val status: String,
    var isSelected: Boolean = false
)
