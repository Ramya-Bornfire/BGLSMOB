package com.example.bgls.UserControl

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Base64   // ✅ ADD THIS IMPORT
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.DataModels.SingleEmployeeResponse
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class EmployeProfileAddActivity : AppCompatActivity() {

    companion object {
        const val MODE_ADD = "add"
        const val MODE_VIEW = "view"
        const val MODE_EDIT = "edit"
        const val EXTRA_MODE = "mode"
        const val EXTRA_EMPLOYEE_ID = "employeeId"
    }

    // All form fields – ensure IDs exist in layout
    private lateinit var etBranchName: EditText
    private lateinit var etEmployeeId: EditText
    private lateinit var etEmployeeName: EditText
    private lateinit var etBankName: EditText
    private lateinit var etAccountNumber: EditText
    private lateinit var etDateOfJoining: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etAdditionalQualification: EditText
    private lateinit var etPassport: EditText
    private lateinit var etDrivingLicense: EditText
    private lateinit var etEmail: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etAlternateMobileNo: EditText
    private lateinit var etAddress1: EditText
    private lateinit var etAddress2: EditText
    private lateinit var etCity: EditText
    private lateinit var etState: EditText
    private lateinit var etZipcode: EditText
    private lateinit var etEmergencyContactPerson: EditText
    private lateinit var etEmergencyContactNo: EditText
    private lateinit var etEmployeeRemarks: EditText

    private lateinit var spinnerBranchId: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerDepartment: Spinner
    private lateinit var spinnerDesignation: Spinner
    private lateinit var spinnerRole: Spinner
    private lateinit var spinnerQualification: Spinner
    private lateinit var spinnerGender: Spinner
    private lateinit var spinnerBloodGroup: Spinner
    private lateinit var spinnerMaritalStatus: Spinner
    private lateinit var spinnerCountry: Spinner

    private lateinit var btnChoosePhoto: Button
    private lateinit var tvPhotoName: TextView
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var btnList: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView

    private var selectedPhotoUri: Uri? = null
    private var currentMode = MODE_ADD
    private var currentEmployeeId: String? = null

    // Launcher for photo picker
    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            selectedPhotoUri = result.data?.data
            selectedPhotoUri?.let { uri ->
                val fileName = getFileName(uri)
                tvPhotoName.text = fileName
                ivPhotoPreview.setImageURI(uri)
                ivPhotoPreview.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employe_profile_add)

        initViews()
        setupSpinners()
        setupDatePickers()
        setupPhotoChooser()
        setupButtons()

        currentMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_ADD
        currentEmployeeId = intent.getStringExtra(EXTRA_EMPLOYEE_ID)

        if (currentMode == MODE_VIEW || currentMode == MODE_EDIT) {
            loadEmployeeData(currentEmployeeId!!)
        }

        if (currentMode == MODE_VIEW) {
            setEditable(false)
            btnSubmit.visibility = View.GONE
        } else {
            btnSubmit.text = if (currentMode == MODE_ADD) "Create" else "Update"
        }
    }

    private fun initViews() {
        etBranchName = findViewById(R.id.etBranchName)
        etEmployeeId = findViewById(R.id.etEmployeeId)
        etEmployeeName = findViewById(R.id.etEmployeeName)
        etBankName = findViewById(R.id.etBankName)
        etAccountNumber = findViewById(R.id.etAccountNumber)
        etDateOfJoining = findViewById(R.id.etDateOfJoining)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        etAdditionalQualification = findViewById(R.id.etAdditionalQualification)
        etPassport = findViewById(R.id.etPassport)
        etDrivingLicense = findViewById(R.id.etDrivingLicense)
        etEmail = findViewById(R.id.etEmail)
        etMobileNo = findViewById(R.id.etMobileNo)
        etAlternateMobileNo = findViewById(R.id.etAlternateMobileNo)
        etAddress1 = findViewById(R.id.etAddress1)
        etAddress2 = findViewById(R.id.etAddress2)
        etCity = findViewById(R.id.etCity)
        etState = findViewById(R.id.etState)
        etZipcode = findViewById(R.id.etZipcode)
        etEmergencyContactPerson = findViewById(R.id.etEmergencyContactPerson)
        etEmergencyContactNo = findViewById(R.id.etEmergencyContactNo)
        etEmployeeRemarks = findViewById(R.id.etEmployeeRemarks)

        spinnerBranchId = findViewById(R.id.spinnerBranchId)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerDepartment = findViewById(R.id.spinnerDepartment)
        spinnerDesignation = findViewById(R.id.spinnerDesignation)
        spinnerRole = findViewById(R.id.spinnerRole)
        spinnerQualification = findViewById(R.id.spinnerQualification)
        spinnerGender = findViewById(R.id.spinnerGender)
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup)
        spinnerMaritalStatus = findViewById(R.id.spinnerMaritalStatus)
        spinnerCountry = findViewById(R.id.spinnerCountry)

        btnChoosePhoto = findViewById(R.id.btnChoosePhoto)
        tvPhotoName = findViewById(R.id.tvPhotoName)
        ivPhotoPreview = findViewById(R.id.ivPhotoPreview)
        btnList = findViewById(R.id.btnList)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupSpinners() {
        setSpinner(spinnerBranchId, listOf("Select", "BR001", "BR002", "BR003"))
        spinnerBranchId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val branchNameMap = mapOf("BR001" to "Head Office", "BR002" to "North Branch", "BR003" to "South Branch")
                etBranchName.setText(branchNameMap[spinnerBranchId.selectedItem.toString()] ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setSpinner(spinnerCategory, listOf("Select", "Full Time", "Part Time", "Contract"))
        setSpinner(spinnerDepartment, listOf("Select", "IT", "HR", "Finance", "Operations", "Marketing"))
        setSpinner(spinnerDesignation, listOf("Select", "Manager", "Developer", "Analyst", "Accountant"))
        setSpinner(spinnerRole, listOf("Select", "Admin", "Manager", "User"))
        setSpinner(spinnerQualification, listOf("Select", "Bachelor", "Master", "PhD"))
        setSpinner(spinnerGender, listOf("Select", "Male", "Female", "Other"))
        setSpinner(spinnerBloodGroup, listOf("Select", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"))
        setSpinner(spinnerMaritalStatus, listOf("Select", "Single", "Married", "Divorced"))
        setSpinner(spinnerCountry, listOf("Select", "Mauritius", "India", "France", "UK", "USA"))
    }

    private fun setSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupDatePickers() {
        listOf(etDateOfJoining, etDateOfBirth).forEach { field ->
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

    private fun setupPhotoChooser() {
        btnChoosePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            photoPickerLauncher.launch(intent)
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "photo_selected"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) name = cursor.getString(nameIndex)
        }
        return name
    }

    private fun setupButtons() {
        btnList.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                if (currentMode == MODE_ADD) createEmployee() else updateEmployee()
            }
        }
        
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun validateForm(): Boolean {
        if (spinnerBranchId.selectedItem.toString() == "Select") {
            Toast.makeText(this, "Select Branch", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etEmployeeId.text.isBlank()) { etEmployeeId.error = "Employee ID required"; return false }
        if (etEmployeeName.text.isBlank()) { etEmployeeName.error = "Employee Name required"; return false }
        if (spinnerDesignation.selectedItem.toString() == "Select") {
            Toast.makeText(this, "Select Designation", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun buildEmployeeFromForm(includePhoto: Boolean = true): EmployeeProfile {
        return EmployeeProfile(
            branchDesc = etBranchName.text.toString(),
            category = spinnerCategory.selectedItem.toString(),
            employeeId = etEmployeeId.text.toString(),
            employeeName = etEmployeeName.text.toString(),
            bank = etBankName.text.toString(),
            bankActNo = etAccountNumber.text.toString(),
            doj = convertDateToBackend(etDateOfJoining.text.toString()),
            dob = convertDateToBackend(etDateOfBirth.text.toString()),
            branchId = spinnerBranchId.selectedItem.toString(),
            department = spinnerDepartment.selectedItem.toString(),
            design = spinnerDesignation.selectedItem.toString(),
            role = spinnerRole.selectedItem.toString(),
            qual = spinnerQualification.selectedItem.toString(),
            addlQual = etAdditionalQualification.text.toString(),
            passport = etPassport.text.toString(),
            drivingLicense = etDrivingLicense.text.toString(),
            gender = spinnerGender.selectedItem.toString(),
            bloodGroup = spinnerBloodGroup.selectedItem.toString(),
            maritalStatus = spinnerMaritalStatus.selectedItem.toString(),
            mobile = etMobileNo.text.toString(),
            altMobile = etAlternateMobileNo.text.toString(),
            email = etEmail.text.toString(),
            addr1 = etAddress1.text.toString(),
            addr2 = etAddress2.text.toString(),
            city = etCity.text.toString(),
            state = etState.text.toString(),
            country = spinnerCountry.selectedItem.toString(),
            postalCode = etZipcode.text.toString(),
            emerContactPerson = etEmergencyContactPerson.text.toString(),
            emerContactNum = etEmergencyContactNo.text.toString(),
            employeeRemarks = etEmployeeRemarks.text.toString(),
            employeePhoto = if (includePhoto && selectedPhotoUri != null) encodePhotoToBase64() else null,
            verifyFlg = "N"  // new employee unverified
        )
    }

    private fun convertDateToBackend(dateStr: String): String? {
        if (dateStr.isBlank()) return null
        return try {
            val parts = dateStr.split("-")
            "${parts[2]}-${parts[1]}-${parts[0]}"   // dd-MM-yyyy → yyyy-MM-dd
        } catch (e: Exception) { null }
    }

    private fun encodePhotoToBase64(): String? {
        return try {
            val inputStream = contentResolver.openInputStream(selectedPhotoUri!!)
            val bytes = inputStream?.readBytes()
            if (bytes != null) Base64.encodeToString(bytes, Base64.DEFAULT) else null
        } catch (e: Exception) { null }
    }

    private fun createEmployee() {
        val emp = buildEmployeeFromForm(true)
        RetrofitClient.api.createEmployee(emp).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EmployeProfileAddActivity, "Employee created", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EmployeProfileAddActivity, "Create failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EmployeProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateEmployee() {
        val emp = buildEmployeeFromForm(false)  // don't overwrite photo unless new selected
        RetrofitClient.api.updateEmployee(emp).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EmployeProfileAddActivity, "Employee updated", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@EmployeProfileAddActivity, "Update failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@EmployeProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadEmployeeData(empId: String) {
        RetrofitClient.api.getEmployeeDetail("view", empId).enqueue(object : Callback<SingleEmployeeResponse> {
            override fun onResponse(call: Call<SingleEmployeeResponse>, response: Response<SingleEmployeeResponse>) {
                if (response.isSuccessful && response.body()?.employee != null) {
                    populateForm(response.body()!!.employee!!)
                } else {
                    Toast.makeText(this@EmployeProfileAddActivity, "Failed to load employee", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onFailure(call: Call<SingleEmployeeResponse>, t: Throwable) {
                Toast.makeText(this@EmployeProfileAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun populateForm(emp: EmployeeProfile) {
        etEmployeeId.setText(emp.employeeId ?: "")
        etEmployeeName.setText(emp.employeeName ?: "")
        etBankName.setText(emp.bank ?: "")
        etAccountNumber.setText(emp.bankActNo ?: "")
        etDateOfJoining.setText(emp.doj?.let { convertToDisplay(it) } ?: "")
        etDateOfBirth.setText(emp.dob?.let { convertToDisplay(it) } ?: "")
        etAdditionalQualification.setText(emp.addlQual ?: "")
        etPassport.setText(emp.passport ?: "")
        etDrivingLicense.setText(emp.drivingLicense ?: "")
        etEmail.setText(emp.email ?: "")
        etMobileNo.setText(emp.mobile ?: "")
        etAlternateMobileNo.setText(emp.altMobile ?: "")
        etAddress1.setText(emp.addr1 ?: "")
        etAddress2.setText(emp.addr2 ?: "")
        etCity.setText(emp.city ?: "")
        etState.setText(emp.state ?: "")
        etZipcode.setText(emp.postalCode ?: "")
        etEmergencyContactPerson.setText(emp.emerContactPerson ?: "")
        etEmergencyContactNo.setText(emp.emerContactNum ?: "")
        etEmployeeRemarks.setText(emp.employeeRemarks ?: "")
        setSpinnerSelection(spinnerBranchId, emp.branchId ?: "Select")
        setSpinnerSelection(spinnerCategory, emp.category ?: "Select")
        setSpinnerSelection(spinnerDepartment, emp.department ?: "Select")
        setSpinnerSelection(spinnerDesignation, emp.design ?: "Select")
        setSpinnerSelection(spinnerRole, emp.role ?: "Select")
        setSpinnerSelection(spinnerQualification, emp.qual ?: "Select")
        setSpinnerSelection(spinnerGender, emp.gender ?: "Select")
        setSpinnerSelection(spinnerBloodGroup, emp.bloodGroup ?: "Select")
        setSpinnerSelection(spinnerMaritalStatus, emp.maritalStatus ?: "Select")
        setSpinnerSelection(spinnerCountry, emp.country ?: "Select")
        // Photo: if base64 string exists, decode and show
        if (!emp.employeePhoto.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(emp.employeePhoto, Base64.DEFAULT)
                ivPhotoPreview.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                ivPhotoPreview.visibility = View.VISIBLE
                tvPhotoName.text = "existing_photo"
            } catch (e: Exception) { }
        }
    }

    private fun convertToDisplay(dateStr: String): String {
        return try {
            val parts = dateStr.split("-")
            "${parts[2]}-${parts[1]}-${parts[0]}"   // yyyy-MM-dd → dd-MM-yyyy
        } catch (e: Exception) { dateStr }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = (0 until adapter.count).firstOrNull { adapter.getItem(it) == value } ?: 0
        spinner.setSelection(position)
    }

    private fun setEditable(enabled: Boolean) {
        val enable = enabled
        etEmployeeId.isEnabled = enable
        etEmployeeName.isEnabled = enable
        etBankName.isEnabled = enable
        etAccountNumber.isEnabled = enable
        etDateOfJoining.isEnabled = enable
        etDateOfBirth.isEnabled = enable
        etAdditionalQualification.isEnabled = enable
        etPassport.isEnabled = enable
        etDrivingLicense.isEnabled = enable
        etEmail.isEnabled = enable
        etMobileNo.isEnabled = enable
        etAlternateMobileNo.isEnabled = enable
        etAddress1.isEnabled = enable
        etAddress2.isEnabled = enable
        etCity.isEnabled = enable
        etState.isEnabled = enable
        etZipcode.isEnabled = enable
        etEmergencyContactPerson.isEnabled = enable
        etEmergencyContactNo.isEnabled = enable
        etEmployeeRemarks.isEnabled = enable
        spinnerBranchId.isEnabled = enable
        spinnerCategory.isEnabled = enable
        spinnerDepartment.isEnabled = enable
        spinnerDesignation.isEnabled = enable
        spinnerRole.isEnabled = enable
        spinnerQualification.isEnabled = enable
        spinnerGender.isEnabled = enable
        spinnerBloodGroup.isEnabled = enable
        spinnerMaritalStatus.isEnabled = enable
        spinnerCountry.isEnabled = enable
        btnChoosePhoto.isEnabled = enable
    }
}