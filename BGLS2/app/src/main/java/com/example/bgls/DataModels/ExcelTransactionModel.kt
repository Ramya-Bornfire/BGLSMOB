package com.example.bgls.DataModels

data class ExcelTransactionModel(
    val tranId: String,
    val names: String,
    val reference: String,
    val mobileNumber: String,
    val amount: String,
    val allocatedAmount: String,
    val transTime: String,
    val status: String,
    var isAccountsSelected: Boolean = false,
    var isValuesSelected: Boolean = false
)
