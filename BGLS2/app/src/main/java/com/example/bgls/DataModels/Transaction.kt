package com.example.bgls.DataModels

import com.example.bgls.ParameterActivity


data class Transaction(
    val name: String,
    val subItems: List<String> = emptyList()
)

data class TransactionAccountsResponse(
    val list: List<ParameterActivity.TransactionAccountItem>? = null
)