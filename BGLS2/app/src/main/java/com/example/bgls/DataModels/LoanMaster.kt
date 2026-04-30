package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

/**
 * Loan list item – matches the JSON keys returned by the
 * backend `api/loans` and `loan/search` endpoints.
 * The backend returns a flat map for each row.
 */
data class LoanMaster(
    @SerializedName("id")                  val id: String? = null,
    @SerializedName("account_holderkey")    val accountHolderKey: String? = null,
    @SerializedName("loan_name")           val loanName: String? = null,
    @SerializedName("assigned_branchkey")   val assignedBranchKey: String? = null,
    @SerializedName("retailer_name")       val retailerName: String? = null,
    @SerializedName("retailer_branch")     val retailerBranch: String? = null,
    @SerializedName("account_state")       val accountState: String? = null,
    @SerializedName("first_name")          val firstName: String? = null,
    @SerializedName("last_name")           val lastName: String? = null,
    @SerializedName("mobile_phone")        val mobilePhone: String? = null
) {
    /** Convenience – combines first + last name the same way the web table does */
    val customerName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}".trim()
}

/** Paginated response wrapper for the `api/loans` endpoint */
data class LoanMasterPagedResponse(
    @SerializedName("data")         val data: List<LoanMaster> = emptyList(),
    @SerializedName("currentPage")  val currentPage: Int = 1,
    @SerializedName("totalPages")   val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Long = 0L
)

/**
 * Full loan detail entity – mirrors LOAN_ACT_MST_ENTITY.
 * Used by the `api/loanMaster?formmode=viewloan` response.
 */
