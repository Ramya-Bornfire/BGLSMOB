package com.example.bgls

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import com.example.bgls.Retrofit.RetrofitClient

class SchemeCodeAddActivity : AppCompatActivity() {

    // Header Information
    private lateinit var tvProduct: EditText
    private lateinit var tvProductType: EditText
    private lateinit var tvId: EditText
    private lateinit var tvState: EditText
    private lateinit var tvProductCategory: EditText
    private lateinit var tvProductDescription: EditText

    // Product Availability
    private lateinit var etAvailableTo: EditText
    private lateinit var etBranches: EditText

    // New Account Settings
    private lateinit var etIdType: EditText
    private lateinit var etUsingTemplate: EditText
    private lateinit var etInitialAccountState: EditText

    // Loan Amount
    private lateinit var etLoanAmountConstraints: EditText
    private lateinit var etAccountsManagedUnderCredit: EditText

    // Interest Rate
    private lateinit var etInterestCalcMethod: EditText
    private lateinit var etAccruedIntPostFreq: EditText
    private lateinit var etInterestType: EditText
    private lateinit var etInterestRateCharged: EditText
    private lateinit var etInterestRateConstraints: EditText
    private lateinit var etDaysInYear: EditText
    private lateinit var etRepaymentIntCalc: EditText
    private lateinit var cbAccruedIntAfterMaturity: CheckBox

    // Repayment Scheduling
    private lateinit var etPaymentIntervalMethod: EditText
    private lateinit var etRepaymentMadeEvery: EditText
    private lateinit var etInstallmentConstraints: EditText
    private lateinit var etFirstDueDateOffset: EditText
    private lateinit var etCollectPrincipalEvery: EditText
    private lateinit var etGracePeriod: EditText
    private lateinit var etRoundOffRepaySchedule: EditText
    private lateinit var etRoundOffRepayCurrency: EditText
    private lateinit var etNonWorkingDaysResched: EditText

    // Repayment Schedule Edit
    private lateinit var cbAdjustPaymentDates: CheckBox
    private lateinit var cbAdjustPrincipalSchedule: CheckBox
    private lateinit var cbAdjustInterestSchedule: CheckBox
    private lateinit var cbAdjustFeeSchedule: CheckBox
    private lateinit var cbAdjustPenaltySchedule: CheckBox
    private lateinit var cbConfigurePaymentHolidays: CheckBox

    // Repayment Collection
    private lateinit var etPrepaymentAcceptance: EditText
    private lateinit var etAcceptPrepayFutureInt: EditText
    private lateinit var etRepaymentAllocationOrder: EditText

    // Arrears Setting
    private lateinit var etArrearsTolerancePeriod: EditText
    private lateinit var etArrearsDayCalcFrom: EditText
    private lateinit var etArrearsToleranceAmt: EditText
    private lateinit var etWithAFloor: EditText
    private lateinit var etNonWorkingDaysArrears: EditText

    // Penalties Setting
    private lateinit var etPenaltyCalcMethod: EditText
    private lateinit var etPenaltyTolerancePeriod: EditText
    private lateinit var etPenaltyRateConstraints: EditText
    private lateinit var etPenaltyRateChange: EditText

    // Transaction Settings
    private lateinit var transactionSettingsContainer: LinearLayout

    // Internal Controls
    private lateinit var cbCloseDormantAccounts: CheckBox
    private lateinit var cbLockArrearsAccount: CheckBox
    private lateinit var cbCapCharges: CheckBox

    // Product Fees
    private lateinit var cbAllowArbitraryFees: CheckBox
    private lateinit var feesContainer: LinearLayout

    // Product Links
    private lateinit var tvLinkedDepositProduct: TextView
    private lateinit var tvSettlementOptions: TextView
    private lateinit var cbEnableLinking: CheckBox

    // Deposit Account Option
    private lateinit var cbAutoSetSettlementAcct: CheckBox
    private lateinit var cbAutoCreateSettlementAcct: CheckBox

    // Securities
    private lateinit var cbEnableGuarantors: CheckBox
    private lateinit var cbEnableCollaterals: CheckBox
    private lateinit var tvRequiredSecurities: TextView

    // Buttons

    private lateinit var btnModify: Button

    private lateinit var btnBack: android.widget.ImageView
    private lateinit var btnHome: android.widget.ImageView

