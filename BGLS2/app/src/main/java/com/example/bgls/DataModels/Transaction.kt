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
    @SerializedName("id") val id: String? = null,
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