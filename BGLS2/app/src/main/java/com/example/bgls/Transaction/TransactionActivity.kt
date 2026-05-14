package com.example.bgls.Transaction

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TransactionDto
import com.example.bgls.DataModels.TransactionMigrationResponse
import com.example.bgls.DataModels.TransactionRecord
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

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

    private lateinit var apiService: ServiceApi
    private var currentFlowCode = "DISBT"
    private var currentTabName = "disbursement"

    private var cachedResponse: TransactionMigrationResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        initViews()
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }
        
        setupSpinner()
        setupRecyclerView()
        setupTabs()
        setupDownload()

        // Initialize Retrofit
        apiService = RetrofitClient.api

        // Fetch data
        fetchAllTransactionData()
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
        adapter = TransactionRecordAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        val tabs = listOf(tabDisbursement, tabInterest, tabFees, tabPenalty, tabRecovery)

        tabDisbursement.setOnClickListener {
            setActiveTab(tabDisbursement, tabs)
            currentFlowCode = "DISBT"
            currentTabName = "disbursement"
            loadDataForCurrentTab()
            btnDownload.text = "Download Disbursement"
        }
        tabInterest.setOnClickListener {
            setActiveTab(tabInterest, tabs)
            currentFlowCode = "INDEM"
            currentTabName = "interest"
            loadDataForCurrentTab()
            btnDownload.text = "Download Interest"
        }
        tabFees.setOnClickListener {
            setActiveTab(tabFees, tabs)
            currentFlowCode = "FEEDEM"
            currentTabName = "fees"
            loadDataForCurrentTab()
            btnDownload.text = "Download Fees"
        }
        tabPenalty.setOnClickListener {
            setActiveTab(tabPenalty, tabs)
            currentFlowCode = "PENDEM"
            currentTabName = "penalty"
            loadDataForCurrentTab()
            btnDownload.text = "Download Penalty"
        }
        tabRecovery.setOnClickListener {
            setActiveTab(tabRecovery, tabs)
            currentFlowCode = "COLL"
            currentTabName = "recovery"
            loadDataForCurrentTab()
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

    private fun fetchAllTransactionData() {
        lifecycleScope.launch {
            try {
                val response = apiService.getTransactionMigration("add")
                if (response.isSuccessful && response.body() != null) {
                    cachedResponse = response.body()
                    loadDataForCurrentTab()
                } else {
                    Toast.makeText(this@TransactionActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TransactionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDataForCurrentTab() {
        val transactionList = when (currentTabName) {
            "disbursement" -> cachedResponse?.disbursement
            "interest" -> cachedResponse?.interest
            "fees" -> cachedResponse?.fees
            "penalty" -> cachedResponse?.penalty
            "recovery" -> cachedResponse?.recovery
            else -> emptyList()
        }
        val records = transactionList?.mapIndexed { index, dto ->
            TransactionRecord(
                sNo = (index + 1).toString(),
                flowId = dto.tranId ?: "",
                flowDate = formatDate(dto.flowDate),
                flowCode = dto.flowCode ?: "",
                flowAmount = formatAmount(dto.tranAmt),
                accountNumber = dto.acctNum ?: "",
                accountName = dto.acctName ?: ""
            )
        } ?: emptyList()
        adapter.updateData(records)
    }

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            val type = when (currentTabName) {
                "disbursement" -> "disbursement"
                "interest" -> "interest"
                "fees" -> "fees"
                "penalty" -> "penalty"
                "recovery" -> "recovery"
                else -> "disbursement"
            }
            downloadExcel(type)
        }
    }

    private fun downloadExcel(type: String) {
        lifecycleScope.launch {
            try {
                val response = apiService.downloadExcel(type)
                if (response.isSuccessful && response.body() != null) {
                    saveExcelFile(response.body()!!, type)
                } else {
                    Toast.makeText(this@TransactionActivity, "Download failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TransactionActivity, "Download error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveExcelFile(body: ResponseBody, type: String) {
        try {
            val file = File(getExternalFilesDir(null), "${type}_${System.currentTimeMillis()}.xlsx")
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            outputStream.close()
            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            // Optional: open file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    androidx.core.content.FileProvider.getUriForFile(
                        this@TransactionActivity,
                        "${packageName}.fileprovider",
                        file
                    ),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun formatAmount(amount: Double?): String {
        return if (amount != null) String.format("%,.2f", amount) else "0.00"
    }
}
