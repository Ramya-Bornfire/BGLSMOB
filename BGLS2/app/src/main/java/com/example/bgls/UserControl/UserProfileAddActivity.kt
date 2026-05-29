package com.example.bgls.UserControl

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.AccessRoleRequest
import com.example.bgls.DataModels.SingleUserResponse
import com.example.bgls.DataModels.UserProfile
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class UserProfileAddActivity : AppCompatActivity() {

    companion object {
        const val MODE_ADD = "add"
        const val MODE_VIEW = "view"
        const val MODE_EDIT = "edit"
        const val EXTRA_MODE = "mode"
        const val EXTRA_USER_ID = "userId"
    }

    // Views
    private lateinit var etUserId: EditText
    private lateinit var etUserName: EditText
    private lateinit var etBranchId: EditText
    private lateinit var etBranchDes: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etEmailId: EditText
    private lateinit var etUserDisableDate: EditText
    private lateinit var etUserDisableTillDate: EditText
    private lateinit var etPassword: EditText
    private lateinit var etPasswordExpiryDate: EditText
    private lateinit var etAccountExpiryDate: EditText
    private lateinit var etLoginFlag: EditText
    private lateinit var etRoleDescription: EditText
    private lateinit var etRemarks: EditText

    private lateinit var spinnerUserStatus: Spinner
    private lateinit var spinnerLoginStatus: Spinner
    private lateinit var spinnerAccountAccessCode: Spinner
    private lateinit var spinnerDocumentAccessCode: Spinner
    private lateinit var spinnerRoleId: Spinner
    private lateinit var spinnerPermissions: Spinner
    private lateinit var spinnerWorkClass: Spinner

    private lateinit var ivTogglePassword: ImageView
    private lateinit var ivAccessControl: ImageView
    private lateinit var ivUserIdInfo: ImageView

    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView

    private var isPasswordVisible = false
    private var currentMode = MODE_ADD
    private var currentUserId: String? = null
    private var accessRoleRequest: AccessRoleRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_add)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupSpinners()
        setupDatePickers()
        setupPasswordToggle()
        setupButtons()

        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_ADD
        currentUserId = intent.getStringExtra(EXTRA_USER_ID)

        if (currentMode == MODE_VIEW || currentMode == MODE_EDIT) {
            loadUserData(currentUserId!!)
        }

        if (currentMode == MODE_VIEW) {
            setEditable(false)
            btnSubmit.visibility = View.GONE
        } else {
            btnSubmit.text = if (currentMode == MODE_ADD) "Create" else "Update"
        }

        // Password field behaviour
        if (currentMode != MODE_ADD) {
            etPassword.hint = "••••••••"
            etPassword.setText("")
            etPassword.isEnabled = (currentMode == MODE_EDIT)
        } else {
            etPassword.hint = "Enter password"
            etPassword.isEnabled = true
        }
    }

    private fun initViews() {
        etUserId = findViewById(R.id.etUserId)
        etUserName = findViewById(R.id.etUserName)
        etBranchId = findViewById(R.id.etBranchId)
        etBranchDes = findViewById(R.id.etBranchDes)
        etMobileNo = findViewById(R.id.etMobileNo)
        etEmailId = findViewById(R.id.etEmailId)
        etUserDisableDate = findViewById(R.id.etUserDisableDate)
        etUserDisableTillDate = findViewById(R.id.etUserDisableTillDate)
        etPassword = findViewById(R.id.etPassword)
        etPasswordExpiryDate = findViewById(R.id.etPasswordExpiryDate)
        etAccountExpiryDate = findViewById(R.id.etAccountExpiryDate)
        etLoginFlag = findViewById(R.id.etLoginFlag)
        etRoleDescription = findViewById(R.id.etRoleDescription)
        etRemarks = findViewById(R.id.etRemarks)

        spinnerUserStatus = findViewById(R.id.spinnerUserStatus)
        spinnerLoginStatus = findViewById(R.id.spinnerLoginStatus)
        spinnerAccountAccessCode = findViewById(R.id.spinnerAccountAccessCode)
        spinnerDocumentAccessCode = findViewById(R.id.spinnerDocumentAccessCode)
        spinnerRoleId = findViewById(R.id.spinnerRoleId)
        spinnerPermissions = findViewById(R.id.spinnerPermissions)
        spinnerWorkClass = findViewById(R.id.spinnerWorkClass)

        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        ivAccessControl = findViewById(R.id.ivAccessControl)
        ivUserIdInfo = findViewById(R.id.ivUserIdInfo)

        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupSpinners() {
        setSpinner(spinnerUserStatus, listOf("Active", "Inactive"))
        setSpinner(spinnerLoginStatus, listOf("Active", "Inactive"))
        setSpinner(spinnerAccountAccessCode, listOf("ALL", "LOAN"))
        setSpinner(spinnerDocumentAccessCode, listOf("ALL", "LOAN"))
        setSpinner(spinnerRoleId, listOf("Select", "ADM", "GEN"))
        setSpinner(spinnerPermissions, listOf("Select", "R", "W"))
        setSpinner(spinnerWorkClass, listOf("Select", "M", "C"))

        spinnerRoleId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val roleDescMap = mapOf("ADM" to "ADMIN", "GEN" to "GENERAL_USER")
                etRoleDescription.setText(roleDescMap[spinnerRoleId.selectedItem.toString()] ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupDatePickers() {
        val fields = listOf(etUserDisableDate, etUserDisableTillDate, etPasswordExpiryDate, etAccountExpiryDate)
        fields.forEach { field ->
            field.setOnClickListener { showDatePicker(field) }
            field.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) showDatePicker(field) }
        }
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            target.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun setupPasswordToggle() {
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            
            // Remember the current cursor position
            val selection = etPassword.selectionStart
            
            if (isPasswordVisible) {
                // Show password: change both inputType and transformationMethod
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivTogglePassword.setImageResource(R.drawable.ic_eye)
            } else {
                // Hide password: restore inputType and transformationMethod
                etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off)
            }
            
            // Restore selection (inputType change resets cursor)
            if (selection >= 0 && selection <= etPassword.text.length) {
                etPassword.setSelection(selection)
            }
        }
        ivUserIdInfo.setOnClickListener { showEmployeePickerDialog() }
        ivAccessControl.setOnClickListener { showAccessControlDialog() }
    }

    private fun showEmployeePickerDialog() {
        Toast.makeText(this, "Employee picker – implement similar to HTML #userModal", Toast.LENGTH_SHORT).show()
    }

    // Programmatic Access Control dialog (no extra XML)
    private fun showAccessControlDialog() {
        val scrollView = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val checkBoxMap = mutableMapOf<String, CheckBox>()

        // Define permission groups (simplified – add all as needed)
        val groups = listOf(
            "Admin" to listOf("orgnaization_details", "user_controls", "reference_code_maintenance", "audit_trail", "notification_Reports"),
            "Migration" to listOf("customer_master", "loan_master", "loan_schedule_migration", "transaction_migration"),
            "Loan Operation" to listOf("loan_operation_ls", "loan_closure"),
            "Transaction Maintenance" to listOf("journal_entries", "account_ledger_posting", "account_ledger", "trial_balance_t", "profit_and_loss_account_t"),
            "Collection Process" to listOf("participating_banks", "loan_collecting"),
            "Batch Job Execution" to listOf("batch_job"),
            "Transaction Reports" to listOf("credit_facility_report", "end_of_month_report", "dab", "consolidated_report", "transaction_report",
                "interest_accrual_report", "penalty_accrual_report", "recovery_report", "demand_generation"),
            "Transaction Inquiries" to listOf("account_balance_inq", "interset_summary_inq", "journal_book", "account_ledgers_i",
                "trial_balance_i", "general_ledger", "profit_and_loss_account_i", "balance_sheet"),
            "Reversal Transactions" to listOf("transaction_reversal", "transaction_accounts")
        )

        for ((groupName, items) in groups) {
            val title = TextView(this).apply {
                text = groupName
                textSize = 18f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, 20, 0, 10)
            }
            container.addView(title)
            for (item in items) {
                val cb = CheckBox(this).apply {
                    text = item.replace("_", " ").replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                }
                checkBoxMap[item] = cb
                container.addView(cb)
            }
        }

        // Preload existing permissions if available
        accessRoleRequest?.let { role ->
            checkBoxMap["orgnaization_details"]?.isChecked = role.orgDetails == "Y"
            checkBoxMap["user_controls"]?.isChecked = role.userControls == "Y"
            checkBoxMap["reference_code_maintenance"]?.isChecked = role.refCodeMaint == "Y"
            checkBoxMap["audit_trail"]?.isChecked = role.auditTrail == "Y"
            checkBoxMap["notification_Reports"]?.isChecked = role.notificationReports == "Y"
            checkBoxMap["customer_master"]?.isChecked = role.customerMaster == "Y"
            checkBoxMap["loan_master"]?.isChecked = role.loanMaster == "Y"
            checkBoxMap["loan_schedule_migration"]?.isChecked = role.loanScheduleMigration == "Y"
            checkBoxMap["transaction_migration"]?.isChecked = role.transactionMigration == "Y"
            checkBoxMap["loan_operation_ls"]?.isChecked = role.loanOperationLs == "Y"
            checkBoxMap["loan_closure"]?.isChecked = role.loanClosure == "Y"
            checkBoxMap["journal_entries"]?.isChecked = role.journalEntries == "Y"
            checkBoxMap["account_ledger_posting"]?.isChecked = role.accountLedgerPosting == "Y"
            checkBoxMap["account_ledger"]?.isChecked = role.accountLedger == "Y"
            checkBoxMap["trial_balance_t"]?.isChecked = role.trialBalanceT == "Y"
            checkBoxMap["profit_and_loss_account_t"]?.isChecked = role.profitLossT == "Y"
            checkBoxMap["participating_banks"]?.isChecked = role.participatingBanks == "Y"
            checkBoxMap["loan_collecting"]?.isChecked = role.loanCollecting == "Y"
            checkBoxMap["batch_job"]?.isChecked = role.batchJob == "Y"
            checkBoxMap["credit_facility_report"]?.isChecked = role.creditFacilityReport == "Y"
            checkBoxMap["end_of_month_report"]?.isChecked = role.endOfMonthReport == "Y"
            checkBoxMap["dab"]?.isChecked = role.dab == "Y"
            checkBoxMap["consolidated_report"]?.isChecked = role.consolidatedReport == "Y"
            checkBoxMap["transaction_report"]?.isChecked = role.transactionReport == "Y"
            checkBoxMap["interest_accrual_report"]?.isChecked = role.interestAccrualReport == "Y"
            checkBoxMap["penalty_accrual_report"]?.isChecked = role.penaltyAccrualReport == "Y"
            checkBoxMap["recovery_report"]?.isChecked = role.recoveryReport == "Y"
            checkBoxMap["demand_generation"]?.isChecked = role.demandGeneration == "Y"
            checkBoxMap["account_balance_inq"]?.isChecked = role.accountBalanceInq == "Y"
            checkBoxMap["interset_summary_inq"]?.isChecked = role.interestSummaryInq == "Y"
            checkBoxMap["journal_book"]?.isChecked = role.journalBook == "Y"
            checkBoxMap["account_ledgers_i"]?.isChecked = role.accountLedgersI == "Y"
            checkBoxMap["trial_balance_i"]?.isChecked = role.trialBalanceI == "Y"
            checkBoxMap["general_ledger"]?.isChecked = role.generalLedger == "Y"
            checkBoxMap["profit_and_loss_account_i"]?.isChecked = role.profitLossI == "Y"
            checkBoxMap["balance_sheet"]?.isChecked = role.balanceSheet == "Y"
            checkBoxMap["transaction_reversal"]?.isChecked = role.transactionReversal == "Y"
            checkBoxMap["transaction_accounts"]?.isChecked = role.transactionAccounts == "Y"
        }

        scrollView.addView(container)
        AlertDialog.Builder(this)
            .setTitle("User Access Modules")
            .setView(scrollView)
            .setPositiveButton("Submit") { _, _ ->
                val isChecked = { key: String -> if (checkBoxMap[key]?.isChecked == true) "Y" else "N" }
                val migrationVal = if (
                    isChecked("customer_master") == "Y" ||
                    isChecked("loan_master") == "Y" ||
                    isChecked("loan_schedule_migration") == "Y" ||
                    isChecked("transaction_migration") == "Y"
                ) "Y" else "N"
                val loanOpVal = if (
                    isChecked("loan_operation_ls") == "Y" ||
                    isChecked("loan_closure") == "Y"
                ) "Y" else "N"
                val transMaintVal = if (
                    isChecked("journal_entries") == "Y" ||
                    isChecked("account_ledger_posting") == "Y" ||
                    isChecked("account_ledger") == "Y" ||
                    isChecked("trial_balance_t") == "Y" ||
                    isChecked("profit_and_loss_account_t") == "Y"
                ) "Y" else "N"
                val colVal = if (
                    isChecked("participating_banks") == "Y" ||
                    isChecked("loan_collecting") == "Y"
                ) "Y" else "N"
                val batchVal = if (
                    isChecked("batch_job") == "Y"
                ) "Y" else "N"
                val inqRepVal = if (
                    isChecked("account_balance_inq") == "Y" ||
                    isChecked("interset_summary_inq") == "Y" ||
                    isChecked("journal_book") == "Y" ||
                    isChecked("account_ledgers_i") == "Y" ||
                    isChecked("trial_balance_i") == "Y" ||
                    isChecked("general_ledger") == "Y" ||
                    isChecked("profit_and_loss_account_i") == "Y" ||
                    isChecked("balance_sheet") == "Y" ||
                    isChecked("credit_facility_report") == "Y" ||
                    isChecked("end_of_month_report") == "Y" ||
                    isChecked("dab") == "Y" ||
                    isChecked("consolidated_report") == "Y" ||
                    isChecked("transaction_report") == "Y" ||
                    isChecked("interest_accrual_report") == "Y" ||
                    isChecked("penalty_accrual_report") == "Y" ||
                    isChecked("recovery_report") == "Y" ||
                    isChecked("demand_generation") == "Y"
                ) "Y" else "N"

                accessRoleRequest = AccessRoleRequest(
                    userId = etUserId.text.toString(),
                    roleId = spinnerRoleId.selectedItem.toString(),
                    roleDesc = etRoleDescription.text.toString(),
                    permissions = spinnerPermissions.selectedItem.toString(),
                    workClass = spinnerWorkClass.selectedItem.toString(),
                    admin = if (spinnerRoleId.selectedItem.toString() == "ADM") "Y" else "N",
                    orgDetails = isChecked("orgnaization_details"),
                    userControls = isChecked("user_controls"),
                    refCodeMaint = isChecked("reference_code_maintenance"),
                    auditTrail = isChecked("audit_trail"),
                    notificationReports = isChecked("notification_Reports"),
                    migration = migrationVal,
                    customerMaster = isChecked("customer_master"),
                    loanMaster = isChecked("loan_master"),
                    loanScheduleMigration = isChecked("loan_schedule_migration"),
                    transactionMigration = isChecked("transaction_migration"),
                    loanOperation = loanOpVal,
                    loanOperationLs = isChecked("loan_operation_ls"),
                    loanClosure = isChecked("loan_closure"),
                    transMaintenance = transMaintVal,
                    journalEntries = isChecked("journal_entries"),
                    accountLedgerPosting = isChecked("account_ledger_posting"),
                    accountLedger = isChecked("account_ledger"),
                    trialBalanceT = isChecked("trial_balance_t"),
                    profitLossT = isChecked("profit_and_loss_account_t"),
                    collectionProcess = colVal,
                    participatingBanks = isChecked("participating_banks"),
                    loanCollecting = isChecked("loan_collecting"),
                    batchJobExecution = batchVal,
                    batchJob = isChecked("batch_job"),
                    inquiriesReports = inqRepVal,
                    accountBalanceInq = isChecked("account_balance_inq"),
                    interestSummaryInq = isChecked("interset_summary_inq"),
                    journalBook = isChecked("journal_book"),
                    accountLedgersI = isChecked("account_ledgers_i"),
                    trialBalanceI = isChecked("trial_balance_i"),
                    generalLedger = isChecked("general_ledger"),
                    profitLossI = isChecked("profit_and_loss_account_i"),
                    balanceSheet = isChecked("balance_sheet"),
                    endOfMonthReport = isChecked("end_of_month_report"),
                    dab = isChecked("dab"),
                    transactionReport = isChecked("transaction_report"),
                    consolidatedReport = isChecked("consolidated_report"),
                    creditFacilityReport = isChecked("credit_facility_report"),
                    interestAccrualReport = isChecked("interest_accrual_report"),
                    penaltyAccrualReport = isChecked("penalty_accrual_report"),
                    recoveryReport = isChecked("recovery_report"),
                    demandGeneration = isChecked("demand_generation"),
                    transactionReversal = isChecked("transaction_reversal"),
                    transactionAccounts = isChecked("transaction_accounts")
                )
                Toast.makeText(this, "Access rights saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupButtons() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                if (currentMode == MODE_ADD) createUser() else updateUser()
            }
        }
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun validateForm(): Boolean {
        if (etUserId.text.isBlank()) { etUserId.error = "User ID required"; return false }
        if (etPasswordExpiryDate.text.isBlank()) { etPasswordExpiryDate.error = "Password Expiry Date required"; return false }
        if (etAccountExpiryDate.text.isBlank()) { etAccountExpiryDate.error = "Account Expiry Date required"; return false }
        if (spinnerRoleId.selectedItem.toString() == "Select") { Toast.makeText(this, "Select a Role", Toast.LENGTH_SHORT).show(); return false }
        return true
    }

    private fun createUser() {
        val user = UserProfile(
            userId = etUserId.text.toString(),
            userName = etUserName.text.toString(),
            userStatus = spinnerUserStatus.selectedItem.toString(),
            loginStatus = spinnerLoginStatus.selectedItem.toString(),
            mobileNumber = etMobileNo.text.toString(),
            emailId = etEmailId.text.toString(),
            branchId = etBranchId.text.toString(),
            branchDes = etBranchDes.text.toString(),
            remarks = etRemarks.text.toString(),
            permissions = if (spinnerPermissions.selectedItem.toString() != "Select") spinnerPermissions.selectedItem.toString() else null,
            workClass = if (spinnerWorkClass.selectedItem.toString() != "Select") spinnerWorkClass.selectedItem.toString() else null,
            acctAccessCode = spinnerAccountAccessCode.selectedItem.toString(),
            docAccessCode = spinnerDocumentAccessCode.selectedItem.toString(),
            roleId = spinnerRoleId.selectedItem.toString(),
            roleDesc = etRoleDescription.text.toString(),
            disableStartDate = convertDateToBackend(etUserDisableDate.text.toString()),
            disableEndDate = convertDateToBackend(etUserDisableTillDate.text.toString()),
            passwordExpiryDate = convertDateToBackend(etPasswordExpiryDate.text.toString()),
            accountExpiryDate = convertDateToBackend(etAccountExpiryDate.text.toString()),
            authFlg = "Y",
            disableFlg = if (spinnerUserStatus.selectedItem.toString() == "Active") "N" else "Y",
            entityFlg = "Y",
            modifyFlg = "N",
            userLockedFlg = "N",
            loginFlg = "N",
            password = etPassword.text.toString()   // send the password for create
        )
        RetrofitClient.api.createUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) submitAccessRole()
                else Toast.makeText(this@UserProfileAddActivity, "Create failed: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UserProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUser() {
        val user = UserProfile(
            userId = etUserId.text.toString(),
            userName = etUserName.text.toString(),
            userStatus = spinnerUserStatus.selectedItem.toString(),
            loginStatus = spinnerLoginStatus.selectedItem.toString(),
            mobileNumber = etMobileNo.text.toString(),
            emailId = etEmailId.text.toString(),
            branchId = etBranchId.text.toString(),
            branchDes = etBranchDes.text.toString(),
            remarks = etRemarks.text.toString(),
            permissions = if (spinnerPermissions.selectedItem.toString() != "Select") spinnerPermissions.selectedItem.toString() else null,
            workClass = if (spinnerWorkClass.selectedItem.toString() != "Select") spinnerWorkClass.selectedItem.toString() else null,
            acctAccessCode = spinnerAccountAccessCode.selectedItem.toString(),
            docAccessCode = spinnerDocumentAccessCode.selectedItem.toString(),
            roleId = spinnerRoleId.selectedItem.toString(),
            roleDesc = etRoleDescription.text.toString(),
            disableStartDate = convertDateToBackend(etUserDisableDate.text.toString()),
            disableEndDate = convertDateToBackend(etUserDisableTillDate.text.toString()),
            passwordExpiryDate = convertDateToBackend(etPasswordExpiryDate.text.toString()),
            accountExpiryDate = convertDateToBackend(etAccountExpiryDate.text.toString()),
            authFlg = "Y",
            disableFlg = if (spinnerUserStatus.selectedItem.toString() == "Active") "N" else "Y",
            entityFlg = "Y",
            modifyFlg = "N",
            userLockedFlg = "N",
            loginFlg = "N",
            password = null   // do not change password on update
        )
        RetrofitClient.api.updateUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) submitAccessRole()
                else Toast.makeText(this@UserProfileAddActivity, "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@UserProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun submitAccessRole() {
        accessRoleRequest?.let { role ->
            RetrofitClient.api.submitAccessRole(role).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Toast.makeText(this@UserProfileAddActivity, "User saved with rights", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@UserProfileAddActivity, "Access role error: ${t.message}", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            })
        } ?: run { setResult(RESULT_OK); finish() }
    }

    // ----- date conversion helpers -----
    private fun convertDateToBackend(dateStr: String): String? {
        if (dateStr.isBlank()) return null
        return try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            // Backend expects yyyy-MM-dd
            if (date != null) {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            } else null
        } catch (e: Exception) { null }
    }

    private fun parseBackendTimestampToYMD(timestamp: Any?): String? {
        if (timestamp == null) return null
        val str = timestamp.toString()
        if (str.isBlank()) return null
        return try {
            str.split("T").firstOrNull()
        } catch (e: Exception) { null }
    }

    private fun convertToDisplay(dateObj: Any?): String {
        if (dateObj == null) return ""
        return when (dateObj) {
            is Double -> {
                // Gson deserializes JSON numbers as Double for Any? fields
                val cal = Calendar.getInstance().apply { timeInMillis = dateObj.toLong() }
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
            }
            is Long -> {
                val cal = Calendar.getInstance().apply { timeInMillis = dateObj }
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(cal.time)
            }
            is String -> {
                // Try to parse ISO format (e.g., "2026-05-21T00:00:00.000+0000")
                try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
                    val date = isoFormat.parse(dateObj)
                    if (date != null) {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
                    } else {
                        dateObj
                    }
                } catch (e: Exception) {
                    // Try plain yyyy-MM-dd format
                    try {
                        val simpleFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = simpleFormat.parse(dateObj)
                        if (date != null) {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
                        } else {
                            dateObj
                        }
                    } catch (e2: Exception) {
                        // If all parsing fails, return the original string
                        dateObj
                    }
                }
            }
            else -> ""
        }
    }

    private fun loadUserData(userId: String) {
        val mode = if (currentMode == MODE_EDIT) "modify" else "view"
        RetrofitClient.api.getUserDetail(mode, userId).enqueue(object : Callback<SingleUserResponse> {
            override fun onResponse(call: Call<SingleUserResponse>, response: Response<SingleUserResponse>) {
                if (response.isSuccessful && response.body()?.userProfile != null) {
                    val body = response.body()!!
                    populateForm(body.userProfile!!)
                    body.access?.let { accessResponse ->
                        accessRoleRequest = accessResponse.toRequest()
                    }
                } else {
                    Toast.makeText(this@UserProfileAddActivity, "Failed to load user", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<SingleUserResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun com.example.bgls.DataModels.AccessRoleResponse.toRequest(): AccessRoleRequest {
        return AccessRoleRequest(
            userId = this.user_id ?: "",
            roleId = this.role_id ?: "",
            roleDesc = this.role_desc,
            permissions = this.permissions,
            workClass = this.work_class,
            admin = this.admin ?: "N",
            orgDetails = this.orgnaization_details ?: "N",
            userControls = this.user_controls ?: "N",
            refCodeMaint = this.reference_code_maintenance ?: "N",
            auditTrail = this.audit_trail ?: "N",
            notificationReports = this.notification_Reports ?: "N",
            migration = this.migration ?: "N",
            customerMaster = this.customer_master ?: "N",
            loanMaster = this.loan_master ?: "N",
            loanScheduleMigration = this.loan_schedule_migration ?: "N",
            transactionMigration = this.transaction_migration ?: "N",
            loanOperation = this.loan_operation ?: "N",
            loanOperationLs = this.loan_operation_ls ?: "N",
            loanClosure = this.loan_closure ?: "N",
            transMaintenance = this.transaction_maintenance ?: "N",
            journalEntries = this.journal_entries ?: "N",
            accountLedgerPosting = this.account_ledger_posting ?: "N",
            accountLedger = this.account_ledger ?: "N",
            trialBalanceT = this.trial_balance_t ?: "N",
            profitLossT = this.profit_and_loss_account_t ?: "N",
            collectionProcess = this.collection_process ?: "N",
            participatingBanks = this.participating_banks ?: "N",
            loanCollecting = this.loan_collecting ?: "N",
            batchJobExecution = this.batch_job_execution ?: "N",
            batchJob = this.batch_job ?: "N",
            inquiriesReports = this.inquiries_and_reports ?: "N",
            accountBalanceInq = this.account_balance_inq ?: "N",
            interestSummaryInq = this.interset_summary_inq ?: "N",
            journalBook = this.journal_book ?: "N",
            accountLedgersI = this.account_ledgers_i ?: "N",
            trialBalanceI = this.trial_balance_i ?: "N",
            generalLedger = this.general_ledger ?: "N",
            profitLossI = this.profit_and_loss_account_i ?: "N",
            balanceSheet = this.balance_sheet ?: "N",
            endOfMonthReport = this.end_of_month_report ?: "N",
            dab = this.dab ?: "N",
            transactionReport = this.transaction_report ?: "N",
            consolidatedReport = this.consolidated_report ?: "N",
            creditFacilityReport = this.credit_facility_report ?: "N",
            interestAccrualReport = this.interest_accrual_report ?: "N",
            penaltyAccrualReport = this.penalty_accrual_report ?: "N",
            recoveryReport = this.recovery_report ?: "N",
            demandGeneration = this.demand_generation ?: "N",
            transactionReversal = this.transaction_reversal ?: "N",
            transactionAccounts = this.transaction_accounts ?: "N"
        )
    }

    private fun populateForm(user: UserProfile) {
        etUserId.setText(user.userId ?: "")
        etUserName.setText(user.userName ?: "")
        etMobileNo.setText(user.mobileNumber ?: "")
        etEmailId.setText(user.emailId ?: "")
        etBranchId.setText(user.branchId ?: "")
        etBranchDes.setText(user.branchDes ?: "")
        etRemarks.setText(user.remarks ?: "")
        etUserDisableDate.setText(convertToDisplay(user.disableStartDate))
        etUserDisableTillDate.setText(convertToDisplay(user.disableEndDate))
        etPasswordExpiryDate.setText(convertToDisplay(user.passwordExpiryDate))
        etAccountExpiryDate.setText(convertToDisplay(user.accountExpiryDate))

        setSpinnerSelection(spinnerUserStatus, user.userStatus ?: "Active")
        setSpinnerSelection(spinnerLoginStatus, user.loginStatus ?: "Active")
        setSpinnerSelection(spinnerRoleId, user.roleId ?: "Select")
        setSpinnerSelection(spinnerPermissions, user.permissions ?: "Select")
        setSpinnerSelection(spinnerWorkClass, user.workClass ?: "Select")
        setSpinnerSelection(spinnerAccountAccessCode, user.acctAccessCode ?: "ALL")
        setSpinnerSelection(spinnerDocumentAccessCode, user.docAccessCode ?: "ALL")
        etRoleDescription.setText(user.roleDesc ?: "")
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = (0 until adapter.count).firstOrNull { adapter.getItem(it) == value } ?: 0
        spinner.setSelection(position)
    }

    private fun setEditable(enabled: Boolean) {
        etUserId.isEnabled = enabled
        etUserName.isEnabled = enabled
        etMobileNo.isEnabled = enabled
        etEmailId.isEnabled = enabled
        etBranchId.isEnabled = enabled
        etBranchDes.isEnabled = enabled
        etUserDisableDate.isEnabled = enabled
        etUserDisableTillDate.isEnabled = enabled
        etPassword.isEnabled = enabled
        etPasswordExpiryDate.isEnabled = enabled
        etAccountExpiryDate.isEnabled = enabled
        etLoginFlag.isEnabled = enabled
        etRoleDescription.isEnabled = enabled
        etRemarks.isEnabled = enabled
        spinnerUserStatus.isEnabled = enabled
        spinnerLoginStatus.isEnabled = enabled
        spinnerAccountAccessCode.isEnabled = enabled
        spinnerDocumentAccessCode.isEnabled = enabled
        spinnerRoleId.isEnabled = enabled
        spinnerPermissions.isEnabled = enabled
        spinnerWorkClass.isEnabled = enabled
        ivTogglePassword.isEnabled = enabled
    }
}