package com.example.bgls.TransactionInquiries

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Adapter.LoanFlowAdapter
import com.example.bgls.Adapter.LoanScheduleAdapter
import com.example.bgls.DataModels.LoanFlowModel
import com.example.bgls.DataModels.LoanScheduleModel
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale
import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LeaseLoanViewActivity : AppCompatActivity() {

    private var loanId = ""
    private var holderKey = ""
    private var branchKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lease_loan_view)

        loanId = intent.getStringExtra("id") ?: ""
        holderKey = intent.getStringExtra("holder_key") ?: ""
        branchKey = intent.getStringExtra("branch_key") ?: ""

        setupButtons()
        setupTabs()
        
        if (loanId.isNotEmpty()) {
            fetchLoanDetails()
        } else {
            populateDummyData()
        }
    }

    private fun setupTabs() {
        val tabLoanDetails = findViewById<TextView>(R.id.tabLoanDetails)
        val tabRepaymentDetails = findViewById<TextView>(R.id.tabRepaymentDetails)
        val tabFlows = findViewById<TextView>(R.id.tabFlows)
        val tabLoanPosition = findViewById<TextView>(R.id.tabLoanPosition)
        val tabAccountLedger = findViewById<TextView>(R.id.tabAccountLedger)

        val layoutLoanDetails = findViewById<LinearLayout>(R.id.layoutLoanDetails)
        val layoutRepaymentDetails = findViewById<LinearLayout>(R.id.layoutRepaymentDetails)
        val layoutLoanPosition = findViewById<LinearLayout>(R.id.layoutLoanPosition)
        val layoutLoanPositionTable = findViewById<LinearLayout>(R.id.layoutLoanPositionTable)
        val layoutAccountLedger = findViewById<LinearLayout>(R.id.layoutAccountLedger)
        val layoutSchedule = findViewById<LinearLayout>(R.id.layoutSchedule)
        val layoutFlows = findViewById<LinearLayout>(R.id.layoutFlows)
        val btnSchedule = findViewById<Button>(R.id.btnSchedule)
        val btnFlows = findViewById<Button>(R.id.btnFlows)

        val tabs = listOf(tabLoanDetails, tabRepaymentDetails, tabFlows, tabLoanPosition, tabAccountLedger)

        tabs.forEach { tab ->
            tab.setOnClickListener {
                // Update UI: Highlight selected tab
                tabs.forEach { 
                    it.setBackgroundColor(Color.parseColor("#F0F0F0"))
                    it.setTextColor(Color.parseColor("#333333"))
                }
                tab.setBackgroundColor(Color.parseColor("#007BFF"))
                tab.setTextColor(Color.WHITE)

                // Toggle Layouts
                when (tab) {
                    tabLoanDetails -> {
                        layoutLoanDetails.visibility = View.VISIBLE
                        layoutRepaymentDetails.visibility = View.GONE
                        layoutLoanPosition.visibility = View.GONE
                        layoutLoanPositionTable.visibility = View.GONE
                        layoutSchedule.visibility = View.GONE
                        layoutFlows.visibility = View.GONE
                        btnSchedule.visibility = View.GONE
                        btnFlows.visibility = View.GONE
                    }
                    tabRepaymentDetails -> {
                        layoutLoanDetails.visibility = View.GONE
                        layoutRepaymentDetails.visibility = View.VISIBLE
                        layoutLoanPosition.visibility = View.GONE
                        layoutLoanPositionTable.visibility = View.GONE
                        layoutFlows.visibility = View.GONE
                        btnSchedule.visibility = View.VISIBLE
                        btnFlows.visibility = View.GONE
                    }
                    tabFlows -> {
                        layoutLoanDetails.visibility = View.GONE
                        layoutRepaymentDetails.visibility = View.VISIBLE
                        layoutLoanPosition.visibility = View.GONE
                        layoutLoanPositionTable.visibility = View.GONE
                        layoutSchedule.visibility = View.GONE
                        btnSchedule.visibility = View.GONE
                        btnFlows.visibility = View.VISIBLE
                    }
                    tabLoanPosition -> {
                        layoutLoanDetails.visibility = View.GONE
                        layoutRepaymentDetails.visibility = View.GONE
                        layoutLoanPosition.visibility = View.VISIBLE
                        layoutLoanPositionTable.visibility = View.VISIBLE
                        layoutAccountLedger.visibility = View.GONE
                        layoutSchedule.visibility = View.GONE
                        layoutFlows.visibility = View.GONE
                        btnSchedule.visibility = View.GONE
                        btnFlows.visibility = View.GONE
                    }
                    tabAccountLedger -> {
                        layoutLoanDetails.visibility = View.GONE
                        layoutRepaymentDetails.visibility = View.GONE
                        layoutLoanPosition.visibility = View.GONE
                        layoutLoanPositionTable.visibility = View.GONE
                        layoutAccountLedger.visibility = View.VISIBLE
                        layoutSchedule.visibility = View.GONE
                        layoutFlows.visibility = View.GONE
                        btnSchedule.visibility = View.GONE
                        btnFlows.visibility = View.GONE
                    }
                    else -> {
                        layoutLoanDetails.visibility = View.GONE
                        layoutRepaymentDetails.visibility = View.GONE
                        layoutLoanPosition.visibility = View.GONE
                        layoutLoanPositionTable.visibility = View.GONE
                        layoutAccountLedger.visibility = View.GONE
                        layoutSchedule.visibility = View.GONE
                        layoutFlows.visibility = View.GONE
                        btnSchedule.visibility = View.GONE
                        btnFlows.visibility = View.GONE
                        Toast.makeText(this, "${tab.text} clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.btnSchedule).setOnClickListener {
            val layoutSchedule = findViewById<LinearLayout>(R.id.layoutSchedule)
            if (layoutSchedule.visibility == View.VISIBLE) {
                layoutSchedule.visibility = View.GONE
            } else {
                layoutSchedule.visibility = View.VISIBLE
            }
        }
        findViewById<Button>(R.id.btnFlows).setOnClickListener {
            val layoutFlows = findViewById<LinearLayout>(R.id.layoutFlows)
            if (layoutFlows.visibility == View.VISIBLE) {
                layoutFlows.visibility = View.GONE
            } else {
                layoutFlows.visibility = View.VISIBLE
            }
        }
        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menu.add("Download Excel")
            popup.setOnMenuItemClickListener { item ->
                if (item.title == "Download Excel") {
                    Toast.makeText(this, "Downloading Excel...", Toast.LENGTH_SHORT).show()
                }
                true
            }
            popup.show()
        }
    }

    private fun fetchLoanDetails() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getDrawDownLoanMaintenance("view", loanId)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val details = data.loanDetails
                    val payment = data.paymentDetails

                    if (details != null) {
                        findViewById<EditText>(R.id.etCustomerId).setText(details["customer_id"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etCustomerName).setText(details["customer_name"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etLoanType).setText(details["loan_type"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etBranchId).setText(details["branch_id"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etBranchName).setText(details["branch_name"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etDateOfLoan).setText(formatApiDate(details["date_of_loan"]?.toString() ?: ""))
                        findViewById<EditText>(R.id.etGlCode).setText(details["gl_code"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etGlDes).setText(details["gl_desc"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etGlshCode).setText(details["glsh_code"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etGlshDes).setText(details["glsh_desc"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etLoanAcctNo).setText(details["loan_accountno"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etLoanCurrency).setText(details["loan_currency"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etInterestRate).setText(details["effective_interest_rate"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etLoanSanctioned).setText(details["loan_sanctioned"]?.toString() ?: "0.00")
                        findViewById<EditText>(R.id.etDisbursement).setText(details["disbursement_amt"]?.toString() ?: "0.00")
                    }

                    if (payment != null) {
                        findViewById<EditText>(R.id.etInstallmentId).setText(payment["inst_id"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etInstallmentStartDate).setText(formatApiDate(payment["inst_start_dt"]?.toString() ?: ""))
                        findViewById<EditText>(R.id.etNoOfInstallment).setText(payment["no_of_inst"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etPrincipalFreq).setText(payment["inst_freq"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etInterestFreq).setText(payment["interest_frequency"]?.toString() ?: "")
                        findViewById<EditText>(R.id.etInstallmentAmount).setText(payment["inst_amount"]?.toString() ?: "0.00")
                    }

                    // Pre-fetch other tab data
                    fetchFlows()
                    fetchLoanPosition()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LeaseLoanViewActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFlows() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getDemandFlow(loanId)
                if (response.isSuccessful && response.body() != null) {
                    val rawFlows: List<List<Any>> = response.body()!!
                    val flowData = rawFlows.map { row: List<Any> ->
                        LoanFlowModel(
                            flowDate = formatApiDate(row.getOrNull(2)?.toString() ?: ""),
                            flowCode = row.getOrNull(4)?.toString() ?: "",
                            flowFreq = row.getOrNull(1)?.toString() ?: "",
                            flowAmt = row.getOrNull(3)?.toString() ?: "0.00"
                        )
                    }
                    val rvFlows = findViewById<RecyclerView>(R.id.rvLoanFlows)
                    rvFlows.layoutManager = LinearLayoutManager(this@LeaseLoanViewActivity)
                    rvFlows.adapter = LoanFlowAdapter(flowData)
                }
            } catch (e: Exception) {}
        }
    }

    private fun fetchLoanPosition() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getLoanPosition(loanId)
                // Position logic here if needed
            } catch (e: Exception) {}
        }
    }

    private fun formatApiDate(rawDate: String): String {
        return try {
            if (rawDate.contains("T")) {
                val parts = rawDate.split("T")[0].split("-")
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                rawDate
            }
        } catch (e: Exception) {
            rawDate
        }
    }

    private fun populateDummyData() {
        // Loan Details
        findViewById<EditText>(R.id.etCustomerId).setText("CUST0000042001")
        findViewById<EditText>(R.id.etCustomerName).setText("TIM DAVID")
        findViewById<EditText>(R.id.etLoanType).setText("LA")
        findViewById<EditText>(R.id.etBranchId).setText("103")
        findViewById<EditText>(R.id.etBranchName).setText("Al Salam Bank Seychelles")
        findViewById<EditText>(R.id.etDateOfLoan).setText("22-05-2026")
        findViewById<EditText>(R.id.etGlCode).setText("1000")
        findViewById<EditText>(R.id.etGlDes).setText("Asset")
        findViewById<EditText>(R.id.etGlshCode).setText("1500")
        findViewById<EditText>(R.id.etGlshDes).setText("LOANS AND ADVANCES")
        findViewById<EditText>(R.id.etSchemeCode).setText("")
        findViewById<EditText>(R.id.etSchemeType).setText("")
        findViewById<EditText>(R.id.etLoanAcctNo).setText("LA0001")
        findViewById<EditText>(R.id.etLoanCurrency).setText("SCR")
        findViewById<EditText>(R.id.etInterestRate).setText("12")
        findViewById<EditText>(R.id.etLoanSanctioned).setText("10,000.00")
        findViewById<EditText>(R.id.etMarginLimit).setText("10")
        findViewById<EditText>(R.id.etDrawingLimit).setText("1,000.00")
        findViewById<EditText>(R.id.etDisbursement).setText("10,000.00")
        findViewById<EditText>(R.id.etLoanOutstanding).setText("")
        findViewById<EditText>(R.id.etLoanPeriodYears).setText("00")
        findViewById<EditText>(R.id.etLoanPeriodMonths).setText("6")
        findViewById<EditText>(R.id.etLoanPeriodDays).setText("00")
        findViewById<EditText>(R.id.etExpiryDate).setText("24-11-2026")
        findViewById<EditText>(R.id.etRepaymentTerms).setText("")
        findViewById<EditText>(R.id.etRecoveryMethod).setText("Office Routing Account")

        // Repayment Details
        findViewById<EditText>(R.id.etRepayCustomerId).setText("CUST0000042001")
        findViewById<EditText>(R.id.etRepayCustomerName).setText("TIM DAVID")
        findViewById<EditText>(R.id.etRepayDateOfLoan).setText("22-05-2026")
        findViewById<EditText>(R.id.etRepayLoanAcctNo).setText("LA0001")
        findViewById<EditText>(R.id.etRepayLoanCurrency).setText("SCR")
        findViewById<EditText>(R.id.etRepayInterestRate).setText("12")
        findViewById<EditText>(R.id.etInstallmentId).setText("1")
        findViewById<EditText>(R.id.etInstallmentStartDate).setText("24-05-2026")
        findViewById<EditText>(R.id.etNoOfInstallment).setText("6")
        findViewById<EditText>(R.id.etPrincipalFreq).setText("Monthly")
        findViewById<EditText>(R.id.etInterestFreq).setText("Monthly")
        findViewById<EditText>(R.id.etInstallmentAmount).setText("1,666.67")
        findViewById<EditText>(R.id.etInstallmentPercent).setText("17")

        // Loan Position
        findViewById<EditText>(R.id.etPosCustomerId).setText("CUST0000042001")
        findViewById<EditText>(R.id.etPosCustomerName).setText("TIM DAVID")
        findViewById<EditText>(R.id.etPosDateOfLoan).setText("22-05-2026")
        findViewById<EditText>(R.id.etPosLoanAcctNo).setText("LA0001")
        findViewById<EditText>(R.id.etPosLoanCurrency).setText("SCR")
        findViewById<EditText>(R.id.etPosInterestRate).setText("12")

        // Account Ledger
        findViewById<EditText>(R.id.etLedAcctId).setText("LA0001")
        findViewById<EditText>(R.id.etLedAcctName).setText("TIM DAVID")
        findViewById<EditText>(R.id.etLedAcctCcy).setText("SCR")
        findViewById<EditText>(R.id.etLedAcctBal).setText("10,000.00")
        findViewById<EditText>(R.id.etLedGenLed).setText("1000")
        findViewById<EditText>(R.id.etLedGlDes).setText("Asset")
        findViewById<EditText>(R.id.etLedGlSubHead).setText("1500")
        findViewById<EditText>(R.id.etLedGlshDes).setText("LOANS AND ADVANCES")
        findViewById<EditText>(R.id.etLedAccountCurrency).setText("SCR")
        findViewById<EditText>(R.id.etLedHomeCurrencyBal).setText("10,000.00")
        findViewById<EditText>(R.id.etLedAcctOpenDate).setText("22-05-2026")
        findViewById<EditText>(R.id.etLedAcctCloseDate).setText("")

        // Schedule Data
        setupScheduleRecyclerView()
        setupFlowsRecyclerView()

        // Audit section
        findViewById<TextView>(R.id.tvEntryUser).text = "EMP02"
        findViewById<TextView>(R.id.tvEntryTime).text = "24-04-2026"
        findViewById<TextView>(R.id.tvModifyUser).text = "EMP02"
        findViewById<TextView>(R.id.tvModifyTime).text = "24-04-2026"
        findViewById<TextView>(R.id.tvVerifyUser).text = "EMP02"
        findViewById<TextView>(R.id.tvVerifyTime).text = "24-04-2026"
    }

    private fun setupScheduleRecyclerView() {
        val rvSchedule = findViewById<RecyclerView>(R.id.rvLoanSchedule)
        val scheduleList = listOf(
            LoanScheduleModel("1", "24-05-2026", "Regular Installment", "2,867.00", "1,667.00", "1,200.00", "0.00", "8,333.00"),
            LoanScheduleModel("2", "24-06-2026", "Regular Installment", "2,867.00", "1,667.00", "1,200.00", "0.00", "6,666.00"),
            LoanScheduleModel("3", "24-07-2026", "Regular Installment", "2,867.00", "1,667.00", "1,200.00", "0.00", "4,999.00"),
            LoanScheduleModel("4", "24-08-2026", "Regular Installment", "2,867.00", "1,667.00", "1,200.00", "0.00", "3,332.00"),
            LoanScheduleModel("5", "24-09-2026", "Regular Installment", "2,867.00", "1,667.00", "1,200.00", "0.00", "1,665.00"),
            LoanScheduleModel("6", "24-10-2026", "Regular Installment", "2,865.00", "1,665.00", "1,200.00", "0.00", "0.00")
        )
        rvSchedule.layoutManager = LinearLayoutManager(this)
        rvSchedule.adapter = LoanScheduleAdapter(scheduleList)
    }

    private fun setupFlowsRecyclerView() {
        val rvFlows = findViewById<RecyclerView>(R.id.rvLoanFlows)
        val flowList = listOf(
            LoanFlowModel("24-06-2026", "RIDEM", "Monthly", "2,867.00"),
            LoanFlowModel("24-07-2026", "RIDEM", "Monthly", "2,867.00"),
            LoanFlowModel("24-08-2026", "RIDEM", "Monthly", "2,867.00"),
            LoanFlowModel("24-09-2026", "RIDEM", "Monthly", "2,867.00"),
            LoanFlowModel("24-10-2026", "RIDEM", "Monthly", "2,867.00"),
            LoanFlowModel("24-11-2026", "RIDEM", "Monthly", "2,865.00")
        )
        rvFlows.layoutManager = LinearLayoutManager(this)
        rvFlows.adapter = LoanFlowAdapter(flowList)
    }
}
