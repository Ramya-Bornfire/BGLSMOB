package com.example.bgls.DataModels

data class LoanSchedule(
    val dueDate: String,
    val principalExpenses: String,
    val interestExpenses: String,
    val feeExpenses: String,
    val penaltyExpenses: String,
    val repaidDate: String,
    val principalPaid: String,
    val interestPaid: String,
    val feePaid: String,
    val penaltyPaid: String,
    val totalDues: String
)
