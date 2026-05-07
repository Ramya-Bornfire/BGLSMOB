package com.example.bgls.DataModels

data class LoanScheduleModel(
    val srlNo: String,
    val date: String,
    val description: String,
    val installmentAmt: String,
    val principalAmt: String,
    val interestAmt: String,
    val chargesAmt: String,
    val principalOutstanding: String
)
