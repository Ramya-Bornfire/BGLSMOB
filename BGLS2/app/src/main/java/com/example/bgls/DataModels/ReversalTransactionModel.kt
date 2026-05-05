package com.example.bgls.DataModels

data class ReversalTransactionModel(
    val tranDate: String,
    val tranId: String,
    val paTranTy: String,
    val currency: String,
    val amount: String,
    val acctId: String,
    val acctName: String,
    val tranParticular: String,
    val status: String
)
