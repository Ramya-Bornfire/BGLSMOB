package com.example.bgls.DataModels

import com.example.bgls.ParameterActivity
import com.google.gson.annotations.SerializedName


data class Transaction(
    val name: String,
    val subItems: List<String> = emptyList()
)

data class TransactionAccountsResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("list") val list: List<TransactionItem>? = null
)

data class TransactionItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("event") val event: String? = null,
    @SerializedName("debitAccountNumber") val debitAccountNumber: String? = null,
    @SerializedName("debitAccountName") val debitAccountName: String? = null,
    @SerializedName("creditAccountNumber") val creditAccountNumber: String? = null,
    @SerializedName("creditAccountName") val creditAccountName: String? = null,
    @SerializedName("tranParticular") val tranParticular: String? = null,
    @SerializedName("accountType") val accountType: String? = null
)
