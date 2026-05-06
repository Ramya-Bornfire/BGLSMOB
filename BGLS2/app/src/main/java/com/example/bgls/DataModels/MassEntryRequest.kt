package com.example.bgls.DataModels


data class MassEntryRequest(
    val tran_id: String,
    val part_tran_id: String,
    val acct_num: String,
    val acct_name: String,
    val part_tran_type: String,   // "Credit" or "Debit"
    val tran_amt: Double,
    val tran_particular: String,
    val tran_remarks: String,
    val rate_code: String?,
    val rate: Double?,
    val add_details: String?
)