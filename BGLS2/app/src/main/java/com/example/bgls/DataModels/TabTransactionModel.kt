package com.example.bgls.DataModels

data class TabTransactionModel(
    val id: String,
    val event: String,
    val debitAccNo: String,
    val debitAccName: String,
    val creditAccNo: String,
    val creditAccName: String,
    val tranParticular: String,
    val type: String
)