data class LoanMasterDetail(
    @SerializedName("encoded_key")              val encodedKey: String? = null,
    @SerializedName("id")                       val id: String? = null,
    @SerializedName("account_holdertype")        val accountHolderType: String? = null,
    @SerializedName("account_holderkey")         val accountHolderKey: String? = null,
    @SerializedName("creation_date")            val creationDate: String? = null,
    @SerializedName("approved_date")            val approvedDate: String? = null,
    @SerializedName("last_modified_date")       val lastModifiedDate: String? = null,
    @SerializedName("closed_date")              val closedDate: String? = null,
    @SerializedName("last_account_appraisaldate") val lastAccountAppraisalDate: String? = null,
    @SerializedName("account_state")            val accountState: String? = null,
    @SerializedName("account_substate")         val accountSubState: String? = null,
    @SerializedName("product_typekey")          val productTypeKey: String? = null,
    @SerializedName("loan_name")                val loanName: String? = null,
    @SerializedName("payment_method")           val paymentMethod: String? = null,
    @SerializedName("assigned_branchkey")        val assignedBranchKey: String? = null,
    @SerializedName("loan_amount")              val loanAmount: Double? = null,
    @SerializedName("interest_rate")            val interestRate: Double? = null,
    @SerializedName("penalty_rate")             val penaltyRate: Double? = null,
    @SerializedName("accrued_interest")         val accruedInterest: Double? = null,
    @SerializedName("accrued_penalty")          val accruedPenalty: Double? = null,
    @SerializedName("principal_due")            val principalDue: Double? = null,
    @SerializedName("principal_paid")           val principalPaid: Double? = null,
    @SerializedName("principal_balance")        val principalBalance: Double? = null,
    @SerializedName("interest_due")             val interestDue: Double? = null,
    @SerializedName("interest_paid")            val interestPaid: Double? = null,
    @SerializedName("interest_balance")         val interestBalance: Double? = null,
    @SerializedName("interest_fromarrearsbalance") val interestFromArrearsBalance: Double? = null,
    @SerializedName("interest_fromarrearsdue")     val interestFromArrearsDue: Double? = null,
    @SerializedName("interest_fromarrearspaid")    val interestFromArrearsPaid: Double? = null,
    @SerializedName("fees_due")                 val feesDue: Double? = null,
    @SerializedName("fees_paid")                val feesPaid: Double? = null,
    @SerializedName("fees_balance")             val feesBalance: Double? = null,
    @SerializedName("penalty_due")              val penaltyDue: Double? = null,
    @SerializedName("penalty_paid")             val penaltyPaid: Double? = null,
    @SerializedName("penalty_balance")          val penaltyBalance: Double? = null,
    @SerializedName("expected_disbursementdate") val expectedDisbursementDate: String? = null,
    @SerializedName("disbursement_date")        val disbursementDate: String? = null,
    @SerializedName("first_repaymentdate")      val firstRepaymentDate: String? = null,
    @SerializedName("grace_period")             val gracePeriod: Double? = null,
    @SerializedName("repayment_installments")   val repaymentInstallments: Double? = null,
    @SerializedName("repayment_periodcount")    val repaymentPeriodCount: Double? = null,
    @SerializedName("days_late")                val daysLate: Double? = null,
    @SerializedName("days_inarrears")           val daysInArrears: Double? = null,
    @SerializedName("repayment_schedule_method") val repaymentScheduleMethod: String? = null,
    @SerializedName("currency_code")            val currencyCode: String? = null,
    @SerializedName("sale_processedbyvgid")     val saleProcessedByVgId: String? = null,
    @SerializedName("sale_processedfor")        val saleProcessedFor: String? = null,
    @SerializedName("sale_referredby")          val saleReferredBy: String? = null,
    @SerializedName("employment_status")        val employmentStatus: String? = null,
    @SerializedName("job_title")                val jobTitle: String? = null,
    @SerializedName("employer_name")            val employerName: String? = null,
    @SerializedName("tuscore")                  val tuScore: Double? = null,
    @SerializedName("tuprobability")            val tuProbability: Double? = null,
    @SerializedName("tufullname")               val tuFullName: String? = null,
    @SerializedName("tureason1")                val tuReason1: String? = null,
    @SerializedName("tureason2")                val tuReason2: String? = null,
    @SerializedName("tureason3")                val tuReason3: String? = null,
    @SerializedName("tureason4")                val tuReason4: String? = null,
    @SerializedName("disposable_income")        val disposableIncome: Double? = null,
    @SerializedName("manualoverride_amount")    val manualOverrideAmount: Double? = null,
    @SerializedName("manualoverride_expiry_date") val manualOverrideExpiryDate: String? = null,
    @SerializedName("cpfees")                   val cpFees: Double? = null,
    @SerializedName("deposit_amount")           val depositAmount: Double? = null,
    @SerializedName("total_product_price")      val totalProductPrice: Double? = null,
    @SerializedName("retailer_name")            val retailerName: String? = null,
    @SerializedName("retailer_branch")          val retailerBranch: String? = null,
    @SerializedName("vg_application_id")        val vgApplicationId: String? = null,
    @SerializedName("contract_signed")          val contractSigned: String? = null,
    @SerializedName("date_of_first_call")       val dateOfFirstCall: String? = null,
    @SerializedName("last_call_outcome")        val lastCallOutcome: String? = null,
    @SerializedName("asondate")                 val asOnDate: String? = null,
    @SerializedName("wallet_account_number")    val walletAccountNumber: String? = null,
    @SerializedName("entry_user")               val entryUser: String? = null,
    @SerializedName("modify_user")              val modifyUser: String? = null,
    @SerializedName("auth_user")                val authUser: String? = null,
    @SerializedName("entry_time")               val entryTime: String? = null,
    @SerializedName("modify_time")              val modifyTime: String? = null,
    @SerializedName("auth_time")                val authTime: String? = null
)

/**
 * Response wrapper for `api/loanMaster?formmode=viewloan`.
 * The backend puts the detail in the "view" key.
 */
data class LoanMasterViewResponse(
    @SerializedName("formmode")       val formmode: String? = null,
    @SerializedName("view")           val view: LoanMasterDetail? = null,
    @SerializedName("customer_id")    val customerId: List<String>? = null,
    @SerializedName("customer_name")  val customerName: List<String>? = null,
    @SerializedName("branchName1")    val branchName1: String? = null,
    @SerializedName("acct_bal")       val acctBal: Double? = null,
    @SerializedName("user")           val user: String? = null
)
