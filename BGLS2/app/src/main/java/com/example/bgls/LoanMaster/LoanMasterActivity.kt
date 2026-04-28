package com.example.bgls.LoanMaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class LoanMasterActivity : AppCompatActivity() {

    // ─── LOAN DETAILS fields ───
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

    // ─── ADDITIONAL DETAILS fields ───
    private lateinit var etSalesProcessedByVGID: EditText
    private lateinit var etSalesProcessedFor: EditText
    private lateinit var etSalesReferredBy: EditText
    private lateinit var etEmploymentStatus: EditText
    private lateinit var etJobTitle: EditText
    private lateinit var etEmployerName: EditText
    private lateinit var etTuCore: EditText
    private lateinit var etTuProbability: EditText
    private lateinit var etTuFullName: EditText
    private lateinit var etTuReasons: EditText
    private lateinit var etTureason1: EditText
    private lateinit var etTureason2: EditText
    private lateinit var etDisposableIncome: EditText
    private lateinit var etManualOverrideAmount: EditText
    private lateinit var etManualOverrideExpiryDate: EditText
    private lateinit var etCpFees: EditText
    private lateinit var etDepositAmount: EditText
    private lateinit var etTotalProductPrice: EditText
    private lateinit var etRetailerName: EditText
    private lateinit var etRetailerBranch: EditText
    private lateinit var etVgApplicationId: EditText
    private lateinit var etContractSigned: EditText
    private lateinit var etDateOfFirstCall: EditText
    private lateinit var etLastCallOutcome: EditText
    private lateinit var etAsOnDate: EditText
    private lateinit var etRemarksAdditional: EditText
    private lateinit var etWalletAccountAdditional: EditText

    // ─── Buttons ───
    private lateinit var btnUpload: Button
    private lateinit var btnList: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_master)

        initViews()
        populateFromIntent()
        setupButtons()
    }

    private fun initViews() {
        // Loan Details
        etCustomerId            = findViewById(R.id.etCustomerId)
        etCustomerName          = findViewById(R.id.etCustomerName)
        etCustomerStatus        = findViewById(R.id.etCustomerStatus)
        etAccountType           = findViewById(R.id.etAccountType)
        etLoanNo                = findViewById(R.id.etLoanNo)
        etLoanName              = findViewById(R.id.etLoanName)
        etAssignedBranch        = findViewById(R.id.etAssignedBranch)
        etOpenDate              = findViewById(R.id.etOpenDate)
        etApprovalDate          = findViewById(R.id.etApprovalDate)
        etLastModifiedDate      = findViewById(R.id.etLastModifiedDate)
        etLastReviewDate        = findViewById(R.id.etLastReviewDate)
        etAccountStatus         = findViewById(R.id.etAccountStatus)
        etCurrencyCode          = findViewById(R.id.etCurrencyCode)
        etLoanAmount            = findViewById(R.id.etLoanAmount)
        etSubStatus             = findViewById(R.id.etSubStatus)
        etPaymentMethod         = findViewById(R.id.etPaymentMethod)
        etPenaltyRate           = findViewById(R.id.etPenaltyRate)
        etRateOfInterest        = findViewById(R.id.etRateOfInterest)
        etDisbursementDate      = findViewById(R.id.etDisbursementDate)
        etFirstPaymentDate      = findViewById(R.id.etFirstPaymentDate)
        etRemarks1              = findViewById(R.id.etRemarks1)
        etRepaymentMethod       = findViewById(R.id.etRepaymentMethod)
        etRepaymentInstalments  = findViewById(R.id.etRepaymentInstalments)
        etWalletAccount         = findViewById(R.id.etWalletAccount)
        etPrincipalDue          = findViewById(R.id.etPrincipalDue)
        etPrincipalPaid         = findViewById(R.id.etPrincipalPaid)
        etPrincipalBalance      = findViewById(R.id.etPrincipalBalance)
        etInterestDue           = findViewById(R.id.etInterestDue)
        etInterestPaid          = findViewById(R.id.etInterestPaid)
        etInterestBalance       = findViewById(R.id.etInterestBalance)
        etFeeDue                = findViewById(R.id.etFeeDue)
        etFeePaid               = findViewById(R.id.etFeePaid)
        etFeeBalance            = findViewById(R.id.etFeeBalance)
        etPenaltyDue            = findViewById(R.id.etPenaltyDue)
        etPenaltyPaid           = findViewById(R.id.etPenaltyPaid)
        etPenaltyBalance        = findViewById(R.id.etPenaltyBalance)

        // Additional Details
        etSalesProcessedByVGID      = findViewById(R.id.etSalesProcessedByVGID)
        etSalesProcessedFor         = findViewById(R.id.etSalesProcessedFor)
        etSalesReferredBy           = findViewById(R.id.etSalesReferredBy)
        etEmploymentStatus          = findViewById(R.id.etEmploymentStatus)
        etJobTitle                  = findViewById(R.id.etJobTitle)
        etEmployerName              = findViewById(R.id.etEmployerName)
        etTuCore                    = findViewById(R.id.etTuCore)
        etTuProbability             = findViewById(R.id.etTuProbability)
        etTuFullName                = findViewById(R.id.etTuFullName)
        etTuReasons                 = findViewById(R.id.etTuReasons)
        etTureason1                 = findViewById(R.id.etTureason1)
        etTureason2                 = findViewById(R.id.etTureason2)
        etDisposableIncome          = findViewById(R.id.etDisposableIncome)
        etManualOverrideAmount      = findViewById(R.id.etManualOverrideAmount)
        etManualOverrideExpiryDate  = findViewById(R.id.etManualOverrideExpiryDate)
        etCpFees                    = findViewById(R.id.etCpFees)
        etDepositAmount             = findViewById(R.id.etDepositAmount)
        etTotalProductPrice         = findViewById(R.id.etTotalProductPrice)
        etRetailerName              = findViewById(R.id.etRetailerName)
        etRetailerBranch            = findViewById(R.id.etRetailerBranch)
        etVgApplicationId           = findViewById(R.id.etVgApplicationId)
        etContractSigned            = findViewById(R.id.etContractSigned)
        etDateOfFirstCall           = findViewById(R.id.etDateOfFirstCall)
        etLastCallOutcome           = findViewById(R.id.etLastCallOutcome)
        etAsOnDate                  = findViewById(R.id.etAsOnDate)
        etRemarksAdditional         = findViewById(R.id.etRemarksAdditional)
        etWalletAccountAdditional   = findViewById(R.id.etWalletAccountAdditional)

        btnUpload = findViewById(R.id.btnUpload)
        btnList   = findViewById(R.id.btnList)
    }

    // ─── If opened from a list with data, pre-fill fields ───
    private fun populateFromIntent() {
        val i = intent
        etCustomerId.setText(i.getStringExtra("customerId") ?: "")
        etCustomerName.setText(i.getStringExtra("customerName") ?: "")
        etCustomerStatus.setText(i.getStringExtra("customerStatus") ?: "")
        etAccountType.setText(i.getStringExtra("accountType") ?: "")
        etLoanNo.setText(i.getStringExtra("loanNo") ?: "")
        etLoanName.setText(i.getStringExtra("loanName") ?: "")
        etAssignedBranch.setText(i.getStringExtra("assignedBranch") ?: "")
        etOpenDate.setText(i.getStringExtra("openDate") ?: "")
        etApprovalDate.setText(i.getStringExtra("approvalDate") ?: "")
        etLastModifiedDate.setText(i.getStringExtra("lastModifiedDate") ?: "")
        etLastReviewDate.setText(i.getStringExtra("lastReviewDate") ?: "")
        etAccountStatus.setText(i.getStringExtra("accountStatus") ?: "")
        etCurrencyCode.setText(i.getStringExtra("currencyCode") ?: "")
        etLoanAmount.setText(i.getStringExtra("loanAmount") ?: "")
        etSubStatus.setText(i.getStringExtra("subStatus") ?: "")
        etPaymentMethod.setText(i.getStringExtra("paymentMethod") ?: "")
        etPenaltyRate.setText(i.getStringExtra("penaltyRate") ?: "")
        etRateOfInterest.setText(i.getStringExtra("rateOfInterest") ?: "")
        etDisbursementDate.setText(i.getStringExtra("disbursementDate") ?: "")
        etFirstPaymentDate.setText(i.getStringExtra("firstPaymentDate") ?: "")
        etRemarks1.setText(i.getStringExtra("remarks1") ?: "")
        etRepaymentMethod.setText(i.getStringExtra("repaymentMethod") ?: "")
        etRepaymentInstalments.setText(i.getStringExtra("repaymentInstalments") ?: "")
        etWalletAccount.setText(i.getStringExtra("walletAccount") ?: "")
        etPrincipalDue.setText(i.getStringExtra("principalDue") ?: "")
        etPrincipalPaid.setText(i.getStringExtra("principalPaid") ?: "")
        etPrincipalBalance.setText(i.getStringExtra("principalBalance") ?: "")
        etInterestDue.setText(i.getStringExtra("interestDue") ?: "")
        etInterestPaid.setText(i.getStringExtra("interestPaid") ?: "")
        etInterestBalance.setText(i.getStringExtra("interestBalance") ?: "")
        etFeeDue.setText(i.getStringExtra("feeDue") ?: "")
        etFeePaid.setText(i.getStringExtra("feePaid") ?: "")
        etFeeBalance.setText(i.getStringExtra("feeBalance") ?: "")
        etPenaltyDue.setText(i.getStringExtra("penaltyDue") ?: "")
        etPenaltyPaid.setText(i.getStringExtra("penaltyPaid") ?: "")
        etPenaltyBalance.setText(i.getStringExtra("penaltyBalance") ?: "")
    }

    private fun setupButtons() {
        btnUpload.setOnClickListener {
            Toast.makeText(this, "Upload clicked", Toast.LENGTH_SHORT).show()
        }
        btnList.setOnClickListener {
            // TODO: Navigate to Loan Master List
             startActivity(Intent(this, LoanMasterListActivity::class.java))
            Toast.makeText(this, "Navigate to List", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}