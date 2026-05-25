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
        val schemeType = when (selected) {
            "FIXED DEPOSIT" -> "TDFIXED"
            else -> "LSRET"
        }
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getSchemeDetails(schemeType)
                }
                if (response.isSuccessful) {
                    val responseString = response.body()?.string() ?: ""
                    val json = org.json.JSONObject(responseString)
                    val data = json.optJSONObject("data")
                    generatedAccountNo = json.optString("loanAccountNo", json.optString("accountNo", ""))
                    schemeGlCode = data?.optString("glcode") ?: ""
                    schemeGlDesc = data?.optString("gldesc") ?: ""
                    schemeGlshCode = data?.optString("glsh_code") ?: ""
                    schemeGlshDesc = data?.optString("glsh_desc") ?: ""
                }
            } catch (e: Exception) {
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
                val params = mutableMapOf<String, String>()
                params["customer_type"] = binding.etCustomerType.text.toString()
                params["first_name"] = binding.etFirstName.text.toString()
                params["middle_name"] = binding.etMiddleName.text.toString()
                params["last_name"] = binding.etLastName.text.toString()
                params["full_name"] = binding.etFullName.text.toString()
                params["dob"] = binding.etDateOfBirth.text.toString()
                params["mobile_no"] = binding.etMobileNoAO.text.toString()
                params["email_id"] = binding.etEmailIdAO.text.toString()
                params["salutation"] = binding.spSalutation.selectedItem.toString()
                params["gender"] = binding.spGender.selectedItem.toString()
                params["martial_status"] = binding.spMartialStatus.selectedItem.toString()
                params["occupation"] = binding.spOccupation.selectedItem.toString()
                params["annual_income"] = binding.etAnnualIncome.text.toString()
                params["monthly_income"] = binding.etMonthlyIncome.text.toString()
                params["address_type"] = binding.spAddressType.selectedItem.toString()
                params["house_no"] = binding.etHouseNo.text.toString()
                params["street_no"] = binding.etStreetNo.text.toString()
                params["street_name"] = binding.etStreetName.text.toString()
                params["city"] = binding.spCity.selectedItem.toString()
                params["address_valid_from"] = binding.etAddressValidFrom.text.toString()
                params["nationality"] = binding.spNationality.selectedItem.toString()
                params["country_of_birth"] = binding.spCountryOfBirth.selectedItem.toString()
                
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.savePersonalDetail(appRefNo, "1", params)
                }
                
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Personal Details Saved", android.widget.Toast.LENGTH_SHORT).show()
                    binding.tabLayout.getTabAt(1)?.select()
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
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"
                
                val formData = mutableMapOf<String, Any>()
                val scheme = binding.spSchemeType.selectedItem.toString()
                formData["schemetype"] = if (scheme == "FIXED DEPOSIT") "TD" else "LA"
                formData["schemecode"] = if (scheme == "FIXED DEPOSIT") "TDFIXED" else "LSRET"
                formData["currency"] = "SCR"
                formData["prisolid"] = binding.etPrimaryBranch.text.toString()
                formData["branch_desc"] = binding.etBranchName.text.toString()
                formData["nationalid"] = binding.etNationalIdAO.text.toString()
                formData["passno"] = binding.etPassportNoAO.text.toString()
                formData["issuedate"] = binding.etIssueDate.text.toString()
                formData["expdate"] = binding.etExpiryDate.text.toString()
                
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
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Account Details Saved", android.widget.Toast.LENGTH_SHORT).show()
                    binding.tabLayout.getTabAt(2)?.select()
                } else {
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Failed to save details", android.widget.Toast.LENGTH_SHORT).show()
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
                    android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Documents Uploaded", android.widget.Toast.LENGTH_SHORT).show()
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
                
                val schedulerBody = "[{\"appl_ref_no\":\"$appRefNo\",\"rec_no\":\"1\",\"img_access_code\":\"GRP\",\"img_group\":\"IND\",\"keyword\":\"SIG\"}]".toRequestBody("application/json".toMediaTypeOrNull())
                val schedulerPart = MultipartBody.Part.createFormData("scheduler", "scheduler.json", schedulerBody)
                
                val dummyBody = byteArrayOf(0).toRequestBody("image/png".toMediaTypeOrNull())
                val photoPart = listOf(MultipartBody.Part.createFormData("photo", "photo.png", dummyBody))
                val signPart = listOf(MultipartBody.Part.createFormData("sign", "sign.png", dummyBody))
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.addSignatureCorporate(schedulerPart, photoPart, signPart, cifId)
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
                        android.widget.Toast.makeText(this@CustomerAccountOpeningActivity, "Application Submitted Successfully", android.widget.Toast.LENGTH_LONG).show()
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
            android.widget.Toast.makeText(this, "Documents Submitted Successfully", android.widget.Toast.LENGTH_SHORT).show()
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
            android.widget.Toast.makeText(this, "Signatures Uploaded Successfully", android.widget.Toast.LENGTH_SHORT).show()
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
            if (binding.etNationalIdAO.text.isNullOrBlank()) { binding.etNationalIdAO.error = "Required field to enter"; isValid = false }
            if (binding.etIssueDate.text.isNullOrBlank()) { binding.etIssueDate.error = "Required field to enter"; isValid = false }
            if (binding.etPassportNoAO.text.isNullOrBlank()) { binding.etPassportNoAO.error = "Required field to enter"; isValid = false }
            if (binding.etExpiryDate.text.isNullOrBlank()) { binding.etExpiryDate.error = "Required field to enter"; isValid = false }
        }

        return isValid
    }

    private fun setupDatePickers() {
        binding.etAccountOpenDate.setOnClickListener { showDatePicker(binding.etAccountOpenDate) }
        binding.etAddressValidFrom.setOnClickListener { showDatePicker(binding.etAddressValidFrom) }
        binding.etIssueDate.setOnClickListener { showDatePicker(binding.etIssueDate) }
        binding.etExpiryDate.setOnClickListener { showDatePicker(binding.etExpiryDate) }
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
                activeTextTarget?.visibility = android.view.View.GONE
            }
        }
        pickSignatureLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { 
                activeImageTarget?.setImageURI(it)
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
        // Populate fields with data from MinimalDataActivity
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

        val schemeTypes = listOf("SELECT", "SAVINGS ACCOUNT", "CURRENT ACCOUNT", "FIXED DEPOSIT")
        val schAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, schemeTypes)
        schAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSchemeType.adapter = schAdapter
    }
}
