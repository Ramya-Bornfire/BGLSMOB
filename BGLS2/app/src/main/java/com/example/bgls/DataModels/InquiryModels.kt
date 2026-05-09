package com.example.bgls.DataModels

data class AccountBalancesResponse(
    val formmode: String?,
    val leaseaccount: List<List<Any>>?,
    val depsoitaccount: List<List<Any>>?,
    val TRANDATE: String?
)

data class InterestSummaryResponse(
    val formmode: String?,
    val fewvalues: List<Any>?,
    val allAccBalances: List<Any>?
)

data class LoanMaintenanceViewResponse(
    val formmode: String?,
    val getDDDetails: List<Any>?,
    val loanDetails: Map<String, Any>?,
    val paymentDetails: Map<String, Any>?
)

data class JournalBookResponse(
    val formmode: String?,
    val jour: List<Map<String, Any>>?
)

data class JournalBookModel(
    val tranDate: String,
    val tranId: String,
    val partTranId: String,
    val partTranType: String,
    val currency: String,
    val amount: String,
    val acctNum: String,
    val acctName: String,
    val particular: String,
    val status: String
)

data class ProfitLossResponse(
    val formmode: String?,
    val balancesheet3: List<Any>?,
    val balancesheet4: List<Any>?,
    val TRANDATE: String?
)

data class BalanceSheetResponse(
    val formmode: String?,
    val balancesheet1: List<Any>?,
    val balancesheet2: List<Any>?,
    val TRANDATE: String?
)

data class AssetLiabilityResponse(
    val msg: List<List<Any>>?,
    val msg1: List<List<Any>>?
)

data class BalancingReportResponse(
    val formmode: String?,
    val chartaccount: Any?,
    val Chart1: Any?,
    val Chart2: Any?,
    val Chart3: Any?,
    val Chart4: Any?,
    val Chart5: Any?,
    val Chart6: Any?,
    val Chart7: Any?,
    val Chart8: Any?
)
