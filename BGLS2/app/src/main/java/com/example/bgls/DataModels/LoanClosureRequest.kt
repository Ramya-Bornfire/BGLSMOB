package com.example.bgls.DataModels

data class LoanPreClosureRequest(
    val list: List<Map<String, Any>>,
    val acct_num: String,
    val total_flow_amt_db: String
)

data class LoanClosureRequest(
    val list: List<Map<String, Any>>,
    val acct_num: String,
    val total_flow_amt_db: String
)
