package com.example.bgls.CustomerMaintenance

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.AccountDetail
import com.example.bgls.CustomerMaster.AccountDetailAdapter
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.R

class CustomerMaintenanceViewActivity : AppCompatActivity() {

    private var isEditMode = false
    private val formRows = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_maintenance_view)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // The customer ID passed from the adapter
        val customerId = intent.getStringExtra("CUSTOMER_ID") ?: "27917600"

        // Setup the specific row fields based on the screenshot
        setupRow(findViewById(R.id.rowCustomerId), "Customer Id", customerId)
        setupRow(findViewById(R.id.rowFirstName), "First Name", "MERCY NYANGOGE")
        setupRow(findViewById(R.id.rowGender), "Gender", "FEMALE")
        setupRow(findViewById(R.id.rowBranch), "Branch", "NAIROBI HEAD OFFICE")
        setupRow(findViewById(R.id.rowCreationDate), "Creation Date", "15-11-2022")
        setupRow(findViewById(R.id.rowLastModificationDate), "Last Modification Date", "03-09-2025")
        setupRow(findViewById(R.id.rowMobileNo), "Mobile No", "254725661248")
        setupRow(findViewById(R.id.rowAddress1), "Address 1", "")
        setupRow(findViewById(R.id.rowCity), "City", "Nairobi")
        setupRow(findViewById(R.id.rowLoanCycle), "Loan Cycle", "0")
        setupRow(findViewById(R.id.rowAssignedUser), "Assigned User", "")

        setupRow(findViewById(R.id.rowCustomerName), "Customer Name", "MERCY NYANGOGE MACHUKA")
        setupRow(findViewById(R.id.rowLastName), "Last Name", "MACHUKA")
        setupRow(findViewById(R.id.rowDob), "Date of Birth", "11-06-1989")
        setupRow(findViewById(R.id.rowClientRoleKey), "Client Role Key", "8a858f765a4e7c76015a5baf3")
        setupRow(findViewById(R.id.rowApprovalDate), "Approval Date", "10-10-2025")
        setupRow(findViewById(R.id.rowActivationDate), "Activation Date", "18-11-2022")
        setupRow(findViewById(R.id.rowEmailId), "Email Id", "mercymachuka24@gmail.com")
        setupRow(findViewById(R.id.rowAddress2), "Address 2", "")
        setupRow(findViewById(R.id.rowSuburb), "Suburb", "Kenya")
        setupRow(findViewById(R.id.rowGroupLoanCycle), "Group Loan Cycle", "0")
        setupRow(findViewById(R.id.rowAsOnDate), "As On Date", "03-09-2025")

        val btnModify = findViewById<Button>(R.id.btnModify)
        val tvMainTitle = findViewById<TextView>(R.id.tvMainTitle)

        btnModify.setOnClickListener {
            isEditMode = !isEditMode
            if (isEditMode) {
                // Enter Edit Mode
                btnModify.text = "Submit"
                tvMainTitle.text = "CUSTOMER MAINTENANCE - MODIFY"
                
                for (row in formRows) {
                    val tvValue = row.findViewById<TextView>(R.id.tvValue)
                    val etValue = row.findViewById<EditText>(R.id.etValue)
                    
                    etValue.setText(tvValue.text)
                    tvValue.visibility = View.GONE
                    etValue.visibility = View.VISIBLE
                }
            } else {
                // Submit Changes (Exit Edit Mode)
                btnModify.text = "Modify"
                tvMainTitle.text = "CUSTOMER MAINTENANCE - VIEW"
                
                for (row in formRows) {
                    val tvValue = row.findViewById<TextView>(R.id.tvValue)
                    val etValue = row.findViewById<EditText>(R.id.etValue)
                    
                    tvValue.text = etValue.text
                    etValue.visibility = View.GONE
                    tvValue.visibility = View.VISIBLE
                }
                
                Toast.makeText(this, "Modified successfully", Toast.LENGTH_SHORT).show()
            }
        }

        val btnAccountDetails = findViewById<Button>(R.id.btnAccountDetails)
        val layoutAccountDetails = findViewById<LinearLayout>(R.id.layoutAccountDetails)
        val recyclerViewAccountDetails = findViewById<RecyclerView>(R.id.recyclerViewAccountDetails)

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
            if (layoutAccountDetails.visibility == View.VISIBLE) {
                layoutAccountDetails.visibility = View.GONE
            } else {
                layoutAccountDetails.visibility = View.VISIBLE
            }
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun setupRow(rowView: View, labelText: String, valueText: String) {
        formRows.add(rowView)
        
        val tvLabel = rowView.findViewById<TextView>(R.id.tvLabel)
        val tvValue = rowView.findViewById<TextView>(R.id.tvValue)
        val etValue = rowView.findViewById<EditText>(R.id.etValue)
        
        tvLabel.text = labelText
        tvValue.text = valueText
        etValue.setText(valueText)
    }
}
