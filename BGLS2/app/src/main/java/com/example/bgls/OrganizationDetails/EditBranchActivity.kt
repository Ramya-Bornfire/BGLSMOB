package com.example.bgls.OrganizationDetails

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.BranchDto
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.RequestBody

class EditBranchActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnUpdate: Button
    private lateinit var btnBack: ImageView

    // EditText fields
    private lateinit var etBranchCode: EditText
    private lateinit var etBranchName: EditText
    private lateinit var etBranchHead: EditText
    private lateinit var etDesignation: EditText
    private lateinit var etSwiftCode: EditText
    private lateinit var etRemarks: EditText
    private lateinit var etLandline: EditText
    private lateinit var etFax: EditText
    private lateinit var etMobile: EditText
    private lateinit var etContactPerson: EditText
    private lateinit var etWebSite: EditText
    private lateinit var etMailId: EditText
    private lateinit var etAddress1: EditText
    private lateinit var etAddress2: EditText
    private lateinit var etCity: EditText
    private lateinit var etState: EditText
    private lateinit var etCountry: EditText
    private lateinit var etZipCode: EditText

    private var branchCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_branch)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize views
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        progressBar = findViewById(R.id.progressBar) // make sure you have a ProgressBar in your layout

        etBranchCode = findViewById(R.id.etBranchCode)
        etBranchName = findViewById(R.id.etBranchName)
        etBranchHead = findViewById(R.id.etBranchHead)
        etDesignation = findViewById(R.id.etDesignation)
        etSwiftCode = findViewById(R.id.etSwiftCode)
        etRemarks = findViewById(R.id.etRemarks)
        etLandline = findViewById(R.id.etLandline)
        etFax = findViewById(R.id.etFax)
        etMobile = findViewById(R.id.etMobile)
        etContactPerson = findViewById(R.id.etContactPerson)
        etWebSite = findViewById(R.id.etWebSite)
        etMailId = findViewById(R.id.etMailId)
        etAddress1 = findViewById(R.id.etAddress1)
        etAddress2 = findViewById(R.id.etAddress2)
        etCity = findViewById(R.id.etCity)
        etState = findViewById(R.id.etState)
        etCountry = findViewById(R.id.etCountry)
        etZipCode = findViewById(R.id.etZipCode)

        // Get branch code from intent (passed by BranchAdapter)
        branchCode = intent.getStringExtra("branch_code") ?: ""
        if (branchCode.isEmpty()) {
            Toast.makeText(this, "Branch code missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Disable editing on branch code (primary key)
        etBranchCode.isEnabled = false

        // Fetch existing branch data
        fetchBranchData()

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnBack.setOnClickListener { finish() }
        btnUpdate.setOnClickListener { updateBranch() }
    }

    private fun fetchBranchData() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getBranchDetailsView(branchCode = branchCode)
                if (response.isSuccessful && response.body() != null) {
                    val branch = response.body()?.branch
                    if (branch != null) {
                        populateFields(branch)
                    } else {
                        Toast.makeText(this@EditBranchActivity, "Branch not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@EditBranchActivity,
                        "Failed to load data: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditBranchActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun populateFields(branch: BranchDto) {
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
    }

    private fun updateBranch() {
        // Validate required fields (adjust as needed)
        if (etBranchCode.text.isBlank()) {
            Toast.makeText(this, "Branch Code is required", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnUpdate.isEnabled = false

        // Build form body (x-www-form-urlencoded) as the backend expects
        val formBody = FormBody.Builder()
            .add("branch_code", etBranchCode.text.toString())
            .add("branch_name", etBranchName.text.toString())
            .add("branch_head", etBranchHead.text.toString())
            .add("designation", etDesignation.text.toString())
            .add("swift_code", etSwiftCode.text.toString())
            .add("remarks", etRemarks.text.toString())
            .add("land_line", etLandline.text.toString())
            .add("fax", etFax.text.toString())
            .add("mobile", etMobile.text.toString())
            .add("cont_person", etContactPerson.text.toString())
            .add("website", etWebSite.text.toString())
            .add("mail_id", etMailId.text.toString())
            .add("add_1", etAddress1.text.toString())
            .add("add_2", etAddress2.text.toString())
            .add("city", etCity.text.toString())
            .add("state", etState.text.toString())
            .add("country", etCountry.text.toString())
            .add("zip_code", etZipCode.text.toString())
            .build()

        lifecycleScope.launch {
            try {
                // Use a raw POST call because the endpoint is not defined as a suspend function in ServiceApi
                val response = RetrofitClient.api.updateBranch(formBody)
                if (response.isSuccessful) {
                    val message = response.body()?.string() ?: "Branch updated successfully"
                    Toast.makeText(this@EditBranchActivity, message, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(
                        this@EditBranchActivity,
                        "Update failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditBranchActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnUpdate.isEnabled = true
            }
        }
    }
}