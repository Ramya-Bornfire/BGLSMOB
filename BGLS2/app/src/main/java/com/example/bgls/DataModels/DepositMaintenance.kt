package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class DepositMaintenanceResponse(
    @SerializedName("formmode") val formmode: String?,
    @SerializedName("getdata") val getdata: List<DepositAccountDetail>?,
    @SerializedName("customerdata") val customerdata: DepositAccountDetail?,
    @SerializedName("listact") val listact: List<DepositFlow>?,
    @SerializedName("listacts") val listacts: List<List<Any>>?
)

data class DepositAccountDetail(
    @SerializedName("branch_id") val branch_id: String?,
    @SerializedName("branch_desc") val branch_desc: String?,
    @SerializedName("cust_id") val cust_id: String?,
    @SerializedName("cust_name") val cust_name: String?,
    @SerializedName("deposit_type") val deposit_type: String?,
    @SerializedName("scheme_code") val scheme_code: String?,
    @SerializedName("glsh_code") val glsh_code: String?,
    @SerializedName("glsh_desc") val glsh_desc: String?,
    @SerializedName("depo_actno") val depo_actno: String?,
    @SerializedName("deposit_date") val deposit_date: String?,
    @SerializedName("deposit_period") val deposit_period: String?,
    @SerializedName("deposit_amt") val deposit_amt: Double?,
    @SerializedName("rate_of_int") val rate_of_int: String?,
    @SerializedName("int_amt") val int_amt: Double?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("currency") val currency: String?,
    @SerializedName("maturity_date") val maturity_date: String?,
    @SerializedName("maturity_amt") val maturity_amt: Any?, // Can be String or Double in JSON
    @SerializedName("entity_flg") val entity_flg: String?,
    @SerializedName("verify_flg") val verify_flg: String?,
    @SerializedName("modify_flg") val modify_flg: String?,
    @SerializedName("del_flg") val del_flg: String?
)

data class DepositFlow(
    @SerializedName("flow_id") val flow_id: String?,
    @SerializedName("flow_code") val flow_code: String?,
    @SerializedName("flow_date") val flow_date: String?,
    @SerializedName("flow_amt") val flow_amt: Double?,
    @SerializedName("clr_bal_amt") val clr_bal_amt: Double?
)
