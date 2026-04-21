package com.example.bgls.DataModels


data class Transaction(
    val name: String,
    val subItems: List<String> = emptyList()
)
