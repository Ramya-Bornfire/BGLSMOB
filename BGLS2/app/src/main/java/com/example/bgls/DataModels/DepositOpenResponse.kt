package com.example.bgls.DataModels

data class DepositOpenResponse(
    val formmode: String?,
    val deposit: List<String>?,          // customer IDs
    val getdata: List<DepositEntity>?,   // existing deposits (for list)
    val depoActNo: String?               // newly generated account number
)

data class DepositEntity(
    val cust_id: String?,
    val cust_name: String?,
    val depo_actno: String?,
    val deposit_type: String?,
    val deposit_amt: Double?,
    val deposit_period: String?,
    val rate_of_int: String?,
    val frequency: String?,
    val deposit_date: String?,
    val maturity_date: String?,
    val int_amt: Double?,
    val maturity_amt: String?,
    val branch_id: String?,
    val branch_desc: String?,
    val scheme_code: String?,
    val glsh_code: String?,
    val glsh_desc: String?,
    val currency: String?,
    val entity_flg: String?
)

data class DepositFlowItem(
    val acid: String?,
    val flow_code: String?,
    val flow_amt: Double?,
    val clr_bal_amt: Double?,
    val flow_date: String?,
    val srl_no: String?,
    val flow_id: String?
)
