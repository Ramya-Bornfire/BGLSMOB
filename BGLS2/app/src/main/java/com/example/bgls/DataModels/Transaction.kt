package com.example.bgls.DataModels

import com.example.bgls.ParameterActivity
import com.google.gson.annotations.SerializedName


data class Transaction(
    val name: String,
    val subItems: List<String> = emptyList()
)

data class TransactionAccountsResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("list") val list: List<TransactionItem>? = null
)

data class TransactionItem(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("glCode") val glCode: String? = null,
    @SerializedName("glDesc") val glDesc: String? = null,
    @SerializedName("glshCode") val glshCode: String? = null,
    @SerializedName("glshDesc") val glshDesc: String? = null,
    @SerializedName("schmCode") val schmCode: String? = null,
    @SerializedName("schmDesc") val schmDesc: String? = null,
    @SerializedName("productKey") val productKey: String? = null,
    @SerializedName("collectionAccount") val collectionAccount: String? = null,
    @SerializedName("interestIncome") val interestIncome: String? = null,
    // Legacy BGLS_ACCOUNTS_TBL fields (kept for backward compatibility)
    @SerializedName("event") val event: String? = null,
    @SerializedName("debitAccountNumber") val debitAccountNumber: String? = null,
    @SerializedName("debitAccountName") val debitAccountName: String? = null,
    @SerializedName("creditAccountNumber") val creditAccountNumber: String? = null,
    @SerializedName("creditAccountName") val creditAccountName: String? = null,
    @SerializedName("tranParticular") val tranParticular: String? = null,
    @SerializedName("accountType") val accountType: String? = null
)


data class TransactionDto(
    @SerializedName("tran_id") val tranId: String?,
    @SerializedName("flow_date") val flowDate: String?,
    @SerializedName("flow_code") val flowCode: String?,
    @SerializedName("tran_amt") val tranAmt: Double?,
    @SerializedName("acct_num") val acctNum: String?,
    @SerializedName("acct_name") val acctName: String?
)

data class BookingDto(
    @SerializedName("id") val id: String?,
    @SerializedName("disbursement_date") val disbursementDate: String?,
    @SerializedName("account_holdertype") val accountHolderType: String?,
    @SerializedName("loan_amount") val loanAmount: Double?,
    @SerializedName("loan_name") val loanName: String?,
    @SerializedName("employer_name") val employerName: String?
)

data class TransactionMigrationResponse(
    val disbursement: List<TransactionDto>?,
    val interest: List<TransactionDto>?,
    val fees: List<TransactionDto>?,
    val penalty: List<TransactionDto>?,
    val recovery: List<TransactionDto>?,
    val booking: List<BookingDto>?
)
// TransactionRequest.kt – used for add / modify
data class TransactionRequest(
    val tran_id: String,
    val part_tran_id: String,
    val acct_num: String,
    val acct_name: String,
    val tran_type: String,
    val part_tran_type: String,    // "Debit" or "Credit"
    val acct_crncy: String,
    val tran_amt: Double,
    val tran_particular: String,
    val tran_remarks: String,
    val flow_code: String?,
    val flow_date: String?,        // format "dd-MM-yyyy"
    val tran_date: String?,
    val value_date: String?,
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
    val entry_user: String?,
    val post_user: String?,
    val entry_time: String?,
    val post_time: String?,
    val tran_status: String,
    val del_flg: String,
    val srl_no: String?
)

data class TransactionPointingEntity(
    @SerializedName("org_tran_date") val orgTranDate: String?,
    @SerializedName("org_tran_id") val orgTranId: String?,
    @SerializedName("org_part_tran_id") val orgPartTranId: String?,
    @SerializedName("org_tran_ref_no") val orgTranRefNo: String?,
    @SerializedName("org_tran_amt") val orgTranAmt: Double?,
    @SerializedName("bal_outstd_amt") val balOutstdAmt: Double?,
    @SerializedName("org_add_details") val orgAddDetails: String?
)