package com.example.bgls.DataModels

data class AccountBalanceLeaseModel(
    val srlNo: Int,
    val customerId: String,
    val accountId: String,
    val accountName: String,
    val dateOfLoan: String,
    val loanAmount: String,
    val disbursedAmount: String
)
