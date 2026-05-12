package com.example.bgls.LoanMaintenance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.CustomerMaster.AccountLedgerActivity
import com.example.bgls.CustomerMaster.LoanScheduleViewActivity
import com.example.bgls.CustomerMaster.WalletActivity
import com.example.bgls.DataModels.LoanMasterViewResponse
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class LoanMaintananceViewActivity : AppCompatActivity() {

    private val TAG = "LoanMasterViewActivity"

    lateinit var btnLedger: Button
    lateinit var btnSchedule: Button
    lateinit var btnWallet: Button
    lateinit var btnVerify: Button

    // UI Fields
    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etCustomerStatus: EditText
    private lateinit var etAccountType: EditText
    private lateinit var etLoanNo: EditText
    private lateinit var etLoanName: EditText
    private lateinit var etAssignedBranch: EditText
    private lateinit var etOpenDate: EditText
    private lateinit var etApprovalDate: EditText
    private lateinit var etLastModifiedDate: EditText
    private lateinit var etLastReviewDate: EditText
    private lateinit var etAccountStatus: EditText
    private lateinit var etCurrencyCode: EditText
    private lateinit var etLoanAmount: EditText
    private lateinit var etSubStatus: EditText
    private lateinit var etPaymentMethod: EditText
    private lateinit var etPenaltyRate: EditText
    private lateinit var etRateOfInterest: EditText
    private lateinit var etDisbursementDate: EditText
    private lateinit var etFirstPaymentDate: EditText
    private lateinit var etRemarks1: EditText
    private lateinit var etRepaymentMethod: EditText
    private lateinit var etRepaymentInstalments: EditText
    private lateinit var etWalletAccount: EditText

    private lateinit var etPrincipalDue: EditText
    private lateinit var etPrincipalPaid: EditText
    private lateinit var etPrincipalBalance: EditText
    private lateinit var etInterestDue: EditText
    private lateinit var etInterestPaid: EditText
    private lateinit var etInterestBalance: EditText
    private lateinit var etFeeDue: EditText
    private lateinit var etFeePaid: EditText
    private lateinit var etFeeBalance: EditText
    private lateinit var etPenaltyDue: EditText
    private lateinit var etPenaltyPaid: EditText
    private lateinit var etPenaltyBalance: EditText
    private lateinit var etAsOnDate: EditText
    private lateinit var etAccruedInterest: EditText
    private lateinit var etAccruedPenalty: EditText

    private lateinit var etSalesProcessedBy: EditText
    private lateinit var etSalesProcessedFor: EditText
    private lateinit var etSalesReferredBy: EditText
    private lateinit var etEmploymentStatus: EditText
    private lateinit var etJobTitle: EditText
    private lateinit var etEmployerName: EditText
    private lateinit var etTUCore: EditText
    private lateinit var etTUProbability: EditText
    private lateinit var etTUFullName: EditText
    private lateinit var etTUReasons: EditText
    private lateinit var etTureason1: EditText
    private lateinit var etTureason2: EditText
    private lateinit var etDisposableIncome: EditText
    private lateinit var etManualOverrideAmt: EditText
    private lateinit var etManualOverrideExp: EditText
    private lateinit var etCPFees: EditText
    private lateinit var etDepositAmount: EditText
    private lateinit var etTotalProductPrice: EditText
    private lateinit var etRetailerName: EditText
    private lateinit var etRetailerBranch: EditText
    private lateinit var etVGApplicationId: EditText
    private lateinit var etContractSigned: EditText
    private lateinit var etDateOfFirstCall: EditText
    private lateinit var etLastCallOutcome: EditText
    private lateinit var etDaysInArrears: EditText
    // Re-using Remarks1 is tricky if there are two in UI, we'll map the first one and ignore the second if it doesn't have a unique ID.
    private lateinit var etBalanceOutstanding: EditText

    private var loanId: String = ""
    private var holderKey: String = ""
    private var branchKey: String = ""
    private var encodedKey: String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_maintanance_view)
        loanId = intent.getStringExtra("loanId") ?: ""
        holderKey = intent.getStringExtra("holderKey") ?: ""
        branchKey = intent.getStringExtra("branchKey") ?: ""
        val tvMainTitle = findViewById<TextView>(R.id.tvMainTitle)
        if (tvMainTitle != null && intent.getStringExtra("source") == "LoanMaintenance") {
            tvMainTitle.text = "LOAN MAINTENANCE - VIEW"
        }
        initViews()
        setupButtons()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        if (loanId.isNotEmpty() && holderKey.isNotEmpty()) {
            fetchLoanDetails(loanId, holderKey, branchKey)
        } else {
            Toast.makeText(this, "Missing loan parameters", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        btnSchedule = findViewById(R.id.btnSchedule)
        btnLedger = findViewById(R.id.btnLedger)
        btnWallet = findViewById(R.id.btnWallet)
        btnVerify = findViewById(R.id.btnVerify)
        btnVerify.visibility = android.view.View.GONE

        etCustomerId = findViewById(R.id.etCustomerId)
        etCustomerName = findViewById(R.id.etCustomerName)
        etCustomerStatus = findViewById(R.id.etCustomerStatus)
        etAccountType = findViewById(R.id.etAccountType)
        etLoanNo = findViewById(R.id.etLoanNo)
        etLoanName = findViewById(R.id.etLoanName)
        etAssignedBranch = findViewById(R.id.etAssignedBranch)
        etOpenDate = findViewById(R.id.etOpenDate)
        etApprovalDate = findViewById(R.id.etApprovalDate)
        etLastModifiedDate = findViewById(R.id.etLastModifiedDate)
        etLastReviewDate = findViewById(R.id.etLastReviewDate)
        etAccountStatus = findViewById(R.id.etAccountStatus)
        etCurrencyCode = findViewById(R.id.etCurrencyCode)
        etLoanAmount = findViewById(R.id.etLoanAmount)
        etSubStatus = findViewById(R.id.etSubStatus)
        etPaymentMethod = findViewById(R.id.etPaymentMethod)
        etPenaltyRate = findViewById(R.id.etPenaltyRate)
        etRateOfInterest = findViewById(R.id.etRateOfInterest)
        etDisbursementDate = findViewById(R.id.etDisbursementDate)
        etFirstPaymentDate = findViewById(R.id.etFirstPaymentDate)
        etRemarks1 = findViewById(R.id.etRemarks1)
        etRepaymentMethod = findViewById(R.id.etRepaymentMethod)
        etRepaymentInstalments = findViewById(R.id.etRepaymentInstalments)
        etWalletAccount = findViewById(R.id.etWalletAccount)

        etPrincipalDue = findViewById(R.id.etPrincipalDue)
        etPrincipalPaid = findViewById(R.id.etPrincipalPaid)
        etPrincipalBalance = findViewById(R.id.etPrincipalBalance)
        etInterestDue = findViewById(R.id.etInterestDue)
        etInterestPaid = findViewById(R.id.etInterestPaid)
        etInterestBalance = findViewById(R.id.etInterestBalance)
        etFeeDue = findViewById(R.id.etFeeDue)
        etFeePaid = findViewById(R.id.etFeePaid)
        etFeeBalance = findViewById(R.id.etFeeBalance)
        etPenaltyDue = findViewById(R.id.etPenaltyDue)
        etPenaltyPaid = findViewById(R.id.etPenaltyPaid)
        etPenaltyBalance = findViewById(R.id.etPenaltyBalance)

        etAsOnDate = findViewById(R.id.etAsOnDate)
        etAccruedInterest = findViewById(R.id.etAccruedInterest)
        etAccruedPenalty = findViewById(R.id.etAccruedPenalty)

        etSalesProcessedBy = findViewById(R.id.etSalesProcessedBy)
        etSalesProcessedFor = findViewById(R.id.etSalesProcessedFor)
        etSalesReferredBy = findViewById(R.id.etSalesReferredBy)
        etEmploymentStatus = findViewById(R.id.etEmploymentStatus)
        etJobTitle = findViewById(R.id.etJobTitle)
        etEmployerName = findViewById(R.id.etEmployerName)
        etTUCore = findViewById(R.id.etTuCore)
        etTUProbability = findViewById(R.id.etTuProbability)
        etTUFullName = findViewById(R.id.etTuFullName)
        etTUReasons = findViewById(R.id.etTuReasons)
        etTureason1 = findViewById(R.id.etTureason1)
        etTureason2 = findViewById(R.id.etTureason2)
        etDisposableIncome = findViewById(R.id.etDisposableIncome)
        etManualOverrideAmt = findViewById(R.id.etManualOverrideAmt)
        etManualOverrideExp = findViewById(R.id.etManualOverrideExp)
        etCPFees = findViewById(R.id.etCpFees)
        etDepositAmount = findViewById(R.id.etDepositAmount)
        etTotalProductPrice = findViewById(R.id.etTotalProductPrice)
        etRetailerName = findViewById(R.id.etRetailerName)
        etRetailerBranch = findViewById(R.id.etRetailerBranch)
        etVGApplicationId = findViewById(R.id.etVgApplicationId)
        etContractSigned = findViewById(R.id.etContractSigned)
        etDateOfFirstCall = findViewById(R.id.etDateOfFirstCall)
        etLastCallOutcome = findViewById(R.id.etLastCallOutcome)
        etDaysInArrears = findViewById(R.id.etDaysInArrears)
        etBalanceOutstanding = findViewById(R.id.etBalanceOutstanding)
    }

    private fun setupButtons() {
        btnSchedule.setOnClickListener {
            val intent = Intent(this, LoanScheduleViewActivity::class.java).apply{
                putExtra("loanId", loanId)
                putExtra("holder_key", holderKey)
                putExtra("encoded_key", encodedKey)
                putExtra("branchKey", branchKey)
                putExtra("source", intent.getStringExtra("source"))
            }
            startActivity(intent)
        }

        btnLedger.setOnClickListener {
            val intent = Intent(this, AccountLedgerActivity::class.java).apply {
                putExtra("acct_num", loanId)
            }
            startActivity(intent)
        }

        btnWallet.setOnClickListener {
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
        }

        btnVerify.setOnClickListener {
            if (loanId.isNotEmpty()) {
                verifyLoan(loanId)
            } else {
                Toast.makeText(this, "Loan ID is missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verifyLoan(id: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.verifyLoanMain(id)
                if (response.isSuccessful) {
                    val message = response.body()?.string() ?: "Verified Successfully"
                    Toast.makeText(this@LoanMaintananceViewActivity, message, Toast.LENGTH_LONG).show()
                    // Optionally refresh the data or finish the activity
                    fetchLoanDetails(loanId, holderKey, branchKey)
                } else {
                    Log.e(TAG, "Verification failed: ${response.code()}")
                    Toast.makeText(this@LoanMaintananceViewActivity, "Verification failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error during verification", e)
                Toast.makeText(this@LoanMaintananceViewActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchLoanDetails(id: String, holderKey: String, branchKey: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoanMaintenanceView(id = id, holderKey = holderKey, branchKey = branchKey)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    populateUI(data)
                    encodedKey = data.view?.encodedKey ?: ""
                    Log.d(TAG, "Encoded key from API: $encodedKey")

                    // Manage Verify button visibility
                    if (data.isUnverified == true) {
                        btnVerify.visibility = android.view.View.VISIBLE
                    } else {
                        btnVerify.visibility = android.view.View.GONE
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    Toast.makeText(this@LoanMaintananceViewActivity, "Failed to load loan details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error", e)
                Toast.makeText(this@LoanMaintananceViewActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        if (dateString.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) return dateString
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            val date = inputFormat.parse(dateString) ?: return dateString
            outputFormat.format(date)
        } catch (e: Exception) {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateString) ?: return dateString
                outputFormat.format(date)
            } catch (e2: Exception) {
                dateString
            }
        }
    }

    private fun formatDecimal(value: Double?): String {
        if (value == null) return ""
        if (value == 0.0) return "0.00"
        return try {
            val formatter = java.text.DecimalFormat("#,##0.00")
            formatter.format(value)
        } catch (e: Exception) {
            value.toString()
        }
    }

    private fun populateUI(data: LoanMasterViewResponse) {
        // Customer Header
        var custId = data.customerId?.firstOrNull() ?: ""
        var custName = data.customerName?.firstOrNull() ?: ""

        if (custId.isEmpty() && data.customerData != null) {
            val cData = data.customerData
            if (cData is List<*>) {
                val firstItem = cData.firstOrNull()
                if (firstItem is List<*>) {
                    custName = firstItem.getOrNull(0)?.toString() ?: ""
                    custId = firstItem.getOrNull(1)?.toString() ?: ""
                } else if (firstItem is String) {
                    custName = cData.getOrNull(0)?.toString() ?: ""
                    custId = cData.getOrNull(1)?.toString() ?: ""
                }
            }
        }

        etCustomerId.setText(custId)
        etCustomerName.setText(custName)

        val view = data.view
        if (view == null) return

        etCustomerStatus.setText(view.accountState ?: "")
        etAccountType.setText(view.accountHolderType ?: "")
        etLoanNo.setText(view.id ?: "")
        etLoanName.setText(view.loanName ?: "")
        etAssignedBranch.setText(data.branchName1 ?: "")

        etOpenDate.setText(formatDate(view.creationDate))
        etApprovalDate.setText(formatDate(view.approvedDate))
        etLastModifiedDate.setText(formatDate(view.lastModifiedDate))
        etLastReviewDate.setText(formatDate(view.closedDate))
        etAccountStatus.setText(view.accountState ?: "")

        etCurrencyCode.setText("KES")
        etLoanAmount.setText(formatDecimal(view.loanAmount))
        etSubStatus.setText(view.accountSubState ?: "")

        etPaymentMethod.setText(view.paymentMethod ?: "")
        etPenaltyRate.setText(view.penaltyRate?.toString() ?: "")
        etRateOfInterest.setText(view.interestRate?.toString() ?: "")

        etDisbursementDate.setText(formatDate(view.expectedDisbursementDate))
        etFirstPaymentDate.setText(formatDate(view.firstRepaymentDate))
        etRemarks1.setText("")
        etRepaymentMethod.setText(view.repaymentScheduleMethod ?: "")
        etRepaymentInstalments.setText(view.repaymentInstallments?.toString() ?: "")
        etWalletAccount.setText(view.walletAccountNumber ?: "")

        // Arrears Details
        etPrincipalDue.setText(formatDecimal(view.principalDue))
        etPrincipalPaid.setText(formatDecimal(view.principalPaid))
        etPrincipalBalance.setText(formatDecimal(view.principalBalance))

        etInterestDue.setText(formatDecimal(view.interestDue))
        etInterestPaid.setText(formatDecimal(view.interestPaid))
        etInterestBalance.setText(formatDecimal(view.interestBalance))

        etFeeDue.setText(formatDecimal(view.feesDue))
        etFeePaid.setText(formatDecimal(view.feesPaid))
        etFeeBalance.setText(formatDecimal(view.feesBalance))

        etPenaltyDue.setText(formatDecimal(view.penaltyDue))
        etPenaltyPaid.setText(formatDecimal(view.penaltyPaid))
        etPenaltyBalance.setText(formatDecimal(view.penaltyBalance))

        etAsOnDate.setText(formatDate(view.asOnDate))
        etAccruedInterest.setText(formatDecimal(view.accruedInterest))
        etAccruedPenalty.setText(formatDecimal(view.accruedPenalty))

        // Additional
        etSalesProcessedBy.setText(view.saleProcessedByVgId ?: "")
        etSalesProcessedFor.setText(view.saleProcessedFor ?: "")
        etSalesReferredBy.setText(view.saleReferredBy ?: "")

        etEmploymentStatus.setText(view.employmentStatus ?: "")
        etJobTitle.setText(view.jobTitle ?: "")
        etEmployerName.setText(view.employerName ?: "")

        etTUCore.setText(view.tuScore?.toString() ?: "")
        etTUProbability.setText(view.tuProbability?.toString() ?: "")
        etTUFullName.setText("") // Intentionally blank as per web UI
        etTUReasons.setText(view.tuFullName ?: "") // Mapped to tufullname as per web UI

        etTureason1.setText(view.tuReason1 ?: "")
        etTureason2.setText(view.tuReason2 ?: "")

        etDisposableIncome.setText(formatDecimal(view.disposableIncome))
        etManualOverrideAmt.setText(formatDecimal(view.manualOverrideAmount))
        etManualOverrideExp.setText(formatDate(view.manualOverrideExpiryDate))

        etCPFees.setText(formatDecimal(view.cpFees))
        etDepositAmount.setText(formatDecimal(view.depositAmount))
        etTotalProductPrice.setText(formatDecimal(view.totalProductPrice))

        etRetailerName.setText(view.retailerName ?: "")
        etRetailerBranch.setText(view.retailerBranch ?: "")
        etVGApplicationId.setText(view.vgApplicationId ?: "")

        etContractSigned.setText(view.contractSigned ?: "")
        etDateOfFirstCall.setText(formatDate(view.dateOfFirstCall))
        etLastCallOutcome.setText(view.lastCallOutcome ?: "")

        etDaysInArrears.setText(view.daysInArrears?.toString() ?: "")
        etBalanceOutstanding.setText(formatDecimal(data.acctBal))
    }
}