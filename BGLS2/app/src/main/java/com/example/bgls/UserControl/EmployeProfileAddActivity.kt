package com.example.bgls.UserControl

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.R
import java.util.Calendar

class EmployeProfileAddActivity : AppCompatActivity() {

    // ─── Form fields ───
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

    // ─── Spinners ───
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

    // ─── Photo ───
    private lateinit var btnChoosePhoto: Button
    private lateinit var tvPhotoName: TextView
    private lateinit var ivPhotoPreview: ImageView
    private var selectedPhotoUri: Uri? = null

    // ─── Buttons ───
    private lateinit var btnList: Button
    private lateinit var btnHome: Button
    private lateinit var btnSubmit: Button
    //
     private lateinit var btnBack: ImageView

    // ─── Photo picker launcher ───
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
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
       // btnHome = findViewById(R.id.btnBack)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupSpinners() {
        // Branch Id → auto fill Branch Name
        setSpinner(spinnerBranchId, listOf("Select", "BR001", "BR002", "BR003"))
        spinnerBranchId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val branchNameMap = mapOf(
                    "BR001" to "Head Office",
                    "BR002" to "North Branch",
                    "BR003" to "South Branch"
                )
                etBranchName.setText(branchNameMap[spinnerBranchId.selectedItem.toString()] ?: "")
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setSpinner(spinnerCategory, listOf("Select", "Full Time", "Part Time", "Contract"))
        setSpinner(spinnerDepartment, listOf("Select", "IT", "HR", "Finance", "Operations", "Marketing"))
        setSpinner(spinnerDesignation, listOf("Select", "Manager", "Developer", "Analyst", "Accountant", "Designer", "Tester"))
        setSpinner(spinnerRole, listOf("Select", "Admin", "Manager", "User", "Viewer"))
        setSpinner(spinnerQualification, listOf("Select", "High School", "Diploma", "Bachelor", "Master", "PhD"))
        setSpinner(spinnerGender, listOf("Select", "Male", "Female", "Other"))
        setSpinner(spinnerBloodGroup, listOf("Select", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"))
        setSpinner(spinnerMaritalStatus, listOf("Select", "Single", "Married", "Divorced", "Widowed"))
        setSpinner(spinnerCountry, listOf("Select", "Mauritius", "India", "France", "UK", "USA", "Other"))
    }

    private fun setSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupDatePickers() {
        listOf(etDateOfJoining, etDateOfBirth).forEach { field ->
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
                targetField.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun setupButtons() {
        btnList.setOnClickListener {
            finish() // Go back to list
        }

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

//        btnHome.setOnClickListener {
//            Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show()
//            // TODO: startActivity(Intent(this, HomeActivity::class.java))
//        }

        btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitForm()
            }
        }
    }

    private fun validateForm(): Boolean {
        if (spinnerBranchId.selectedItem.toString() == "Select") {
            Toast.makeText(this, "Please select a Branch Id", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etEmployeeId.text.isNullOrBlank()) {
            etEmployeeId.error = "Employee Id is required"
            etEmployeeId.requestFocus()
            return false
        }
        if (etEmployeeName.text.isNullOrBlank()) {
            etEmployeeName.error = "Employee Name is required"
            etEmployeeName.requestFocus()
            return false
        }
        if (spinnerDesignation.selectedItem.toString() == "Select") {
            Toast.makeText(this, "Please select a Designation", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun submitForm() {
        // Build serial number from current list size + 1
        val srlNo = (System.currentTimeMillis() % 1000).toString()

        val newEmployee = EmployeeProfile(
            srlNo = srlNo,
            employeeId = etEmployeeId.text.toString().trim(),
            name = etEmployeeName.text.toString().trim(),
            designation = spinnerDesignation.selectedItem.toString(),
            category = spinnerCategory.selectedItem.toString(),
            mobile = etMobileNo.text.toString().trim(),
            email = etEmail.text.toString().trim(),
            profileStatus = "Verified"
        )

        // Send back to UserControlActivity
        val resultIntent = Intent()
        resultIntent.putExtra("newSrlNo", newEmployee.srlNo)
        resultIntent.putExtra("newEmployeeId", newEmployee.employeeId)
        resultIntent.putExtra("newName", newEmployee.name)
        resultIntent.putExtra("newDesignation", newEmployee.designation)
        resultIntent.putExtra("newCategory", newEmployee.category)
        resultIntent.putExtra("newMobile", newEmployee.mobile)
        resultIntent.putExtra("newEmail", newEmployee.email)
        resultIntent.putExtra("newProfileStatus", newEmployee.profileStatus)
        setResult(RESULT_OK, resultIntent)

        Toast.makeText(this, "${newEmployee.name} added successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}