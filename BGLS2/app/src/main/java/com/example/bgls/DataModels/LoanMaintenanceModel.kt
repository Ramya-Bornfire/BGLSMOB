package com.example.bgls.DataModels

data class LoanMaintenanceModel(
    val sNo: String,
    val loanId: String,
    val loanType: String,
    val loanName: String,
    val mobileNo: String,
    val retailerBranchId: String,
    val status: String,
    val isVerified: Boolean
)
