package com.example.bgls.DataModels

// DataModels/JournalEntryResponse.kt
data class JournalEntryViewResponse(
    val formmode: String? = null,
    val ledgervalues: JournalEntryItem? = null,
    val currentPartTran: Int? = null,
    val maxPartTran: Int? = null,
    val gldetails: Any? = null,
    val jour: List<JournalEntryItem>? = null,
    val tableparttran: List<JournalEntryItem>? = null
)

data class JournalEntryDetailModel(
    val tranId: String,
    val partTranId: String,
    val acctId: String,
    val acctName: String,
    val tranType: String,
    val partTranType: String,
    val currency: String,
    val amount: String,
    val particulars: String,
    val remarks: String,
    val flowCode: String,
    val flowDate: String,
    val tranDate: String,
    val valueDate: String,
    val entryUser: String,
    val tranStatus: String,
    val deleted: String
)
data class JournalEntryItem(
    val tran_id: String?,
    val part_tran_id: Int?,
    val acct_num: String?,
    val acct_name: String?,
    val tran_type: String?,
    val part_tran_type: String?,
    val acct_crncy: String?,
    val tran_amt: Double?,
    val tran_particular: String?,
    val tran_remarks: String?,
    val flow_code: String?,
    val flow_date: String?,
    val tran_date: String?,
    val value_date: String?,
    val entry_user: String?,
    val post_user: String?,
    val tran_status: String?,
    val del_flg: String?,
    val tran_code: String?,
    val tran_rpt_code: String?,
    val tran_ref_no: String?,
    val add_details: String?,
    val partition_type: String?,
    val partition_det: String?,
    val instr_num: String?,
    val instr_date: String?,
    val ref_crncy: String?,
    val ref_crncy_amt: Double?,
    val rate_code: String?,
    val rate: Double?,
    val modify_user: String?,
    val modify_time: String?,
    val srl_no: String?,
    val entry_time: String? = null,
    val post_time: String? = null
)
data class TransactionDetailsResponse(
    val ledgervalues: JournalEntryItem?
)
data class JournalEntryListResponse(
    val tran_date: String?,
    val tran_id: String,
    val part_tran_id: Int,
    val part_tran_type: String,
    val acct_crncy: String,
    val tran_amt: Double,
    val acct_num: String,
    val acct_name: String,
    val tran_particular: String,
    val tran_status: String
)
data class JournalEntryAddScreenResponse(
    val plusonetran2: String,
    val partTranId: String,
    val user: String,
    val tranStatus: String,
    val currentDate: String,  // date as string, format dd-MM-yyyy
    val popup: List<ChartAccountItem>, // for account search modal
    // other fields if needed
)