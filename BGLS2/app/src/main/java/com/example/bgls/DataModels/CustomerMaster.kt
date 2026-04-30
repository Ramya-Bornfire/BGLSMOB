package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

/**
 * Full Customer Master entity – mirrors CLIENT_MASTER_ENTITY fields returned by the backend.
 * All fields are nullable to handle partial API responses safely.
 */
data class CustomerMaster(
    // ── Identity ──
    @SerializedName("customer_id")         val customerId: String? = null,
    @SerializedName("first_name")          val firstName: String? = null,
    @SerializedName("last_name")           val lastName: String? = null,
    @SerializedName("gender")              val gender: String? = null,
    @SerializedName("birth_date")          val dob: String? = null,

    // ── Branch / Role ──
    @SerializedName("assigned_branch_key") val branchKey: String? = null,
    @SerializedName("client_role_key")     val clientRoleKey: String? = null,

    // ── Dates ──
    @SerializedName("creation_date")            val creationDate: String? = null,
    @SerializedName("approved_date")            val approvalDate: String? = null,
    @SerializedName("last_modified_date")       val lastModificationDate: String? = null,
    @SerializedName("activation_date")          val activationDate: String? = null,
    @SerializedName("asondate")                 val asOnDate: String? = null,

    // ── Contact ──
    @SerializedName("mobile_phone")        val mobileNo: String? = null,
    @SerializedName("email_address")       val email: String? = null,

    // ── Address ──
    @SerializedName("address_line1")       val address1: String? = null,
    @SerializedName("address_line2")       val address2: String? = null,
    @SerializedName("city")                val city: String? = null,
    @SerializedName("suburb")              val suburb: String? = null,

    // ── Loan Info ──
    @SerializedName("loan_cycle")          val loanCycle: String? = null,
    @SerializedName("group_loan_cycle")    val groupLoanCycle: String? = null,

    // ── Assignment / Status ──
    @SerializedName("assigned_user_key")   val assignedUser: String? = null,
    @SerializedName("auth_flg")            val authFlg: String? = null,
    @SerializedName("client_state")        val status: String? = null
) {
    val customerName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}".trim()

    var branchName: String = ""

    init {
        branchName = branchKey ?: ""
    }
}

/** Paginated response wrapper for AllApprovedCust / ApprovedCust / NotApprovedCust */
data class CustomerMasterPagedResponse(
    @SerializedName("data")         val data: List<CustomerMaster> = emptyList(),
    @SerializedName("currentPage")  val currentPage: Int = 1,
    @SerializedName("totalPages")   val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Long = 0L
)

/** Response wrapper for the customerMaster list formmode */
data class CustomerMasterListResponse(
    @SerializedName("formmode") val formmode: String?,
    @SerializedName("list")     val list: List<CustomerMaster>?
)

/** Response wrapper for the customerMaster view formmode */
data class CustomerMasterViewResponse(
    @SerializedName("formmode")    val formmode: String?,
    @SerializedName("customer")    val customer: CustomerMaster?,
    @SerializedName("branchName1") val branchName1: String?,
    @SerializedName("isUnverified") val isUnverified: Boolean?
)

/** One row from getAccDet – backend returns List<Object[]> with columns in a fixed order */
data class AccountDetailDto(
    val accountId: String,
    val accountName: String,
    val dateOfLoan: String,
    val loanAmount: String,
    val loanBalance: String
)
