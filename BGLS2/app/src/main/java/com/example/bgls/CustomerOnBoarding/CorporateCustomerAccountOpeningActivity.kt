package com.example.bgls.CustomerOnBoarding

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bgls.R
import com.example.bgls.databinding.ActivityCorporateCustomerAccountOpeningBinding
import com.google.android.material.tabs.TabLayout
import com.example.bgls.util.SignaturePadView
import androidx.lifecycle.lifecycleScope
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class CorporateCustomerAccountOpeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCorporateCustomerAccountOpeningBinding
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
        binding = ActivityCorporateCustomerAccountOpeningBinding.inflate(layoutInflater)
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
        setupCalculations()

        receiveData()
        setupDatePickers()
        setupMandatoryLabels()
        setupDocumentMaster()
        setupSignatureUpload()

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
                if (selected != "SELECT") {
                    fetchSchemeDetails(selected)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // ── DTI Validation Button ─────────────────────────────────────────────
        binding.btnDtiValidation.setOnClickListener { showDtiDialog() }

        // ── RP1 CIF ID lookup: when user leaves the CIF field, fetch details ──
        binding.etRP1CifId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val cifId = binding.etRP1CifId.text.toString().trim()
                if (cifId.isNotEmpty()) fetchRP1CifDetails(cifId)
            }
        }
    }

    private fun fetchSchemeDetails(selected: String) {
        val isDeposit = selected == "DEPOSIT ACCOUNT"
        val schemeType = when (selected) {
            "DEPOSIT ACCOUNT" -> "TDFIXED"
            else -> "LSRET"  // LOAN ACCOUNT
        }
        // GL values matching web: Loan=Asset(1000/1500), Deposit=Liability(2000/2500)
        val glCode   = if (isDeposit) "2000" else "1000"
        val glDesc   = if (isDeposit) "Liability" else "Asset"
        val glshCode = if (isDeposit) "2500" else "1500"
        val glshDesc = if (isDeposit) "DEPOSITS" else "LOANS AND ADVANCES"
        val branchId   = intent.getStringExtra("primary_branch") ?: ""
        val branchName = intent.getStringExtra("branch_name") ?: ""

        // Populate immediately (before API responds) so fields are never blank
        binding.etSchemeCode.setText(schemeType)
        binding.etGlCode.setText(glCode)
        binding.etGlDesc.setText(glDesc)
        binding.etGlshCode.setText(glshCode)
        binding.etGlshDesc.setText(glshDesc)
        binding.etAccountBranchId.setText(branchId)
        binding.etAccountBranchName.setText(branchName)
        
        // --- Toggle field visibility based on scheme type ---
        if (isDeposit) {
            binding.layoutDepositFields.visibility = android.view.View.VISIBLE
            binding.layoutLoanFields.visibility = android.view.View.GONE
        } else {
            binding.layoutDepositFields.visibility = android.view.View.GONE
            binding.layoutLoanFields.visibility = android.view.View.VISIBLE
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getSchemeDetails(schemeType)
                }
                if (response.isSuccessful) {
                    // MUST use .string() not .toString() — .toString() returns object reference
                    val rawJson = response.body()?.string() ?: ""
                    android.util.Log.d("CorporateScheme", "Response: $rawJson")
                    try {
                        val json = org.json.JSONObject(rawJson)
                        generatedAccountNo = json.optString("loanAccountNo",
                            json.optString("accountNo", ""))
                        val data = json.optJSONObject("data")
                        if (data != null) {
                            val safeString = { key: String, fallback: String ->
                                val value = data.optString(key)
                                if (value == "null" || value.isEmpty()) fallback else value
                            }
                            // Backend field names from BACP_PARAMETER table:
                            // gl_code  → "gl_code"
                            // gl_desc  → "schmdesc"
                            // glsh     → "glsh"
                            schemeGlCode   = safeString("gl_code", glCode)
                            schemeGlDesc   = safeString("schmdesc", glDesc)
                            schemeGlshCode = safeString("glsh", glshCode)
                            schemeGlshDesc = glshDesc
                        } else {
                            schemeGlCode = glCode; schemeGlDesc = glDesc
                            schemeGlshCode = glshCode; schemeGlshDesc = glshDesc
                        }
                    } catch (jsonEx: Exception) {
                        schemeGlCode = glCode; schemeGlDesc = glDesc
                        schemeGlshCode = glshCode; schemeGlshDesc = glshDesc
                    }
                    withContext(Dispatchers.Main) {
                        binding.etSchemeCode.setText(schemeType)
                        binding.etGlCode.setText(schemeGlCode)
                        binding.etGlDesc.setText(schemeGlDesc)
                        binding.etGlshCode.setText(schemeGlshCode)
                        binding.etGlshDesc.setText(schemeGlshDesc)
                        binding.etAccountBranchId.setText(branchId)
                        binding.etAccountBranchName.setText(branchName)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ── DTI Validation Dialog (matches web modal exactly) ────────────────────
    private fun showDtiDialog() {
        val ctx = this
        val layout = android.widget.LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 16)
        }

        fun row(label: String, view: android.view.View) {
            val tv = android.widget.TextView(ctx).apply { text = label; textSize = 13f; setPadding(0, 8, 0, 2) }
            layout.addView(tv)
            layout.addView(view)
        }

        val etBorrower = android.widget.EditText(ctx).apply {
            setText(binding.etCorporateName.text.toString())
            isFocusable = false
        }
        val etConstitution = android.widget.EditText(ctx).apply {
            setText(binding.etCustomerType.text.toString())
            isFocusable = false
        }
        val etMonthIncome = android.widget.EditText(ctx).apply {
            setText(binding.etMonthlyIncome.text.toString())
            isFocusable = false
        }
        val etRepayment = android.widget.EditText(ctx).apply {
            hint = "Enter monthly repayment amount"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val etDtiRatio = android.widget.EditText(ctx).apply {
            hint = "DTI Ratio"
            isFocusable = false
        }
        val tvStatus = android.widget.TextView(ctx).apply {
            text = ""
            textSize = 14f
            android.util.TypedValue().also { ctx.theme.resolveAttribute(android.R.attr.colorPrimary, it, true) }
        }
        val tvNote = android.widget.TextView(ctx).apply {
            text = "NOTE: ≤35% → Favorable | 36%–49% → Adequate | ≥50% → Not Favorable"
            textSize = 11f
            setTextColor(android.graphics.Color.parseColor("#555555"))
            setPadding(0, 12, 0, 0)
        }
        val btnCalc = android.widget.Button(ctx).apply {
            text = "Calculate DTI"
            setOnClickListener {
                val monthlyIncome = etMonthIncome.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
                val repayment = etRepayment.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
                if (monthlyIncome > 0) {
                    val dti = (repayment / monthlyIncome) * 100
                    etDtiRatio.setText(String.format("%.2f%%", dti))
                    val (status, color) = when {
                        dti <= 35.0 -> "Favorable" to android.graphics.Color.parseColor("#4CAF50")
                        dti <= 49.0 -> "Adequate" to android.graphics.Color.parseColor("#FF9800")
                        else        -> "Not Favorable" to android.graphics.Color.parseColor("#F44336")
                    }
                    tvStatus.text = status
                    tvStatus.setTextColor(color)
                } else {
                    etDtiRatio.setText("Invalid input")
                    tvStatus.text = ""
                }
            }
        }

        row("Borrower Name", etBorrower)
        row("Constitution", etConstitution)
        row("Monthly Income", etMonthIncome)
        row("Monthly Repayment", etRepayment)
        layout.addView(btnCalc)
        row("DTI Ratio (Repayment / Income × 100)", etDtiRatio)
        layout.addView(android.widget.TextView(ctx).apply { text = "Status:"; textSize = 13f; setPadding(0, 8, 0, 2) })
        layout.addView(tvStatus)
        layout.addView(tvNote)

        android.app.AlertDialog.Builder(ctx)
            .setTitle("DTI – DEBT TO INCOME RATIO")
            .setView(layout)
            .setNegativeButton("Close", null)
            .show()
    }

    // ── RP1 CIF ID Auto-fill from API ─────────────────────────────────────────
    private fun fetchRP1CifDetails(cifId: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getCustomerMaster(formmode = "view", id = cifId)
                }
                if (response.isSuccessful) {
                    val data = response.body()
                    // CustomerMasterViewResponse may have list or single entity – try to get name fields
                    withContext(Dispatchers.Main) {
                        val rawJson = response.raw().body?.string() ?: return@withContext
                        try {
                            val json = org.json.JSONObject(rawJson)
                            // Try common field names from web
                            val firstName = json.optString("ca_first_name", json.optString("firstName", ""))
                            val lastName  = json.optString("ca_last_name",  json.optString("lastName", ""))
                            val fullName  = json.optString("ca_full_name",  json.optString("fullName", "$firstName $lastName".trim()))
                            val shortName = json.optString("ca_short_name", json.optString("shortName", ""))
                            if (fullName.isNotEmpty()) binding.etRP1FullName.setText(fullName)
                            if (shortName.isNotEmpty()) binding.etRP1ShortName.setText(shortName)
                            if (firstName.isNotEmpty()) binding.etRP1FirstName.setText(firstName)
                            if (lastName.isNotEmpty())  binding.etRP1LastName.setText(lastName)
                        } catch (ignored: Exception) {}
                    }
                }
            } catch (e: Exception) {
                // Silently ignore – CIF lookup is optional auto-fill
                android.util.Log.w("CorporateCIF", "RP1 CIF lookup failed: ${e.message}")
            }
        }
    }



    private fun savePersonalDetails() {
        // Validations for mandatory fields
        if (binding.etMonthlyIncome.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Monthly Income is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spAddressType.selectedItem.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "Address Type is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etHouseNo.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "House No is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etStreetNo.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Street No is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etStreetName.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Street Name is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spCity.selectedItem.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "City is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etAddressValidFrom.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Address Valid From is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spNationality.selectedItem.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "Nationality is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.spCountryOfBirth.selectedItem.toString() == "SELECT") {
            android.widget.Toast.makeText(this, "Country of Birth is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etEmailIdAO.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Email Id is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etMobileNoAO.text.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Mobile No is required", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Saving Corporate Details...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                fun formatDateForBackend(s: String): String {
                    return try {
                        val parts = s.split("-")
                        if (parts.size == 3 && parts[2].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else s
                    } catch (e: Exception) { s }
                }

                val params = mutableMapOf<String, String>()
                
                params["ca_customer_type_1"] = binding.etCustomerType.text.toString()
                params["ca_cif_id_1"] = binding.etCifId.text.toString()
                params["ca_primary_branch_1"] = binding.etPrimaryBranch.text.toString()
                params["ca_branch_name_1"] = binding.etBranchDesc.text.toString()
                
                params["constitutionName"] = binding.etConstitutionName.text.toString()
                params["corporateName"] = binding.etCorporateName.text.toString()
                params["tradeName"] = binding.etTradeName.text.toString()
                params["certificateIncorporation"] = binding.etCertIncorp.text.toString()
                params["businessRegistration"] = binding.etBusRegNo.text.toString()
                params["dateIncorporation"] = formatDateForBackend(binding.etDateIncorp.text.toString())
                
                params["ca_postal_code"] = binding.etPostBoxNo.text.toString()
                
                // --- ADDED FOR BACKEND COMPATIBILITY (Prevents 500 Rollback) ---
                params["cif_id"] = binding.etCifId.text.toString()
                params["ca_solid"] = binding.etPrimaryBranch.text.toString()
                val currentDate = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                params["ca_acct_opendate"] = formatDateForBackend(currentDate)
                params["ca_date_of_birth"] = formatDateForBackend(binding.etDateIncorp.text.toString())
                params["ca_first_name"] = binding.etCorporateName.text.toString()
                params["ca_last_name"] = binding.etTradeName.text.toString()
                params["ca_preferred_name"] = binding.etCorporateName.text.toString()
                params["shortName"] = binding.etCorporateName.text.toString()
                params["ca_customer_type"] = binding.etCustomerType.text.toString()
                params["ca_currency"] = intent.getStringExtra("currency") ?: ""
                // ---------------------------------------------------------------
                params["landlineNo"] = binding.etLandLineNo.text.toString()
                params["ca_fax_no"] = binding.etFaxNo.text.toString()
                params["ca_email_address"] = binding.etEmail.text.toString()
                params["website"] = binding.etWebsite.text.toString()
                
                params["business"] = binding.etBusiness.text.toString()
                params["annualIncome"] = binding.etAnnualIncome.text.toString().replace(",", "")
                params["monthlyIncome"] = binding.etMonthlyIncome.text.toString().replace(",", "")
                params["loanObligations"] = binding.etLoanObligations.text.toString().replace(",", "")
                params["familyMaintenance"] = ""
                
                params["ca_address_type_1"] = binding.spAddressType.selectedItem.toString()
                params["ca_house_no_1"] = binding.etHouseNo.text.toString()
                params["ca_street_no_1"] = binding.etStreetNo.text.toString()
                params["ca_street_name_1"] = binding.etStreetName.text.toString()
                params["ca_country_1"] = binding.etCountry.text.toString()
                params["ca_state_1"] = binding.etState.text.toString()
                params["ca_city_1"] = binding.spCity.selectedItem.toString()
                params["ca_postal_code_1"] = binding.etPostalCode.text.toString()
                params["ca_address_valid_from_1"] = formatDateForBackend(binding.etAddressValidFrom.text.toString())
                
                params["ca_nationality_1"] = binding.spNationality.selectedItem.toString()
                params["ca_country_of_birth_1"] = binding.spCountryOfBirth.selectedItem.toString()
                params["ca_country_of_origin_1"] = binding.spCountryOfOrigin.selectedItem.toString()
                params["ca_email_id_1"] = binding.etEmailIdAO.text.toString()
                params["ca_countrycode_1"] = binding.tvMobilePrefix.text.toString()
                params["ca_mobile_no_1"] = binding.etMobileNoAO.text.toString()
                
                params["ca_customer_type_2"] = binding.spRP1CustomerType.selectedItem.toString().takeIf { it != "SELECT" } ?: ""
                params["ca_cif_id_2"] = binding.etRP1CifId.text.toString()
                params["ca_primary_branch_2"] = binding.etRP1PrimaryBranch.text.toString()
                params["ca_branch_name_2"] = binding.etRP1BranchDesc.text.toString()
                params["ca_salutation_2"] = binding.spRP1Salutation.selectedItem.toString().takeIf { it != "SELECT" } ?: ""
                params["ca_first_name_2"] = binding.etRP1FirstName.text.toString()
                params["ca_middle_name_2"] = binding.etRP1MiddleName.text.toString()
                params["ca_last_name_2"] = binding.etRP1LastName.text.toString()
                params["ca_full_name_2"] = binding.etRP1FullName.text.toString()
                params["ca_short_name_2"] = binding.etRP1ShortName.text.toString()
                params["ca_annual_income_2"] = binding.etRP1AnnualIncome.text.toString().replace(",", "")
                params["ca_monthly_income_2"] = binding.etRP1MonthlyIncome.text.toString().replace(",", "")
                params["ca_loan_obligations_2"] = binding.etRP1LoanObligations.text.toString().replace(",", "")
                params["ca_family_maintenance_2"] = binding.etRP1FamilyMaintenance.text.toString().replace(",", "")
                params["ca_address_type_2"] = binding.spRP1AddressType.selectedItem.toString()
                params["ca_house_no_2"] = binding.etRP1HouseNo.text.toString()
                params["ca_street_no_2"] = binding.etRP1StreetNo.text.toString()
                params["ca_street_name_2"] = binding.etRP1StreetName.text.toString()
                params["ca_country_2"] = binding.etRP1Country.text.toString()
                params["ca_state_2"] = binding.etRP1State.text.toString()
                params["ca_city_2"] = binding.spRP1City.selectedItem.toString()
                params["ca_postal_code_2"] = binding.etRP1PostalCode.text.toString()
                params["ca_address_valid_from_2"] = formatDateForBackend(binding.etRP1AddressValidFrom.text.toString())
                params["ca_nationality_2"] = binding.spRP1Nationality.selectedItem.toString()
                params["ca_country_of_birth_2"] = binding.spRP1CountryOfBirth.selectedItem.toString()
                params["ca_country_of_origin_2"] = binding.spRP1CountryOfOrigin.selectedItem.toString()
                params["ca_email_id_2"] = binding.etRP1EmailId.text.toString()
                params["ca_countrycode_2"] = "+248"
                params["ca_mobile_no_2"] = binding.etRP1MobileNo.text.toString()

                // Add empty values for _3 to _10
                for (i in 3..10) {
                    params["ca_customer_type_$i"] = ""
                    params["ca_cif_id_$i"] = ""
                    params["ca_primary_branch_$i"] = ""
                    params["ca_branch_name_$i"] = ""
                    params["ca_salutation_$i"] = ""
                    params["ca_first_name_$i"] = ""
                    params["ca_middle_name_$i"] = ""
                    params["ca_last_name_$i"] = ""
                    params["ca_full_name_$i"] = ""
                    params["ca_short_name_$i"] = ""
                    params["ca_loan_obligations_$i"] = ""
                    params["ca_family_maintenance_$i"] = ""
                    params["ca_address_type_$i"] = "SELECT"
                    params["ca_house_no_$i"] = ""
                    params["ca_street_no_$i"] = ""
                    params["ca_street_name_$i"] = ""
                    params["ca_country_$i"] = ""
                    params["ca_state_$i"] = ""
                    params["ca_city_$i"] = "SELECT"
                    params["ca_postal_code_$i"] = ""
                    params["ca_address_valid_from_$i"] = ""
                    params["ca_nationality_$i"] = "SELECT"
                    params["ca_country_of_birth_$i"] = "SELECT"
                    params["ca_country_of_origin_$i"] = "SELECT"
                    params["ca_email_id_$i"] = ""
                    params["ca_countrycode_$i"] = ""
                    params["ca_mobile_no_$i"] = ""
                    params["ca_annual_income_$i"] = ""
                    params["ca_monthly_income_$i"] = ""
                }
                
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN02602"
                params["customer_group"] = intent.getStringExtra("customer_group") ?: "CORPORATE CUSTOMER"
                params["account_type"] = "CORPORATE CUSTOMER"
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.savePersonalDetail(appRefNo, "9", intent.getStringExtra("passno") ?: "", intent.getStringExtra("nationalid") ?: "", params)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    var msg = "Corporate Details Saved"
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
                    android.app.AlertDialog.Builder(this@CorporateCustomerAccountOpeningActivity)
                        .setMessage(msg)
                        .setPositiveButton("Okay") { dialog, _ ->
                            dialog.dismiss()
                            binding.tabLayout.getTabAt(1)?.select()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to save details", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                
                val formData = mutableMapOf<String, Any>()
                val scheme = binding.spSchemeType.selectedItem.toString()
                val isDeposit = scheme == "FIXED DEPOSIT" || scheme == "DEPOSIT ACCOUNT"
                val schemetype = if (isDeposit) "TD" else "LA"
                formData["schemetype"] = schemetype
                formData["schmcode"] = if (isDeposit) "TDFIXED" else "LSRET"
                formData["currency"] = "SCR"
                formData["prisolid"] = binding.etAccountBranchId.text.toString()
                formData["branch_desc"] = binding.etBranchDesc.text.toString()
                formData["certificate_registration"] = binding.etCertReg.text.toString()
                formData["business_registration"] = binding.etBusReg.text.toString()
                formData["date_incorporation"] = binding.etDateIncorp.text.toString()
                formData["countryOrigin"] = binding.spCountryOperation.selectedItem.toString()
                
                // Common fields mapped exactly as web expects them (nationalid maps to CertReg for Corporate)
                formData["nationalid"] = binding.etCertReg.text.toString()
                formData["passno"] = binding.etBusReg.text.toString()
                formData["issuedate"] = ""
                formData["expdate"] = ""
                formData["customer_type"] = binding.etCustomerType.text.toString()
                formData["annual_income"] = binding.etAnnualIncome.text?.toString()?.replace(",", "") ?: ""
                formData["monthly_income"] = binding.etMonthlyIncome.text?.toString()?.replace(",", "") ?: ""

                fun formatDateForBackend(s: String): String {
                    return try {
                        val parts = s.split("-")
                        if (parts.size == 3 && parts[2].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else s
                    } catch (e: Exception) { s }
                }
                fun String?.takeIfNotEmpty(): String? = if (this.isNullOrBlank()) null else this
                
                if (isDeposit) {
                    formData["account_no"] = binding.etDepositAccountNo.text?.toString() ?: ""
                    formData["deposit_account_no"] = binding.etDepositAccountNo.text?.toString() ?: ""
                    formData["td_deposit_accountno"] = binding.etDepositAccountNo.text?.toString() ?: ""
                    formData["gl_code"]           = binding.etGlCode.text?.toString() ?: ""
                    formData["gl_desc"]           = binding.etGlDesc.text?.toString() ?: ""
                    formData["glsh_code"]         = binding.etGlshCode.text?.toString() ?: ""
                    formData["glsh_desc"]         = binding.etGlshDesc.text?.toString() ?: ""
                    formData["deposit_date"]      = binding.etDateOfDeposit.text?.toString() ?: ""
                    formData["deposit_amt"]       = binding.etDepositAmount.text?.toString()?.replace(",", "") ?: ""
                    formData["deposit_period"]    = binding.etDepositPeriod.text?.toString() ?: ""
                    formData["maturity_date"]     = formatDateForBackend(binding.etMaturityDate.text?.toString() ?: "")
                    formData["rate_of_int"]       = binding.etRateOfInterest.text?.toString() ?: ""
                    formData["int_amt"]           = binding.etInterestAmount.text?.toString()?.replace(",", "") ?: ""
                    formData["maturity_amt"]      = binding.etMaturityAmount.text?.toString()?.replace(",", "") ?: ""
                    formData["compounding_factor"]= try { binding.spDepositCompoundingFactor.selectedItem?.toString() ?: "" } catch (e: Exception) { "" }
                    formData["deposit_type"]      = try { binding.spDepositType.selectedItem?.toString() ?: "" } catch (e: Exception) { "Fixed" }
                    formData["frequency"]         = try { binding.spDepositFrequency.selectedItem?.toString() ?: "" } catch (e: Exception) { "Monthly" }
                    formData["deposit_frequency"] = try { binding.spDepositFrequency2.selectedItem?.toString() ?: "" } catch (e: Exception) { "Monthly" }
                    formData["interest_type"]     = try { binding.spDepositInterestType.selectedItem?.toString() ?: "" } catch (e: Exception) { "SIMPLE" }
                } else {
                    formData["account_no"]        = binding.etLoanAccountNo.text?.toString() ?: ""
                    formData["loan_accountno"]    = binding.etLoanAccountNo.text?.toString() ?: ""
                    formData["la_loan_accountno"] = binding.etLoanAccountNo.text?.toString() ?: ""
                    formData["date_of_loan"]      = formatDateForBackend(binding.etDateOfLoan.text?.toString() ?: "")
                    formData["gl_code"]           = binding.etGlCode.text?.toString() ?: ""
                    formData["gl_desc"]           = binding.etGlDesc.text?.toString() ?: ""
                    formData["gl_code_loan"]      = binding.etGlCode.text?.toString() ?: ""
                    formData["gl_desc_loan"]      = binding.etGlDesc.text?.toString() ?: ""
                    formData["glsh_code_loan"]    = binding.etGlshCode.text?.toString() ?: ""
                    formData["glsh_desc_loan"]    = binding.etGlshDesc.text?.toString() ?: ""
                    formData["glsh_code"]         = binding.etGlshCode.text?.toString() ?: ""
                    formData["glsh_desc"]         = binding.etGlshDesc.text?.toString() ?: ""
                    formData["loan_sanctioned"]   = binding.etLoanSanctioned.text?.toString()?.replace(",", "") ?: ""
                    formData["margin_limit"]      = binding.etMargin.text?.toString()?.replace(",", "") ?: ""
                    formData["effective_interest_rate"] = binding.etInterestRate.text?.toString() ?: ""
                    formData["effective_fees_rate"]= binding.etFeesRate.text?.toString() ?: ""
                    formData["recovery_method"]   = binding.spRecoveryMethod.selectedItem?.toString() ?: ""
                    formData["la_remarks"]        = binding.etRemarks.text?.toString() ?: "" 
                    formData["inst_start_dt"]     = binding.etInstallmentStartDate.text?.toString() ?: ""
                    formData["loan_period"]       = binding.etLoanPeriod.text?.toString() ?: ""
                    formData["disbursement"]      = binding.etDisbursement.text?.toString()?.replace(",", "") ?: ""
                    formData["loan_outstanding"]  = binding.etOutstanding.text?.toString()?.replace(",", "") ?: ""
                    formData["repayment_terms"]   = binding.etRepaymentTerms.text?.toString() ?: ""
                }
                
                val finalAccountNo = if (isDeposit) binding.etDepositAccountNo.text?.toString() ?: "" else binding.etLoanAccountNo.text?.toString() ?: ""
                val body = mapOf(
                    "formData" to formData,
                    "loanAccountNo" to finalAccountNo,
                    "accountNo" to finalAccountNo,
                    "scheduleList" to emptyList<Any>()
                )
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.saveAccountDetails(appRefNo, body)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (isDeposit) {
                        val depositReq = com.example.bgls.Retrofit.DepositEntityRequest(
                            depo_actno = binding.etDepositAccountNo.text?.toString()?.takeIfNotEmpty(),
                            deposit_date = formatDateForBackend(binding.etDateOfDeposit.text?.toString() ?: "").takeIfNotEmpty(),
                            deposit_amt = binding.etDepositAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                            currency = binding.etDepositAccountCurrency.text?.toString()?.takeIfNotEmpty(),
                            deposit_period = binding.etDepositPeriod.text?.toString()?.takeIfNotEmpty(),
                            maturity_date = formatDateForBackend(binding.etMaturityDate.text?.toString() ?: "").takeIfNotEmpty(),
                            rate_of_int = binding.etRateOfInterest.text?.toString()?.takeIfNotEmpty(),
                            int_amt = binding.etInterestAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                            maturity_amt = binding.etMaturityAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                            deposit_type = binding.spDepositType.selectedItem?.toString()?.takeIfNotEmpty(),
                            frequency = binding.spDepositFrequency.selectedItem?.toString()?.takeIfNotEmpty(),
                            gl_code = binding.etGlCode.text?.toString()?.takeIfNotEmpty(),
                            gl_desc = binding.etGlDesc.text?.toString()?.takeIfNotEmpty(),
                            glsh_code = binding.etGlshCode.text?.toString()?.takeIfNotEmpty(),
                            glsh_desc = binding.etGlshDesc.text?.toString()?.takeIfNotEmpty(),
                            cust_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),
                            cust_name = binding.etCorporateName.text?.toString()?.takeIfNotEmpty(),
                            scheme_code = binding.etSchemeCode.text?.toString()?.takeIfNotEmpty(),
                            branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),
                            branch_desc = binding.etBranchDesc.text?.toString()?.takeIfNotEmpty(),
                            deposit_frequency = binding.spDepositFrequency2.selectedItem?.toString()?.takeIfNotEmpty(),
                            interest_type = binding.spDepositInterestType.selectedItem?.toString()?.takeIfNotEmpty()
                        )
                        withContext(Dispatchers.IO) {
                            RetrofitClient.api.depositAddCust(depositReq)
                        }
                    } else {
                        val leaseReq = com.example.bgls.Retrofit.LeaseDataRequest(
                            loanDetails = com.example.bgls.Retrofit.LoanDetailsRequest(
                                customer_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),
                                customer_name = binding.etCorporateName.text?.toString()?.takeIfNotEmpty(),
                                branch_name = binding.etBranchDesc.text?.toString()?.takeIfNotEmpty(),
                                branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),
                                loan_type = schemetype.takeIfNotEmpty(),
                                loan_accountno = binding.etLoanAccountNo.text?.toString()?.takeIfNotEmpty(),
                                date_of_loan = formatDateForBackend(binding.etDateOfLoan.text?.toString() ?: "").takeIfNotEmpty(),
                                loan_sanctioned = binding.etLoanSanctioned.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                margin_limit = binding.etMargin.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                drawing_limit = binding.etDrawingLimit.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                loan_currency = "SCR",
                                disbursement = binding.etDisbursement.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                loan_outstanding = binding.etOutstanding.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                loan_period = binding.etLoanPeriod.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                expiry_date = formatDateForBackend(binding.etExpiryDate.text?.toString() ?: "").takeIfNotEmpty(),
                                repayment_terms = binding.etRepaymentTerms.text?.toString()?.takeIfNotEmpty(),
                                recovery_method = binding.spRecoveryMethod.selectedItem?.toString()?.takeIfNotEmpty(),
                                effective_interest_rate = binding.etInterestRate.text?.toString()?.takeIfNotEmpty(),
                                effective_fees_rate = binding.etFeesRate.text?.toString()?.takeIfNotEmpty(),
                                gl_code = binding.etGlCode.text?.toString()?.takeIfNotEmpty(),
                                gl_desc = binding.etGlDesc.text?.toString()?.takeIfNotEmpty(),
                                glsh_code = binding.etGlshCode.text?.toString()?.takeIfNotEmpty(),
                                glsh_desc = binding.etGlshDesc.text?.toString()?.takeIfNotEmpty()
                            ),
                            repaymentDetails = com.example.bgls.Retrofit.RepaymentDetailsRequest(
                                customer_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),
                                branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),
                                account_no = binding.etLoanAccountNo.text?.toString()?.takeIfNotEmpty(),
                                acid = binding.etLoanAccountNo.text?.toString()?.takeIfNotEmpty(),
                                inst_id = binding.etInstallmentId.text?.toString()?.takeIfNotEmpty(),
                                inst_start_dt = formatDateForBackend(binding.etInstallmentStartDate.text?.toString() ?: "").takeIfNotEmpty(),
                                inst_freq = binding.spPrincipalInstallmentFreq.selectedItem?.toString()?.takeIfNotEmpty(),
                                inst_amount = binding.etInstallmentAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                no_of_inst = binding.etNoOfInstallment.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                inst_pct = binding.etInstallmentPercentage.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),
                                interest_frequency = binding.spInterestInstallmentFreq.selectedItem?.toString()?.takeIfNotEmpty(),
                                maturity_flg = if (binding.rbCapYes.isChecked) "Y" else "N"
                            )
                        )
                        withContext(Dispatchers.IO) {
                            RetrofitClient.api.addLeaseAccount(leaseReq)
                        }
                    }
                    
                    val msg = response.body()?.string()?.takeIf { it.isNotBlank() } ?: "Account Details Saved"
                    android.app.AlertDialog.Builder(this@CorporateCustomerAccountOpeningActivity)
                        .setMessage(msg)
                        .setPositiveButton("Okay") { dialog, _ ->
                            dialog.dismiss()
                            binding.tabLayout.getTabAt(2)?.select()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to save details: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                    onComplete()
                } else {
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to upload documents", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                    
                    val groupValue = spinner?.selectedItem?.toString() ?: "COR"
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
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "No signatures to upload", android.widget.Toast.LENGTH_SHORT).show()
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
                        android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, msg, android.widget.Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to finalize", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to upload signatures", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                        android.app.AlertDialog.Builder(this@CorporateCustomerAccountOpeningActivity)
                            .setMessage("Document Master Submitted Successfully")
                            .setPositiveButton("Okay") { dialog, _ -> dialog.dismiss() }
                            .setCancelable(false)
                            .show()
                    } else {
                        android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to submit documents", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
            val docTypes = listOf("SELECT", "Certificate of Incorporation", "Memorandum", "Articles of Association")
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
                    
                    photoParts.add(getMultipartBody(photoTag, "photo", this@CorporateCustomerAccountOpeningActivity, i))
                    signParts.add(getMultipartBody(signTag, "sign", this@CorporateCustomerAccountOpeningActivity, i))
                }
                
                val gson = com.google.gson.Gson()
                val schedulerJson = gson.toJson(requestsList)
                val schedulerBody = schedulerJson.toRequestBody("application/json".toMediaTypeOrNull())
                val schedulerPart = okhttp3.MultipartBody.Part.createFormData("scheduler", "scheduler.json", schedulerBody)
                
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.api.addSignatureCorporate(schedulerPart, photoParts, signParts, cifId)
                }
                
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Signatures Uploaded Successfully", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Upload Failed: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
            val groups = listOf("Select", "Director", "Authorized Signatory", "Other")
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
            binding.tvLabelBranchDesc to "Branch Desc *",
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
            binding.tvLabelCertReg to "Certification of Registration *",
            binding.tvLabelBusReg to "Business Registration *",
            binding.tvLabelDateIncorp to "Date of Incorporation *",
            binding.tvLabelCountryOperation to "Country of Operation *",
            
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
            if (binding.etMonthlyIncome.text.isNullOrBlank()) { binding.etMonthlyIncome.error = "Required"; isValid = false }
            if (binding.etEmailIdAO.text.isNullOrBlank()) { binding.etEmailIdAO.error = "Required"; isValid = false }
            if (binding.etMobileNoAO.text.isNullOrBlank()) { binding.etMobileNoAO.error = "Required"; isValid = false }

            // Address Details
            if (binding.spAddressType.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Address Type", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etHouseNo.text.isNullOrBlank()) { binding.etHouseNo.error = "Required"; isValid = false }
            if (binding.etStreetNo.text.isNullOrBlank()) { binding.etStreetNo.error = "Required"; isValid = false }
            if (binding.etStreetName.text.isNullOrBlank()) { binding.etStreetName.error = "Required"; isValid = false }
            if (binding.spCity.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select City", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etAddressValidFrom.text.isNullOrBlank()) { binding.etAddressValidFrom.error = "Required"; isValid = false }
            if (binding.spNationality.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Nationality", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.spCountryOfBirth.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Country of Birth", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
        } else if (currentTab == 1) { // Account Details
            if (binding.spSchemeType.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Scheme Type", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
            if (binding.etCertReg.text.isNullOrBlank()) { binding.etCertReg.error = "Required"; isValid = false }
            if (binding.etBusReg.text.isNullOrBlank()) { binding.etBusReg.error = "Required"; isValid = false }
            if (binding.etDateIncorp.text.isNullOrBlank()) { binding.etDateIncorp.error = "Required"; isValid = false }
            if (binding.spCountryOperation.selectedItemPosition == 0) { android.widget.Toast.makeText(this, "Please select Country of Operation", android.widget.Toast.LENGTH_SHORT).show(); isValid = false }
        }

        return isValid
    }

    private fun setupDatePickers() {
        binding.etDateOfIncorp.setOnClickListener { showDatePicker(binding.etDateOfIncorp) }
        binding.etAddressValidFrom.setOnClickListener { showDatePicker(binding.etAddressValidFrom) }
        binding.etDateIncorp.setOnClickListener { showDatePicker(binding.etDateIncorp) } // Account Details Tab
        binding.etDateOfDeposit.setOnClickListener { showDatePicker(binding.etDateOfDeposit) }
        binding.etMaturityDate.setOnClickListener { showDatePicker(binding.etMaturityDate) }
        binding.etRP1AddressValidFrom.setOnClickListener { showDatePicker(binding.etRP1AddressValidFrom) }
        binding.etInstallmentStartDate.setOnClickListener { showDatePicker(binding.etInstallmentStartDate) }
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
        val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN01955"
        binding.tvAppRefNoHeader.text = "APP REF NO : $appRefNo"
    }

    private fun receiveData() {
        val primaryBranch = intent.getStringExtra("primary_branch") ?: "103"
        val branchName    = intent.getStringExtra("branch_name") ?: "Al Salam Bank Seychelles Limited"

        binding.etCustomerType.setText(intent.getStringExtra("customer_type") ?: "CORPORATE")
        binding.etCifId.setText(intent.getStringExtra("cif_id") ?: "")
        binding.etPrimaryBranch.setText(primaryBranch)
        binding.etBranchDesc.setText(branchName)
        
        binding.etConstitutionName.setText(intent.getStringExtra("constitution") ?: "")
        binding.etCorporateName.setText(intent.getStringExtra("full_name") ?: "")
        binding.etTradeName.setText(intent.getStringExtra("short_name") ?: "")
        
        binding.etCertIncorp.setText(intent.getStringExtra("cert_incorp") ?: "")
        binding.etBusRegNo.setText(intent.getStringExtra("bus_reg_no") ?: "")
        binding.etDateOfIncorp.setText(intent.getStringExtra("dob") ?: "")
        binding.etDateIncorp.setText(intent.getStringExtra("dob") ?: "")
        
        binding.etPostBoxNo.setText(intent.getStringExtra("post_box_no") ?: "")
        binding.etLandLineNo.setText(intent.getStringExtra("land_line_no") ?: "")
        binding.etFaxNo.setText(intent.getStringExtra("fax_no") ?: "")
        
        binding.etEmail.setText(intent.getStringExtra("email_id") ?: "")
        binding.etEmailIdAO.setText(intent.getStringExtra("email_id") ?: "")
        binding.etWebsite.setText(intent.getStringExtra("website") ?: "")

        // Also pre-fill Account Details tab branch fields immediately from intent
        binding.etAccountBranchId.setText(primaryBranch)
        binding.etAccountBranchName.setText(branchName)
        binding.etCertReg.setText(intent.getStringExtra("nationalid") ?: intent.getStringExtra("cert_incorp") ?: "")
        binding.etBusReg.setText(intent.getStringExtra("passno") ?: intent.getStringExtra("bus_reg_no") ?: "")
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

    private fun setupCalculations() {

        fun formatCurrency(value: Double): String {
            val formatter = java.text.DecimalFormat("#,##0.00")
            return formatter.format(value)
        }

        fun calculateLoanMath() {
            try {
                val s = binding.etLoanSanctioned.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
                val m = binding.etMargin.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
                if (s > 0 && m >= 0) {
                    val d = s - (s * m / 100)
                    binding.etDrawingLimit.setText(formatCurrency(d))
                } else {
                    binding.etDrawingLimit.setText("")
                }
                val i = binding.etInterestRate.text.toString().toDoubleOrNull() ?: 0.0
                val p = binding.etLoanPeriod.text.toString().toIntOrNull() ?: 0
                if (s > 0 && i > 0 && p > 0) {
                    val r = i / (12 * 100)
                    val emi = (s * r * Math.pow(1 + r, p.toDouble())) / (Math.pow(1 + r, p.toDouble()) - 1)
                    binding.etInstallmentAmount.setText(formatCurrency(emi))
                } else {
                    binding.etInstallmentAmount.setText("")
                }
            } catch (e: Exception) {}
        }
        binding.etLoanSanctioned.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }
        })
        binding.etMargin.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }
        })
        binding.etInterestRate.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }
        })
        binding.etLoanPeriod.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }
        })
        fun calculateDepositMath() {
            try {
                val p = binding.etDepositAmount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
                val r = binding.etRateOfInterest.text.toString().toDoubleOrNull() ?: 0.0
                val t = binding.etDepositPeriod.text.toString().toIntOrNull() ?: 0
                if (p > 0 && r > 0 && t > 0) {
                    val interest = (p * r * t) / (12 * 100)
                    val maturity = p + interest
                    binding.etInterestAmount.setText(formatCurrency(interest))
                    binding.etMaturityAmount.setText(formatCurrency(maturity))
                } else {
                    binding.etInterestAmount.setText("")
                    binding.etMaturityAmount.setText("")
                }
            } catch (e: Exception) {}
        }
        binding.etDepositAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }
        })
        binding.etRateOfInterest.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }
        })
        binding.etDepositPeriod.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }
        })
        binding.etAnnualIncome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                try {
                    val annual = s?.toString()?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    val monthly = annual / 12
                    binding.etMonthlyIncome.setText(formatCurrency(monthly))
                } catch (e: Exception) {}
            }
        })
    }

    private fun setupSpinners() {

        // ── helper to build a standard adapter ──────────────────────────────
        fun makeAdapter(items: List<String>) =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, items).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        // ─────────────────────────────────────────────────────────────────────
        // MAIN CORPORATE DETAILS – Address Section
        // Exact values from web HTML:
        //   ca_address_type: SELECT | MAILING | PERMANENT
        //   ca_city:         SELECT | CHENNAI | THANJAVUR | SALEM
        //   ca_nationality:  SELECT | SEYCHELLOIS  | INDIAN | USA
        //   ca_country_of_birth / countryOrigin: SELECT | SEYCHELLES | INDIA | USA
        // ─────────────────────────────────────────────────────────────────────
        val addressTypes = listOf("SELECT", "MAILING", "PERMANENT")
        binding.spAddressType.adapter = makeAdapter(addressTypes)

        val cities = listOf("SELECT", "CHENNAI", "THANJAVUR", "SALEM")
        binding.spCity.adapter = makeAdapter(cities)

        // Web value has trailing space: "SEYCHELLOIS " – keep exactly to match backend
        val nationalities = listOf("SELECT", "SEYCHELLOIS ", "INDIAN", "USA")
        binding.spNationality.adapter = makeAdapter(nationalities)

        val countries = listOf("SELECT", "SEYCHELLES", "INDIA", "USA")
        binding.spCountryOfBirth.adapter   = makeAdapter(countries)
        binding.spCountryOfOrigin.adapter  = makeAdapter(countries)
        binding.spCountryOperation.adapter = makeAdapter(countries)

        // ─────────────────────────────────────────────────────────────────────
        // RELATED PARTY 1 – Customer Type
        // web values: INDIVIDUAL | JOINT ACCOUNT | CORPORATE CUSTOMER
        // ─────────────────────────────────────────────────────────────────────
        val rp1CustomerTypes = listOf("SELECT", "INDIVIDUAL", "JOINT ACCOUNT", "CORPORATE CUSTOMER")
        binding.spRP1CustomerType.adapter = makeAdapter(rp1CustomerTypes)

        // ─────────────────────────────────────────────────────────────────────
        // RELATED PARTY 1 – Salutation  (web: MR | MS)
        // ─────────────────────────────────────────────────────────────────────
        val salutations = listOf("SELECT", "MR", "MS")
        binding.spRP1Salutation.adapter = makeAdapter(salutations)

        // ─────────────────────────────────────────────────────────────────────
        // RELATED PARTY 1 – Address (same lists as main section)
        // ─────────────────────────────────────────────────────────────────────
        binding.spRP1AddressType.adapter    = makeAdapter(addressTypes)
        binding.spRP1City.adapter           = makeAdapter(cities)
        binding.spRP1Nationality.adapter    = makeAdapter(nationalities)
        binding.spRP1CountryOfBirth.adapter = makeAdapter(countries)
        binding.spRP1CountryOfOrigin.adapter = makeAdapter(countries)

        // ─────────────────────────────────────────────────────────────────────
        // Auto-fill RP1 read-only branch fields from intent (same branch)
        // ─────────────────────────────────────────────────────────────────────
        binding.etRP1PrimaryBranch.setText(intent.getStringExtra("primary_branch") ?: "103")
        binding.etRP1BranchDesc.setText(
            intent.getStringExtra("branch_name") ?: "Al Salam Bank Seychelles Limited"
        )

        // ─────────────────────────────────────────────────────────────────────
        // RP1 Annual Income → auto-calculate Monthly Income
        // ─────────────────────────────────────────────────────────────────────
        binding.etRP1AnnualIncome.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                try {
                    val annual = s?.toString()?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                    val monthly = annual / 12
                    val fmt = java.text.DecimalFormat("#,##0.00")
                    binding.etRP1MonthlyIncome.setText(fmt.format(monthly))
                } catch (e: Exception) {}
            }
        })

        // ─────────────────────────────────────────────────────────────────────
        // Account scheme type spinner
        // ─────────────────────────────────────────────────────────────────────
        val schemeTypes = listOf("SELECT", "LOAN ACCOUNT", "DEPOSIT ACCOUNT")
        binding.spSchemeType.adapter = makeAdapter(schemeTypes)
        
        val frequencies = listOf("MONTHLY", "QUARTERLY", "HALFYEARLY", "YEARLY")
        binding.spDepositCompoundingFactor.adapter = makeAdapter(frequencies)
        binding.spDepositFrequency.adapter = makeAdapter(frequencies)
        binding.spDepositFrequency2.adapter = makeAdapter(frequencies)
        binding.spPrincipalInstallmentFreq.adapter = makeAdapter(frequencies)
        binding.spInterestInstallmentFreq.adapter = makeAdapter(frequencies)

        val interestTypes = listOf("SIMPLE", "COMPOUND")
        binding.spDepositInterestType.adapter = makeAdapter(interestTypes)

        val depositTypes = listOf("Fixed", "Recurring")
        binding.spDepositType.adapter = makeAdapter(depositTypes)
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
