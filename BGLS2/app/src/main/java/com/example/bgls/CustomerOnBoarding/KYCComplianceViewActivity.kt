package com.example.bgls.CustomerOnBoarding

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class KYCComplianceViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kyccompliance_view)

        // Get data from intent
        val appRefNo = intent.getStringExtra("appRefNo") ?: "ARN0586"
        val customerName = intent.getStringExtra("customerName") ?: "GIVI"
        val customerGroup = intent.getStringExtra("customerGroup") ?: ""
        val isFromApproval = intent.getBooleanExtra("isFromApproval", false)
        val isFromCompliance = intent.getBooleanExtra("isFromCompliance", false)

        // Hide KYC section and show relevant sections based on source
        if (isFromApproval) {
            findViewById<android.view.View>(R.id.llKycComplianceSection).visibility = android.view.View.GONE
            findViewById<TextView>(R.id.tvHeaderTitle).text = "LIST FOR APPROVAL - VIEW"
            
            // Update buttons for Approval
            findViewById<Button>(R.id.btnSubmit).text = "Approve"
            findViewById<Button>(R.id.btnHold).text = "Hold" // Keep Hold visible
            findViewById<Button>(R.id.btnReject).visibility = android.view.View.VISIBLE
        } else if (isFromCompliance) {
            findViewById<android.view.View>(R.id.llKycComplianceSection).visibility = android.view.View.GONE
            findViewById<android.view.View>(R.id.llComplianceDeptSection).visibility = android.view.View.VISIBLE
            findViewById<TextView>(R.id.tvHeaderTitle).text = "COMPLIANCE DEPARTMENT - VIEW"
            
            // Initialize Compliance Spinners
            setupComplianceSpinners()
        }

        // Reject button click listener
        findViewById<Button>(R.id.btnReject).setOnClickListener {
            // Reject logic
            finish()
        }

        // Update Header
        findViewById<TextView>(R.id.tvAppRefNoHeader).text = "App Ref No : $appRefNo"
        
        // Find views and set data (Mocking some for now as per screenshot)
        findViewById<TextView>(R.id.tvCustomerGroup).text = customerGroup
        findViewById<TextView>(R.id.tvFirstName).text = customerName
        findViewById<TextView>(R.id.tvFullName).text = customerName
        findViewById<TextView>(R.id.tvShortName).text = customerName
        
        findViewById<TextView>(R.id.tvFirstNameP).text = customerName
        findViewById<TextView>(R.id.tvFullNameP).text = customerName
        findViewById<TextView>(R.id.tvShortNameP).text = customerName

        // KYC Compliance Spinner
        val spKycCompliance = findViewById<android.widget.Spinner>(R.id.spKycCompliance)
        val options = arrayOf("SELECT", "COMPLETED", "PARTIAL")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spKycCompliance.adapter = adapter

        // Buttons
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            // Home logic
            finish()
        }
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            when {
                isFromApproval -> {
                    android.widget.Toast.makeText(this, "Record Approved successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
                isFromCompliance -> {
                    android.widget.Toast.makeText(this, "Compliance updated successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
                else -> {
                    android.widget.Toast.makeText(this, "KYC Submitted successfully", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
        findViewById<Button>(R.id.btnHold).setOnClickListener {
            // Hold logic
            finish()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupComplianceSpinners() {
        val options = arrayOf("SELECT", "OKAY", "NOT OKAY")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        findViewById<android.widget.Spinner>(R.id.spCompCustomerDetails).adapter = adapter
        findViewById<android.widget.Spinner>(R.id.spCompAccountDetails).adapter = adapter
        findViewById<android.widget.Spinner>(R.id.spCompDocuments).adapter = adapter
        findViewById<android.widget.Spinner>(R.id.spCompPhoto).adapter = adapter
        findViewById<android.widget.Spinner>(R.id.spCompSignature).adapter = adapter

        // Date Picker logic
        val tvComplianceDate = findViewById<TextView>(R.id.tvComplianceDate)
        findViewById<android.view.View>(R.id.llComplianceDate).setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%02d-%s-%d", selectedDay, 
                    arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")[selectedMonth], 
                    selectedYear)
                tvComplianceDate.text = date
            }, year, month, day).show()
        }
    }
}
