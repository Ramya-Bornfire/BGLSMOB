package com.example.bgls


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.DataModels.Transaction
import com.example.bgls.databinding.ActivityMainBinding
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageView
import android.view.Gravity
import android.widget.Toast
import com.example.bgls.AuditTrial.AuditTrailDetailsActivity
import com.example.bgls.ChartOfAccounts.ChartOfAccountsActivity
import com.example.bgls.CustomerMaintenance.CustomerMaintenanceActivity
import com.example.bgls.CustomerMaster.CustomerMasterActivity
import com.example.bgls.LoanMaintenance.LoanMaintananceListActivity
import com.example.bgls.LoanMaintenance.LoanMaintenanceActivity
import com.example.bgls.LoanMaster.LoanMasterActivity
import com.example.bgls.LoanSchedule.LoanScheduleActivity
import com.example.bgls.OrganizationDetails.OrganizationDetialsActivity
import com.example.bgls.ReversalTransaction.FailedReversalActivity
import com.example.bgls.ReversalTransaction.RecoveryReversalActivity
import com.example.bgls.ReversalTransaction.TransactionsReversalActivity
import com.example.bgls.Transaction.TransactionActivity
import com.example.bgls.TransactionMaintenance.AccountLedgerPositingActivity
import com.example.bgls.TransactionMaintenance.JournalEntriesActivity
import com.example.bgls.TransactionMaintenance.ProfileAndLossAccountActivity
import com.example.bgls.TransactionMaintenance.TransAccountLedgerActivity
import com.example.bgls.TransactionMaintenance.TrialBalanceActivity
import com.example.bgls.UserControl.UserControlActivity
import com.example.bgls.TransactionInquiries.AccountBalanceActivity
import com.example.bgls.TransactionInquiries.InterestSummaryActivity
import com.example.bgls.TransactionInquiries.BalanceSheetActivity
import com.example.bgls.WalletMaintenance.WalletInquiryActivity
import com.example.bgls.WalletMaintenance.WalletMaintenanceListActivity


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val menuIcon = findViewById<ImageView>(R.id.menuIcon)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

