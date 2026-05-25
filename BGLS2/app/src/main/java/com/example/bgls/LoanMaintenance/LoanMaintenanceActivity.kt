package com.example.bgls.LoanMaintenance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.LoanMaintenance.LoanMaintenanceAdapter
import com.example.bgls.DataModels.LoanMaintenanceModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.google.android.material.navigation.NavigationView

class LoanMaintenanceActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanMaintenanceAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuIcon: ImageView
    
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 18
    
    private var currentStatusFilter: String = "Select Status"
    private var currentVerifiedFilter: String = "Verified"

    // Extended dummy data simulating the image
    private val allData = (1..100).map { i ->
        val status = if (i % 5 == 0) "ACTIVE" else "ACTIVE_IN_ARREARS"
        val branch = if (i % 2 == 0) "Nairobi CBD" else "Tom Mboya street"
        val loanName = "CUSTOMER $i LIMITED"
        
        LoanMaintenanceModel(
            sNo = i.toString(),
            loanId = "BFM19070${1000 + i}",
            loanType = "Boda Financing Monthly",
            loanName = loanName,
            mobileNo = "25471741840${i%10}",
            retailerBranchId = branch,
            status = status,
            isVerified = i % 3 != 0 // Mix of true and false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_maintenance)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        menuIcon = findViewById(R.id.menuIcon)

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigate back to MainActivity (or any home screen)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_profile -> {
                    // TODO: Open Profile activity if needed
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    // TODO: Implement logout logic (clear session, go to login)
                    Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        setupSpinners()
        setupPagination()
        updateTableData()
    }

    private fun setupSpinners() {
        val filterOptions = arrayOf("Select Filter", "Loan ID", "Loan Name", "Mobile No")
        val statusOptions = arrayOf("Select Status", "ACTIVE", "ACTIVE_IN_ARREARS", "CLOSED")
        val verifiedOptions = arrayOf("Verified", "Unverified", "All")

        val spinnerFilter = findViewById<Spinner>(R.id.spinnerFilter)
        val spinnerStatus = findViewById<Spinner>(R.id.spinnerStatus)
        val spinnerVerified = findViewById<Spinner>(R.id.spinnerVerified)

        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        spinnerVerified.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, verifiedOptions)

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                currentPage = 1
                updateTableData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerVerified.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                currentVerifiedFilter = verifiedOptions[position]
                currentPage = 1
                updateTableData()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupPagination() {
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateTableData()
            }
        }

        btnNext.setOnClickListener {
            val totalPages = (allData.size + itemsPerPage - 1) / itemsPerPage
            if (currentPage < totalPages) {
                currentPage++
                updateTableData()
            }
        }
    }

    private fun updateTableData() {
        val filteredData = allData.filter { item ->
            val matchStatus = if (currentStatusFilter == "Select Status") true else item.status == currentStatusFilter
            val matchVerified = if (currentVerifiedFilter == "All") true else item.isVerified == (currentVerifiedFilter == "Verified")
            matchStatus && matchVerified
        }

        val totalPages = if (filteredData.isEmpty()) 1 else (filteredData.size + itemsPerPage - 1) / itemsPerPage
        
        val startIndex = (currentPage - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, filteredData.size)
        val currentList = if (startIndex < filteredData.size) filteredData.subList(startIndex, endIndex) else emptyList()

        recyclerView = findViewById(R.id.recyclerViewLoanMaintenance)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LoanMaintenanceAdapter(this, currentList)
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.tvPageInfo).text = "Page $currentPage of $totalPages"
    }
}