package com.example.bgls.LoanSchedule

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanSchedule
import com.example.bgls.LoanSchedule.LoanScheduleActivityAdapter
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.google.android.material.navigation.NavigationView

class LoanScheduleActivity : AppCompatActivity() {

    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etAccountId: EditText
    private lateinit var etAccountName: EditText
    private lateinit var etLoanAmount: EditText
    private lateinit var etLoanDate: EditText
    
    private lateinit var btnUpload: Button
    private lateinit var btnList: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var menuIcon: ImageView
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanScheduleActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_schedule)
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
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupButtons()
        setupRecyclerView()
        loadDummyData()
    }

    private fun initViews() {
        etCustomerId   = findViewById(R.id.etCustomerId)
        etCustomerName = findViewById(R.id.etCustomerName)
        etAccountId    = findViewById(R.id.etAccountId)
        etAccountName  = findViewById(R.id.etAccountName)
        etLoanAmount   = findViewById(R.id.etLoanAmount)
        etLoanDate     = findViewById(R.id.etLoanDate)
        
        btnUpload      = findViewById(R.id.btnUpload)
        btnList        = findViewById(R.id.btnList)
        
        recyclerView   = findViewById(R.id.recyclerViewLoanSchedule)
    }

    private fun setupButtons() {
        btnUpload.setOnClickListener {
            Toast.makeText(this, "Uploading schedule...", Toast.LENGTH_SHORT).show()
        }
        btnList.setOnClickListener {
            val intent = android.content.Intent(this, LoanScheduleListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        adapter = LoanScheduleActivityAdapter(this, emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadDummyData() {
        // Sample data for demonstration
        etCustomerId.setText("22187093")
        etCustomerName.setText("BEATRICE KEMUNTO OBWOCHA")
        etAccountId.setText("CCN78a879844c515110a41a")
        etAccountName.setText("Consumer Credit New Client")
        etLoanAmount.setText("127,285.00")
        etLoanDate.setText("19-06-2023")

        val dummyList = listOf(
            LoanSchedule(
                "19-07-2023",
                "5,000.00",
                "1,200.00",
                "200.00",
                "0.00",
                "20-07-2023",
                "5,000.00",
                "1,200.00",
                "200.00",
                "0.00",
                "0.00"
            ),
            LoanSchedule("19-08-2023", "5,000.00", "1,150.00", "200.00", "0.00", "21-08-2023", "5,000.00", "1,150.00", "200.00", "0.00", "0.00"),
            LoanSchedule("19-09-2023", "5,000.00", "1,100.00", "200.00", "50.00", "25-09-2023", "5,000.00", "1,100.00", "200.00", "50.00", "0.00"),
            LoanSchedule("19-10-2023", "5,000.00", "1,050.00", "200.00", "0.00", "19-10-2023", "5,000.00", "1,050.00", "200.00", "0.00", "0.00"),
            LoanSchedule("19-11-2023", "5,000.00", "1,000.00", "200.00", "0.00", "", "0.00", "0.00", "0.00", "0.00", "6,200.00")
        )
        adapter.updateList(dummyList)
    }
}