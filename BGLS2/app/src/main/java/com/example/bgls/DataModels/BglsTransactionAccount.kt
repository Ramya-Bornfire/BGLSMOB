package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class BglsTransactionAccount(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("gl_code") val gl_code: String? = null,
    @SerializedName("scheme_code") val scheme_code: String? = null,
    @SerializedName("glsh_code") val glsh_code: String? = null,
    @SerializedName("interest_income") val interest_income: String? = null,
    @SerializedName("fees_income") val fees_income: String? = null,
    @SerializedName("collection_account") val collection_account: String? = null,
    @SerializedName("product_key") val product_key: String? = null,
    @SerializedName("gl_description") val gl_description: String? = null,
    @SerializedName("scheme_description") val scheme_description: String? = null,
    @SerializedName("glsh_description") val glsh_description: String? = null,
    @SerializedName("interest_receivable") val interest_receivable: String? = null,
    @SerializedName("penalty_income") val penalty_income: String? = null,
    @SerializedName("loan_parking_account") val loan_parking_account: String? = null,
    @SerializedName("entity_flg") val entity_flg: String? = null,
    @SerializedName("del_flg") val del_flg: String? = null
)

data class BglsTransactionAccountDetailResponse(
    @SerializedName("formmode") val formmode: String? = null,
    @SerializedName("account") val account: BglsTransactionAccount? = null
)