    // Mode flag - true for Add mode, false for View mode
    private var isAddMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheme_code_add)

        initializeViews()
        setupClickListeners()
        clearAllFields() // Clear all fields on start
        setFieldsEditable(false) // Make all fields non-editable initially

        // Check if we're in Add mode or View mode
        isAddMode = intent.getBooleanExtra("MODE", true)
        if (isAddMode) {
            btnModify.text = "Add"
        } else {
            btnModify.text = "Modify"
            // If in view mode, you would load data from API here
        }
    }

    private fun initializeViews() {
        // Header Information
        tvProduct = findViewById(R.id.tvProduct)
        tvProductType = findViewById(R.id.tvProductType)
        tvId = findViewById(R.id.tvId)
        tvState = findViewById(R.id.tvState)
        tvProductCategory = findViewById(R.id.tvProductCategory)
        tvProductDescription = findViewById(R.id.tvProductDescription)

        // Product Availability
        etAvailableTo = findViewById(R.id.etAvailableTo)
        etBranches = findViewById(R.id.etBranches)

        // New Account Settings
        etIdType = findViewById(R.id.etIdType)
        etUsingTemplate = findViewById(R.id.etUsingTemplate)
        etInitialAccountState = findViewById(R.id.etInitialAccountState)

        // Loan Amount
        etLoanAmountConstraints = findViewById(R.id.etLoanAmountConstraints)
        etAccountsManagedUnderCredit = findViewById(R.id.etAccountsManagedUnderCredit)

        // Interest Rate
        etInterestCalcMethod = findViewById(R.id.etInterestCalcMethod)
        etAccruedIntPostFreq = findViewById(R.id.etAccruedIntPostFreq)
        etInterestType = findViewById(R.id.etInterestType)
        etInterestRateCharged = findViewById(R.id.etInterestRateCharged)
        etInterestRateConstraints = findViewById(R.id.etInterestRateConstraints)
        etDaysInYear = findViewById(R.id.etDaysInYear)
        etRepaymentIntCalc = findViewById(R.id.etRepaymentIntCalc)
        cbAccruedIntAfterMaturity = findViewById(R.id.cbAccruedIntAfterMaturity)

        // Repayment Scheduling
        etPaymentIntervalMethod = findViewById(R.id.etPaymentIntervalMethod)
        etRepaymentMadeEvery = findViewById(R.id.etRepaymentMadeEvery)
        etInstallmentConstraints = findViewById(R.id.etInstallmentConstraints)
        etFirstDueDateOffset = findViewById(R.id.etFirstDueDateOffset)
        etCollectPrincipalEvery = findViewById(R.id.etCollectPrincipalEvery)
        etGracePeriod = findViewById(R.id.etGracePeriod)
        etRoundOffRepaySchedule = findViewById(R.id.etRoundOffRepaySchedule)
        etRoundOffRepayCurrency = findViewById(R.id.etRoundOffRepayCurrency)
        etNonWorkingDaysResched = findViewById(R.id.etNonWorkingDaysResched)

        // Repayment Schedule Edit
        cbAdjustPaymentDates = findViewById(R.id.cbAdjustPaymentDates)
        cbAdjustPrincipalSchedule = findViewById(R.id.cbAdjustPrincipalSchedule)
        cbAdjustInterestSchedule = findViewById(R.id.cbAdjustInterestSchedule)
        cbAdjustFeeSchedule = findViewById(R.id.cbAdjustFeeSchedule)
        cbAdjustPenaltySchedule = findViewById(R.id.cbAdjustPenaltySchedule)
        cbConfigurePaymentHolidays = findViewById(R.id.cbConfigurePaymentHolidays)

        // Repayment Collection
        etPrepaymentAcceptance = findViewById(R.id.etPrepaymentAcceptance)
        etAcceptPrepayFutureInt = findViewById(R.id.etAcceptPrepayFutureInt)
        etRepaymentAllocationOrder = findViewById(R.id.etRepaymentAllocationOrder)

        // Arrears Setting
        etArrearsTolerancePeriod = findViewById(R.id.etArrearsTolerancePeriod)
        etArrearsDayCalcFrom = findViewById(R.id.etArrearsDayCalcFrom)
        etArrearsToleranceAmt = findViewById(R.id.etArrearsToleranceAmt)
        etWithAFloor = findViewById(R.id.etWithAFloor)
        etNonWorkingDaysArrears = findViewById(R.id.etNonWorkingDaysArrears)

        // Penalties Setting
        etPenaltyCalcMethod = findViewById(R.id.etPenaltyCalcMethod)
        etPenaltyTolerancePeriod = findViewById(R.id.etPenaltyTolerancePeriod)
        etPenaltyRateConstraints = findViewById(R.id.etPenaltyRateConstraints)
        etPenaltyRateChange = findViewById(R.id.etPenaltyRateChange)

        // Internal Controls
        cbCloseDormantAccounts = findViewById(R.id.cbCloseDormantAccounts)
        cbLockArrearsAccount = findViewById(R.id.cbLockArrearsAccount)
        cbCapCharges = findViewById(R.id.cbCapCharges)

        // Product Fees
        cbAllowArbitraryFees = findViewById(R.id.cbAllowArbitraryFees)

        // Product Links
        tvLinkedDepositProduct = findViewById(R.id.tvLinkedDepositProduct)
        tvSettlementOptions = findViewById(R.id.tvSettlementOptions)
        cbEnableLinking = findViewById(R.id.cbEnableLinking)

        // Deposit Account Option
        cbAutoSetSettlementAcct = findViewById(R.id.cbAutoSetSettlementAcct)
        cbAutoCreateSettlementAcct = findViewById(R.id.cbAutoCreateSettlementAcct)

        // Securities
        cbEnableGuarantors = findViewById(R.id.cbEnableGuarantors)
        cbEnableCollaterals = findViewById(R.id.cbEnableCollaterals)
        tvRequiredSecurities = findViewById(R.id.tvRequiredSecurities)

        // Find Containers
        transactionSettingsContainer = findViewById(R.id.transactionSettingsContainer)
        feesContainer = findViewById(R.id.feesContainer)

        // Find Buttons

        btnModify = findViewById(R.id.btnModify)

        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun clearAllFields() {
        // Clear all EditText fields
        val allEditTexts = getAllEditTexts(findViewById<ViewGroup>(android.R.id.content))
        allEditTexts.forEach { editText ->
            editText.text.clear()
        }

        // Reset all CheckBoxes to false (unchecked)
        val allCheckBoxes = getAllCheckBoxes(findViewById<ViewGroup>(android.R.id.content))
        allCheckBoxes.forEach { checkBox ->
            checkBox.isChecked = false
        }
    }

    private fun setFieldsEditable(enabled: Boolean) {
        val contentView = window.decorView.findViewById<ViewGroup>(android.R.id.content)

        // Set all EditText fields
        val allEditTexts = getAllEditTexts(contentView)
        allEditTexts.forEach { editText ->
            editText.isEnabled = enabled
            editText.isFocusable = enabled
            editText.isFocusableInTouchMode = enabled
            editText.background = if (enabled) {
                ContextCompat.getDrawable(this, android.R.drawable.editbox_background)
            } else {
                ContextCompat.getDrawable(this, android.R.drawable.editbox_background)
            }
        }

        // Set all CheckBoxes
        val allCheckBoxes = getAllCheckBoxes(contentView)
        allCheckBoxes.forEach { checkBox ->
            checkBox.isEnabled = enabled
            checkBox.isClickable = enabled
        }
    }

    private fun getAllEditTexts(view: View): List<EditText> {
        val editTexts = mutableListOf<EditText>()
        if (view is EditText) {
            editTexts.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                editTexts.addAll(getAllEditTexts(view.getChildAt(i)))
            }
        }
        return editTexts
    }

    private fun getAllCheckBoxes(view: View): List<CheckBox> {
        val checkBoxes = mutableListOf<CheckBox>()
        if (view is CheckBox) {
            checkBoxes.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                checkBoxes.addAll(getAllCheckBoxes(view.getChildAt(i)))
            }
        }
        return checkBoxes
    }

    private fun setupClickListeners() {


        btnModify.setOnClickListener {
            if (isAddMode) {
                // Enable editing to add new data
                setFieldsEditable(true)
                isAddMode = false
                btnModify.text = "Save"
                Toast.makeText(this, "You can now enter data", Toast.LENGTH_SHORT).show()
            } else {
                // Save the data (will call API later)
                saveParameterData()
            }
        }

      

        btnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun saveParameterData() {
        // Here you will make your API call to save the data
        // Collect all data from EditTexts and CheckBoxes

        val schemeCode = com.example.bgls.DataModels.SchemeCode(
            product = tvProduct.text.toString(),
            productType = tvProductType.text.toString(),
            id = tvId.text.toString(),
            state = tvState.text.toString(),
            productCategory = tvProductCategory.text.toString(),
            productDescription = tvProductDescription.text.toString(),
            availableTo = etAvailableTo.text.toString(),
            branches = etBranches.text.toString()
        )

        val apiCall = if (btnModify.text == "Add") {
            RetrofitClient.api.addParameter(schemeCode)
        } else {
            RetrofitClient.api.updateParameter(schemeCode)
        }

        apiCall.enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SchemeCodeAddActivity, "Data saved successfully!", Toast.LENGTH_SHORT).show()
                    setFieldsEditable(false)
                    isAddMode = true
                    btnModify.text = "Add"
                    finish()
                } else {
                    Toast.makeText(this@SchemeCodeAddActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                Toast.makeText(this@SchemeCodeAddActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Parameter")
            .setMessage("Are you sure you want to delete this parameter?")
            .setPositiveButton("Delete") { _, _ ->
                deleteParameter()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteParameter() {
        val schemeCode = com.example.bgls.DataModels.SchemeCode(
            id = tvId.text.toString()
        )
        RetrofitClient.api.deleteParameter(schemeCode).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SchemeCodeAddActivity, "Parameter deleted successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@SchemeCodeAddActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                Toast.makeText(this@SchemeCodeAddActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}