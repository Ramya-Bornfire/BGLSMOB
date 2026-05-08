package com.example.bgls.DataModels

// CreditFacilityResponse.kt
data class CreditFacilityResponse(
    val formmode: String?,
    val loanvalues: List<List<Any?>>?,   // [accountNo, accountName]
    val loanDetails: List<List<Any?>>?   // [accountNo, accountName]
)
