package com.example.bgls.DataModels

data class AccountLedgerPostingModel(
    val tranDate: String,
    val tranId: String,
    val partTranId: String,
    val partTranType: String,
    val currency: String,
    val amount: String,
    val acctId: String,
    val acctName: String,
    val tranParticular: String,
    val status: String,
    var isSelected: Boolean = false
)
