package com.example.bgls.CustomerMaintenance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.AccountDetail
import com.example.bgls.CustomerMaster.AccountDetailAdapter
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.DataModels.CustomerMasterViewResponse
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response           // <-- ADD THIS IMPORT

class CustomerMaintenanceViewActivity : AppCompatActivity() {

    private var isEditMode = false
    private val formRows = mutableListOf<View>()
    private var currentCustomer: CustomerMaster? = null
    private var branchKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_maintenance_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val customerId = intent.getStringExtra("CUSTOMER_ID") ?: ""
        branchKey = intent.getStringExtra("BRANCH_KEY") ?: ""

        if (customerId.isEmpty()) {
            Toast.makeText(this, "Customer ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            fetchCustomerDetails(customerId)
        }

        findViewById<Button>(R.id.btnAccountDetails).setOnClickListener {
            val layoutAccountDetails = findViewById<LinearLayout>(R.id.layoutAccountDetails)
            layoutAccountDetails.visibility =
                if (layoutAccountDetails.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        val btnModify = findViewById<Button>(R.id.btnModify)
        val tvMainTitle = findViewById<TextView>(R.id.tvMainTitle)
        btnModify.setOnClickListener {
            if (!isEditMode) {
                enterEditMode(btnModify, tvMainTitle)
            } else {
                submitModification(btnModify, tvMainTitle)
            }
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }
    }

    private suspend fun fetchCustomerDetails(customerId: String) {
        try {
            val response: Response<CustomerMasterViewResponse> =
                RetrofitClient.api.getCustomerMaster("view", customerId, branchKey, null)
            if (response.isSuccessful) {
                val viewResponse = response.body()
                if (viewResponse != null && viewResponse.customer != null) {
                    currentCustomer = viewResponse.customer!!
                    populateFields(currentCustomer!!)
                    loadAccountDetails(customerId)
                } else {
                    showToast("Customer data empty")
                }
            } else {
                showToast("Failed to load customer: ${response.code()}")
            }
        } catch (e: Exception) {
            showToast("Network error: ${e.message}")
        }
    }

    private fun populateFields(customer: CustomerMaster) {
        // ... same mapping as before ...
        setupRow(findViewById(R.id.rowCustomerId), "Customer Id", customer.customerId ?: "")
        setupRow(findViewById(R.id.rowFirstName), "First Name", customer.firstName ?: "")
        setupRow(findViewById(R.id.rowGender), "Gender", customer.gender ?: "")
        setupRow(findViewById(R.id.rowBranch), "Branch", customer.branchName)
        setupRow(findViewById(R.id.rowCreationDate), "Creation Date", customer.creationDate ?: "")
        setupRow(findViewById(R.id.rowLastModificationDate), "Last Modification Date", customer.lastModificationDate ?: "")
        setupRow(findViewById(R.id.rowMobileNo), "Mobile No", customer.mobileNo ?: "")
        setupRow(findViewById(R.id.rowAddress1), "Address 1", customer.address1 ?: "")
        setupRow(findViewById(R.id.rowCity), "City", customer.city ?: "")
        setupRow(findViewById(R.id.rowLoanCycle), "Loan Cycle", customer.loanCycle ?: "")
        setupRow(findViewById(R.id.rowAssignedUser), "Assigned User", customer.assignedUser ?: "")
        setupRow(findViewById(R.id.rowCustomerName), "Customer Name", customer.customerName)
        setupRow(findViewById(R.id.rowLastName), "Last Name", customer.lastName ?: "")
        setupRow(findViewById(R.id.rowDob), "Date of Birth", customer.dob ?: "")
        setupRow(findViewById(R.id.rowClientRoleKey), "Client Role Key", customer.clientRoleKey ?: "")
        setupRow(findViewById(R.id.rowApprovalDate), "Approval Date", customer.approvalDate ?: "")
        setupRow(findViewById(R.id.rowActivationDate), "Activation Date", customer.activationDate ?: "")
        setupRow(findViewById(R.id.rowEmailId), "Email Id", customer.email ?: "")
        setupRow(findViewById(R.id.rowAddress2), "Address 2", customer.address2 ?: "")
        setupRow(findViewById(R.id.rowSuburb), "Suburb", customer.suburb ?: "")
        setupRow(findViewById(R.id.rowGroupLoanCycle), "Group Loan Cycle", customer.groupLoanCycle ?: "")
        setupRow(findViewById(R.id.rowAsOnDate), "As On Date", customer.asOnDate ?: "")
    }
    private suspend fun loadAccountDetails(customerId: String) {
        try {
            val response = RetrofitClient.api.getAccDet(customerId)
            if (response.isSuccessful) {
                val rawRows = response.body() ?: emptyList()
                val accounts = rawRows.mapNotNull { row ->
                    if (row.size >= 6) {
                        AccountDetail(
                            holderKey   = row.getOrNull(0)?.toString() ?: "",
                            accountId   = row.getOrNull(1)?.toString() ?: "",
                            accountName = row.getOrNull(2)?.toString() ?: "",
                            dateOfLoan  = row[3]?.toString() ?: "",
                            loanAmount  = row[4]?.toString() ?: "",
                            loanBalance = row[5]?.toString() ?: ""
                        )
                    } else null
                }

                val recyclerViewAccountDetails = findViewById<RecyclerView>(R.id.recyclerViewAccountDetails)
                recyclerViewAccountDetails.layoutManager = LinearLayoutManager(this)
                recyclerViewAccountDetails.adapter = AccountDetailAdapter(
                    this@CustomerMaintenanceViewActivity,
                    accounts
                ) { account ->
                    val intent = Intent(this@CustomerMaintenanceViewActivity, LoanMasterViewActivity::class.java)
                    intent.putExtra("loanId", account.accountId)
                    intent.putExtra("holderKey", account.holderKey)
                    intent.putExtra("branchKey", branchKey)   // branchKey is already defined in the activity
                    startActivity(intent)
                }
            } else {
                showToast("Failed to load account details")
            }
        } catch (e: Exception) {
            showToast("Error loading account details: ${e.message}")
        }
    }


    // ... setupRow, enterEditMode, submitModification etc. remain identical ...
    private fun setupRow(rowView: View, labelText: String, valueText: String) {
        formRows.add(rowView)
        val tvLabel = rowView.findViewById<TextView>(R.id.tvLabel)
        val tvValue = rowView.findViewById<TextView>(R.id.tvValue)
        val etValue = rowView.findViewById<EditText>(R.id.etValue)
        tvLabel.text = labelText
        tvValue.text = valueText
        etValue.setText(valueText)
        etValue.visibility = View.GONE
    }

    private fun enterEditMode(btnModify: Button, tvMainTitle: TextView) {
        isEditMode = true
        btnModify.text = "Submit"
        tvMainTitle.text = "CUSTOMER MAINTENANCE - MODIFY"
        for (row in formRows) {
            val tvValue = row.findViewById<TextView>(R.id.tvValue)
            val etValue = row.findViewById<EditText>(R.id.etValue)
            etValue.setText(tvValue.text)
            tvValue.visibility = View.GONE
            etValue.visibility = View.VISIBLE
        }
    }

    private fun submitModification(btnModify: Button, tvMainTitle: TextView) {
        if (currentCustomer == null) {
            showToast("No customer data to modify")
            return
        }

        val fields = mutableMapOf<String, String>().apply {
            put("customer_id", getEditedText(R.id.rowCustomerId))
            put("first_name", getEditedText(R.id.rowFirstName))
            put("last_name", getEditedText(R.id.rowLastName))
            put("gender", getEditedText(R.id.rowGender))
            put("birth_date", getEditedText(R.id.rowDob))
            put("mobile_phone", getEditedText(R.id.rowMobileNo))
            put("email_address", getEditedText(R.id.rowEmailId))
            put("address_line1", getEditedText(R.id.rowAddress1))
            put("address_line2", getEditedText(R.id.rowAddress2))
            put("city", getEditedText(R.id.rowCity))
            put("suburb", getEditedText(R.id.rowSuburb))
            put("loan_cycle", getEditedText(R.id.rowLoanCycle))
            put("group_loan_cycle", getEditedText(R.id.rowGroupLoanCycle))
            put("assigned_user_key", getEditedText(R.id.rowAssignedUser))
            put("assigned_branch_key", branchKey)
        }

        lifecycleScope.launch {
            try {
                val response: Response<okhttp3.ResponseBody> = RetrofitClient.api.modifyCustomer(fields)
                if (response.isSuccessful) {
                    Toast.makeText(this@CustomerMaintenanceViewActivity, "Modified successfully", Toast.LENGTH_SHORT).show()

                    isEditMode = false
                    btnModify.text = "Modify"
                    tvMainTitle.text = "CUSTOMER MAINTENANCE - VIEW"

                    for (row in formRows) {
                        val tvValue = row.findViewById<TextView>(R.id.tvValue)
                        val etValue = row.findViewById<EditText>(R.id.etValue)
                        tvValue.text = etValue.text
                        etValue.visibility = View.GONE
                        tvValue.visibility = View.VISIBLE
                    }
                } else {
                    showToast("Modification failed: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("Network error: ${e.message}")
            }
        }
    }

    private fun getEditedText(rowId: Int): String {
        val row = formRows.find { it.id == rowId } ?: return ""
        return row.findViewById<EditText>(R.id.etValue).text.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}