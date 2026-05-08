package com.example.bgls.DataModels

data class WalletAccountModel(
    val category: String,
    val custId: String,
    val accNo: String,
    val name: String,
    val openDate: String,
    val closeDate: String,
    val currency: String,
    val balance: String,
    val status: String,
    var isSelected: Boolean = false
)
