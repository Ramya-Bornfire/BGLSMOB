package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")      val status: String?,
    @SerializedName("message")     val message: String?,
    @SerializedName("user")        val user: LoginUser?,
    @SerializedName("permissions") val permissions: LoginPermissions?
)

data class LoginUser(
    @SerializedName("userid")     val userid: String?,
    @SerializedName("username")   val username: String?,
    @SerializedName("branchId")   val branchId: String?,
    @SerializedName("branchName") val branchName: String?,
    @SerializedName("loginTime")  val loginTime: String?
)

data class LoginPermissions(
    @SerializedName("admin")                    val admin: String?,
    @SerializedName("orgnaizationDetails")      val orgnaizationDetails: String?,
    @SerializedName("userControls")             val userControls: String?,
    @SerializedName("referenceCodeMaintenance") val referenceCodeMaintenance: String?,
    @SerializedName("auditTrail")               val auditTrail: String?,
    @SerializedName("dayEndOperation")          val dayEndOperation: String?,
    @SerializedName("customerMaintenance")      val customerMaintenance: String?,
    @SerializedName("loanMaintenance")          val loanMaintenance: String?,
    @SerializedName("migration")                val migration: String?,
    @SerializedName("customerMaster")           val customerMaster: String?,
    @SerializedName("loanMaster")               val loanMaster: String?,
    @SerializedName("loanScheduleMigration")    val loanScheduleMigration: String?,
    @SerializedName("transactionMigration")     val transactionMigration: String?,
    @SerializedName("loanOperation")            val loanOperation: String?,
    @SerializedName("loanOperationLs")          val loanOperationLs: String?,
    @SerializedName("loanClosure")              val loanClosure: String?,
    @SerializedName("transactionMaintenance")   val transactionMaintenance: String?,
    @SerializedName("journalEntries")           val journalEntries: String?,
    @SerializedName("accountLedgerPosting")     val accountLedgerPosting: String?,
    @SerializedName("accountLedger")            val accountLedger: String?,
    @SerializedName("trialBalanceT")            val trialBalanceT: String?,
    @SerializedName("profitAndLossAccountT")    val profitAndLossAccountT: String?,
    @SerializedName("collectionProcess")        val collectionProcess: String?,
    @SerializedName("participatingBanks")       val participatingBanks: String?,
    @SerializedName("loanCollecting")           val loanCollecting: String?,
    @SerializedName("batchJobExecution")        val batchJobExecution: String?,
    @SerializedName("batchJob")                 val batchJob: String?,
    @SerializedName("inquiriesAndReports")      val inquiriesAndReports: String?,
    @SerializedName("accountBalanceInq")        val accountBalanceInq: String?,
    @SerializedName("intersetSummaryInq")       val intersetSummaryInq: String?,
    @SerializedName("journalBook")              val journalBook: String?,
    @SerializedName("accountLedgersI")          val accountLedgersI: String?,
    @SerializedName("trialBalanceI")            val trialBalanceI: String?,
    @SerializedName("generalLedger")            val generalLedger: String?,
    @SerializedName("profitAndLossAccountI")    val profitAndLossAccountI: String?,
    @SerializedName("balanceSheet")             val balanceSheet: String?,
    @SerializedName("balanceSheets")            val balanceSheets: String?,
    @SerializedName("creditFacilityReport")     val creditFacilityReport: String?,
    @SerializedName("endOfMonthReport")         val endOfMonthReport: String?,
    @SerializedName("dab")                      val dab: String?,
    @SerializedName("consolidatedReport")       val consolidatedReport: String?,
    @SerializedName("transactionReport")        val transactionReport: String?,
    @SerializedName("interestAccrualReport")    val interestAccrualReport: String?,
    @SerializedName("penaltyAccrualReport")     val penaltyAccrualReport: String?,
    @SerializedName("recoveryReport")           val recoveryReport: String?,
    @SerializedName("demandGeneration")         val demandGeneration: String?,
    @SerializedName("transactionAccounts")      val transactionAccounts: String?,
    @SerializedName("transactionReversal")      val transactionReversal: String?,
    @SerializedName("notificationReports")      val notificationReports: String?
)