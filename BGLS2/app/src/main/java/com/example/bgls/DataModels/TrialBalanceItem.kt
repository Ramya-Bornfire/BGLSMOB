package com.example.bgls.DataModels

data class TrialBalanceItem(
    val glCode: String,
    val acctName: String,
    val openingBal: Double,
    val credit: Double,
    val debit: Double,
    val netChange: Double,
    val closingBal: Double
)
