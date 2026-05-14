package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

/**
 * Response model for accountLedger2 API with formmode=list.
 * Returns a list of chart accounts for the Account Ledger list screen.
 */
data class AccountLedgerListResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("chartaccount") val chartaccount: List<ChartAccountItem>? = null,
    @SerializedName("TRANDATE") val tranDate: String? = null
)
