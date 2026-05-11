package com.example.bgls.CustomerMaster

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountDetailDto
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import android.content.Intent

class CustomerMasterViewActivity : AppCompatActivity() {

    // ─── Form fields (read-only) ───
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

    // ─── Account details section ───
    private lateinit var btnAccountDetails: Button
    private lateinit var layoutAccountDetails: LinearLayout
    private lateinit var recyclerViewAccountDetails: RecyclerView

    // ─── Loading indicators ───
    private lateinit var progressBarView: ProgressBar
    private lateinit var progressBarAccDet: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master_view)

        initViews()
        // Populate immediately from Intent extras (cached data from list)
        populateFromIntent()
        // Then fetch full detail from server to fill any missing / updated fields
        fetchFullDetail()
        setupAccountDetailsButton()
    }

    // ─── View binding ─────────────────────────────────────────────────────────

    private fun initViews() {
        etCustomerId           = findViewById(R.id.etCustomerId)
        etCustomerName         = findViewById(R.id.etCustomerName)
        etFirstName            = findViewById(R.id.etFirstName)
        etLastName             = findViewById(R.id.etLastName)
        etGender               = findViewById(R.id.etGender)
        etDateOfBirth          = findViewById(R.id.etDateOfBirth)
        etBranch               = findViewById(R.id.etBranch)
        etClientRoleKey        = findViewById(R.id.etClientRoleKey)
        etCreationDate         = findViewById(R.id.etCreationDate)
        etApprovalDate         = findViewById(R.id.etApprovalDate)
        etLastModificationDate = findViewById(R.id.etLastModificationDate)
        etActivationDate       = findViewById(R.id.etActivationDate)
        etMobileNo             = findViewById(R.id.etMobileNo)
        etEmailId              = findViewById(R.id.etEmailId)
        etAddress1             = findViewById(R.id.etAddress1)
        etAddress2             = findViewById(R.id.etAddress2)
        etCity                 = findViewById(R.id.etCity)
        etSuburb               = findViewById(R.id.etSuburb)
        etLoanCycle            = findViewById(R.id.etLoanCycle)
        etGroupLoanCycle       = findViewById(R.id.etGroupLoanCycle)
        etAssignedUser         = findViewById(R.id.etAssignedUser)
        etAsOnDate             = findViewById(R.id.etAsOnDate)

        btnAccountDetails        = findViewById(R.id.btnAccountDetails)
        layoutAccountDetails     = findViewById(R.id.layoutAccountDetails)
        recyclerViewAccountDetails = findViewById(R.id.recyclerViewAccountDetails)

        progressBarView   = findViewById(R.id.progressBarView)
        progressBarAccDet = findViewById(R.id.progressBarAccDet)
    }

    // ─── Fast-populate from Intent extras (data already held in list) ──────────

    private fun populateFromIntent() {
        val i = intent
        etCustomerId.setText(i.getStringExtra("customerId")          ?: "")
        etCustomerName.setText(i.getStringExtra("customerName")      ?: "")
        etFirstName.setText(i.getStringExtra("firstName")            ?: "")
        etLastName.setText(i.getStringExtra("lastName")              ?: "")
        etGender.setText(i.getStringExtra("gender")                  ?: "")
        etDateOfBirth.setText(i.getStringExtra("dob")                ?: "")
        etBranch.setText(i.getStringExtra("branchName")              ?: "")
        etClientRoleKey.setText(i.getStringExtra("clientRoleKey")    ?: "")
        etCreationDate.setText(i.getStringExtra("creationDate")      ?: "")
        etApprovalDate.setText(i.getStringExtra("approvalDate")      ?: "")
        etLastModificationDate.setText(i.getStringExtra("lastModificationDate") ?: "")
        etActivationDate.setText(i.getStringExtra("activationDate")  ?: "")
        etMobileNo.setText(i.getStringExtra("mobileNo")              ?: "")
        etEmailId.setText(i.getStringExtra("email")                  ?: "")
        etAddress1.setText(i.getStringExtra("address1")              ?: "")
        etAddress2.setText(i.getStringExtra("address2")              ?: "")
        etCity.setText(i.getStringExtra("city")                      ?: "")
        etSuburb.setText(i.getStringExtra("suburb")                  ?: "")
        etLoanCycle.setText(i.getStringExtra("loanCycle")            ?: "")
        etGroupLoanCycle.setText(i.getStringExtra("groupLoanCycle")  ?: "")
        etAssignedUser.setText(i.getStringExtra("assignedUser")      ?: "")
        etAsOnDate.setText(i.getStringExtra("asOnDate")              ?: "")
    }

    // ─── Full detail API fetch ─────────────────────────────────────────────────

    private fun fetchFullDetail() {
        val customerId = intent.getStringExtra("customerId") ?: return
        val branchKey  = intent.getStringExtra("branchKey")  ?: ""

        progressBarView.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCustomerMaster(
                    formmode  = "view",
                    id        = customerId,
                    branchKey = branchKey
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val customer = body?.customer
                    if (customer != null) {
                        populateFromModel(customer, body.branchName1)
                    }
                } else {
                    Toast.makeText(
                        this@CustomerMasterViewActivity,
                        "Detail load failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CustomerMasterViewActivity,
                    "Network error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBarView.visibility = View.GONE
            }
        }
    }

    /** Overwrite all EditText fields with the freshly fetched server data */
    private fun populateFromModel(c: CustomerMaster, branchNameFromApi: String?) {
        etCustomerId.setText(c.customerId                   ?: etCustomerId.text.toString())
        etCustomerName.setText(c.customerName               ?: etCustomerName.text.toString())
        etFirstName.setText(c.firstName                     ?: etFirstName.text.toString())
        etLastName.setText(c.lastName                       ?: etLastName.text.toString())
        etGender.setText(c.gender                           ?: etGender.text.toString())
        etDateOfBirth.setText(c.dob                         ?: etDateOfBirth.text.toString())
        etBranch.setText(
            branchNameFromApi ?: c.branchName               ?: etBranch.text.toString()
        )
        etClientRoleKey.setText(c.clientRoleKey             ?: etClientRoleKey.text.toString())
        etCreationDate.setText(formatDate(c.creationDate)           ?: etCreationDate.text.toString())
        etApprovalDate.setText(formatDate(c.approvalDate)           ?: etApprovalDate.text.toString())
        etLastModificationDate.setText(
            formatDate(c.lastModificationDate) ?: etLastModificationDate.text.toString()
        )
        etActivationDate.setText(formatDate(c.activationDate)       ?: etActivationDate.text.toString())
        etMobileNo.setText(c.mobileNo                       ?: etMobileNo.text.toString())
        etEmailId.setText(c.email                           ?: etEmailId.text.toString())
        etAddress1.setText(c.address1                       ?: etAddress1.text.toString())
        etAddress2.setText(c.address2                       ?: etAddress2.text.toString())
        etCity.setText(c.city                               ?: etCity.text.toString())
        etSuburb.setText(c.suburb                           ?: etSuburb.text.toString())
        etLoanCycle.setText(c.loanCycle                     ?: etLoanCycle.text.toString())
        etGroupLoanCycle.setText(c.groupLoanCycle           ?: etGroupLoanCycle.text.toString())
        etAssignedUser.setText(c.assignedUser               ?: etAssignedUser.text.toString())
        etAsOnDate.setText(formatDate(c.asOnDate)           ?: etAsOnDate.text.toString())
    }

    private fun formatDate(dateString: String?): String? {
        if (dateString.isNullOrEmpty()) return dateString
        try {
            // Check if it's already an epoch timestamp in milliseconds
            val epoch = dateString.toLongOrNull()
            if (epoch != null) {
                val date = java.util.Date(epoch)
                val format = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                return format.format(date)
            }
            
            // Try ISO format
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
            val date = inputFormat.parse(dateString)
            return if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            return dateString
        }
    }

    // ─── Account details section ───────────────────────────────────────────────

    private fun setupAccountDetailsButton() {
        recyclerViewAccountDetails.layoutManager = LinearLayoutManager(this)

        btnAccountDetails.setOnClickListener {
            if (layoutAccountDetails.visibility == View.VISIBLE) {
                layoutAccountDetails.visibility = View.GONE
            } else {
                layoutAccountDetails.visibility = View.VISIBLE
                fetchAccountDetails()
            }
        }
    }

    private fun fetchAccountDetails() {
        var customerId = intent.getStringExtra("customerId")
        if (customerId.isNullOrEmpty()) {
            customerId = intent.getStringExtra("CUSTOMER_ID")
        }
        if (customerId.isNullOrEmpty()) {
            customerId = etCustomerId.text.toString()
        }
        
        if (customerId.isNullOrEmpty()) {
            android.util.Log.e("AccountDetails", "Customer ID is empty")
            return
        }

        // Only fetch if the recycler is empty
        if (recyclerViewAccountDetails.adapter != null &&
            (recyclerViewAccountDetails.adapter?.itemCount ?: 0) > 0) return

        progressBarAccDet.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                android.util.Log.d("AccountDetails", "Fetching details for ID: $customerId")
                val response = RetrofitClient.api.getAccDet(customerId)
                if (response.isSuccessful) {
                    val rawRows = response.body() ?: emptyList()
                    android.util.Log.d("AccountDetails", "Fetched ${rawRows.size} rows")
                    
                    if (rawRows.isEmpty()) {
                        Toast.makeText(this@CustomerMasterViewActivity, "No accounts found for this customer", Toast.LENGTH_SHORT).show()
                    }

                    // Each row is a List<Any?> with columns:
                    // [0]=holderKey, [1]=accountId, [2]=accountName, [3]=dateOfLoan, [4]=loanAmount, [5]=loanBalance
                    val accounts: List<AccountDetail> = rawRows.map { row ->
                        AccountDetail(
                            holderKey   = row.getOrNull(0)?.toString() ?: "",
                            accountId   = row.getOrNull(1)?.toString() ?: "",
                            accountName = row.getOrNull(2)?.toString() ?: "",
                            dateOfLoan  = row.getOrNull(3)?.toString() ?: "",
                            loanAmount  = row.getOrNull(4)?.toString() ?: "",
                            loanBalance = row.getOrNull(5)?.toString() ?: ""
                        )
                    }
                    recyclerViewAccountDetails.isNestedScrollingEnabled = false
                    recyclerViewAccountDetails.adapter =
                        AccountDetailAdapter(this@CustomerMasterViewActivity, accounts) { account ->
                            val intent = Intent(this@CustomerMasterViewActivity,
                                LoanMasterViewActivity::class.java)
                            intent.putExtra("loanId", account.accountId)
                            intent.putExtra("holderKey", account.holderKey)
                            intent.putExtra("branchKey", this@CustomerMasterViewActivity.intent.getStringExtra("branchKey") ?: "")
                            startActivity(intent)
                        }
                } else {
                    Toast.makeText(
                        this@CustomerMasterViewActivity,
                        "Account details failed: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@CustomerMasterViewActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBarAccDet.visibility = View.GONE
            }
        }
    }
}