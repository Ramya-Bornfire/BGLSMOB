package com.example.bgls

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class CustomerMasterActivity : AppCompatActivity() {

    // ─── Form fields ───
    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etGender: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etBranch: EditText
    private lateinit var etClientRoleKey: EditText
    private lateinit var etCreationDate: EditText
    private lateinit var etApprovalDate: EditText
    private lateinit var etLastModificationDate: EditText
    private lateinit var etActivationDate: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etEmailId: EditText
    private lateinit var etAddress1: EditText
    private lateinit var etAddress2: EditText
    private lateinit var etCity: EditText
    private lateinit var etSuburb: EditText
    private lateinit var etLoanCycle: EditText
    private lateinit var etGroupLoanCycle: EditText
    private lateinit var etAssignedUser: EditText
    private lateinit var etAsOnDate: EditText

    // ─── Buttons ───
    private lateinit var btnUpload: Button
    private lateinit var btnList: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master)

        initViews()
        setupDatePickers()
        setupButtons()
    }

    private fun initViews() {
        etCustomerId            = findViewById(R.id.etCustomerId)
        etCustomerName          = findViewById(R.id.etCustomerName)
        etFirstName             = findViewById(R.id.etFirstName)
        etLastName              = findViewById(R.id.etLastName)
        etGender                = findViewById(R.id.etGender)
        etDateOfBirth           = findViewById(R.id.etDateOfBirth)
        etBranch                = findViewById(R.id.etBranch)
        etClientRoleKey         = findViewById(R.id.etClientRoleKey)
        etCreationDate          = findViewById(R.id.etCreationDate)
        etApprovalDate          = findViewById(R.id.etApprovalDate)
        etLastModificationDate  = findViewById(R.id.etLastModificationDate)
        etActivationDate        = findViewById(R.id.etActivationDate)
        etMobileNo              = findViewById(R.id.etMobileNo)
        etEmailId               = findViewById(R.id.etEmailId)
        etAddress1              = findViewById(R.id.etAddress1)
        etAddress2              = findViewById(R.id.etAddress2)
        etCity                  = findViewById(R.id.etCity)
        etSuburb                = findViewById(R.id.etSuburb)
        etLoanCycle             = findViewById(R.id.etLoanCycle)
        etGroupLoanCycle        = findViewById(R.id.etGroupLoanCycle)
        etAssignedUser          = findViewById(R.id.etAssignedUser)
        etAsOnDate              = findViewById(R.id.etAsOnDate)

        btnUpload = findViewById(R.id.btnUpload)
        btnList   = findViewById(R.id.btnList)
    }

    // ─── Date picker on all date fields ───
    private fun setupDatePickers() {
        val dateFields = listOf(
            etDateOfBirth,
            etCreationDate,
            etApprovalDate,
            etLastModificationDate,
            etActivationDate,
            etAsOnDate
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
                targetField.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupButtons() {
        btnUpload.setOnClickListener {
            // TODO: Handle file upload
            Toast.makeText(this, "Upload clicked", Toast.LENGTH_SHORT).show()
        }


        btnList.setOnClickListener {
            // TODO: Navigate to Customer List screen
             startActivity(Intent(this, CustomerMasterListActivity::class.java))
            Toast.makeText(this, "Navigate to List", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}