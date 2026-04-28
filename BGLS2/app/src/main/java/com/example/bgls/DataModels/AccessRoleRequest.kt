package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class AccessRoleRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("role_id") val roleId: String,
    @SerializedName("role_desc") val roleDesc: String?,
    @SerializedName("permissions") val permissions: String?,
    @SerializedName("work_class") val workClass: String?,

    // Admin & core modules
    @SerializedName("admin") val admin: String,
    @SerializedName("orgnaization_details") val orgDetails: String,
    @SerializedName("user_controls") val userControls: String,
    @SerializedName("reference_code_maintenance") val refCodeMaint: String,
    @SerializedName("audit_trail") val auditTrail: String,
    @SerializedName("notification_Reports") val notificationReports: String,

    // Migration
    @SerializedName("migration") val migration: String,
    @SerializedName("customer_master") val customerMaster: String,
    @SerializedName("loan_master") val loanMaster: String,
    @SerializedName("loan_schedule_migration") val loanScheduleMigration: String,
    @SerializedName("transaction_migration") val transactionMigration: String,

    // Loan operation
    @SerializedName("loan_operation") val loanOperation: String,
    @SerializedName("loan_operation_ls") val loanOperationLs: String,
    @SerializedName("loan_closure") val loanClosure: String,

    // Transaction maintenance
    @SerializedName("transaction_maintenance") val transMaintenance: String,
    @SerializedName("journal_entries") val journalEntries: String,
    @SerializedName("account_ledger_posting") val accountLedgerPosting: String,
    @SerializedName("account_ledger") val accountLedger: String,
    @SerializedName("trial_balance_t") val trialBalanceT: String,
    @SerializedName("profit_and_loss_account_t") val profitLossT: String,

    // Collection
    @SerializedName("collection_process") val collectionProcess: String,
    @SerializedName("participating_banks") val participatingBanks: String,
    @SerializedName("loan_collecting") val loanCollecting: String,

    // Batch job
    @SerializedName("batch_job_execution") val batchJobExecution: String,
    @SerializedName("batch_job") val batchJob: String,

    // Reports & inquiries
    @SerializedName("inquiries_and_reports") val inquiriesReports: String,
    @SerializedName("account_balance_inq") val accountBalanceInq: String,
    @SerializedName("interset_summary_inq") val interestSummaryInq: String,
    @SerializedName("journal_book") val journalBook: String,
    @SerializedName("account_ledgers_i") val accountLedgersI: String,
    @SerializedName("trial_balance_i") val trialBalanceI: String,
    @SerializedName("general_ledger") val generalLedger: String,
    @SerializedName("profit_and_loss_account_i") val profitLossI: String,
    @SerializedName("balance_sheet") val balanceSheet: String,
    @SerializedName("end_of_month_report") val endOfMonthReport: String,
    @SerializedName("dab") val dab: String,
    @SerializedName("transaction_report") val transactionReport: String,
    @SerializedName("consolidated_report") val consolidatedReport: String,
    @SerializedName("credit_facility_report") val creditFacilityReport: String,
    @SerializedName("interest_accrual_report") val interestAccrualReport: String,
    @SerializedName("penalty_accrual_report") val penaltyAccrualReport: String,
    @SerializedName("recovery_report") val recoveryReport: String,
    @SerializedName("demand_generation") val demandGeneration: String,

    // Reversal transactions (new in your HTML)
    @SerializedName("transaction_reversal") val transactionReversal: String,
    @SerializedName("transaction_accounts") val transactionAccounts: String
)