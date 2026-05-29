package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserProfile(
    @SerializedName("userid") val userId: String?,
    @SerializedName("username") val userName: String?,
    @SerializedName("user_status") val userStatus: String?,
    @SerializedName("auth_flg") val authFlg: String? = "Y",
    @SerializedName("disable_flg") val disableFlg: String? = null,
    @SerializedName("entity_flg") val entityFlg: String? = "Y",
    @SerializedName("login_status") val loginStatus: String? = "Active",
    @SerializedName("modify_flg") val modifyFlg: String? = "N",
    @SerializedName("user_locked_flg") val userLockedFlg: String? = "N",
    @SerializedName("login_flg") val loginFlg: String? = "N",

    @SerializedName("mob_number") val mobileNumber: String? = null,
    @SerializedName("email_id") val emailId: String? = null,
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("branch_des") val branchDes: String? = null,

    @SerializedName("disable_start_date") val disableStartDate: Any? = null,
    @SerializedName("disable_end_date")   val disableEndDate: Any?   = null,
    @SerializedName("pass_exp_date")      val passwordExpiryDate: Any? = null,
    @SerializedName("acc_exp_date")       val accountExpiryDate: Any? = null,

    @SerializedName("role_id") val roleId: String? = null,
    @SerializedName("role_desc") val roleDesc: String? = null,
    @SerializedName("permissions") val permissions: String? = null,
    @SerializedName("work_class") val workClass: String? = null,
    @SerializedName("acct_access_code") val acctAccessCode: String? = "ALL",
    @SerializedName("doc_access_code") val docAccessCode: String? = "ALL",
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("password") val password: String? = null
)

data class UserProfileResponse(
    val formmode: String?,
    val userProfiles: List<UserProfile>?
)

data class SingleUserResponse(
    val formmode: String?,
    val userProfile: UserProfile?,
    val access: AccessRoleResponse?
)

data class AccessRoleResponse(
    val user_id: String?,
    val role_id: String?,
    val role_desc: String?,
    val permissions: String?,
    val work_class: String?,
    val admin: String?,
    val orgnaization_details: String?,
    val user_controls: String?,
    val reference_code_maintenance: String?,
    val audit_trail: String?,
    val notification_Reports: String?,
    val migration: String?,
    val customer_master: String?,
    val loan_master: String?,
    val loan_schedule_migration: String?,
    val transaction_migration: String?,
    val loan_operation: String?,
    val loan_operation_ls: String?,
    val loan_closure: String?,
    val transaction_maintenance: String?,
    val journal_entries: String?,
    val account_ledger_posting: String?,
    val account_ledger: String?,
    val trial_balance_t: String?,
    val profit_and_loss_account_t: String?,
    val collection_process: String?,
    val participating_banks: String?,
    val loan_collecting: String?,
    val batch_job_execution: String?,
    val batch_job: String?,
    val inquiries_and_reports: String?,
    val account_balance_inq: String?,
    val interset_summary_inq: String?,
    val journal_book: String?,
    val account_ledgers_i: String?,
    val trial_balance_i: String?,
    val general_ledger: String?,
    val profit_and_loss_account_i: String?,
    val balance_sheet: String?,
    val end_of_month_report: String?,
    val dab: String?,
    val transaction_report: String?,
    val consolidated_report: String?,
    val credit_facility_report: String?,
    val interest_accrual_report: String?,
    val penalty_accrual_report: String?,
    val recovery_report: String?,
    val demand_generation: String?,
    val transaction_reversal: String?,
    val transaction_accounts: String?
)