// 🔥 CLICK MENU ICON → OPEN DRAWER
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }
        navigationView.setNavigationItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_logout -> {
                    Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show()
                }
            }

            drawerLayout.closeDrawer(Gravity.LEFT)
            true
        }
        val txtLoginTime = findViewById<TextView>(R.id.txtLoginTime)

        val currentTime = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        txtLoginTime.text = "$currentTime"

        val list = listOf(
            Transaction(
                "Admin",
                listOf("Organization Details", "User Control", "Parameters", "Audit Trail")
            ),
            Transaction(
                "Migration",
                listOf("Customer Master", "Loan Master", "Loan Schedule", "Transaction")
            ),
            Transaction("Chart of Accounts", listOf("Chart of Accounts")),

            Transaction("Customer Onboarding",
                listOf("Minimal Data",
                    "Approval",
                    "Disbursement",
                    "KYC Compliance",
                    "Compliance Department",
                    "Hold and Reject")),
            Transaction("Customer Maintenance" ,listOf("Customer Maintenance")),
            Transaction("Loan Maintenance",listOf("Loan Maintenance")),
            Transaction("Loan Operation",
                listOf("Loan Operation","Loan Closure")),
            Transaction("Deposit Maintanance"),
            Transaction("Wallet Maintanances",listOf("Wallet Maintanance","Wallet Inquries")),
            Transaction("Transaction Maintanance",
                listOf("Journal Entries","Account Ledger Positing","Account Leader","Trial Balance","Profile and Loss Account")),
            Transaction("Reversal Transactions",
                listOf("Transaction Reversal","Recovery Reversal","Failed Reversal")),
            Transaction("Collection Process",
                listOf("Loan Collection")),
            Transaction("Batch Job Execution",listOf("Batch Job")),
            Transaction("Transaction Reports",
                listOf("Credit Facility Report",
                        "End Of Month Report",
                        "DAB Reports",
                        "Consolidated Loan Reports",
                        "Transaction Reports",
                        "Recovery Report",
                        "Demand generation Report",
                        "Interest Accrual Report",
                        "Penalty Accrual Report")),
            Transaction("Transaction Inquiries",
                listOf("Account Balances",
                    "Interest Summary",
                    "Journal Book",
                    "Account Ledger",
                    "Trial Balance",
                    "Profile and Loss Account",
                    "Balance Sheet",
                    "Balancing Report"))

        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TransactionAdapter(list) {name,subItem ->

            when (name){
                "Deposit Maintanance" -> {
                    startActivity(Intent(this, com.example.bgls.DepositAccount.DepositAccountMaintenanceListActivity::class.java))
                }

            }
            when (subItem) {

                "Organization Details" -> {
                    startActivity(Intent(this, OrganizationDetialsActivity::class.java))
                }

                "User Control" -> {
                    startActivity(Intent(this, UserControlActivity::class.java))
                }
                "Parameters" -> {
                    startActivity(Intent(this, ParameterActivity::class.java))
                }
                "Audit Trail" -> {
                    startActivity(Intent(this, AuditTrailDetailsActivity::class.java))
                }

                "Customer Master"->{
                    startActivity(Intent(this, CustomerMasterActivity::class.java))
                }
                "Loan Master"->{
                    startActivity(Intent(this, LoanMasterActivity::class.java))
                }
                "Loan Schedule"->{
                    startActivity(Intent(this, LoanScheduleActivity::class.java))
                }
                "Transaction"->{
                    startActivity(Intent(this, TransactionActivity::class.java))
                }
                "Chart of Accounts"->{
                    startActivity(Intent(this, ChartOfAccountsActivity::class.java))
                }
                "Customer Maintenance"->{
                    startActivity(Intent(this, CustomerMaintenanceActivity::class.java))

                }
                "Loan Maintenance"->{
                    startActivity(Intent(this, LoanMaintananceListActivity::class.java))
                }

                "Journal Entries"->{
                    startActivity(Intent(this, JournalEntriesActivity::class.java))

                }
                "Account Ledger Positing"->{
                    startActivity(Intent(this, AccountLedgerPositingActivity::class.java))


                }
                "Account Ledger"->{
                    startActivity(Intent(this, TransAccountLedgerActivity::class.java))


                }
                "Trial Balance"->{
                    startActivity(Intent(this, TrialBalanceActivity::class.java))
                }

                "Profile and Loss Account"->{
                    startActivity(Intent(this, ProfileAndLossAccountActivity::class.java))
                }
                "Loan Operation" -> {
                    startActivity(Intent(this, com.example.bgls.LoanOperation.LoanOperationActivity::class.java))
                }
                "Loan Closure" -> {
                    startActivity(Intent(this, com.example.bgls.LoanOperation.LoanClosureActivity::class.java))
                }
                "Credit Facility Report" -> {
                    startActivity(Intent(this, com.example.bgls.TransactionReports.CreditFacilityReportActivity::class.java))
                }
                "End Of Month Report" -> {
                    startActivity(Intent(this, com.example.bgls.TransactionReports.EndOfMonthReportActivity::class.java))
                }
                "DAB Reports" -> {
                    startActivity(Intent(this, com.example.bgls.TransactionReports.DABReportActivity::class.java))
                }
                "Consolidated Loan Reports" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Consolidated Loan - Reports")
                    startActivity(intent)
                }
                "Transaction Reports" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Transaction - Reports")
                    startActivity(intent)
                }
                "Recovery Report" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Recovery - Reports")
                    intent.putExtra("SHOW_SPINNER", true) // Recovery report has the Excel spinner
                    startActivity(intent)
                }
                "Demand generation Report" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Demand Generation - Reports")
                    startActivity(intent)
                }
                "Interest Accrual Report" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Interest Accrual - Reports")
                    startActivity(intent)
                }
                "Penalty Accrual Report" -> {
                    val intent = Intent(this, com.example.bgls.TransactionReports.GenericReportActivity::class.java)
                    intent.putExtra("REPORT_TITLE", "Penalty Accrual - Reports")
                    startActivity(intent)
                }
                "Wallet Maintanance" -> {
                    startActivity(Intent(this, WalletMaintenanceListActivity::class.java))
                }
                "Wallet Inquries" -> {
                    startActivity(Intent(this, WalletInquiryActivity::class.java))
                }
//                "Account Opening" -> {
//                    startActivity(Intent(this, com.example.bgls.DepositAccount.DepositAccountOpeningActivity::class.java))
//                }
                "Loan Collection" -> {
                    startActivity(Intent(this, com.example.bgls.LoanCollectionActivity::class.java))
                }


                "Transaction Reversal"->{
                    startActivity(Intent(this, TransactionsReversalActivity::class.java))

                }
                "Recovery Reversal"->{
                    startActivity(Intent(this,RecoveryReversalActivity::class.java))
                }
                "Failed Reversal"->{
                    startActivity(Intent(this,FailedReversalActivity::class.java))
                }
                "Account Leader" -> {

                    val intent = Intent(this, ParameterActivity::class.java)
                    intent.putExtra("MODULE_NAME", "Account Ledger")
                    startActivity(intent)

                }

                "Account Ledger"->{
                    startActivity(Intent(this, TransAccountLedgerActivity::class.java))

                }

                "Account Balances" -> {
                    startActivity(Intent(this, AccountBalanceActivity::class.java))
                }
                "Interest Summary" -> {
                    startActivity(Intent(this, InterestSummaryActivity::class.java))
                }
                "Journal Book" -> {
                    startActivity(Intent(this, com.example.bgls.TransactionMaintenance.JournalEntriesListActivity::class.java))
                }
                "Balance Sheet" -> {
                    startActivity(Intent(this, BalanceSheetActivity::class.java))
                }
                "Balancing Report" -> {
                    startActivity(Intent(this, com.example.bgls.TransactionInquiries.BalancingReportActivity::class.java))
                }
                "Batch Job" -> {
                    startActivity(Intent(this, com.example.bgls.BatchJobExecution.BatchJobActivity::class.java))
                }


                else -> {
                    Toast.makeText(this, "$subItem Clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

