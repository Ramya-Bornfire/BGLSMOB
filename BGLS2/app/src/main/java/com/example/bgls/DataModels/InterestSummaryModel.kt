package com.example.bgls.DataModels

data class InterestSummaryModel(
    val loanNo: String,
    val name: String,
    val dateOfLoan: String,
    val loanAmt: String,
    val interestRate: String,
    val liability: String,
    val accruedInterest: String,
    val bookedInterest: String,
    val appliedInterest: String
)
