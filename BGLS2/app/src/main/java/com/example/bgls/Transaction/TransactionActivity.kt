package com.example.bgls.Transaction

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TransactionMigrationResponse
import com.example.bgls.DataModels.TransactionRecord
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TransactionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionRecordAdapter
    private lateinit var btnDownload: Button
    private lateinit var spinnerFilter: Spinner
    private lateinit var progressBar: ProgressBar   // add this to your XML

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

        apiService = RetrofitClient.api
        fetchAllTransactionData()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewTransactions)
        btnDownload = findViewById(R.id.btnDownload)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        progressBar = findViewById(R.id.progressBar)   // make sure you have this in XML

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
            btnDownload.text = "Export Disbursement"
        }
        tabInterest.setOnClickListener {
            setActiveTab(tabInterest, tabs)
            currentFlowCode = "INDEM"
            currentTabName = "interest"
            loadDataForCurrentTab()
            btnDownload.text = "Export Interest"
        }
        tabFees.setOnClickListener {
            setActiveTab(tabFees, tabs)
            currentFlowCode = "FEEDEM"
            currentTabName = "fees"
            loadDataForCurrentTab()
            btnDownload.text = "Export Fees"
        }
        tabPenalty.setOnClickListener {
            setActiveTab(tabPenalty, tabs)
            currentFlowCode = "PENDEM"
            currentTabName = "penalty"
            loadDataForCurrentTab()
            btnDownload.text = "Export Penalty"
        }
        tabRecovery.setOnClickListener {
            setActiveTab(tabRecovery, tabs)
            currentFlowCode = "COLL"
            currentTabName = "recovery"
            loadDataForCurrentTab()
            btnDownload.text = "Export Recovery"
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
        progressBar.visibility = android.view.View.VISIBLE
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
            } finally {
                progressBar.visibility = android.view.View.GONE
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
            exportCurrentTabToCsv()
        }
    }

    /**
     * Export the currently displayed transaction records as a CSV file.
     */
    private fun exportCurrentTabToCsv() {
        val records = adapter.getCurrentList()   // you need to expose this in adapter
        if (records.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val fileName = "${currentTabName}_$timestamp.csv"

        val csvContent = StringBuilder()
        // Header
        csvContent.append("S.No,Flow ID,Flow Date,Flow Code,Flow Amount,Account Number,Account Name\n")
        // Rows
        records.forEach { record ->
            csvContent.append("${record.sNo},")
                .append("${escapeCsv(record.flowId)},")
                .append("${escapeCsv(record.flowDate)},")
                .append("${escapeCsv(record.flowCode)},")
                .append("${record.flowAmount},")
                .append("${escapeCsv(record.accountNumber)},")
                .append("${escapeCsv(record.accountName)}\n")
        }

        try {
            val file = File(getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { fos ->
                fos.write(csvContent.toString().toByteArray())
            }
            Toast.makeText(this, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Optional: open file with a CSV viewer
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    androidx.core.content.FileProvider.getUriForFile(
                        this@TransactionActivity,
                        "${packageName}.fileprovider",
                        file
                    ),
                    "text/csv"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open CSV"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null || dateStr == "null" || dateStr.isEmpty()) return ""
        try {
            if (dateStr.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) return dateStr
            val timestamp = dateStr.toLongOrNull()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }
            val cleanDate = if (dateStr.contains("T")) dateStr.substringBefore("T")
            else if (dateStr.contains(" ")) dateStr.substringBefore(" ")
            else dateStr
            val parts = cleanDate.split("-")
            if (parts.size == 3 && parts[0].length == 4) {
                return "${parts[2]}-${parts[1]}-${parts[0]}"
            }
        } catch (e: Exception) { }
        return dateStr
    }

    private fun formatAmount(amount: Double?): String {
        return if (amount != null) String.format("%,.2f", amount) else "0.00"
    }
}