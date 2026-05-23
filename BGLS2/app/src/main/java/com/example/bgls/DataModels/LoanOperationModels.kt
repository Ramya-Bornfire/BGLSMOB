package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class LoanOperationResponse(
    val formmode: String?,
    val TRANDATE: String?,
    val collection: List<List<Any>>?,
    val getlist: List<Any>?,
    val tranData: List<Any>?,
    val tranId: String?
)

data class MultipleTransactionRequest(
    @SerializedName("acct_namedata") val acctNamedata: String,
    @SerializedName("tran_id") val tranId: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("tran_particulardata") val tranParticulardata: String,
    @SerializedName("acct_num") val acctNum: String,
    @SerializedName("tran_remarks") val tranRemarks: String?,
    @SerializedName("globalAuthUser") val globalAuthUser: String?
)

data class SettlementRecord(
    @SerializedName("acct_num") val acctNum: String,
    val allocation: Double,
)

data class LoanFlowDetail(
    @SerializedName("flow_amt") val flowAmt: Double,
    @SerializedName("tran_amt") val tranAmt: Double?,
    @SerializedName("flow_date") val flowDate: String,
    @SerializedName("flow_id") val flowId: String,
    @SerializedName("flow_code") val flowCode: String,
    @SerializedName("loan_acct_no") val loanAcctNo: String,
    @SerializedName("acct_name") val acctName: String,
    @SerializedName("encoded_key") val encodedKey: String
)

data class LoanClosureDataResponse(
    @SerializedName("tran_date") val tranDate: String,
    @SerializedName("flow_total_amt") val flowTotalAmt: Double,
    @SerializedName("loan_flows") val loanFlows: List<LoanFlowDetail>
)

data class AccountSearchResponse(
    val id: String,
    val name: String
)

data class LoanFlowTransactionRequest(
    @SerializedName("flow_amt") val flowAmt: String,
    @SerializedName("flow_code") val flowCode: String,
    @SerializedName("flow_date") val flowDate: String,
    @SerializedName("flow_id") val flowId: String,
    @SerializedName("loan_acct_no") val loanAcctNo: String,
    @SerializedName("acct_name") val acctName: String,
    @SerializedName("from_date") val fromDate: String,
    @SerializedName("to_date") val toDate: String,
    @SerializedName("encoded_key") val encodedKey: String,
    @SerializedName("operation_type") val operationType: String,
    @SerializedName("tran_amt") val tranAmt: String? = null,
    @SerializedName("days") val days: Int? = null,
    @SerializedName("interest_percentage") val interestPercentage: Double? = null
)
