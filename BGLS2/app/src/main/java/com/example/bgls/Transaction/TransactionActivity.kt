package com.example.bgls.Transaction

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Transaction.TransactionRecordAdapter
import com.example.bgls.DataModels.TransactionRecord
import com.example.bgls.R

class TransactionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionRecordAdapter
    private lateinit var btnDownload: Button
    private lateinit var spinnerFilter: Spinner
    
    private lateinit var tabDisbursement: TextView
    private lateinit var tabInterest: TextView
    private lateinit var tabFees: TextView
    private lateinit var tabPenalty: TextView
    private lateinit var tabRecovery: TextView

    private val disbursementData = listOf(
        TransactionRecord("1", "TR00001", "18-01-2024", "DISBT", "45,000.00", "CCN6c3ddd52b6078e302218", "ANDREW GICHERU"),
        TransactionRecord("2", "TR00002", "17-05-2024", "DISBT", "209,175.00", "CCR6cc960ec118f5fdc66fd", "IVY NDIIRA"),
        TransactionRecord("3", "TR00003", "15-04-2025", "DISBT", "136,000.00", "CCRf8ec49f3769f4c680e58", "MUCHIRI JOSEPH MWANGI"),
        TransactionRecord("4", "TR00004", "08-09-2025", "DISBT", "40,000.00", "CEF6c0c74a11e3174d78148", "MUCHIRI JOSEPH MWANGI")
    )

    private val interestData = listOf(
        TransactionRecord("1", "TR00101", "20-01-2024", "INT", "1,250.00", "CCN6c3ddd52b6078e302218", "ANDREW GICHERU"),
        TransactionRecord("2", "TR00102", "19-05-2024", "INT", "5,400.00", "CCR6cc960ec118f5fdc66fd", "IVY NDIIRA")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSpinner()
        setupRecyclerView()
        setupTabs()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewTransactions)
        btnDownload = findViewById(R.id.btnDownload)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        
        tabDisbursement = findViewById(R.id.tabDisbursement)
        tabInterest = findViewById(R.id.tabInterest)
        tabFees = findViewById(R.id.tabFees)
        tabPenalty = findViewById(R.id.tabPenalty)
        tabRecovery = findViewById(R.id.tabRecovery)
    }

    private fun setupSpinner() {
        val filterOptions = listOf("Select Filter", "Flow Id", "Account Number", "Account Name")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = adapter
    }

    private fun setupRecyclerView() {
        adapter = TransactionRecordAdapter(disbursementData)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        val tabs = listOf(tabDisbursement, tabInterest, tabFees, tabPenalty, tabRecovery)

        tabDisbursement.setOnClickListener { 
            setActiveTab(tabDisbursement, tabs)
            adapter.updateData(disbursementData)
            btnDownload.text = "Download Disbursement"
        }
        tabInterest.setOnClickListener { 
            setActiveTab(tabInterest, tabs)
            adapter.updateData(interestData)
            btnDownload.text = "Download Interest"
        }
        tabFees.setOnClickListener { 
            setActiveTab(tabFees, tabs)
            adapter.updateData(emptyList()) // Placeholder
            btnDownload.text = "Download Fees"
        }
        tabPenalty.setOnClickListener { 
            setActiveTab(tabPenalty, tabs)
            adapter.updateData(emptyList()) // Placeholder
            btnDownload.text = "Download Penalty"
        }
        tabRecovery.setOnClickListener { 
            setActiveTab(tabRecovery, tabs)
            adapter.updateData(emptyList()) // Placeholder
            btnDownload.text = "Download Recovery"
        }
    }

    private fun setActiveTab(activeTab: TextView, allTabs: List<TextView>) {
        allTabs.forEach { tab ->
            if (tab == activeTab) {
                tab.setTextColor(android.graphics.Color.WHITE)
                tab.setBackgroundColor(android.graphics.Color.parseColor("#1785A3"))
            } else {
                tab.setTextColor(android.graphics.Color.parseColor("#666666"))
                tab.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }
    }
}