package com.example.bgls.DataModels

data class TabLedgerModel(
    val head: String,
    val acctId: String,
    val acctName: String,
    val currency: String,
    val credits: String,
    val debits: String,
    val balance: String,
    val status: String
)
