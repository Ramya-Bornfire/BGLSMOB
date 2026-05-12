package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

/** Matches the DAB_Entity returned by /api/getDabAcctList */
data class DabAccountModel(
    @SerializedName("acct_num")   val acctNum: String?   = null,   // ✅ matches backend JSON
    @SerializedName("acct_name")  val acctName: String?  = null,
    @SerializedName("loan_acct_no") val loanAcctNo: String? = null
) {
    val displayId: String get() = acctNum ?: loanAcctNo ?: ""
    val displayName: String get() = acctName ?: ""
}

/** Matches the map returned by /api/BatchJobconsistencyCheck */
data class ConsistencyCheckResponse(
    @SerializedName("custcheck1") val custcheck1: Any? = null,
    @SerializedName("custcheck2") val custcheck2: Any? = null,
    @SerializedName("custcheck3") val custcheck3: Any? = null
) {
    private fun rowToString(row: Any?): Triple<String, String, String> {
        if (row is List<*> && row.size >= 3) {
            return Triple(
                row[0]?.toString() ?: "-",
                row[1]?.toString() ?: "-",
                row[2]?.toString() ?: "-"
            )
        }
        return Triple("-", "-", "-")
    }

    val trmRow:  Triple<String,String,String> get() = rowToString(custcheck1)
    val coaRow:  Triple<String,String,String> get() = rowToString(custcheck2)
    val dabRow:  Triple<String,String,String> get() = rowToString(custcheck3)
}

data class FlowDetail(
    val flowDate: String,
    val flowId: String,
    val flowCode: String,
    val flowAmt: Double,
    val acctNo: String,
    val acctName: String,
    val encodedKey: String? = null,
    val interest: Double? = null,
    val days: Int? = null,
    val interestPercentage: Double? = null
)