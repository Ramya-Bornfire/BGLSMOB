package com.example.bgls.OrganizationDetails

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.BranchDto
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class ViewBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_branch)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // Get branch code from intent (sent by BranchAdapter)
        val branchCode = intent.getStringExtra("branch_code") ?: ""
        if (branchCode.isEmpty()) {
            Toast.makeText(this, "Branch code missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch branch details from API
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getBranchDetailsView(branchCode = branchCode)
                if (response.isSuccessful && response.body() != null) {
                    val branch = response.body()?.branch
                    if (branch != null) {
                        populateUI(branch)
                    } else {
                        Toast.makeText(this@ViewBranchActivity, "Branch not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(this@ViewBranchActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ViewBranchActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun populateUI(branch: BranchDto) {
        // Bind all EditTexts (same as your original code)
        val etBranchCode = findViewById<EditText>(R.id.etBranchCode)
        val etBranchName = findViewById<EditText>(R.id.etBranchName)
        val etBranchHead = findViewById<EditText>(R.id.etBranchHead)
        val etDesignation = findViewById<EditText>(R.id.etDesignation)
        val etSwiftCode = findViewById<EditText>(R.id.etSwiftCode)
        val etRemarks = findViewById<EditText>(R.id.etRemarks)
        val etLandline = findViewById<EditText>(R.id.etLandline)
        val etFax = findViewById<EditText>(R.id.etFax)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etContactPerson = findViewById<EditText>(R.id.etContactPerson)
        val etWebSite = findViewById<EditText>(R.id.etWebSite)
        val etMailId = findViewById<EditText>(R.id.etMailId)
        val etAddress1 = findViewById<EditText>(R.id.etAddress1)
        val etAddress2 = findViewById<EditText>(R.id.etAddress2)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etState = findViewById<EditText>(R.id.etState)
        val etCountry = findViewById<EditText>(R.id.etCountry)
        val etZipCode = findViewById<EditText>(R.id.etZipCode)

        // Set data
        etBranchCode.setText(branch.branchCode ?: "")
        etBranchName.setText(branch.branchName ?: "")
        etBranchHead.setText(branch.branchHead ?: "")
        etDesignation.setText(branch.designation ?: "")
        etSwiftCode.setText(branch.swiftCode ?: "")
        etRemarks.setText(branch.remarks ?: "")
        etLandline.setText(branch.landline ?: "")
        etFax.setText(branch.fax ?: "")
        etMobile.setText(branch.mobile ?: "")
        etContactPerson.setText(branch.contactPerson ?: "")
        etWebSite.setText(branch.website ?: "")
        etMailId.setText(branch.email ?: "")
        etAddress1.setText(branch.address1 ?: "")
        etAddress2.setText(branch.address2 ?: "")
        etCity.setText(branch.city ?: "")
        etState.setText(branch.state ?: "")
        etCountry.setText(branch.country ?: "")
        etZipCode.setText(branch.zip ?: "")

        // Disable editing
        val allFields = listOf(
            etBranchCode, etBranchName, etBranchHead, etDesignation,
            etSwiftCode, etRemarks, etLandline, etFax,
            etMobile, etContactPerson, etWebSite, etMailId,
            etAddress1, etAddress2, etCity, etState, etCountry, etZipCode
        )
        allFields.forEach {
            it.isFocusable = false
            it.isClickable = false
        }
    }
}