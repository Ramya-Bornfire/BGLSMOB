package com.example.bgls.CustomerOnBoarding

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.ApprovalViewResponse
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class KYCComplianceViewActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private var appRefNo = ""
    private var recNo = ""
    private var schemeType = ""
    private var accountNo = ""
    private var schemeCode = ""
    private var isFromApproval = false
    private var isFromCompliance = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyccompliance_view)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false)
        }

        appRefNo = intent.getStringExtra("appRefNo") ?: ""
        val customerName = intent.getStringExtra("customerName") ?: ""
        val customerGroup = intent.getStringExtra("customerGroup") ?: ""
        isFromApproval = intent.getBooleanExtra("isFromApproval", false)
        isFromCompliance = intent.getBooleanExtra("isFromCompliance", false)

        // Set header and button visibility
        if (isFromApproval) {
            findViewById<LinearLayout>(R.id.llKycComplianceSection).visibility = android.view.View.GONE
            findViewById<TextView>(R.id.tvHeaderTitle).text = "LIST FOR APPROVAL - VIEW"
            findViewById<Button>(R.id.btnSubmit).text = "Approve"
            findViewById<Button>(R.id.btnHold).visibility = android.view.View.VISIBLE
            findViewById<Button>(R.id.btnReject).visibility = android.view.View.VISIBLE
        } else if (isFromCompliance) {
            findViewById<LinearLayout>(R.id.llKycComplianceSection).visibility = android.view.View.GONE
            findViewById<LinearLayout>(R.id.llComplianceDeptSection).visibility = android.view.View.VISIBLE
            findViewById<TextView>(R.id.tvHeaderTitle).text = "COMPLIANCE DEPARTMENT - VIEW"
            setupComplianceSpinners()
        }

        findViewById<TextView>(R.id.tvAppRefNoHeader).text = "App Ref No : $appRefNo"

        // Buttons
        findViewById<Button>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            if (isFromApproval) approveRecord()
            else if (isFromCompliance) submitKycCompliance()
            else Toast.makeText(this, "KYC Submitted successfully", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnHold).setOnClickListener { showHoldDialog() }
        findViewById<Button>(R.id.btnReject).setOnClickListener { showRejectDialog() }

        fetchFullDetails()
    }

   /* private fun fetchFullDetails() {
        progressDialog.show()
        val call = if (isFromApproval) {
            RetrofitClient.api.getApprovalView(applRefNo = appRefNo)
        } else {
            RetrofitClient.api.getKycView(applRefNo = appRefNo)
        }

        call.enqueue(object : Callback<ApprovalViewResponse> {
            override fun onResponse(call: Call<ApprovalViewResponse>, response: Response<ApprovalViewResponse>) {
                progressDialog.dismiss()

                // Log the raw response string first (to see what we received)
                response.errorBody()?.let {
                    val errorBodyString = it.string()
                    Log.e("API_ERROR", "Error body: $errorBodyString")
                }

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val data = response.body()!!
                        // Log success
                        Log.d("API_RESPONSE", "Success: ${data.customerRequest.appl_ref_no}")
                        populateAllFields(data)
                        recNo = data.customerRequest.rec_no
                        schemeType = data.customerRequest.ca_schemetype ?: ""
                        schemeCode = data.customerRequest.ca_scheme_code ?: ""
                        accountNo = if (schemeType == "LA") data.customerRequest.la_loan_accountno ?: ""
                        else data.customerRequest.td_deposit_accountno ?: ""
                    } catch (e: Exception) {
                        Log.e("API_RESPONSE", "Parsing error: ${e.message}", e)
                        Toast.makeText(this@KYCComplianceViewActivity, "Failed to parse data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Server returned an error (likely the circular reference)
                    Log.e("API_RESPONSE", "Response unsuccessful. Code: ${response.code()}")
                    Toast.makeText(this@KYCComplianceViewActivity, "Server error – could not load details", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApprovalViewResponse>, t: Throwable) {
                progressDialog.dismiss()
                Log.e("API_RESPONSE", "Network failure: ${t.message}", t)
                Toast.makeText(this@KYCComplianceViewActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }*/
   private fun fetchFullDetails() {
       progressDialog.show()
       val call = RetrofitClient.api.getApprovalView(applRefNo = appRefNo)
       call.enqueue(object : Callback<ApprovalViewResponse> {
           override fun onResponse(call: Call<ApprovalViewResponse>, response: Response<ApprovalViewResponse>) {
               progressDialog.dismiss()
               response.errorBody()?.let {
                   Log.e("API_ERROR", "Error body: ${it.string()}")
               }
               if (response.isSuccessful && response.body() != null) {
                   try {
                       val data = response.body()!!
                       Log.d("API_RESPONSE", "Success: ${data.customerRequest.appl_ref_no}")
                       populateAllFields(data)
                       recNo = data.customerRequest.rec_no
                       schemeType = data.customerRequest.ca_schemetype ?: ""
                       schemeCode = data.customerRequest.ca_scheme_code ?: ""
                       accountNo = if (schemeType == "LA") data.customerRequest.la_loan_accountno ?: ""
                       else data.customerRequest.td_deposit_accountno ?: ""
                   } catch (e: Exception) {
                       Log.e("API_RESPONSE", "Parsing error: ${e.message}", e)
                       Toast.makeText(this@KYCComplianceViewActivity, "Failed to parse data", Toast.LENGTH_SHORT).show()
                   }
               } else {
                   Log.e("API_RESPONSE", "Response unsuccessful. Code: ${response.code()}")
                   Toast.makeText(this@KYCComplianceViewActivity, "Server error – could not load details", Toast.LENGTH_LONG).show()
               }
           }

           override fun onFailure(call: Call<ApprovalViewResponse>, t: Throwable) {
               progressDialog.dismiss()
               Log.e("API_RESPONSE", "Network failure: ${t.message}", t)
               Toast.makeText(this@KYCComplianceViewActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
           }
       })
   }
    private fun populateAllFields(data: ApprovalViewResponse) {
        val req = data.customerRequest

        // Minimal Data
        setText(R.id.tvPrimaryBranch, req.ca_solid)
        setText(R.id.tvBranchName, req.branch_desc)
        setText(R.id.tvCustomerGroup, req.ca_customer_type)
        setText(R.id.tvFirstName, req.ca_first_name)
        setText(R.id.tvMiddleName, req.mid_name)
        setText(R.id.tvLastName, req.ca_last_name)
        setText(R.id.tvShortName, req.ca_preferred_name)
        // Full Name: use ca_full_name_1 (DB full name), fallback to preferred name or first name
        setText(R.id.tvFullName, req.ca_full_name_1 ?: req.ca_preferred_name ?: req.ca_first_name)
        setText(R.id.tvDateOfBirth, formatDate(req.ca_date_of_birth))
        setText(R.id.tvMobileCode, req.ca_countrycode_1)
        setText(R.id.tvMobileNumber, req.ca_mobile_number)
        setText(R.id.tvPassportNumber, req.ca_passport_number)
        setText(R.id.tvNationalId, req.ca_idenditification_number)

        // Personal Details
        setText(R.id.tvCustomerTypeP, req.la_customer_type ?: req.ca_customer_type)
        setText(R.id.tvCifId, req.cif_id)
        setText(R.id.tvPrimaryBranchP, req.ca_solid)
        setText(R.id.tvBranchNameP, req.branch_desc)
        setText(R.id.tvSalutation, req.ca_saluation)
        setText(R.id.tvFirstNameP, req.ca_first_name)
        setText(R.id.tvMiddleNameP, req.mid_name)
        setText(R.id.tvLastNameP, req.ca_last_name)
        // Full Name in Personal section
        setText(R.id.tvFullNameP, req.ca_full_name_1 ?: req.ca_preferred_name ?: req.ca_first_name)
        setText(R.id.tvShortNameP, req.ca_preferred_name)
        setText(R.id.tvOccupation, req.ca_occupation1)
        setText(R.id.tvGender, req.ca_gender)
        setText(R.id.tvMaritalStatus, req.ca_martial_staus)
        setText(R.id.tvDateOfBirthP, formatDate(req.ca_date_of_birth))
        setText(R.id.tvAnnualIncome, req.annual_income)
        setText(R.id.tvMonthlyIncome, req.monthly_income)
        setText(R.id.tvLoanObligations, req.loan_obligations)
        setText(R.id.tvFamilyMaintenance, req.family_maintenance)
        setText(R.id.tvEmailId, req.ca_email_id)
        setText(R.id.tvMobileCodeP, req.ca_countrycode_1)
        setText(R.id.tvMobileNumberP, req.ca_mobile_number)

        // Address Details
        setText(R.id.tvAddressType, req.ca_address_type)
        setText(R.id.tvHouseNo, req.ca_house_no)
        setText(R.id.tvStreetNo, req.ca_street_no)
        setText(R.id.tvStreetName, req.ca_street_name)
        setText(R.id.tvCountry, req.ca_country)
        setText(R.id.tvState, req.ca_state)
        setText(R.id.tvCity, req.ca_city)
        setText(R.id.tvPostalCode, req.ca_postal_code)
        setText(R.id.tvAddressValidFrom, formatDate(req.ca_address_validation_form))
        setText(R.id.tvNationality, req.ca_nationality)
        setText(R.id.tvCountryOfBirth, req.ca_country_of_birth)
        setText(R.id.tvCountryOfOrigin, req.countryOrigin)

        // Account Details (common)
        setText(R.id.tvSchemeType, req.ca_schemetype)
        setText(R.id.tvSchemeCode, req.ca_scheme_code)
        setText(R.id.tvAccBranchId, req.ca_solid)
        setText(R.id.tvAccBranchName, req.branch_desc)

        // GL & GLSH (always present)
        setText(R.id.tvGlCode, req.la_glcode ?: req.td_glcode)
        setText(R.id.tvGlDesc, req.la_gldesc ?: req.td_gldesc)
        setText(R.id.tvGlshCode, req.la_glshcode ?: req.td_glshcode)
        setText(R.id.tvGlshDesc, req.la_glshdesc ?: req.td_glshdesc)

        when (req.ca_schemetype) {
            "LA" -> {
                val loan = data.loanDetails
                if (loan != null) {
                    // Populate from separate loanDetails object (approved/processed records)
                    setText(R.id.tvLoanAccNo, loan.loan_accountno)
                    setText(R.id.tvDateOfLoan, formatDate(loan.date_of_loan))
                    setText(R.id.tvAccCurrency, loan.loan_currency)
                    setText(R.id.tvInterestRate, loan.effective_interest_rate)
                    setText(R.id.tvLoanSanctioned, formatAmount(loan.loan_sanctioned))
                    setText(R.id.tvMarginPercent, loan.margin_limit?.toString())
                    setText(R.id.tvDrawingLimit, formatAmount(loan.drawing_limit))
                    setText(R.id.tvOutstanding, formatAmount(loan.loan_outstanding))
                    setText(R.id.tvDisbursement, formatAmount(loan.disbursement))
                    setText(R.id.tvRecoveryMethod, loan.recovery_method)
                    setText(R.id.tvExpiryDate, formatDate(loan.expiry_date))
                    setText(R.id.tvRemarksAcc, req.la_remarks)

                    // Payment details
                    data.paymentDetails?.let { pay ->
                        setText(R.id.tvInstallmentId, pay.inst_id)
                        setText(R.id.tvInstStartDate, formatDate(pay.inst_start_dt))
                        setText(R.id.tvNoOfInst, pay.no_of_inst?.toString())
                        setText(R.id.tvPrincInstFreq, pay.inst_freq)
                        setText(R.id.tvIntInstFreq, pay.interest_frequency)
                        setText(R.id.tvInstAmount, formatAmount(pay.inst_amount))
                        setText(R.id.tvInstPercent, pay.inst_pct?.toString())
                    }
                } else {
                    // Fallback: populate from CustomerRequest fields directly (pending/new records)
                    setText(R.id.tvLoanAccNo, req.la_loan_accountno)
                    setText(R.id.tvDateOfLoan, formatDate(req.la_date_loan))
                    setText(R.id.tvLoanSanctioned, req.la_loan_sanctioned)
                    setText(R.id.tvMarginPercent, req.la_margin)
                    setText(R.id.tvDrawingLimit, req.la_drawing_limit)
                    setText(R.id.tvOutstanding, req.la_outstanding)
                    setText(R.id.tvDisbursement, req.la_disbursement)
                    setText(R.id.tvRecoveryMethod, req.la_recovery_method)
                    setText(R.id.tvExpiryDate, formatDate(req.la_expiry_date))
                    setText(R.id.tvRemarksAcc, req.la_remarks)
                }
            }
            "TD" -> {
                val dep = data.depositData
                if (dep != null) {
                    // Populate from separate depositData object (approved/processed records)
                    setText(R.id.tvLoanAccNo, dep.depo_actno)          // Deposit account number
                    setText(R.id.tvDateOfLoan, formatDate(dep.deposit_date))
                    setText(R.id.tvAccCurrency, dep.currency)
                    setText(R.id.tvLoanSanctioned, dep.deposit_amt)    // Deposit amount
                    setText(R.id.tvDisbursement, dep.int_amt)          // Interest amount
                    setText(R.id.tvExpiryDate, formatDate(dep.maturity_date))
                    setText(R.id.tvInterestRate, dep.rate_of_int)
                    setText(R.id.tvMarginPercent, dep.deposit_period)
                } else {
                    // Fallback: populate from CustomerRequest fields directly (pending/new records)
                    setText(R.id.tvLoanAccNo, req.td_deposit_accountno)
                    setText(R.id.tvDateOfLoan, formatDate(req.td_date_deposit))
                    setText(R.id.tvAccCurrency, req.td_currency)
                    setText(R.id.tvLoanSanctioned, req.td_deposit_amt)
                    setText(R.id.tvDisbursement, req.td_interest_amt)
                    setText(R.id.tvExpiryDate, formatDate(req.td_maturity))
                    setText(R.id.tvInterestRate, req.td_rate_interest)
                    setText(R.id.tvMarginPercent, req.td_period)
                }
            }
        }

        // Identification
        setText(R.id.tvNationalIdIden, req.ca_idenditification_number)
        setText(R.id.tvIssueDate, formatDate(req.ca_issue_date))
        setText(R.id.tvPassportNoIden, req.ca_passport_number)
        setText(R.id.tvExpiryDateIden, formatDate(req.ca_expiry_date))

        // Other Details (radio buttons)
        if ("Y".equals(req.ca_non_resident, ignoreCase = true))
            findViewById<RadioButton>(R.id.rbNonResidentYes).isChecked = true
        else findViewById<RadioButton>(R.id.rbNonResidentNo).isChecked = true

        if ("Y".equals(req.ca_staff_indicator, ignoreCase = true))
            findViewById<RadioButton>(R.id.rbStaffIndicatorYes).isChecked = true
        else findViewById<RadioButton>(R.id.rbStaffIndicatorNo).isChecked = true

        if ("Y".equals(req.ca_trdfin, ignoreCase = true))
            findViewById<RadioButton>(R.id.rbTradeFinanceYes).isChecked = true
        else findViewById<RadioButton>(R.id.rbTradeFinanceNo).isChecked = true

        if ("Y".equals(req.ca_minor_indicator, ignoreCase = true))
            findViewById<RadioButton>(R.id.rbMinorIndicatorYes).isChecked = true
        else findViewById<RadioButton>(R.id.rbMinorIndicatorNo).isChecked = true
    }

    private fun setText(viewId: Int, value: String?) {
        findViewById<TextView>(viewId).text = value ?: ""
    }


    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val localDate = LocalDate.parse(dateStr) // handles ISO 8601 only
            localDate.toString() // yyyy-MM-dd
        } catch (e: Exception) {
            // Fallback to custom formats using DateTimeFormatter
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.US)
            val localDate = LocalDate.parse(dateStr, formatter)
            localDate.toString()
        } catch (e: Exception) {
            dateStr
        }
    }
    private fun formatAmount(amount: Double?): String {
        if (amount == null) return ""
        return String.format("%,.2f", amount)
    }

    // ---------- API Actions ----------
    private fun approveRecord() {
        if (recNo.isEmpty()) {
            Toast.makeText(this, "Missing record number for approval", Toast.LENGTH_SHORT).show()
            return
        }
        // Use a fallback: if accountNo is still empty, try to use appRefNo or a blank string
        val effectiveAccountNo = if (accountNo.isNotEmpty()) accountNo else appRefNo
        val effectiveSchemeType = if (schemeType.isNotEmpty()) schemeType else "LA"

        progressDialog.setMessage("Approving...")
        progressDialog.show()
        RetrofitClient.api.approveRecord(
            recNo = recNo,
            appRefNo = appRefNo,
            schemeType = effectiveSchemeType,
            accountNo = effectiveAccountNo,
            schemeCode = schemeCode
        ).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@KYCComplianceViewActivity, "Approved successfully", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Approval failed"
                    Toast.makeText(this@KYCComplianceViewActivity, errMsg, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@KYCComplianceViewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showHoldDialog() {
        val editText = EditText(this)
        editText.hint = "Hold remarks"
        AlertDialog.Builder(this)
            .setTitle("Hold Record")
            .setView(editText)
            .setPositiveButton("Confirm") { _, _ ->
                val remarks = editText.text.toString()
                if (remarks.isBlank()) {
                    Toast.makeText(this, "Remarks required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                progressDialog.setMessage("Holding...")
                progressDialog.show()
                RetrofitClient.api.holdRecord(
                    recNo = recNo,
                    appRefNo = appRefNo,
                    remarks = remarks
                ).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Toast.makeText(this@KYCComplianceViewActivity, "Record held", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@KYCComplianceViewActivity, "Hold failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(this@KYCComplianceViewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRejectDialog() {
        val editText = EditText(this)
        editText.hint = "Reject remarks"
        AlertDialog.Builder(this)
            .setTitle("Reject Record")
            .setView(editText)
            .setPositiveButton("Confirm") { _, _ ->
                val remarks = editText.text.toString()
                if (remarks.isBlank()) {
                    Toast.makeText(this, "Remarks required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                progressDialog.setMessage("Rejecting...")
                progressDialog.show()
                RetrofitClient.api.rejectRecord(
                    recNo = recNo,
                    appRefNo = appRefNo,
                    remarks = remarks
                ).enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        progressDialog.dismiss()
                        if (response.isSuccessful) {
                            Toast.makeText(this@KYCComplianceViewActivity, "Record rejected", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@KYCComplianceViewActivity, "Reject failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(this@KYCComplianceViewActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitKycCompliance() {
        val tvComplianceDate = findViewById<TextView>(R.id.tvComplianceDate)
        val complianceDate = tvComplianceDate.text.toString()
        if (complianceDate.isEmpty()) {
            Toast.makeText(this, "Please select compliance date", Toast.LENGTH_SHORT).show()
            return
        }

        // Get spinner selections
        val spCustomer = findViewById<Spinner>(R.id.spCompCustomerDetails).selectedItem.toString()
        val spAccount = findViewById<Spinner>(R.id.spCompAccountDetails).selectedItem.toString()
        val spDocs = findViewById<Spinner>(R.id.spCompDocuments).selectedItem.toString()
        val spPhoto = findViewById<Spinner>(R.id.spCompPhoto).selectedItem.toString()
        val spSignature = findViewById<Spinner>(R.id.spCompSignature).selectedItem.toString()

        // Validate that no spinner is left on "SELECT"
        if (spCustomer == "SELECT" || spAccount == "SELECT" || spDocs == "SELECT" ||
            spPhoto == "SELECT" || spSignature == "SELECT") {
            Toast.makeText(this, "Please select status for all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Build a single compliance string (format: "Customer:OKAY;Account:NOT OKAY;...")
        val compliance = "Customer:$spCustomer;Account:$spAccount;Documents:$spDocs;Photo:$spPhoto;Signature:$spSignature"

        // Compliance officer – ideally get from login, but for now use a default
        val kycOfficer = "SYSTEM"   // or get from shared preferences
        val remarks = ""            // optional, can be left empty

        progressDialog.setMessage("Submitting KYC Compliance...")
        progressDialog.show()

        RetrofitClient.api.submitKycCompliance(
            recNo = recNo,
            appRefNo = appRefNo,
            kycDate = complianceDate,
            kycOfficer = kycOfficer,
            remarks = remarks,
            compliance = compliance,
            reviewDate = complianceDate   // or use another date
        ).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    Toast.makeText(this@KYCComplianceViewActivity, "KYC Compliance submitted", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@KYCComplianceViewActivity, "Submission failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@KYCComplianceViewActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun setupComplianceSpinners() {
        val options = arrayOf("SELECT", "OKAY", "NOT OKAY")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        findViewById<Spinner>(R.id.spCompCustomerDetails).adapter = adapter
        findViewById<Spinner>(R.id.spCompAccountDetails).adapter = adapter
        findViewById<Spinner>(R.id.spCompDocuments).adapter = adapter
        findViewById<Spinner>(R.id.spCompPhoto).adapter = adapter
        findViewById<Spinner>(R.id.spCompSignature).adapter = adapter

        val tvComplianceDate = findViewById<TextView>(R.id.tvComplianceDate)
        findViewById<LinearLayout>(R.id.llComplianceDate).setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val date = String.format("%02d-%s-%d", day,
                    arrayOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[month], year)
                tvComplianceDate.text = date
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}