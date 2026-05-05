package com.example.bgls.DataModels

data class DepositAccountModel(
    val custId: String,
    val custName: String,
    val actNo: String,
    val dateOfDeposit: String,
    val depositAmount: String,
    val status: String
)
