package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class LoanScheduleListModel(
    val sno: String? = null,
    @SerializedName("loan_name") val loanName: String? = null,
    @SerializedName("id") val loanId: String? = null,
    @SerializedName("retailer_name") val retailerName: String? = null,
    @SerializedName("retailer_branch") val retailerBranchId: String? = null,
    @SerializedName("account_state") val status: String? = null,
    @SerializedName("account_holderkey") val accountHolderKey: String? = null,
    @SerializedName("encoded_key") val encodedKey: String? = null
)
