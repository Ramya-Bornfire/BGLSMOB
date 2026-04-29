package com.example.bgls.ChartOfAccounts

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TabChartModel
import com.example.bgls.DataModels.TabLedgerModel
import com.example.bgls.DataModels.TabTransactionModel
import com.example.bgls.R

class ChartOfAccountsActivity : AppCompatActivity() {

    private lateinit var btnTabChart: Button
    private lateinit var btnTabLedger: Button
    private lateinit var btnTabTransaction: Button

    private lateinit var scrollTabChart: HorizontalScrollView
    private lateinit var scrollTabLedger: HorizontalScrollView
    private lateinit var scrollTabTransaction: HorizontalScrollView

    private lateinit var rvTabChart: RecyclerView
    private lateinit var rvTabLedger: RecyclerView
    private lateinit var rvTabTransaction: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var spinnerOffice: Spinner
    private lateinit var tvMainTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart_of_accounts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerViews()
        setupTabs()
        setupActionButtons()
        
        // Initial state
        selectTab(btnTabChart, scrollTabChart)
    }

    private fun initViews() {
        btnTabChart = findViewById(R.id.btnTabChart)
        btnTabLedger = findViewById(R.id.btnTabLedger)
        btnTabTransaction = findViewById(R.id.btnTabTransaction)

        scrollTabChart = findViewById(R.id.scrollTabChart)
        scrollTabLedger = findViewById(R.id.scrollTabLedger)
        scrollTabTransaction = findViewById(R.id.scrollTabTransaction)

        rvTabChart = findViewById(R.id.rvTabChart)
        rvTabLedger = findViewById(R.id.rvTabLedger)
        rvTabTransaction = findViewById(R.id.rvTabTransaction)
        btnAdd=findViewById<Button>(R.id.btnAdd)
        spinnerOffice = findViewById(R.id.spinnerOffice)
        tvMainTitle = findViewById(R.id.tvMainTitle)
    }

    private fun setupRecyclerViews() {
        // Initialize adapters with empty lists, data will be loaded dynamically based on Spinner selection
        rvTabChart.layoutManager = LinearLayoutManager(this)
        rvTabChart.adapter = TabChartAdapter(this, emptyList())

        rvTabLedger.layoutManager = LinearLayoutManager(this)
        rvTabLedger.adapter = TabLedgerAdapter(this, emptyList())

        rvTabTransaction.layoutManager = LinearLayoutManager(this)
        rvTabTransaction.adapter = TabTransactionAdapter(this, emptyList())
    }

    private var activeTabId: Int = R.id.btnTabChart

    private fun setupActionButtons() {
        btnAdd.setOnClickListener {
            if (activeTabId == R.id.btnTabTransaction) {
                val intent = android.content.Intent(this, TransactionAccountAddActivity::class.java)
                startActivity(intent)
            } else {
                val intent = android.content.Intent(this, ChartOfAccountsAddActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupTabs() {
        btnTabChart.setOnClickListener { selectTab(btnTabChart, scrollTabChart) }
        btnTabLedger.setOnClickListener { selectTab(btnTabLedger, scrollTabLedger) }
        btnTabTransaction.setOnClickListener { selectTab(btnTabTransaction, scrollTabTransaction) }
    }

    private fun selectTab(selectedButton: Button, selectedScroll: HorizontalScrollView) {
        activeTabId = selectedButton.id
        
        // Reset all buttons
        val buttons = listOf(btnTabChart, btnTabLedger, btnTabTransaction)
        for (btn in buttons) {
            btn.setBackgroundResource(R.drawable.tab_unselected)
            btn.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.cyanblue))
        }

        // Reset all scroll views
        val scrolls = listOf(scrollTabChart, scrollTabLedger, scrollTabTransaction)
        for (scroll in scrolls) {
            scroll.visibility = View.GONE
        }

        // Highlight selected
        selectedButton.setBackgroundResource(R.drawable.tab_selected)
        selectedButton.setTextColor(Color.WHITE)
        selectedScroll.visibility = View.VISIBLE

        // Handle Add Button, Spinner, and Title visibility/text
        when (selectedButton.id) {
            R.id.btnTabChart -> {
                btnAdd.visibility = View.VISIBLE
                spinnerOffice.visibility = View.VISIBLE
                tvMainTitle.text = "CHART OF ACCOUNTS"
            }
            R.id.btnTabLedger -> {
                btnAdd.visibility = View.GONE
                spinnerOffice.visibility = View.VISIBLE
                tvMainTitle.text = "ACCOUNT LEDGER"
            }
            R.id.btnTabTransaction -> {
                btnAdd.visibility = View.VISIBLE
                spinnerOffice.visibility = View.GONE // Hide dropdown for Transaction tab
                tvMainTitle.text = "TRANSACTION ACCOUNTS"
            }
        }

        // Update dropdown logic dynamically based on active tab
        if (selectedButton.id != R.id.btnTabTransaction) {
            updateSpinnerForTab(selectedButton.id)
        } else {
            // Even if dropdown is hidden, we need to load data for the Transaction tab
            loadDataForTab(selectedButton.id, "Default")
        }
    }

    private fun updateSpinnerForTab(tabId: Int) {
        val options = when (tabId) {
            R.id.btnTabChart -> listOf("Office", "Customer", "Mirror")
            R.id.btnTabLedger -> listOf("Office", "Customer")
            R.id.btnTabTransaction -> listOf("Transaction Option 1", "Transaction Option 2")
            else -> listOf("Office", "Customer", "Mirror")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Temporarily remove listener to avoid rapid re-firing during setup
        spinnerOffice.onItemSelectedListener = null
        spinnerOffice.adapter = adapter

        spinnerOffice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = options[position]
                loadDataForTab(tabId, selectedOption)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Initial load for this tab's default selection (Office is index 0)
        loadDataForTab(tabId, options[0])
    }

    private fun loadDataForTab(tabId: Int, selectedOption: String) {
        when (tabId) {
            R.id.btnTabChart -> {
                val data = if (selectedOption == "Customer") {
                    listOf(
                        TabChartModel("Asset", "2000", "CUST", "2100001110", "CUSTOMER LOAN ACC", "KES", "500.00", "0.00", "500.00", "Active"),
                        TabChartModel("Asset", "2000", "CUST", "2100001120", "CUSTOMER SAVINGS ACC", "KES", "0.00", "100.00", "100.00", "Active")
                    )
                } else if (selectedOption == "Mirror") {
                    listOf(
                        TabChartModel("Asset", "3000", "MIRR", "3100001110", "MIRROR ACC 1", "KES", "0.00", "0.00", "0.00", "Active")
                    )
                } else {
                    // Default Office Data
                    listOf(
                        TabChartModel("Asset", "1000", "OAGEN", "1100001110", "BANK ACCOUNT", "KES", "0.00", "0.00", "0.00", "Active"),
                        TabChartModel("Asset", "1000", "OAGEN", "1100001120", "CASH ON HAND", "KES", "0.00", "0.00", "0.00", "Active"),
                        TabChartModel("Asset", "1000", "OAGEN", "1100001130", "PETTY CASH", "KES", "0.00", "0.00", "0.00", "Active"),
                        TabChartModel("Asset", "129", "", "1291100001", "Interest Receivable-Consumer Credit New Client", "KES", "0.00", "0.00", "0.00", "Active"),
                        TabChartModel("Asset", "129", "", "1291200001", "Interest Receivable-Consumer Credit Repeat Client", "KES", "0.00", "0.00", "0.00", "Active")
                    )
                }
                (rvTabChart.adapter as? TabChartAdapter)?.updateList(data)
            }
            R.id.btnTabLedger -> {
                val data = if (selectedOption == "Customer") {
                    listOf(
                        TabLedgerModel("INCOME", "CUST_ACC_001", "Customer Interest Income", "KES", "15000", "2000", "CR", "Active")
                    )
                } else {
                    listOf(
                        TabLedgerModel("ASSET", "OFF_ACC_001", "Office Cash Account", "KES", "10000", "5000", "CR", "Active"),
                        TabLedgerModel("LIABILITY", "OFF_ACC_002", "Office Loan Account", "KES", "2000", "8000", "DR", "Active")
                    )
                }
                (rvTabLedger.adapter as? TabLedgerAdapter)?.updateList(data)
            }
            R.id.btnTabTransaction -> {
                val data = if (selectedOption == "Transaction Option 2") {
                    listOf(
                        TabTransactionModel("3", "Transfer", "ACC003", "Customer Account", "ACC004", "Vendor Account", "Fund Transfer", "TR")
                    )
                } else {
                    listOf(
                        TabTransactionModel("1", "Deposit", "ACC001", "Cash Account", "ACC002", "Bank Account", "Cash Deposit", "CR"),
                        TabTransactionModel("2", "Withdrawal", "ACC002", "Bank Account", "ACC001", "Cash Account", "ATM Withdrawal", "DR")
                    )
                }
                (rvTabTransaction.adapter as? TabTransactionAdapter)?.updateList(data)
            }
        }
    }
}