package com.example.bgls.CustomerOnBoarding

import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.databinding.ActivityCustomerAccountOpeningBinding
import com.google.android.material.tabs.TabLayout
import com.example.bgls.util.SignaturePadView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CustomerAccountOpeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerAccountOpeningBinding
    private lateinit var pickPhotoLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>
    private lateinit var pickSignatureLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>
    private lateinit var pickDocumentLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest>
    private var activeImageTarget: android.widget.ImageView? = null
    private var activeTextTarget: android.widget.TextView? = null

    private var generatedAccountNo: String = ""
    private var schemeGlCode: String = ""
    private var schemeGlDesc: String = ""
    private var schemeGlshCode: String = ""
    private var schemeGlshDesc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCustomerAccountOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupHeader()
        setupPickers()
        setupTabs()
        setupSpinners()
        receiveData()
        setupDatePickers()
        setupMandatoryLabels()
        setupDocumentMaster()
        setupSignatureUpload()
        setupPersonalDetails()
        setupCalculations()
        
        binding.btnDtiValidation.setOnClickListener { showDtiValidationDialog() }
        binding.btnSchedule.setOnClickListener { showScheduleDialog() }
        binding.btnDepositFlow.setOnClickListener { showDepositFlowDialog() }

        binding.btnPrevious.setOnClickListener {
            finish()
        }

        binding.btnNext.setOnClickListener {
            if (validateCurrentTab()) {
                val currentTab = binding.tabLayout.selectedTabPosition
                when (currentTab) {
                    0 -> savePersonalDetails()
                    1 -> saveAccountDetails()
                    2 -> uploadDocuments { binding.tabLayout.getTabAt(3)?.select() }
                    3 -> uploadSignaturesAndFinalize()
                    else -> {
                        if (currentTab < binding.tabLayout.tabCount - 1) {
                            binding.tabLayout.getTabAt(currentTab + 1)?.select()
                        }
                    }
                }
            }
        }

        binding.spSchemeType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()
                when (selected) {
                    "LOAN ACCOUNT"-> {
                        binding.layoutLoanFields.visibility = View.VISIBLE
                        binding.layoutDepositFields.visibility = View.GONE
                        if (selected != "SELECT") fetchSchemeDetails(selected)
                    }
                    "DEPOSIT ACCOUNT" -> {
                        binding.layoutLoanFields.visibility = View.GONE
                        binding.layoutDepositFields.visibility = View.VISIBLE
                        fetchSchemeDetails(selected)
                    }
                    else -> {
                        binding.layoutLoanFields.visibility = View.GONE
                        binding.layoutDepositFields.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun fetchSchemeDetails(selected: String) {
        val isDeposit = selected == "DEPOSIT ACCOUNT"
        val schemeCode = when (selected) {
            "DEPOSIT ACCOUNT" -> "TDFIXED"
            "LOAN ACCOUNT" -> "LSRET"
            "SAVINGS ACCOUNT" -> "SBRET"
            "CURRENT ACCOUNT" -> "CARET"
            else -> "LSRET"
        }

        val glCode   = if (isDeposit) "2000" else "1000"
        val glDesc   = if (isDeposit) "Liability" else "Asset"
        val glshCode = if (isDeposit) "2500" else "1500"
        val glshDesc = if (isDeposit) "TERM DEPOSIT GENERAL" else "LOAN ACCOUNT GENERAL"

        val branchId   = intent.getStringExtra("primary_branch") ?: ""
        val branchName = intent.getStringExtra("branch_name") ?: ""
        val today      = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())

        binding.etSchemeCode.setText(schemeCode)
        binding.etGlCode.setText(glCode)
        binding.etGlDesc.setText(glDesc)
        binding.etGlshCode.setText(glshCode)
        binding.etGlshDesc.setText(glshDesc)
        binding.etAccountBranchId.setText(branchId)
        binding.etAccountBranchName.setText(branchName)

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getSchemeDetails(schemeCode)
                }
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string() ?: ""
                    android.util.Log.d("SchemeDetails", "Response: $rawJson")
                    
                    var loanAmount = ""
                    var loanPeriod = ""
                    var interestRateLoan = ""
                    var collateralMargin = ""
                    var recoveryMethod = ""
                    var repaymentPeriod = ""
                    
                    var depositAmount = ""
                    var depositPeriod = ""
                    var interestRateDeposit = ""
                    var interestCompoundFrequency = ""

                    try {
                        val json = org.json.JSONObject(rawJson)
                        generatedAccountNo = json.optString("loanAccountNo", json.optString("accountNo", ""))

                        val data = json.optJSONObject("data")
                        if (data != null) {
                            val safeString = { key: String, fallback: String ->
                                val value = data.optString(key)
                                if (value == "null" || value.isEmpty()) fallback else value
                            }

                            schemeGlCode   = safeString("gl_code", glCode)
                            schemeGlDesc   = safeString("schmdesc", glDesc)
                            schemeGlshCode = safeString("glsh", glshCode)
                            schemeGlshDesc = glshDesc
                            
                            loanAmount = safeString("loan_amount", "")
                            loanPeriod = safeString("loan_period", "")
                            interestRateLoan = safeString("interest_rate_loan", "")
                            collateralMargin = safeString("collateral_margin", "")
                            recoveryMethod = safeString("recovery_method", "")
                            repaymentPeriod = safeString("repayment_period", "")
                            
                            depositAmount = safeString("deposit_amount", "")
                            depositPeriod = safeString("deposit_period", "")
                            interestRateDeposit = safeString("interest_rate_deposit", "")
                            interestCompoundFrequency = safeString("interest_compund_frequency", "")
                        } else {
                            schemeGlCode   = glCode
                            schemeGlDesc   = glDesc
                            schemeGlshCode = glshCode
                            schemeGlshDesc = glshDesc
                        }
                    } catch (jsonEx: Exception) {
                        android.util.Log.e("SchemeDetails", "JSON parse error: ${jsonEx.message}")
                        generatedAccountNo = ""
                        schemeGlCode   = glCode
                        schemeGlDesc   = glDesc
                        schemeGlshCode = glshCode
                        schemeGlshDesc = glshDesc
                    }

                    withContext(Dispatchers.Main) {
                        binding.etSchemeCode.setText(schemeCode)
                        binding.etGlCode.setText(schemeGlCode)
                        binding.etGlDesc.setText(schemeGlDesc)
                        binding.etGlshCode.setText(schemeGlshCode)
                        binding.etGlshDesc.setText(schemeGlshDesc)
                        binding.etAccountBranchId.setText(branchId)
                        binding.etAccountBranchName.setText(branchName)
                        if (isDeposit) {
                            binding.etDepositAccountNo.setText(generatedAccountNo)
                            binding.etDateOfDeposit.setText(today)
                            binding.etDepositAmount.setText(depositAmount)
                            binding.etDepositPeriod.setText(depositPeriod)
                            binding.etRateOfInterest.setText(interestRateDeposit)
                            binding.etCompoundingFactor.setText(interestCompoundFrequency)
                        } else {
                            binding.etLoanAccountNo.setText(generatedAccountNo)
                            binding.etDateOfLoan.setText(today)
                            binding.etLoanSanctioned.setText(loanAmount)
                            binding.etLoanPeriod.setText(loanPeriod)
                            binding.etInterestRate.setText(interestRateLoan)
                            binding.etFeesRate.setText(interestRateLoan) // assuming fees rate and interest rate fall back to this, adjust as needed
                            binding.etMargin.setText(collateralMargin)
                            binding.etRepaymentTerms.setText(repaymentPeriod)

                            // Setup recovery method spinner if applicable
                            if (recoveryMethod.isNotEmpty()) {
                                val adapter = binding.spRecoveryMethod.adapter
                                if (adapter != null) {
                                    for (i in 0 until adapter.count) {
                                        if (adapter.getItem(i).toString().equals(recoveryMethod, ignoreCase = true)) {
                                            binding.spRecoveryMethod.setSelection(i)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    android.util.Log.e("SchemeDetails", "API error: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        if (isDeposit) binding.etDateOfDeposit.setText(today)
                        else binding.etDateOfLoan.setText(today)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("SchemeDetails", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun savePersonalDetails() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Saving Personal Details...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val customerType = binding.etCustomerType.text.toString()
                val request = com.example.bgls.DataModels.CustomerRequest(
                    la_customer_type = intent.getStringExtra("customer_type") ?: "",
                    cif_id = intent.getStringExtra("cif_id") ?: "",
                    ca_solid = intent.getStringExtra("primary_branch") ?: "",
                    ca_acct_opendate = formatDateForBackend(binding.etAccountOpenDate.text?.toString() ?: ""),
                    ca_date_of_birth = formatDateForBackend(binding.etDateOfBirth.text?.toString() ?: ""),
                    ca_address_validation_form = formatDateForBackend(binding.etAddressValidFrom.text?.toString() ?: ""),
                    ca_remarks = "", // Update dynamically if you have an etRemarks binding
                    shortName = intent.getStringExtra("short_name") ?: "",
                    ca_customer_type = customerType,
                    ca_customer_type_1 = customerType, 
                    ca_first_name = binding.etFirstName.text?.toString() ?: "",
                    mid_name = binding.etMiddleName.text?.toString() ?: "",
                    ca_last_name = binding.etLastName.text?.toString() ?: "",
                    ca_preferred_name = binding.etFullName.text?.toString() ?: "",
                    
                    ca_currency = intent.getStringExtra("currency") ?: "", 
                    loan_obligations = "",
                    family_maintenance = "",
                    ca_email_id = binding.etEmailIdAO.text?.toString() ?: "",
                    ca_countrycode_1 = "",
                    ca_mobile_number = binding.etMobileNoAO.text?.toString() ?: "",
                    ca_saluation = binding.spSalutation.selectedItem?.toString() ?: "",
                    ca_gender = binding.spGender.selectedItem?.toString() ?: "",
                    ca_martial_staus = binding.spMartialStatus.selectedItem?.toString() ?: "",
                    ca_occupation1 = binding.spOccupation.selectedItem?.toString() ?: "",
                    annual_income = binding.etAnnualIncome.text?.toString() ?: "",
                    monthly_income = binding.etMonthlyIncome.text?.toString() ?: "",
                    ca_address_type = binding.spAddressType.selectedItem?.toString() ?: "",
                    ca_house_no = binding.etHouseNo.text?.toString() ?: "",
                    ca_street_no = binding.etStreetNo.text?.toString() ?: "",
                    ca_street_name = binding.etStreetName.text?.toString() ?: "",
                    ca_country = "", // Not explicitly found in Spinners; fetch if added
                    ca_state = "",
                    ca_city = binding.spCity.selectedItem?.toString() ?: "",
                    ca_postal_code = "",
                    
                    ca_nationality = binding.spNationality.selectedItem?.toString() ?: "",
                    ca_country_of_birth = binding.spCountryOfBirth.selectedItem?.toString() ?: "",
                    countryOrigin = binding.spCountryOfOrigin.selectedItem?.toString() ?: "",
                    branch_desc = intent.getStringExtra("branch_name") ?: "",
                    ca_cif_id_1 = intent.getStringExtra("cif_id") ?: ""
                )
                
                val params = request.toMap()

                val passno = intent.getStringExtra("passno") ?: ""
                val nationalid = intent.getStringExtra("nationalid") ?: ""
                val appRefNo = intent.getStringExtra("app_ref_no") ?: ""
                
                val recNo = if (customerType.equals("Joint Account", ignoreCase = true)) "2" else "1"
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.savePersonalDetail(appRefNo, recNo, passno, nationalid, params)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    var msg = "Personal Details Saved"
                    if (!rawJson.isNullOrBlank()) {
                        try {
                            val jsonObject = org.json.JSONObject(rawJson)
                            if (jsonObject.has("message")) {
                                msg = jsonObject.getString("message")
                            } else {
                                msg = rawJson
                            }
                        } catch (e: Exception) {
                            msg = rawJson
                        }
                    }
                    android.app.AlertDialog.Builder(this@CustomerAccountOpeningActivity)
                        .setMessage(msg)
                        .setPositiveButton("Okay") { dialog, _ ->
                            dialog.dismiss()
                            binding.tabLayout.getTabAt(1)?.select()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to save details", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveAccountDetails() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Saving Account Details...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            try {
                val appRefNo = intent.getStringExtra("app_ref_no") ?: ""
                val selected = binding.spSchemeType.selectedItem?.toString() ?: ""
                val isDeposit = selected == "FIXED DEPOSIT"
                val schemetype = if (isDeposit) "TD" else "LA"

                fun formatDateForBackend(s: String): String {
                    return try {
                        val parts = s.split("-")
                        if (parts.size == 3 && parts[2].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else s
                    } catch (e: Exception) { s }
                }

                val formData = mutableMapOf<String, Any>(
                    "schemetype"  to schemetype,
                    "schemecode"  to (binding.etSchemeCode.text?.toString() ?: ""),
                    "currency"    to "SCR",
                    "prisolid"    to (binding.etAccountBranchId.text?.toString() ?: ""),
                    "branch_desc" to (binding.etAccountBranchName.text?.toString() ?: ""),
                    "nationalid"  to (binding.etNationalIdAO.text?.toString() ?: ""),
                    "passno"      to (binding.etPassportNoAO.text?.toString() ?: ""),
                    "issuedate"   to formatDateForBackend(binding.etIssueDate.text?.toString() ?: ""),
                    "expdate"     to formatDateForBackend(binding.etExpiryDate.text?.toString() ?: "")
                )

                if (isDeposit) {
                    formData["gl_code"]           = binding.etGlCode.text?.toString() ?: ""
                    formData["gl_desc"]           = binding.etGlDesc.text?.toString() ?: ""
                    formData["glsh_code"]         = binding.etGlshCode.text?.toString() ?: ""
                    formData["glsh_desc"]         = binding.etGlshDesc.text?.toString() ?: ""
                    formData["deposit_account_no"]= binding.etDepositAccountNo.text?.toString() ?: ""
                    formData["deposit_date"]      = binding.etDateOfDeposit.text?.toString() ?: ""
                    formData["deposit_amt"]       = binding.etDepositAmount.text?.toString() ?: ""
                    formData["deposit_period"]    = binding.etDepositPeriod.text?.toString() ?: ""
                    formData["maturity_date"]     = formatDateForBackend(binding.etMaturityDate.text?.toString() ?: "")
                    formData["rate_of_int"]       = binding.etRateOfInterest.text?.toString() ?: ""
                    formData["int_amt"]           = binding.etInterestAmount.text?.toString() ?: ""
                    formData["compounding_factor"]= binding.etCompoundingFactor.text?.toString() ?: ""
                    formData["maturity_amt"]      = binding.etMaturityAmount.text?.toString() ?: ""
                } else {
                    formData["gl_code_loan"]      = binding.etGlCode.text?.toString() ?: ""
                    formData["gl_desc_loan"]      = binding.etGlDesc.text?.toString() ?: ""
                    formData["glsh_code_loan"]    = binding.etGlshCode.text?.toString() ?: ""
                    formData["glsh_desc_loan"]    = binding.etGlshDesc.text?.toString() ?: ""
                    formData["account_no"]        = binding.etLoanAccountNo.text?.toString() ?: ""
                    formData["loan_sanctioned"]   = binding.etLoanSanctioned.text?.toString() ?: ""
                    formData["margin_limit"]      = binding.etMargin.text?.toString() ?: ""
                    formData["recovery_method"]   = binding.spRecoveryMethod.selectedItem?.toString() ?: ""
                    formData["la_remarks"]        = binding.etLoanRemarks.text?.toString() ?: ""
                    formData["loan_period"]       = binding.etLoanPeriod.text?.toString() ?: ""
                    formData["effective_fees_rate"]= binding.etFeesRate.text?.toString() ?: ""
                    formData["customer_type"]     = binding.etCustomerType.text?.toString() ?: ""
                    formData["monthly_income"]    = binding.etMonthlyIncome.text?.toString() ?: ""
                    formData["annual_income"]     = binding.etAnnualIncome.text?.toString() ?: ""
                }

                val finalAccountNo = if (isDeposit) binding.etDepositAccountNo.text?.toString() ?: "" else binding.etLoanAccountNo.text?.toString() ?: ""
                val body = mapOf(
                    "formData"      to formData,
                    "loanAccountNo" to finalAccountNo,
                    "accountNo"     to finalAccountNo,
                    "scheduleList"  to emptyList<Any>()
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.saveAccountDetails(appRefNo, body)
                }

                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val msg = response.body()?.string()?.takeIf { it.isNotBlank() } ?: "Account Details Saved Successfully"
                    android.app.AlertDialog.Builder(this@CustomerAccountOpeningActivity)
                        .setMessage(msg)
                        .setPositiveButton("Okay") { dialog, _ ->
                            dialog.dismiss()
                            binding.tabLayout.getTabAt(2)?.select()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    val errBody = response.errorBody()?.string() ?: "Failed to save Account Details"
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, errBody.take(200), android.widget.Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadDocuments(onComplete: () -> Unit) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Uploading Documents...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                val parts = mutableListOf<MultipartBody.Part>()
                
                val dummyBody = "dummy".toRequestBody("text/plain".toMediaTypeOrNull())
                parts.add(MultipartBody.Part.createFormData("files", "dummy.txt", dummyBody))
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.uploadImage(parts, appRefNo)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "Documents Uploaded"
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                    onComplete()
                } else {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to upload documents", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadSignaturesAndFinalize() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Submitting Application...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                val cifId = intent.getStringExtra("cif_id") ?: "CUST001"
                
                val schedulerList = org.json.JSONArray()
                val photoParts = mutableListOf<okhttp3.MultipartBody.Part>()
                val signParts = mutableListOf<okhttp3.MultipartBody.Part>()
                
                for (i in 0 until binding.containerSignatureRows.childCount) {
                    val row = binding.containerSignatureRows.getChildAt(i) as? android.widget.LinearLayout ?: continue
                    val spinner = row.getChildAt(0) as? android.widget.Spinner
                    val etGroup = row.getChildAt(1) as? android.widget.EditText
                    val etKeyword = row.getChildAt(2) as? android.widget.EditText
                    val photoBox = row.getChildAt(3) as? android.widget.FrameLayout
                    val sigBox = row.getChildAt(4) as? android.widget.FrameLayout
                    
                    val photoImageView = photoBox?.getChildAt(0) as? android.widget.ImageView
                    val sigImageView = sigBox?.getChildAt(0) as? android.widget.ImageView
                    
                    val groupValue = spinner?.selectedItem?.toString() ?: "IND"
                    val accessCode = etGroup?.text?.toString()?.ifEmpty { "GRP" } ?: "GRP"
                    val keyword = etKeyword?.text?.toString()?.ifEmpty { "SIG" } ?: "SIG"
                    
                    val obj = org.json.JSONObject().apply {
                        put("appl_ref_no", appRefNo)
                        put("rec_no", (i + 1).toString())
                        put("img_access_code", accessCode)
                        put("img_group", groupValue)
                        put("keyword", keyword)
                    }
                    schedulerList.put(obj)
                    
                    val photoBytes = photoImageView?.let { getBytesFromImageView(it) } ?: ByteArray(0)
                    val pBody = photoBytes.toRequestBody("image/png".toMediaTypeOrNull())
                    photoParts.add(okhttp3.MultipartBody.Part.createFormData("photo", "photo_${i + 1}.png", pBody))
                    
                    val signBytes = sigImageView?.let { getBytesFromImageView(it) } ?: ByteArray(0)
                    val sBody = signBytes.toRequestBody("image/png".toMediaTypeOrNull())
                    signParts.add(okhttp3.MultipartBody.Part.createFormData("sign", "sign_${i + 1}.png", sBody))
                }
                
                if (schedulerList.length() == 0) {
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "No signatures to upload", android.widget.Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val schedulerBody = schedulerList.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val schedulerPart = okhttp3.MultipartBody.Part.createFormData("scheduler", "scheduler.json", schedulerBody)
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.addSignatureCorporate(schedulerPart, photoParts, signParts, cifId)
                }
                
                if (response.isSuccessful) {
                    val finalResponse = withContext(Dispatchers.IO) {
                        RetrofitClient.api.finalizeSubmission(
                            appRefNo = appRefNo,
                            recNo = "1"
                        )
                    }
                    
                    progressDialog.dismiss()
                    if (finalResponse.isSuccessful) {
                        val msg = finalResponse.body()?.string() ?: "Application Submitted Successfully"
                        android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, msg, android.widget.Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to finalize", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to upload signatures", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDocumentMaster() {
        binding.containerDocumentRows.removeAllViews()
        addDocumentRow()
        binding.btnAddDoc.setOnClickListener { addDocumentRow() }
        binding.btnRemoveDoc.setOnClickListener {
            val count = binding.containerDocumentRows.childCount
            if (count > 0) {
                binding.containerDocumentRows.removeViewAt(count - 1)
            }
        }
        binding.btnSubmitDoc.setOnClickListener {
            val dynamicValues = mutableListOf<Map<String, String>>()
            for (i in 0 until binding.containerDocumentRows.childCount) {
                val row = binding.containerDocumentRows.getChildAt(i) as android.widget.LinearLayout
                val spinner = row.getChildAt(0) as android.widget.Spinner
                val etCode = row.getChildAt(1) as android.widget.EditText
                val etDesc = row.getChildAt(2) as android.widget.EditText
                val etId = row.getChildAt(3) as android.widget.EditText
                val etPlace = row.getChildAt(4) as android.widget.EditText
                val etIssueDate = row.getChildAt(5) as android.widget.EditText
                val etExpiryDate = row.getChildAt(6) as android.widget.EditText
                val uploadLayout = row.getChildAt(7) as android.widget.LinearLayout
                val tvStatus = uploadLayout.getChildAt(1) as android.widget.TextView

                val docType = spinner.selectedItem?.toString() ?: ""
                val fileName = tvStatus.text.toString().takeIf { it != "No file" } ?: ""

                val map = mapOf(
                    "filename" to fileName,
                    "doctype" to docType,
                    "doccode" to etCode.text.toString(),
                    "doctypesesc" to etDesc.text.toString(),
                    "uniqueid" to etId.text.toString(),
                    "placeofissue" to etPlace.text.toString(),
                    "issuedate" to etIssueDate.text.toString(),
                    "exprydate" to etExpiryDate.text.toString()
                )
                dynamicValues.add(map)
            }

            val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
            val cifId = intent.getStringExtra("cif_id") ?: "CUST001"
            val recNo = "1"

            val progressDialog = android.app.ProgressDialog(this).apply {
                setMessage("Submitting Documents...")
                setCancelable(false)
                show()
            }

            lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.uploadDocumentMaster(
                            applRefNo = appRefNo,
                            recNo = recNo,
                            cifId = cifId,
                            dynamicValues = dynamicValues
                        )
                    }

                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        android.app.AlertDialog.Builder(this@CustomerAccountOpeningActivity)
                            .setMessage("Document Master Submitted Successfully")
                            .setPositiveButton("Okay") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .show()
                    } else {
                        android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to submit documents", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun addDocumentRow() {
        val context = this
        val row = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(4, 4, 4, 4)
        }

        val spinner = android.widget.Spinner(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(30), 1.2f).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) }
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.spinner_with_arrow)
            val docTypes = listOf("SELECT", "Passport", "National ID", "Driver License")
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_item, docTypes).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        fun createEditText(weight: Float, hint: String = "") = android.widget.EditText(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(30), weight).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) }
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.edittext_background)
            textSize = 10f
            setPadding(dpToPx(4), 0, dpToPx(4), 0)
            if (hint.isNotEmpty()) setHint(hint)
        }

        val etCode = createEditText(1.0f)
        val etDesc = createEditText(1.5f)
        val etId = createEditText(1.0f)
        val etPlace = createEditText(1.0f)
        val etIssueDate = createEditText(1.0f, "dd-mm-yyyy").apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        val etExpiryDate = createEditText(1.0f, "dd-mm-yyyy").apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }

        val uploadLayout = android.widget.LinearLayout(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(30), 1.5f).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) }
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.edittext_background)
            setPadding(dpToPx(4), 0, dpToPx(4), 0)

            val btnChoose = android.widget.Button(context).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, dpToPx(24))
                text = "Choose file"
                isAllCaps = false
                textSize = 8f
                setPadding(0, 0, 0, 0)
                minHeight = 0
                minWidth = 0
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0"))
                setTextColor(android.graphics.Color.parseColor("#333333"))
            }
            
            val tvStatus = android.widget.TextView(context).apply {
                text = "No file"
                textSize = 7f
                setPadding(dpToPx(2), 0, 0, 0)
                setTextColor(android.graphics.Color.parseColor("#666666"))
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            addView(btnChoose)
            addView(tvStatus)

            setOnClickListener {
                activeTextTarget = tvStatus
                pickDocumentLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }

        row.addView(spinner)
        row.addView(etCode)
        row.addView(etDesc)
        row.addView(etId)
        row.addView(etPlace)
        row.addView(etIssueDate)
        row.addView(etExpiryDate)
        row.addView(uploadLayout)

        binding.containerDocumentRows.addView(row)
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    
    private fun setupSignatureUpload() {
        binding.containerSignatureRows.removeAllViews()
        addSignatureRow()
        binding.btnAddSig.setOnClickListener { addSignatureRow() }
        binding.btnRemoveSig.setOnClickListener {
            val count = binding.containerSignatureRows.childCount
            if (count > 0) {
                binding.containerSignatureRows.removeViewAt(count - 1)
            }
        }
        binding.btnSubmitSig.setOnClickListener {
            uploadSignatures()
        }
    }

    private fun getMultipartBody(tag: Any?, paramName: String, context: android.content.Context, index: Int): okhttp3.MultipartBody.Part {
        if (tag == null) {
            val emptyBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
            return okhttp3.MultipartBody.Part.createFormData(paramName, "", emptyBody)
        }
        return try {
            when (tag) {
                is android.net.Uri -> {
                    val inputStream = context.contentResolver.openInputStream(tag)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
                        okhttp3.MultipartBody.Part.createFormData(paramName, "file_${index}.jpg", requestBody)
                    } else {
                        val emptyBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                        okhttp3.MultipartBody.Part.createFormData(paramName, "", emptyBody)
                    }
                }
                is android.graphics.Bitmap -> {
                    val stream = java.io.ByteArrayOutputStream()
                    tag.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    val requestBody = bytes.toRequestBody("image/png".toMediaTypeOrNull())
                    okhttp3.MultipartBody.Part.createFormData(paramName, "file_${index}.png", requestBody)
                }
                else -> {
                    val emptyBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                    okhttp3.MultipartBody.Part.createFormData(paramName, "", emptyBody)
                }
            }
        } catch (e: Exception) {
            val emptyBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
            okhttp3.MultipartBody.Part.createFormData(paramName, "", emptyBody)
        }
    }

    private fun uploadSignatures() {
        val childCount = binding.containerSignatureRows.childCount
        if (childCount == 0) {
            android.widget.Toast.makeText(this, "Please add at least one signature row", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Uploading Signatures...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                val cifId = binding.etCifId.text.toString().takeIf { it.isNotEmpty() } ?: "CUST0000140901"
                
                val requestsList = mutableListOf<Map<String, Any>>()
                val photoParts = mutableListOf<okhttp3.MultipartBody.Part>()
                val signParts = mutableListOf<okhttp3.MultipartBody.Part>()
                
                for (i in 0 until childCount) {
                    val row = binding.containerSignatureRows.getChildAt(i) as? android.widget.LinearLayout ?: continue
                    
                    val spinner = row.getChildAt(0) as android.widget.Spinner
                    val etGroup = row.getChildAt(1) as android.widget.EditText
                    val etKeyword = row.getChildAt(2) as android.widget.EditText
                    
                    val photoBox = row.getChildAt(3) as android.widget.FrameLayout
                    val sigBox = row.getChildAt(4) as android.widget.FrameLayout
                    
                    val photoTag = photoBox.getChildAt(0).tag
                    val signTag = sigBox.getChildAt(0).tag
                    
                    val sigData = mapOf(
                        "appl_ref_no" to appRefNo,
                        "rec_no" to (i + 1).toString(),
                        "img_access_code" to spinner.selectedItem.toString(),
                        "img_group" to etGroup.text.toString(),
                        "keyword" to etKeyword.text.toString()
                    )
                    requestsList.add(sigData)
                    
                    photoParts.add(getMultipartBody(photoTag, "photo", this@CustomerAccountOpeningActivity, i))
                    signParts.add(getMultipartBody(signTag, "sign", this@CustomerAccountOpeningActivity, i))
                }
                
                val gson = com.google.gson.Gson()
                val schedulerJson = gson.toJson(requestsList)
                val schedulerBody = schedulerJson.toRequestBody("application/json".toMediaTypeOrNull())
                val schedulerPart = okhttp3.MultipartBody.Part.createFormData("scheduler", "scheduler.json", schedulerBody)
                
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.api.addSignatureCorporate(schedulerPart, photoParts, signParts, cifId)
                }
                
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Signatures Uploaded Successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Upload Failed: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

    private fun addSignatureRow() {
        val context = this
        val row = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val spinner = android.widget.Spinner(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(30), 1.5f).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) }
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.spinner_with_arrow)
            val groups = listOf("Select", "Individual", "Joint", "Authorized Signatory")
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_item, groups).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        fun createEditText(weight: Float) = android.widget.EditText(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(30), weight).apply { setMargins(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2)) }
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.edittext_background)
            textSize = 10f
            setPadding(dpToPx(4), 0, dpToPx(4), 0)
        }

        val etGroup = createEditText(1.0f)
        val etKeyword = createEditText(1.5f)

        fun createPhotoPlaceholder(text: String, weight: Float, isPhoto: Boolean) = android.widget.FrameLayout(context).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(0, dpToPx(100), weight).apply { setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)) }
            background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.edittext_background)
            
            val imageView = android.widget.ImageView(context).apply {
                scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            }
            
            val textView = android.widget.TextView(context).apply {
                this.text = text
                textSize = 8f
                gravity = android.view.Gravity.CENTER
                layoutParams = android.widget.FrameLayout.LayoutParams(android.widget.FrameLayout.LayoutParams.WRAP_CONTENT, android.widget.FrameLayout.LayoutParams.WRAP_CONTENT).apply { gravity = android.view.Gravity.CENTER }
                setTextColor(android.graphics.Color.parseColor("#666666"))
            }

            addView(imageView)
            addView(textView)

            setOnClickListener {
                activeImageTarget = imageView
                activeTextTarget = textView
                
                if (isPhoto) {
                    pickPhotoLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    showSignatureOptionsDialog(imageView, textView)
                }
            }
        }

        val photoBox = createPhotoPlaceholder("CHOOSE IMAGE", 1.2f, true)
        val sigBox = createPhotoPlaceholder("SIGNATURE (Click to Choose)", 1.2f, false)

        row.addView(spinner)
        row.addView(etGroup)
        row.addView(etKeyword)
        row.addView(photoBox)
        row.addView(sigBox)

        binding.containerSignatureRows.addView(row)
    }

    private fun showSignatureOptionsDialog(imageView: android.widget.ImageView, textView: android.widget.TextView) {
        val options = arrayOf("Upload from Gallery", "Sign on Screen")
        android.app.AlertDialog.Builder(this)
            .setTitle("Signature Option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        activeImageTarget = imageView
                        activeTextTarget = textView
                        pickSignatureLauncher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    1 -> {
                        showSignaturePadDialog(imageView, textView)
                    }
                }
            }
            .show()
    }

    private fun showSignaturePadDialog(imageView: android.widget.ImageView, textView: android.widget.TextView) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_signature_pad)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val signatureView = dialog.findViewById<com.example.bgls.util.SignaturePadView>(R.id.signatureView)
        val btnClear = dialog.findViewById<android.widget.Button>(R.id.btnClear)
        val btnSave = dialog.findViewById<android.widget.Button>(R.id.btnSave)
        val btnCancel = dialog.findViewById<android.widget.Button>(R.id.btnCancel)

        btnClear.setOnClickListener { signatureView.clear() }
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            if (!signatureView.isEmpty()) {
                val bitmap = signatureView.getSignatureBitmap()
                imageView.setImageBitmap(bitmap)
                imageView.tag = bitmap
                textView.visibility = android.view.View.GONE
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(this, "Please sign first", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun setupMandatoryLabels() {
        val mandatoryViews = listOf(
            binding.tvLabelCustomerType to "Customer Type *",
            binding.tvLabelPrimaryBranch to "Primary Branch *",
            binding.tvLabelBranchName to "Branch Name *",
            binding.tvLabelAccountOpenDate to "Account Open Date *",
            binding.tvLabelSalutation to "Salutation *",
            binding.tvLabelFirstName to "First Name *",
            binding.tvLabelFullName to "Full Name *",
            binding.tvLabelGender to "Gender *",
            binding.tvLabelMartialStatus to "Martial Status *",
            binding.tvLabelOccupation to "Occupation *",
            binding.tvLabelDateOfBirth to "Date of Birth *",
            binding.tvLabelHomeCurrency to "Home Currency *",
            binding.tvLabelAnnualIncome to "Annual Income *",
            binding.tvLabelMonthlyIncome to "Monthly Income *",
            binding.tvLabelEmailId to "Email Id *",
            binding.tvLabelMobileNo to "Mobile No *",
            binding.tvLabelAddressType to "Address Type *",
            binding.tvLabelHouseNo to "House no *",
            binding.tvLabelStreetNo to "Street No *",
            binding.tvLabelStreetName to "Street Name *",
            binding.tvLabelCountry to "Country *",
            binding.tvLabelCity to "City *",
            binding.tvLabelAddressValidFrom to "Address Valid From *",
            binding.tvLabelNationality to "Nationality *",
            binding.tvLabelCountryOfBirth to "Country of Birth *",
            // Account Details Tab Labels
            binding.tvLabelSchemeType to "Scheme Type *",
            binding.tvLabelAccountBranchId to "Account Branch Id *",
            binding.tvLabelNationalIdAO to "National Id *",
            binding.tvLabelIssueDate to "Issue Date *",
            binding.tvLabelPassportNoAO to "Passport No *",
            binding.tvLabelExpiryDate to "Expiry Date *",
            // Signature Tab Labels
            binding.tvLabelImageAccessGroup to "Image Access Group *",
            binding.tvLabelPhoto to "Photo *",
            binding.tvLabelSignature to "Signature *"
        )

        mandatoryViews.forEach { (view, text) ->
            view.text = android.text.Html.fromHtml(text.replace("*", "<font color='#FF0000'>*</font>"), android.text.Html.FROM_HTML_MODE_LEGACY)
        }
    }

    private fun validateCurrentTab(): Boolean {
        val currentTab = binding.tabLayout.selectedTabPosition
        var isValid = true

        if (currentTab == 0) { // Personal Details
            if (binding.etAccountOpenDate.text.isNullOrBlank()) { binding.etAccountOpenDate.error = "Required field to enter"; isValid = false }
            if (binding.spSalutation.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Salutation", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etFirstName.text.isNullOrBlank()) { binding.etFirstName.error = "Required field to enter"; isValid = false }
            if (binding.etFullName.text.isNullOrBlank()) { binding.etFullName.error = "Required field to enter"; isValid = false }
            if (binding.spGender.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Gender", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.spMartialStatus.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Martial Status", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.spOccupation.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Occupation", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etAnnualIncome.text.isNullOrBlank()) { binding.etAnnualIncome.error = "Required field to enter"; isValid = false }
            if (binding.etMonthlyIncome.text.isNullOrBlank()) { binding.etMonthlyIncome.error = "Required field to enter"; isValid = false }
            if (binding.etEmailIdAO.text.isNullOrBlank()) { binding.etEmailIdAO.error = "Required field to enter"; isValid = false }
            if (binding.etMobileNoAO.text.isNullOrBlank()) { binding.etMobileNoAO.error = "Required field to enter"; isValid = false }

            // Address Details (Still in Tab 0 for now as per layout)
            if (binding.spAddressType.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Address Type", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etHouseNo.text.isNullOrBlank()) { binding.etHouseNo.error = "Required field to enter"; isValid = false }
            if (binding.etStreetNo.text.isNullOrBlank()) { binding.etStreetNo.error = "Required field to enter"; isValid = false }
            if (binding.etStreetName.text.isNullOrBlank()) { binding.etStreetName.error = "Required field to enter"; isValid = false }
            if (binding.spCity.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select City", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etAddressValidFrom.text.isNullOrBlank()) { binding.etAddressValidFrom.error = "Required field to enter"; isValid = false }
            if (binding.spNationality.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Nationality", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.spCountryOfBirth.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Country of Birth", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
        } else if (currentTab == 1) { // Account Details
            if (binding.spSchemeType.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Scheme Type", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            
            val selected = binding.spSchemeType.selectedItem?.toString() ?: ""
            if (selected == "LOAN ACCOUNT") {
                if (binding.etLoanSanctioned.text.isNullOrBlank()) { binding.etLoanSanctioned.error = "Required field to enter"; isValid = false }
                if (binding.etLoanPeriod.text.isNullOrBlank()) { binding.etLoanPeriod.error = "Required field to enter"; isValid = false }
                if (binding.etMargin.text.isNullOrBlank()) { binding.etMargin.error = "Required field to enter"; isValid = false }
                if (binding.etFeesRate.text.isNullOrBlank()) { binding.etFeesRate.error = "Required field to enter"; isValid = false }
            } else if (selected == "FIXED DEPOSIT") {
                if (binding.etDepositAmount.text.isNullOrBlank()) { binding.etDepositAmount.error = "Required field to enter"; isValid = false }
                if (binding.etDepositPeriod.text.isNullOrBlank()) { binding.etDepositPeriod.error = "Required field to enter"; isValid = false }
                if (binding.etRateOfInterest.text.isNullOrBlank()) { binding.etRateOfInterest.error = "Required field to enter"; isValid = false }
            }

            if (binding.etNationalIdAO.text.isNullOrBlank()) { binding.etNationalIdAO.error = "Required field to enter"; isValid = false }
            if (binding.etIssueDate.text.isNullOrBlank()) { binding.etIssueDate.error = "Required field to enter"; isValid = false }
            if (binding.etPassportNoAO.text.isNullOrBlank()) { binding.etPassportNoAO.error = "Required field to enter"; isValid = false }
            if (binding.etExpiryDate.text.isNullOrBlank()) { binding.etExpiryDate.error = "Required field to enter"; isValid = false }
        }

        return isValid
    }

    private fun setupDatePickers() {
        binding.etAccountOpenDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etAddressValidFrom.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etIssueDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etExpiryDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etLoanExpiryDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etInstallmentStartDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
        binding.etMaturityDate.apply { setOnClickListener { showDatePicker(this) }; isFocusable = false }
    }

    private fun showDatePicker(editText: android.widget.EditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun setupPickers() {
        pickPhotoLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { 
                activeImageTarget?.setImageURI(it)
                activeImageTarget?.tag = it
                activeTextTarget?.visibility = android.view.View.GONE
            }
        }
        pickSignatureLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { 
                activeImageTarget?.setImageURI(it)
                activeImageTarget?.tag = it
                activeTextTarget?.visibility = android.view.View.GONE
            }
        }
        pickDocumentLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { 
                activeTextTarget?.text = "File selected"
                activeTextTarget?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            }
        }
    }

    private fun setupHeader() {
        val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
        binding.tvAppRefNoHeader.text = "APP REF NO : $appRefNo"
    }

    private fun receiveData() {
        // Populate Personal Details fields with data from MinimalDataActivity
        binding.etCustomerType.setText(intent.getStringExtra("customer_type"))
        binding.etPrimaryBranch.setText(intent.getStringExtra("primary_branch"))
        binding.etBranchName.setText(intent.getStringExtra("branch_name"))
        binding.etFirstName.setText(intent.getStringExtra("first_name"))
        binding.etMiddleName.setText(intent.getStringExtra("middle_name"))
        binding.etLastName.setText(intent.getStringExtra("last_name"))
        binding.etShortName.setText(intent.getStringExtra("short_name"))
        binding.etFullName.setText(intent.getStringExtra("full_name"))
        binding.etDateOfBirth.setText(intent.getStringExtra("dob"))

        val fullMobile = intent.getStringExtra("mobile_no") ?: ""
        if (fullMobile.startsWith("+248")) {
            binding.etMobileNoAO.setText(fullMobile.replace("+248", "").trim())
        } else {
            binding.etMobileNoAO.setText(fullMobile)
        }

        binding.etEmailIdAO.setText(intent.getStringExtra("email_id"))

        // Also pre-fill Account Details tab branch fields immediately from intent
        // (so they show even before user selects a scheme type)
        val primaryBranch = intent.getStringExtra("primary_branch") ?: ""
        val branchName    = intent.getStringExtra("branch_name") ?: ""
        binding.etAccountBranchId.setText(primaryBranch)
        binding.etAccountBranchName.setText(branchName)
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showLayout(binding.layoutPersonalDetails)
                    1 -> showLayout(binding.layoutAccountDetails)
                    2 -> showLayout(binding.layoutDocumentMaster)
                    3 -> showLayout(binding.layoutSignatureUpload)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showLayout(layout: View) {
        binding.layoutPersonalDetails.visibility = View.GONE
        binding.layoutAccountDetails.visibility = View.GONE
        binding.layoutDocumentMaster.visibility = View.GONE
        binding.layoutSignatureUpload.visibility = View.GONE
        layout.visibility = View.VISIBLE
    }

    private fun setupSpinners() {
        val salutations = listOf("SELECT", "MR", "MS")
        val salAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, salutations)
        salAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSalutation.adapter = salAdapter

        val genders = listOf("SELECT", "MALE", "FEMALE", "OTHERS")
        val genAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        genAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spGender.adapter = genAdapter

        val martialStatus = listOf("SELECT", "SINGLE", "MARRIED", "DIVORCED", "WIDOWED")
        val marAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, martialStatus)
        marAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spMartialStatus.adapter = marAdapter

        val occupations = listOf("SELECT", "SALARIED", "SELF-EMPLOYED", "BUSINESS", "RETIRED", "STUDENT")
        val occAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, occupations)
        occAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spOccupation.adapter = occAdapter

        val addressTypes = listOf("SELECT", "PERMANENT", "RESIDENTIAL", "OFFICE")
        val addrAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, addressTypes)
        addrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAddressType.adapter = addrAdapter

        val cities = listOf("SELECT", "CHENNAI", "SALEM", "THANJAVUR")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCity.adapter = cityAdapter

        val nationalities = listOf("SELECT", "Seychellois", "Indian","USA")
        val natAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nationalities)
        natAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNationality.adapter = natAdapter

        val countries = listOf("SELECT", "Seychelles", "India", "USA")
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCountryOfBirth.adapter = countryAdapter
        binding.spCountryOfOrigin.adapter = countryAdapter

        val schemeTypes = listOf("SELECT", "LOAN ACCOUNT", "DEPOSIT ACCOUNT")
        val schAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schemeTypes)
        schAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSchemeType.adapter = schAdapter

        val recoveryMethods = listOf("SELECT", "EMI", "BULLET", "REDUCING BALANCE")
        val recAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recoveryMethods)
        recAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRecoveryMethod.adapter = recAdapter

        val instFreqs = listOf("SELECT", "MONTHLY", "QUARTERLY", "HALF-YEARLY", "YEARLY")
        val freqAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, instFreqs)
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPrincipalInstFreq.adapter = freqAdapter
        binding.spInterestInstFreq.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, instFreqs).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val interestTypes = listOf("SELECT", "SIMPLE", "COMPOUND")
        val intTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, interestTypes)
        intTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spInterestType.adapter = intTypeAdapter

        val frequencies = listOf("SELECT","MONTHLY","YEARLY")
        val freqAdp = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        freqAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spFrequency.adapter = freqAdp

        val depositTypes = listOf("SELECT","RECURRING", "FIXED","REINVESTMENT")
        val depTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, depositTypes)
        depTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDepositType.adapter = depTypeAdapter

        val depositFreqs = listOf("SELECT", "MONTHLY", "QUARTERLY", "HALF-YEARLY", "YEARLY")
        val depFreqAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, depositFreqs)
        depFreqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spDepositFrequency.adapter = depFreqAdapter
    }
    
    private fun showDtiValidationDialog() {
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val etMonthlyIncome = android.widget.EditText(this).apply {
            hint = "Monthly Income"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(binding.etMonthlyIncome.text.toString().replace(",", ""))
        }

        val etMonthlyRepayment = android.widget.EditText(this).apply {
            hint = "Monthly Repayment"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(binding.etInstallmentAmount.text.toString().replace(",", ""))
        }

        val tvStatus = android.widget.TextView(this).apply {
            textSize = 16f
            setPadding(0, 20, 0, 0)
        }

        layout.addView(etMonthlyIncome)
        layout.addView(etMonthlyRepayment)
        layout.addView(tvStatus)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("DTI Validation")
            .setView(layout)
            .setPositiveButton("Calculate", null)
            .setNegativeButton("Close", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val income = etMonthlyIncome.text.toString().toDoubleOrNull() ?: 0.0
                val repayment = etMonthlyRepayment.text.toString().toDoubleOrNull() ?: 0.0

                if (income > 0) {
                    val dtiRatio = (repayment / income) * 100
                    val status = when {
                        dtiRatio <= 35 -> "Favorable"
                        dtiRatio <= 49 -> "Adequate"
                        else -> "Not Favorable"
                    }
                    tvStatus.text = "DTI Ratio: ${String.format("%.2f", dtiRatio)}%\nStatus: $status"
                } else {
                    tvStatus.text = "Please enter valid Monthly Income"
                }
            }
        }
        dialog.show()
    }

    private fun showScheduleDialog() {
        val creationDate = binding.etDateOfLoan.text.toString()
        val interestRate = binding.etInterestRate.text.toString().replace(",", "")
        val installID = "1"
        val installStartDate = binding.etInstallmentStartDate.text.toString()
        val pricipleFreq = binding.spPrincipalInstFreq.selectedItem?.toString() ?: ""
        val noOfInstallment = binding.etNoOfInstallments.text.toString()
        val installAmount = binding.etInstallmentAmount.text.toString().replace(",", "")
        val interestFreq = binding.spInterestInstFreq.selectedItem?.toString() ?: ""
        val feesRate = binding.etFeesRate.text.toString().replace(",", "")

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Demand Schedule Details")
            .setMessage("Fetching Schedule...")
            .setPositiveButton("Close", null)
            .create()
        dialog.show()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getInterestDetails(
                        creationDate, interestRate, installID, installStartDate,
                        pricipleFreq, noOfInstallment, installAmount, interestFreq, feesRate
                    )
                }
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.isNotEmpty()) {
                        val sb = StringBuilder()
                        for (row in data) {
                            sb.append("Inst No: ${row["no_of_instalment"]}, Date: ${row["installment_date"]}, Amt: ${row["installment_amount"]}\n")
                        }
                        dialog.setMessage(sb.toString())
                    } else {
                        dialog.setMessage("No Schedule Found")
                    }
                } else {
                    dialog.setMessage("Error fetching schedule")
                }
            } catch (e: Exception) {
                dialog.setMessage("Error: ${e.message}")
            }
        }
    }

    private fun showDepositFlowDialog() {
        val depositType = binding.spDepositType.selectedItem?.toString() ?: ""
        val depoActNo = binding.etDepositAccountNo.text.toString()
        val depositDate = binding.etDateOfDeposit.text.toString()
        val depositAmt = binding.etDepositAmount.text.toString().replace(",", "")
        val currency = binding.etDepositCurrency.text.toString()
        val depositPeriod = binding.etDepositPeriod.text.toString()
        val maturityDate = binding.etMaturityDate.text.toString()
        val branchId = binding.etAccountBranchId.text.toString()
        val branchName = binding.etAccountBranchName.text.toString()
        val depositFrequency = binding.spDepositFrequency.selectedItem?.toString() ?: ""
        val interestType = binding.spInterestType.selectedItem?.toString() ?: ""
        val intAmt = binding.etInterestAmount.text.toString().replace(",", "")
        val rateOfInt = binding.etRateOfInterest.text.toString().replace(",", "")

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Deposit Flow Details")
            .setMessage("Fetching Flow...")
            .setPositiveButton("Close", null)
            .create()
        dialog.show()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getDepositFlow(
                        depositType, depoActNo, depositDate, depositAmt, currency, depositPeriod, maturityDate, branchId, branchName, depositFrequency, interestType, intAmt, rateOfInt
                    )
                }
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null && data.isNotEmpty()) {
                        val sb = StringBuilder()
                        for (row in data) {
                            sb.append("Date: ${row["deposit_date"]}, Amt: ${row["deposit_amt"]}, Mat Amt: ${row["maturity_amt"]}\n")
                        }
                        dialog.setMessage(sb.toString())
                    } else {
                        dialog.setMessage("No Flow Found")
                    }
                } else {
                    dialog.setMessage("Error fetching flow")
                }
            } catch (e: Exception) {
                dialog.setMessage("Error: ${e.message}")
            }
        }
    }

        private fun formatDateForBackend(dateString: String): String {
        if (dateString.isBlank()) return ""
        return try {
            val s = dateString.replace("/", "-")
            val parts = s.split("-")
            if (parts.size == 3) {
                if (parts[0].length <= 2 && parts[2].length == 4) {
                    "${parts[2]}-${parts[1]}-${parts[0]}"
                } else {
                    s
                }
            } else {
                s
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun setupCalculations() {
        val formatCurrency = { amount: Double ->
            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("en", "IN"))
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            formatter.format(amount)
        }

        // Income Calculation
        binding.etAnnualIncome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (binding.etAnnualIncome.hasFocus()) {
                    try {
                        val rawValue = s.toString().replace(",", "")
                        if (rawValue.isNotEmpty()) {
                            val annualIncome = rawValue.toDouble()
                            val monthlyIncome = annualIncome / 12
                            binding.etMonthlyIncome.setText(formatCurrency(monthlyIncome))
                        } else {
                            binding.etMonthlyIncome.setText("")
                        }
                    } catch (e: Exception) {
                        binding.etMonthlyIncome.setText("")
                    }
                }
            }
        })

        val calculateLoanFinancials = {
            try {
                val loanStr = binding.etLoanSanctioned.text.toString().replace(",", "")
                val marginStr = binding.etMargin.text.toString().replace(",", "")
                val periodStr = binding.etLoanPeriod.text.toString()
                
                val loanAmount = loanStr.toDoubleOrNull() ?: 0.0
                val marginPct = marginStr.toDoubleOrNull() ?: 0.0
                val period = periodStr.toDoubleOrNull() ?: 0.0
                
                if (loanAmount > 0) {
                    binding.etOutstanding.setText(formatCurrency(loanAmount))
                    binding.etDisbursement.setText(formatCurrency(loanAmount))
                    
                    val drawingLimit = loanAmount * (marginPct / 100.0)
                    binding.etDrawingLimit.setText(formatCurrency(drawingLimit))
                    
                    if (period > 0) {
                        binding.etNoOfInstallments.setText(period.toInt().toString())
                        val installment = loanAmount / period
                        binding.etInstallmentAmount.setText(formatCurrency(installment))
                        
                        val pct = Math.round((installment / loanAmount) * 100).toInt()
                        binding.etInstallmentPct.setText(pct.toString())
                    } else {
                        binding.etNoOfInstallments.setText("")
                        binding.etInstallmentAmount.setText("")
                        binding.etInstallmentPct.setText("")
                    }
                } else {
                    binding.etOutstanding.setText("")
                    binding.etDisbursement.setText("")
                    binding.etDrawingLimit.setText("")
                    binding.etNoOfInstallments.setText("")
                    binding.etInstallmentAmount.setText("")
                    binding.etInstallmentPct.setText("")
                }
            } catch (e: Exception) {}
        }

        val calculateDepositFinancials = {
            try {
                val depositStr = binding.etDepositAmount.text.toString().replace(",", "")
                val rateStr = binding.etRateOfInterest.text.toString().replace(",", "")
                val periodStr = binding.etDepositPeriod.text.toString()
                val compFactorStr = binding.etCompoundingFactor.text.toString()
                
                val depositAmt = depositStr.toDoubleOrNull() ?: 0.0
                val rate = rateStr.toDoubleOrNull() ?: 0.0
                val period = periodStr.toDoubleOrNull() ?: 0.0
                val compFactor = compFactorStr.toDoubleOrNull() ?: 1.0
                
                val frequency = binding.spFrequency.selectedItem?.toString() ?: ""
                val interestType = binding.spInterestType.selectedItem?.toString() ?: "Simple"
                
                if (depositAmt > 0 && rate > 0 && period > 0) {
                    val periodInYears = if (frequency.equals("Yearly", ignoreCase = true)) period else period / 12.0
                    val rateDecimal = rate / 100.0
                    
                    val interestAmt = if (interestType.equals("Compound", ignoreCase = true)) {
                        val n = if (compFactor > 0) compFactor else 1.0
                        depositAmt * (Math.pow(1.0 + (rateDecimal / n), n * periodInYears)) - depositAmt
                    } else {
                        depositAmt * rateDecimal * periodInYears
                    }
                    
                    val maturityAmt = depositAmt + interestAmt
                    binding.etInterestAmount.setText(formatCurrency(interestAmt))
                    binding.etMaturityAmount.setText(formatCurrency(maturityAmt))
                } else {
                    binding.etInterestAmount.setText("")
                    binding.etMaturityAmount.setText("")
                }
            } catch (e: Exception) {}
        }

        val loanWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanFinancials() }
        }
        binding.etLoanSanctioned.addTextChangedListener(loanWatcher)
        binding.etMargin.addTextChangedListener(loanWatcher)

        val depositWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositFinancials() }
        }
        binding.etDepositAmount.addTextChangedListener(depositWatcher)
        binding.etRateOfInterest.addTextChangedListener(depositWatcher)
        binding.etCompoundingFactor.addTextChangedListener(depositWatcher)

        // Expiry Date Calculation
        val calculateExpiry = {
            try {
                val loanPeriodStr = binding.etLoanPeriod.text.toString()
                val startDateStr = binding.etInstallmentStartDate.text.toString()
                if (loanPeriodStr.isNotEmpty() && startDateStr.isNotEmpty()) {
                    val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                    val date = sdf.parse(startDateStr)
                    if (date != null) {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = date
                        cal.add(java.util.Calendar.MONTH, loanPeriodStr.toInt())
                        binding.etLoanExpiryDate.setText(sdf.format(cal.time))
                    }
                } else {
                    binding.etLoanExpiryDate.setText("")
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        
        binding.etLoanPeriod.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { 
                calculateExpiry() 
                calculateLoanFinancials()
            }
        })
        binding.etInstallmentStartDate.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateExpiry() }
        })

        // Maturity Date Calculation
        val calculateMaturity = {
            try {
                val depositPeriodStr = binding.etDepositPeriod.text.toString()
                val depositDateStr = binding.etDateOfDeposit.text.toString()
                val frequency = binding.spFrequency.selectedItem?.toString() ?: ""
                
                if (depositPeriodStr.isNotEmpty() && depositDateStr.isNotEmpty()) {
                    val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                    val date = sdf.parse(depositDateStr)
                    if (date != null) {
                        val cal = java.util.Calendar.getInstance()
                        cal.time = date
                        if (frequency.equals("Yearly", ignoreCase = true)) {
                            cal.add(java.util.Calendar.YEAR, depositPeriodStr.toInt())
                        } else {
                            cal.add(java.util.Calendar.MONTH, depositPeriodStr.toInt())
                        }
                        binding.etMaturityDate.setText(sdf.format(cal.time))
                    }
                } else {
                    binding.etMaturityDate.setText("")
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        
        binding.etDepositPeriod.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { 
                calculateMaturity() 
                calculateDepositFinancials()
            }
        })
        binding.etDateOfDeposit.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateMaturity() }
        })
        
        binding.spFrequency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                calculateMaturity()
                calculateDepositFinancials()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
        
        binding.spInterestType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                calculateDepositFinancials()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupPersonalDetails() {
        // Auto-update Full Name
        val updateFullName = {
            val first = binding.etFirstName.text.toString().trim()
            val middle = binding.etMiddleName.text.toString().trim()
            val last = binding.etLastName.text.toString().trim()
            val nameParts = listOf(first, middle, last).filter { it.isNotEmpty() }
            binding.etFullName.setText(nameParts.joinToString(" "))
        }

        val nameWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { updateFullName() }
        }
        binding.etFirstName.addTextChangedListener(nameWatcher)
        binding.etMiddleName.addTextChangedListener(nameWatcher)
        binding.etLastName.addTextChangedListener(nameWatcher)

        // Auto-update Short Name
        binding.etFirstName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val shortName = binding.etShortName.text.toString().trim()
                val firstName = binding.etFirstName.text.toString().trim()
                if (shortName.isEmpty() && firstName.length >= 3) {
                    binding.etShortName.setText(firstName.take(5).uppercase())
                }
            }
        }

        // Date of Birth validation
        binding.etDateOfBirth.isEnabled = true
        binding.etDateOfBirth.isFocusable = false
        binding.etDateOfBirth.setOnClickListener { 
            val calendar = java.util.Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDateOfBirth.setText(formattedDate)
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.etDateOfBirth.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val dobStr = s.toString()
                if (dobStr.isNotEmpty()) {
                    try {
                        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                        val dob = sdf.parse(dobStr)
                        if (dob != null) {
                            val today = java.util.Calendar.getInstance()
                            val dobCal = java.util.Calendar.getInstance()
                            dobCal.time = dob
                            
                            var age = today.get(java.util.Calendar.YEAR) - dobCal.get(java.util.Calendar.YEAR)
                            if (today.get(java.util.Calendar.DAY_OF_YEAR) < dobCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                                age--
                            }
                            
                            if (age < 18) {
                                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Customer is a Minor, Not Eligible.", android.widget.Toast.LENGTH_LONG).show()
                                binding.etDateOfBirth.setText("")
                            } else if (age >= 60) {
                                android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Senior Citizen", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {}
                }
            }
        })

        // Mobile Number
        binding.etMobileNoAO.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val valStr = s.toString().replace(Regex("\\D"), "")
                if (valStr.length > 7) {
                    val trimmed = valStr.substring(0, 7)
                    if (s.toString() != trimmed) {
                        binding.etMobileNoAO.setText(trimmed)
                        binding.etMobileNoAO.setSelection(trimmed.length)
                    }
                }
            }
        })
        
        // Loan Installment Freq Spinners
        val freqOptions = arrayOf("SELECT", "Monthly")
        val freqAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, freqOptions)
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPrincipalInstFreq.adapter = freqAdapter
        binding.spInterestInstFreq.adapter = freqAdapter
        
        // Installment Defaults
        binding.etInstallmentId.setText("1")
        
        if (binding.etInstallmentStartDate.text.toString().isEmpty()) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MONTH, 1)
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
            binding.etInstallmentStartDate.setText(sdf.format(cal.time))
        }
    }

    private fun getFullMobileNumber(): String {
        val mobile = binding.etMobileNoAO.text.toString().trim()
        return if (mobile.isNotEmpty()) "+248$mobile" else ""
    }

    private fun getCapAtMaturity(): String {
        return if (binding.rbCapYes.isChecked) "Y" else "N"
    }

    private fun validateSubmission(): Boolean {
        val startDateStr = binding.etInstallmentStartDate.text.toString()
        if (startDateStr.isNotEmpty()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val startDate = sdf.parse(startDateStr)
                if (startDate != null) {
                    val today = java.util.Calendar.getInstance()
                    today.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    today.set(java.util.Calendar.MINUTE, 0)
                    today.set(java.util.Calendar.SECOND, 0)
                    today.set(java.util.Calendar.MILLISECOND, 0)
                    
                    if (startDate.before(today.time)) {
                        android.widget.Toast.makeText(this, "Installment Start Date cannot be in the past.", android.widget.Toast.LENGTH_SHORT).show()
                        return false
                    }
                }
            } catch (e: Exception) {}
        } else {
             android.widget.Toast.makeText(this, "Installment Start Date is required.", android.widget.Toast.LENGTH_SHORT).show()
             return false
        }
        
        if (binding.spPrincipalInstFreq.selectedItem?.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "Please select Principal Installment Freq", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (binding.spInterestInstFreq.selectedItem?.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "Please select Interest Installment Freq", android.widget.Toast.LENGTH_SHORT).show()
            return false
        }
        
        val dobStr = binding.etDateOfBirth.text.toString()
        if (dobStr.isNotEmpty()) {
            try {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                val dob = sdf.parse(dobStr)
                if (dob != null) {
                    val today = java.util.Calendar.getInstance()
                    val dobCal = java.util.Calendar.getInstance()
                    dobCal.time = dob
                    
                    var age = today.get(java.util.Calendar.YEAR) - dobCal.get(java.util.Calendar.YEAR)
                    if (today.get(java.util.Calendar.DAY_OF_YEAR) < dobCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                        age--
                    }
                    
                    if (age < 18) {
                        android.widget.Toast.makeText(this, "Customer is a Minor, Not Eligible.", android.widget.Toast.LENGTH_LONG).show()
                        return false
                    }
                }
            } catch (e: Exception) {}
        }
        
        return true
    }

    private fun getBytesFromImageView(imageView: android.widget.ImageView): ByteArray {
        val drawable = imageView.drawable ?: return ByteArray(0)
        if (drawable is android.graphics.drawable.BitmapDrawable) {
            val bitmap = drawable.bitmap
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            return stream.toByteArray()
        }
        return ByteArray(0)
    }
}
