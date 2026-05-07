package com.example.bgls.DataModels

data class AccountBalanceDepositModel(
    val srlNo: Int,
    val customerId: String,
    val accountId: String,
    val accountName: String,
    val dateOfPeriod: String,
    val depositAmount: String,
    val period: String,
    val rateOfInterest: String,
    val maturityDate: String,
    val accountBalance: String
)
