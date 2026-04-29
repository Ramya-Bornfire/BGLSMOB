package com.example.bgls.DataModels

data class TransactionRecord(
    val sNo: String,
    val flowId: String,
    val flowDate: String,
    val flowCode: String,
    val flowAmount: String,
    val accountNumber: String,
    val accountName: String
)
