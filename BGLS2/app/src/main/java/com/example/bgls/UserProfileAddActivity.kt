package com.example.bgls
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.UserProfile
import java.text.SimpleDateFormat
import java.util.*

class UserProfileAddActivity : AppCompatActivity() {

    // ─── Form fields ───
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

    // ─── Spinners ───
    private lateinit var spinnerUserStatus: Spinner
    private lateinit var spinnerLoginStatus: Spinner
    private lateinit var spinnerAccountAccessCode: Spinner
    private lateinit var spinnerDocumentAccessCode: Spinner
    private lateinit var spinnerRoleId: Spinner
    private lateinit var spinnerPermissions: Spinner
    private lateinit var spinnerWorkClass: Spinner

    // ─── Icons ───
    private lateinit var ivTogglePassword: ImageView
    private lateinit var ivAccessControl: ImageView
    private lateinit var ivUserIdInfo: ImageView

    // ─── Buttons ───
    private lateinit var btnHome: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView

    // ─── Audit footer ───
    private lateinit var tvEntryUser: TextView
    private lateinit var tvModifyUser: TextView
    private lateinit var tvVerifyUser: TextView
    private lateinit var tvEntryTime: TextView
    private lateinit var tvModifyTime: TextView
    private lateinit var tvVerifyTime: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_add)

        initViews()
        setupSpinners()
        setupDatePickers()
        setupPasswordToggle()
        setupButtons()
      //  populateAuditFooter()
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

       // btnHome = findViewById(R.id.btnHome)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)

//        tvEntryUser = findViewById(R.id.tvEntryUser)
//        tvModifyUser = findViewById(R.id.tvModifyUser)
//        tvVerifyUser = findViewById(R.id.tvVerifyUser)
//        tvEntryTime = findViewById(R.id.tvEntryTime)
//        tvModifyTime = findViewById(R.id.tvModifyTime)
//        tvVerifyTime = findViewById(R.id.tvVerifyTime)
    }

    // ─── Setup all Spinners with options ───
    private fun setupSpinners() {
        setSpinner(spinnerUserStatus, listOf("Active", "Inactive"))
        setSpinner(spinnerLoginStatus, listOf("Active", "Inactive"))
        setSpinner(spinnerAccountAccessCode, listOf("All", "Loan"))
        setSpinner(spinnerDocumentAccessCode, listOf("All", "Loan"))
        setSpinner(spinnerRoleId, listOf("Select", "ADM", "GEN"))
        setSpinner(spinnerPermissions, listOf("Select", "Read", "Write"))
        setSpinner(spinnerWorkClass, listOf("Select", "Maker", "Checker"))

        // When Role Id changes → auto-fill Role Description
        spinnerRoleId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val roleDescMap = mapOf(
                    "ADMIN" to "Administrator with full access",
                    "MANAGER" to "Manager level access",
                    "USER" to "Standard user access",
                    "VIEWER" to "Read-only access"
                )
                val selected = spinnerRoleId.selectedItem.toString()
                etRoleDescription.setText(roleDescMap[selected] ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    // ─── Date Picker for all date fields ───
    private fun setupDatePickers() {
        val dateFields = listOf(
            etUserDisableDate,
            etUserDisableTillDate,
            etPasswordExpiryDate,
            etAccountExpiryDate
        )
        dateFields.forEach { field ->
            field.setOnClickListener { showDatePicker(field) }
            field.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDatePicker(field)
            }
        }
    }

    private fun showDatePicker(targetField: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formatted = String.format("%02d-%02d-%04d", day, month + 1, year)
                targetField.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ─── Password show/hide toggle ───
    private fun setupPasswordToggle() {
        ivTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            } else {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        ivUserIdInfo.setOnClickListener {
            Toast.makeText(this, "User ID is auto-generated or enter manually", Toast.LENGTH_SHORT).show()
        }

        ivAccessControl.setOnClickListener {
            // TODO: Open Access Control search/picker dialog
            Toast.makeText(this, "Open Access Control picker", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Button actions ───
    private fun setupButtons() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitForm()
            }
        }
//        btnHome.setOnClickListener {
//            // TODO: Navigate to Home
//            Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show()
//        }
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    // ─── Validation — checks required fields marked with * ───
    private fun validateForm(): Boolean {
        if (etUserId.text.isNullOrBlank()) {
            etUserId.error = "User Id is required"
            etUserId.requestFocus()
            return false
        }
        if (etPasswordExpiryDate.text.isNullOrBlank()) {
            etPasswordExpiryDate.error = "Password Expiry Date is required"
            etPasswordExpiryDate.requestFocus()
            return false
        }
        if (etAccountExpiryDate.text.isNullOrBlank()) {
            etAccountExpiryDate.error = "Account Expiry Date is required"
            etAccountExpiryDate.requestFocus()
            return false
        }
        if (spinnerRoleId.selectedItem.toString() == "Select") {
            Toast.makeText(this, "Please select a Role Id", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ─── Submit — build object and send to API ───
//    private fun submitForm() {
//        val newUser = mapOf(
//            "userId" to etUserId.text.toString(),
//            "userName" to etUserName.text.toString(),
//            "branchId" to etBranchId.text.toString(),
//            "mobileNo" to etMobileNo.text.toString(),
//            "emailId" to etEmailId.text.toString(),
//            "userDisableDate" to etUserDisableDate.text.toString(),
//            "userDisableTillDate" to etUserDisableTillDate.text.toString(),
//            "password" to etPassword.text.toString(),
//            "passwordExpiryDate" to etPasswordExpiryDate.text.toString(),
//            "userStatus" to spinnerUserStatus.selectedItem.toString(),
//            "loginStatus" to spinnerLoginStatus.selectedItem.toString(),
//            "accountExpiryDate" to etAccountExpiryDate.text.toString(),
//            "loginFlag" to etLoginFlag.text.toString(),
//            "accountAccessCode" to spinnerAccountAccessCode.selectedItem.toString(),
//            "documentAccessCode" to spinnerDocumentAccessCode.selectedItem.toString(),
//            "roleId" to spinnerRoleId.selectedItem.toString(),
//            "permissions" to spinnerPermissions.selectedItem.toString(),
//            "workClass" to spinnerWorkClass.selectedItem.toString(),
//            "remarks" to etRemarks.text.toString()
//        )

//        // TODO: Replace with actual Retrofit API call
//        // RetrofitClient.instance.createUser(newUser)
//        Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show()
//        finish() // Go back to list
//    }
private fun submitForm() {
    if (!validateForm()) return

    // Build the new user object
    val newUser = UserProfile(
        userId = etUserId.text.toString().trim(),
        userName = etUserName.text.toString().trim(),
        status = spinnerUserStatus.selectedItem.toString()
    )

    // Send back to UserProfileListActivity
    val resultIntent = Intent()
    resultIntent.putExtra("newUserId", newUser.userId)
    resultIntent.putExtra("newUserName", newUser.userName)
    resultIntent.putExtra("newUserStatus", newUser.status)
    setResult(RESULT_OK, resultIntent)

    Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show()
    finish() // Close this screen → goes back to list
}

    // ─── Populate audit footer ───
//    private fun populateAuditFooter() {
//        val currentUser = "EMP04" // TODO: get from session/SharedPreferences
//        val currentTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
//
//        tvEntryUser.text = currentUser
//        tvModifyUser.text = currentUser
//        tvVerifyUser.text = ""
//        tvEntryTime.text = currentTime
//        tvModifyTime.text = currentTime
//        tvVerifyTime.text = ""
//    }
}