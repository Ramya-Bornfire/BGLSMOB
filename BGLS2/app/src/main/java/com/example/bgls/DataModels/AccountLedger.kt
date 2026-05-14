package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class ChartAccountDetail(
    @SerializedName("acct_num") val acctNum: String? = null,
    @SerializedName("acct_name") val acctName: String? = null,
    @SerializedName("acct_crncy") val acctCrncy: String? = null,
    @SerializedName("acct_bal") val acctBal: Double? = null,
    @SerializedName("gl_code") val glCode: String? = null,
    @SerializedName("gl_desc") val glDesc: String? = null,
    @SerializedName("glsh_code") val glshCode: String? = null,
    @SerializedName("glsh_desc") val glshDesc: String? = null,
    @SerializedName("ref_crncy") val refCrncy: String? = null,
    @SerializedName("acct_status") val acctStatus: String? = null,
    @SerializedName("entity_flg") val entityFlg: String? = null,
    @SerializedName("classification") val classification: String? = null,
    @SerializedName("entry_user") val entryUser: String? = null,
    @SerializedName("auth_user") val authUser: String? = null,
    @SerializedName("del_flg") val delFlg: String? = null,
    @SerializedName("ref_crncy_bal") val refCrncyBal: Double? = null,
    @SerializedName("modify_user") val modifyUser: String? = null,
    @SerializedName("modify_time") val modifyTime: String? = null,
    @SerializedName("entry_time") val entryTime: String? = null,
    @SerializedName("acct_opn_date") val acctOpnDate: String? = null,
    @SerializedName("acct_cls_date") val acctClsDate: String? = null
)

data class AccountLedgerViewResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("chartaccount") val chartAccount: ChartAccountDetail? = null,
    @SerializedName("dataList") val dataList: List<List<Any?>>? = null,
    @SerializedName("TRANDATE") val tranDate: String? = null
)
