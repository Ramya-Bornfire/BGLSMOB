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
            Transaction("Customer Onboarding",
                listOf("Minimal Data",
                    "Approval",
                    "Disbursement",
                    "KYC Compliance",
                    "Compliance Department",
                    "Hold and Reject")),
            Transaction("Customer Maintenance"),
            Transaction("Loan Maintenance"),
            Transaction("Loan Operation",
                listOf("Loan Operation","Loan Closure")),
            Transaction("Deposit Accounts",
                listOf("Account Opening","Account Maintenace")),
            Transaction("Transaction Maintanance",
                listOf("Journal Entries","Account Ledger Positing","Account Leader","Trial Balance","Profile and Loss Account")),
            Transaction("Reversal Transactions",
                listOf("Transaction Reversal","Recovery Reversal","Failed Reversal")),
            Transaction("Collection Process",
                listOf("Loan Collection","Recovery Path")),
            Transaction("Batch Job Execution"),
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
        binding.recyclerView.adapter = TransactionAdapter(list) { subItem ->

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

                else -> {
                    Toast.makeText(this, "$subItem Clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

