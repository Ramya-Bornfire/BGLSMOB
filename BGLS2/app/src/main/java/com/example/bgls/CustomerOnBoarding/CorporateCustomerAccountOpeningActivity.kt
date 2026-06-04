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
    }

    private fun fetchSchemeDetails(selected: String) {
        val isDeposit = selected == "FIXED DEPOSIT"
        val schemeType = when (selected) {
            "FIXED DEPOSIT" -> "TDFIXED"
            else -> "LSRET"  // LOAN ACCOUNT
        }
        // GL values matching web: Loan=Asset(1000/1500), Deposit=Liability(2000/2500)
        val glCode   = if (isDeposit) "2000" else "1000"
        val glDesc   = if (isDeposit) "Liability" else "Asset"
        val glshCode = if (isDeposit) "2500" else "1500"
        val glshDesc = if (isDeposit) "TERM DEPOSIT GENERAL" else "LOAN ACCOUNT GENERAL"
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
                            // Backend field names from BACP_PARAMETER table:
                            // gl_code  → "gl_code"   (confirmed in ParametersDetails.java)
                            // gl_desc  → "schmdesc"  (no gl_desc column; schmdesc is the description)
                            // glsh     → "glsh"      (field is "glsh", NOT "glsh_code")
                            // glsh_desc→ hardcoded in web JS switch; not stored in DB
                            schemeGlCode   = data.optString("gl_code", glCode)
                            schemeGlDesc   = data.optString("schmdesc", glDesc)  // correct DB column
                            schemeGlshCode = data.optString("glsh", glshCode)    // correct DB column name
                            schemeGlshDesc = glshDesc  // hardcoded: "LOAN ACCOUNT GENERAL" / "TERM DEPOSIT GENERAL"
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

    private fun savePersonalDetails() {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Saving Corporate Details...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val customerType = binding.etCustomerType.text.toString()
                val request = com.example.bgls.DataModels.CustomerRequest(
                    ca_customer_type_1 = customerType,
                    corporateName = binding.etCorporateName.text.toString(),
                    tradeName = binding.etTradeName.text.toString(),
                    date_incorporation = binding.etDateOfIncorp.text.toString(),
                    ca_email_id = binding.etEmailIdAO.text.toString(),
                    ca_mobile_number = binding.etMobileNoAO.text.toString(),
                    monthly_income = binding.etMonthlyIncome.text.toString(),
                    ca_address_type = binding.spAddressType.selectedItem.toString(),
                    ca_house_no = binding.etHouseNo.text.toString(),
                    ca_street_no = binding.etStreetNo.text.toString(),
                    ca_street_name = binding.etStreetName.text.toString(),
                    ca_city = binding.spCity.selectedItem.toString(),
                    ca_address_validation_form = binding.etAddressValidFrom.text.toString(),
                    ca_nationality = binding.spNationality.selectedItem.toString(),
                    ca_country_of_birth = binding.spCountryOfBirth.selectedItem.toString(),
                    branch_desc = intent.getStringExtra("branch_name") ?: "",
                    ca_cif_id_1 = intent.getStringExtra("cif_id") ?: ""
                )
                
                val params = request.toMap()
                
                val appRefNo = intent.getStringExtra("app_ref_no") ?: ""
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.savePersonalDetail(appRefNo, "1", "", "", params)
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
                formData["schemetype"] = if (scheme == "FIXED DEPOSIT") "TD" else "LA"
                formData["schemecode"] = if (scheme == "FIXED DEPOSIT") "TDFIXED" else "LSRET"
                formData["currency"] = "SCR"
                formData["prisolid"] = binding.etPrimaryBranch.text.toString()
                formData["branch_desc"] = binding.etBranchDesc.text.toString()
                formData["certificate_registration"] = binding.etCertReg.text.toString()
                formData["business_registration"] = binding.etBusReg.text.toString()
                formData["date_incorporation"] = binding.etDateIncorp.text.toString()
                formData["countryOrigin"] = binding.spCountryOperation.selectedItem.toString()
                
                if (formData["schemetype"] == "LA") {
                    formData["gl_code_loan"] = schemeGlCode
                    formData["gl_desc_loan"] = schemeGlDesc
                    formData["glsh_code_loan"] = schemeGlshCode
                    formData["glsh_desc_loan"] = schemeGlshDesc
                    formData["account_no"] = generatedAccountNo
                } else {
                    formData["gl_code"] = schemeGlCode
                    formData["gl_desc"] = schemeGlDesc
                    formData["glsh_code"] = schemeGlshCode
                    formData["glsh_desc"] = schemeGlshDesc
                    formData["deposit_account_no"] = generatedAccountNo
                }
                
                val body = mapOf(
                    "formData" to formData,
                    "loanAccountNo" to generatedAccountNo,
                    "accountNo" to generatedAccountNo,
                    "scheduleList" to emptyList<Any>()
                )
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.saveAccountDetails(appRefNo, body)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
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
                    val errBody = response.errorBody()?.string() ?: "Failed to save details"
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, errBody.take(200), android.widget.Toast.LENGTH_LONG).show()
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
        binding.etPrimaryBranch.setText(primaryBranch)
        binding.etBranchDesc.setText(branchName)
        
        binding.etCorporateName.setText(intent.getStringExtra("full_name") ?: "")
        binding.etTradeName.setText(intent.getStringExtra("short_name") ?: "")
        binding.etDateOfIncorp.setText(intent.getStringExtra("dob") ?: "")
        binding.etDateIncorp.setText(intent.getStringExtra("dob") ?: "")
        binding.etEmail.setText(intent.getStringExtra("email_id") ?: "")
        binding.etEmailIdAO.setText(intent.getStringExtra("email_id") ?: "")

        // Also pre-fill Account Details tab branch fields immediately from intent
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
        val addressTypes = listOf("SELECT", "PERMANENT", "REGISTERED", "OFFICE")
        val addrAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, addressTypes)
        addrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spAddressType.adapter = addrAdapter

        val cities = listOf("SELECT", "Victoria", "Anse Boileau", "Beau Vallon")
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCity.adapter = cityAdapter

        val nationalities = listOf("SELECT", "Seychellois", "Indian", "British")
        val natAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nationalities)
        natAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNationality.adapter = natAdapter

        val countries = listOf("SELECT", "Seychelles", "India", "UK")
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCountryOfBirth.adapter = countryAdapter
        binding.spCountryOfOrigin.adapter = countryAdapter
        binding.spCountryOperation.adapter = countryAdapter

        val schemeTypes = listOf("SELECT", "CURRENT ACCOUNT", "FIXED DEPOSIT")
        val schAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schemeTypes)
        schAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSchemeType.adapter = schAdapter
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
