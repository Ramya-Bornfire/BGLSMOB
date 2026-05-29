package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class BglsTransactionAccount(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("glCode") val gl_code: String? = null,
    @SerializedName("schmCode") val scheme_code: String? = null,
    @SerializedName("glshCode") val glsh_code: String? = null,
    @SerializedName("interestIncome") val interest_income: String? = null,
    @SerializedName("feesIncome") val fees_income: String? = null,
    @SerializedName("collectionAccount") val collection_account: String? = null,
    @SerializedName("productKey") val product_key: String? = null,
    @SerializedName("glDesc") val gl_description: String? = null,
    @SerializedName("schmDesc") val scheme_description: String? = null,
    @SerializedName("glshDesc") val glsh_description: String? = null,
    @SerializedName("interestReceivable") val interest_receivable: String? = null,
    @SerializedName("penaltyIncome") val penalty_income: String? = null,
    @SerializedName("loanParkingAccount") val loan_parking_account: String? = null,
    @SerializedName("entityFlg") val entity_flg: String? = null,
    @SerializedName("delFlg") val del_flg: String? = null
)

data class BglsTransactionAccountDetailResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("account") val account: BglsTransactionAccount? = null
)
