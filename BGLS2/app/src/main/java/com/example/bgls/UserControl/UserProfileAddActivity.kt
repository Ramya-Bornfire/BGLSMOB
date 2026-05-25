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
            etPassword.transformationMethod = if (isPasswordVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()
            ivTogglePassword.setImageResource(if (isPasswordVisible) android.R.drawable.ic_menu_close_clear_cancel else android.R.drawable.ic_menu_view)
            etPassword.setSelection(etPassword.text.length)
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
            "Transaction Reports" to listOf("credit_facility_report1", "end_of_month_report", "dab_report", "consolidated_report", "transaction_report",
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
        }

        scrollView.addView(container)
        AlertDialog.Builder(this)
            .setTitle("User Access Modules")
            .setView(scrollView)
            .setPositiveButton("Submit") { _, _ ->
                accessRoleRequest = AccessRoleRequest(
                    userId = etUserId.text.toString(),
                    roleId = spinnerRoleId.selectedItem.toString(),
                    roleDesc = etRoleDescription.text.toString(),
                    permissions = spinnerPermissions.selectedItem.toString(),
                    workClass = spinnerWorkClass.selectedItem.toString(),
                    admin = "N", orgDetails = if (checkBoxMap["orgnaization_details"]?.isChecked == true) "Y" else "N",
                    userControls = if (checkBoxMap["user_controls"]?.isChecked == true) "Y" else "N",
                    refCodeMaint = if (checkBoxMap["reference_code_maintenance"]?.isChecked == true) "Y" else "N",
                    auditTrail = if (checkBoxMap["audit_trail"]?.isChecked == true) "Y" else "N",
                    notificationReports = if (checkBoxMap["notification_Reports"]?.isChecked == true) "Y" else "N",
                    migration = "N", customerMaster = "N", loanMaster = "N", loanScheduleMigration = "N",
                    transactionMigration = "N", loanOperation = "N", loanOperationLs = "N", loanClosure = "N",
                    transMaintenance = "N", journalEntries = "N", accountLedgerPosting = "N", accountLedger = "N",
                    trialBalanceT = "N", profitLossT = "N", collectionProcess = "N", participatingBanks = "N",
                    loanCollecting = "N", batchJobExecution = "N", batchJob = "N", inquiriesReports = "N",
                    accountBalanceInq = "N", interestSummaryInq = "N", journalBook = "N", accountLedgersI = "N",
                    trialBalanceI = "N", generalLedger = "N", profitLossI = "N", balanceSheet = "N",
                    endOfMonthReport = "N", dab = "N", transactionReport = "N", consolidatedReport = "N",
                    creditFacilityReport = "N", interestAccrualReport = "N", penaltyAccrualReport = "N",
                    recoveryReport = "N", demandGeneration = "N", transactionReversal = "N", transactionAccounts = "N"
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
    private fun convertDateToBackend(dateStr: String): Long? {
        if (dateStr.isBlank()) return null
        return try {
            val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.parse(dateStr)?.time
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
        val ymd = parseBackendTimestampToYMD(dateObj) ?: return ""
        return try {
            val ymdParts = ymd.split("-")
            "${ymdParts[2]}-${ymdParts[1]}-${ymdParts[0]}"  // yyyy-MM-dd → dd-MM-yyyy
        } catch (e: Exception) { "" }
    }

    private fun loadUserData(userId: String) {
        RetrofitClient.api.getUserDetail("view", userId).enqueue(object : Callback<SingleUserResponse> {
            override fun onResponse(call: Call<SingleUserResponse>, response: Response<SingleUserResponse>) {
                if (response.isSuccessful && response.body()?.userProfile != null) {
                    populateForm(response.body()!!.userProfile!!)
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