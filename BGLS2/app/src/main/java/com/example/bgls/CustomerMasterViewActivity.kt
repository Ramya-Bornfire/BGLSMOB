package com.example.bgls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.AccountDetail
import com.example.bgls.Adapter.AccountDetailAdapter

class CustomerMasterViewActivity : AppCompatActivity() {

    // ─── All form fields (read-only) ───
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

    private lateinit var btnAccountDetails: Button
    private lateinit var layoutAccountDetails: LinearLayout
    private lateinit var recyclerViewAccountDetails: RecyclerView
//    private lateinit var btnHome: Button
//    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master_view)

        initViews()
        populateFromIntent()
        setupButtons()
    }

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

        btnAccountDetails = findViewById(R.id.btnAccountDetails)
        layoutAccountDetails = findViewById(R.id.layoutAccountDetails)
        recyclerViewAccountDetails = findViewById(R.id.recyclerViewAccountDetails)
       // btnHome           = findViewById(R.id.btnHome)
     //   btnBack           = findViewById(R.id.btnBack)
    }

    // ─── Receive data from CustomerMasterListActivity and fill fields ───
    private fun populateFromIntent() {
        val i = intent
        etCustomerId.setText(i.getStringExtra("customerId") ?: "")
        etCustomerName.setText(i.getStringExtra("customerName") ?: "")
        etFirstName.setText(i.getStringExtra("firstName") ?: "")
        etLastName.setText(i.getStringExtra("lastName") ?: "")
        etGender.setText(i.getStringExtra("gender") ?: "")
        etDateOfBirth.setText(i.getStringExtra("dob") ?: "")
        etBranch.setText(i.getStringExtra("branchName") ?: "")
        etClientRoleKey.setText(i.getStringExtra("clientRoleKey") ?: "")
        etCreationDate.setText(i.getStringExtra("creationDate") ?: "")
        etApprovalDate.setText(i.getStringExtra("approvalDate") ?: "")
        etLastModificationDate.setText(i.getStringExtra("lastModificationDate") ?: "")
        etActivationDate.setText(i.getStringExtra("activationDate") ?: "")
        etMobileNo.setText(i.getStringExtra("mobileNo") ?: "")
        etEmailId.setText(i.getStringExtra("email") ?: "")
        etAddress1.setText(i.getStringExtra("address1") ?: "")
        etAddress2.setText(i.getStringExtra("address2") ?: "")
        etCity.setText(i.getStringExtra("city") ?: "")
        etSuburb.setText(i.getStringExtra("suburb") ?: "")
        etLoanCycle.setText(i.getStringExtra("loanCycle") ?: "")
        etGroupLoanCycle.setText(i.getStringExtra("groupLoanCycle") ?: "")
        etAssignedUser.setText(i.getStringExtra("assignedUser") ?: "")
        etAsOnDate.setText(i.getStringExtra("asOnDate") ?: "")
    }


    private fun setupButtons() {
        // Setup Account Details Table
        val dummyAccounts = listOf(
            AccountDetail("CCN60aeed68fb1843c0ec57", "Consumer Credit New Client", "14-12-2022", "36,620.00", "-124,289.00"),
            AccountDetail("CCN3851754328850517ca23", "Consumer Credit New Client", "18-11-2022", "92,985.00", "-272,359.00"),
            AccountDetail("CCN1ab32cd3b745999db7ea", "Consumer Credit New Client", "19-11-2022", "147,390.00", "-493,673.90"),
            AccountDetail("CCNa73a323563f3537360c5", "Consumer Credit New Client", "23-11-2022", "86,985.00", "-288,240.00")
        )
        recyclerViewAccountDetails.layoutManager = LinearLayoutManager(this)
        recyclerViewAccountDetails.adapter = AccountDetailAdapter(this, dummyAccounts) { account ->
            val intent = Intent(this, LoanMasterViewActivity::class.java)
            intent.putExtra("loanNo", account.accountId)
            startActivity(intent)
        }

        btnAccountDetails.setOnClickListener {
            // Toggle visibility of Account Details table
            if (layoutAccountDetails.visibility == View.VISIBLE) {
                layoutAccountDetails.visibility = View.GONE
            } else {
                layoutAccountDetails.visibility = View.VISIBLE
            }
        }

//        btnHome.setOnClickListener {
//            // TODO: Navigate to Home
//            Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show()
//        }

//        btnBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
    }
}