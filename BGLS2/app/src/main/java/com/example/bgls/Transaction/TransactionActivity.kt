package com.example.bgls.Transaction

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
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
    private lateinit var progressBar: ProgressBar

    private lateinit var tabDisbursement: TextView
    private lateinit var tabInterest: TextView
    private lateinit var tabFees: TextView
    private lateinit var tabPenalty: TextView
    private lateinit var tabRecovery: TextView

    // ─── Column-filter views ───
    private lateinit var layoutDefault: LinearLayout
    private lateinit var layoutFilter: LinearLayout

    private lateinit var tvFlowId: TextView
    private lateinit var etFlowId: EditText
    private lateinit var tvAccountNumber: TextView
    private lateinit var etAccountNumber: EditText
    private lateinit var tvAccountName: TextView
    private lateinit var etAccountName: EditText

    private lateinit var allTvs: List<TextView>
    private lateinit var allEts: List<EditText>

    private lateinit var apiService: ServiceApi
    private var currentFlowCode = "DISBT"
    private var currentTabName = "disbursement"
    private var cachedResponse: TransactionMigrationResponse? = null

    // ─── Filter State ───
    private var isFilterMode = false
    private var fullList: List<TransactionRecord> = emptyList()
    private val filterOptions = listOf("Select Filter", "Flow Id", "Account Number", "Account Name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        initViews()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        setupColumnFilter()
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
        progressBar = findViewById(R.id.progressBar)

        tabDisbursement = findViewById(R.id.tabDisbursement)
        tabInterest = findViewById(R.id.tabInterest)
        tabFees = findViewById(R.id.tabFees)
        tabPenalty = findViewById(R.id.tabPenalty)
        tabRecovery = findViewById(R.id.tabRecovery)

        layoutDefault  = findViewById(R.id.layoutDefaultHeader)
        layoutFilter   = findViewById(R.id.layoutFilterHeader)

        tvFlowId = findViewById(R.id.tvHdrFlowId)
        etFlowId = findViewById(R.id.etFilterFlowId)
        tvAccountNumber = findViewById(R.id.tvHdrAccountNumber)
        etAccountNumber = findViewById(R.id.etFilterAccountNumber)
        tvAccountName = findViewById(R.id.tvHdrAccountName)
        etAccountName = findViewById(R.id.etFilterAccountName)

        allTvs = listOf(tvFlowId, tvAccountNumber, tvAccountName)
        allEts = listOf(etFlowId, etAccountNumber, etAccountName)
    }

    // ─── Column-filter (dual header) ─────────────────────────────────────────

    private fun activateColumn(clickedTv: TextView?, clickedEt: EditText?) {
        isFilterMode = true
        layoutDefault.visibility = View.GONE
        layoutFilter.visibility  = View.VISIBLE

        allTvs.forEachIndexed { i, tv ->
            val et = allEts[i]
            if (tv === clickedTv) {
                tv.visibility = View.GONE
                et.visibility = View.VISIBLE
                et.requestFocus()
            } else {
                tv.visibility = View.VISIBLE
                et.visibility = View.GONE
                et.setText("")
            }
        }
        applyColumnFilters()
    }

    private fun clearAllFilters() {
        isFilterMode = false
        layoutDefault.visibility = View.VISIBLE
        layoutFilter.visibility  = View.GONE
        allEts.forEach { it.setText("") }
        allTvs.forEach { it.visibility = View.VISIBLE }
        adapter.updateData(fullList)
        spinnerFilter.setSelection(0, false)
    }

    private fun setupColumnFilter() {
        tvFlowId.setOnClickListener { activateColumn(tvFlowId, etFlowId) }
        tvAccountNumber.setOnClickListener { activateColumn(tvAccountNumber, etAccountNumber) }
        tvAccountName.setOnClickListener { activateColumn(tvAccountName, etAccountName) }

        allEts.forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    applyColumnFilters()
                }
            })
        }
    }

    private fun applyColumnFilters() {
        val qFlowId = etFlowId.text.toString().trim().lowercase()
        val qAccNum = etAccountNumber.text.toString().trim().lowercase()
        val qAccName = etAccountName.text.toString().trim().lowercase()

        val filtered = fullList.filter { c ->
            (qFlowId.isEmpty() || c.flowId.lowercase().contains(qFlowId)) &&
            (qAccNum.isEmpty() || c.accountNumber.lowercase().contains(qAccNum)) &&
            (qAccName.isEmpty() || c.accountName.lowercase().contains(qAccName))
        }
        adapter.updateData(filtered)
    }

    private fun setupSpinner() {
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedFilter = filterOptions[pos]
                if (selectedFilter == "Select Filter") {
                    if (isFilterMode) clearAllFilters()
                } else {
                    when (selectedFilter) {
                        "Flow Id" -> activateColumn(tvFlowId, etFlowId)
                        "Account Number" -> activateColumn(tvAccountNumber, etAccountNumber)
                        "Account Name" -> activateColumn(tvAccountName, etAccountName)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionRecordAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupTabs() {
        val tabs = listOf(tabDisbursement, tabInterest, tabFees, tabPenalty, tabRecovery)

        val tabClickListener = View.OnClickListener { v ->
            val clickedTab = v as TextView
            setActiveTab(clickedTab, tabs)
            
            when (clickedTab) {
                tabDisbursement -> {
                    currentFlowCode = "DISBT"
                    currentTabName = "disbursement"
                    btnDownload.text = "Export Disbursement"
                }
                tabInterest -> {
                    currentFlowCode = "INDEM"
                    currentTabName = "interest"
                    btnDownload.text = "Export Interest"
                }
                tabFees -> {
                    currentFlowCode = "FEEDEM"
                    currentTabName = "fees"
                    btnDownload.text = "Export Fees"
                }
                tabPenalty -> {
                    currentFlowCode = "PENDEM"
                    currentTabName = "penalty"
                    btnDownload.text = "Export Penalty"
                }
                tabRecovery -> {
                    currentFlowCode = "COLL"
                    currentTabName = "recovery"
                    btnDownload.text = "Export Recovery"
                }
            }
            
            // Clear filters when switching tabs to avoid confusion
            if (isFilterMode) {
                clearAllFilters()
            }
            loadDataForCurrentTab()
        }

        tabs.forEach { it.setOnClickListener(tabClickListener) }
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
        
        fullList = records
        // If there's an active filter, re-apply it instead of showing all, 
        // though our tab switcher clears it above anyway.
        if (isFilterMode) {
            applyColumnFilters()
        } else {
            adapter.updateData(fullList)
        }
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
        // Need to add getCurrentList() to adapter or just use fullList if we want to export all.
        // It's better to export the currently filtered list if adapter exposes it.
        val records = adapter.getCurrentList() // Ensure adapter has this method!
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
                .append("${record.flowAmount.replace(",", "")},") // Remove commas from formatted amount
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