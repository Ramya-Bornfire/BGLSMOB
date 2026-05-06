package com.example.bgls.DataModels

data class ProfitAndLossAccountResponse(
    val balancesheet3: List<ChartAccountApiItem>?,
    val balancesheet4: List<ChartAccountApiItem>?,
    val formmode: String?
)

data class IncomeExpenditureResponse(
    val msg: List<DABItem>?,
    val msg1: List<DABItem>?
)

data class DABItem(
    val glsh_code: String?,
    val glsh_desc: String?,
    val acct_num: String?,
    val acct_name: String?,
    val acct_crncy: String?,
    val tran_date_bal: Double?
)
