package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
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
import com.example.bgls.DataModels.ChartAccountItem   // ✅ correct class
import com.example.bgls.DataModels.TabChartModel
import com.example.bgls.DataModels.TabLedgerModel
import com.example.bgls.DataModels.TabTransactionModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.material.navigation.NavigationView


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

    private var activeTabId: Int = R.id.btnTabChart

    private lateinit var chartAdapter: TabChartAdapter
    private lateinit var ledgerAdapter: TabLedgerAdapter
    private lateinit var transactionAdapter: TabTransactionAdapter
    private lateinit var btnFilter: Button
    private lateinit var filterRowChart: android.widget.LinearLayout
    private lateinit var filterRowLedger: android.widget.LinearLayout
    private lateinit var filterRowTransaction: android.widget.LinearLayout
    
    private lateinit var headerRowChart: android.widget.LinearLayout
    private lateinit var headerRowLedger: android.widget.LinearLayout
    private lateinit var headerRowTransaction: android.widget.LinearLayout

    // Chart Filters
    private lateinit var etFilterHead: android.widget.EditText
    private lateinit var etFilterGl: android.widget.EditText
    private lateinit var etFilterScheme: android.widget.EditText
    private lateinit var etFilterAcctId: android.widget.EditText
    private lateinit var etFilterAcctName: android.widget.EditText
    private lateinit var etFilterCurrency: android.widget.EditText
    private lateinit var etFilterCredits: android.widget.EditText
    private lateinit var etFilterDebits: android.widget.EditText
    private lateinit var etFilterBalance: android.widget.EditText
    private lateinit var etFilterStatus: android.widget.EditText

    // Ledger Filters
    private lateinit var etFilterLedgerHead: android.widget.EditText
    private lateinit var etFilterLedgerAcctId: android.widget.EditText
    private lateinit var etFilterLedgerAcctName: android.widget.EditText
    private lateinit var etFilterLedgerCurrency: android.widget.EditText
    private lateinit var etFilterLedgerCredits: android.widget.EditText
    private lateinit var etFilterLedgerDebits: android.widget.EditText
    private lateinit var etFilterLedgerBalance: android.widget.EditText
    private lateinit var etFilterLedgerStatus: android.widget.EditText

    // Transaction Filters
    private lateinit var etFilterTranId: android.widget.EditText
    private lateinit var etFilterTranEvent: android.widget.EditText
    private lateinit var etFilterTranDebitNo: android.widget.EditText
    private lateinit var etFilterTranDebitName: android.widget.EditText
    private lateinit var etFilterTranCreditNo: android.widget.EditText
    private lateinit var etFilterTranCreditName: android.widget.EditText
    private lateinit var etFilterTranParticular: android.widget.EditText
    private lateinit var etFilterTranType: android.widget.EditText
    
    private var isFilterVisible = false
    private var isFirstResume = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart_of_accounts)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        initViews()
        setupRecyclerViews()
        setupTabs()
        setupActionButtons()
        setupFilterActions()

        // Initial tab selection (optional)
        when (intent.getStringExtra("SELECT_TAB")) {
            "LEDGER" -> selectTab(btnTabLedger, scrollTabLedger)
            "TRANSACTION" -> selectTab(btnTabTransaction, scrollTabTransaction)
            else -> selectTab(btnTabChart, scrollTabChart)
        }
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
        btnAdd = findViewById(R.id.btnAdd)
        spinnerOffice = findViewById(R.id.spinnerOffice)
        tvMainTitle = findViewById(R.id.tvMainTitle)
        btnFilter = findViewById(R.id.btnFilter)
        
        filterRowChart = findViewById(R.id.filterRowChart)
        filterRowLedger = findViewById(R.id.filterRowLedger)
        filterRowTransaction = findViewById(R.id.filterRowTransaction)

        headerRowChart = findViewById(R.id.headerRowChart)
        headerRowLedger = findViewById(R.id.headerRowLedger)
        headerRowTransaction = findViewById(R.id.headerRowTransaction)

        // Initialize Chart Filters
        etFilterHead = findViewById(R.id.etFilterHead)
        etFilterGl = findViewById(R.id.etFilterGl)
        etFilterScheme = findViewById(R.id.etFilterScheme)
        etFilterAcctId = findViewById(R.id.etFilterAcctId)
        etFilterAcctName = findViewById(R.id.etFilterAcctName)
        etFilterCurrency = findViewById(R.id.etFilterCurrency)
        etFilterCredits = findViewById(R.id.etFilterCredits)
        etFilterDebits = findViewById(R.id.etFilterDebits)
        etFilterBalance = findViewById(R.id.etFilterBalance)
        etFilterStatus = findViewById(R.id.etFilterStatus)

        // Initialize Ledger Filters
        etFilterLedgerHead = findViewById(R.id.etFilterLedgerHead)
        etFilterLedgerAcctId = findViewById(R.id.etFilterLedgerAcctId)
        etFilterLedgerAcctName = findViewById(R.id.etFilterLedgerAcctName)
        etFilterLedgerCurrency = findViewById(R.id.etFilterLedgerCurrency)
        etFilterLedgerCredits = findViewById(R.id.etFilterLedgerCredits)
        etFilterLedgerDebits = findViewById(R.id.etFilterLedgerDebits)
        etFilterLedgerBalance = findViewById(R.id.etFilterLedgerBalance)
        etFilterLedgerStatus = findViewById(R.id.etFilterLedgerStatus)

        // Initialize Transaction Filters
        etFilterTranId = findViewById(R.id.etFilterTranId)
        etFilterTranEvent = findViewById(R.id.etFilterTranEvent)
        etFilterTranDebitNo = findViewById(R.id.etFilterTranDebitNo)
        etFilterTranDebitName = findViewById(R.id.etFilterTranDebitName)
        etFilterTranCreditNo = findViewById(R.id.etFilterTranCreditNo)
        etFilterTranCreditName = findViewById(R.id.etFilterTranCreditName)
        etFilterTranParticular = findViewById(R.id.etFilterTranParticular)
        etFilterTranType = findViewById(R.id.etFilterTranType)

        // Standardize all filter EditTexts to match ParameterActivity behavior
        val allFilters = listOf(
            etFilterHead, etFilterGl, etFilterScheme, etFilterAcctId, etFilterAcctName,
            etFilterCurrency, etFilterCredits, etFilterDebits, etFilterBalance, etFilterStatus,
            etFilterLedgerHead, etFilterLedgerAcctId, etFilterLedgerAcctName, etFilterLedgerCurrency,
            etFilterLedgerCredits, etFilterLedgerDebits, etFilterLedgerBalance, etFilterLedgerStatus,
            etFilterTranId, etFilterTranEvent, etFilterTranDebitNo, etFilterTranDebitName,
            etFilterTranCreditNo, etFilterTranCreditName, etFilterTranParticular, etFilterTranType
        )
        
        allFilters.forEach { et ->
            et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            et.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            et.setSingleLine(true)
        }
    }

    private fun setupRecyclerViews() {
        chartAdapter = TabChartAdapter(this, emptyList())
        ledgerAdapter = TabLedgerAdapter(this, emptyList())
        transactionAdapter = TabTransactionAdapter(this, emptyList())

        rvTabChart.layoutManager = LinearLayoutManager(this)
        rvTabChart.adapter = chartAdapter

        rvTabLedger.layoutManager = LinearLayoutManager(this)
        rvTabLedger.adapter = ledgerAdapter

        rvTabTransaction.layoutManager = LinearLayoutManager(this)
        rvTabTransaction.adapter = transactionAdapter
    }

    private fun setupActionButtons() {
        btnAdd.setOnClickListener {
            if (activeTabId == R.id.btnTabTransaction) {
                startActivity(Intent(this, TransactionAccountAddActivity::class.java))
            } else {
                startActivity(Intent(this, ChartOfAccountsAddActivity::class.java))
            }
        }
    }

    private fun setupFilterActions() {
        btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            updateFilterVisibility()
            if (!isFilterVisible) {
                clearAllFilters()
            } else {
                // Apply filters immediately when shown to respect any existing text
                applyChartFilters()
                applyLedgerFilters()
                applyTransactionFilters()
            }
        }

        // --- Chart Tab Filters ---
        val chartFilters = listOf(
            etFilterHead, etFilterGl, etFilterScheme, etFilterAcctId, etFilterAcctName,
            etFilterCurrency, etFilterCredits, etFilterDebits, etFilterBalance, etFilterStatus
        )
        chartFilters.forEach { et ->
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyChartFilters()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }

        // --- Ledger Tab Filters ---
        val ledgerFilters = listOf(
            etFilterLedgerHead, etFilterLedgerAcctId, etFilterLedgerAcctName, etFilterLedgerCurrency,
            etFilterLedgerCredits, etFilterLedgerDebits, etFilterLedgerBalance, etFilterLedgerStatus
        )
        ledgerFilters.forEach { et ->
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyLedgerFilters()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }

        // --- Transaction Tab Filters ---
        val tranFilters = listOf(
            etFilterTranId, etFilterTranEvent, etFilterTranDebitNo, etFilterTranDebitName,
            etFilterTranCreditNo, etFilterTranCreditName, etFilterTranParticular, etFilterTranType
        )
        tranFilters.forEach { et ->
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyTransactionFilters()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun updateFilterVisibility() {
        // Tab 1
        headerRowChart.visibility = if (isFilterVisible && activeTabId == R.id.btnTabChart) View.GONE else (if (activeTabId == R.id.btnTabChart) View.VISIBLE else View.GONE)
        filterRowChart.visibility = if (isFilterVisible && activeTabId == R.id.btnTabChart) View.VISIBLE else View.GONE

        // Tab 2
        headerRowLedger.visibility = if (isFilterVisible && activeTabId == R.id.btnTabLedger) View.GONE else (if (activeTabId == R.id.btnTabLedger) View.VISIBLE else View.GONE)
        filterRowLedger.visibility = if (isFilterVisible && activeTabId == R.id.btnTabLedger) View.VISIBLE else View.GONE

        // Tab 3
        headerRowTransaction.visibility = if (isFilterVisible && activeTabId == R.id.btnTabTransaction) View.GONE else (if (activeTabId == R.id.btnTabTransaction) View.VISIBLE else View.GONE)
        filterRowTransaction.visibility = if (isFilterVisible && activeTabId == R.id.btnTabTransaction) View.VISIBLE else View.GONE
    }

    private fun clearAllFilters() {
        val allFilters = listOf(
            R.id.etFilterHead, R.id.etFilterGl, R.id.etFilterScheme, R.id.etFilterAcctId, R.id.etFilterAcctName,
            R.id.etFilterCurrency, R.id.etFilterCredits, R.id.etFilterDebits, R.id.etFilterBalance, R.id.etFilterStatus,
            R.id.etFilterLedgerHead, R.id.etFilterLedgerAcctId, R.id.etFilterLedgerAcctName, R.id.etFilterLedgerCurrency,
            R.id.etFilterLedgerCredits, R.id.etFilterLedgerDebits, R.id.etFilterLedgerBalance, R.id.etFilterLedgerStatus,
            R.id.etFilterTranId, R.id.etFilterTranEvent, R.id.etFilterTranDebitNo, R.id.etFilterTranDebitName,
            R.id.etFilterTranCreditNo, R.id.etFilterTranCreditName, R.id.etFilterTranParticular, R.id.etFilterTranType
        )
        allFilters.forEach { id -> findViewById<android.widget.EditText>(id).text.clear() }
        
        chartAdapter.filter("", "", "", "", "", "", "", "", "", "")
        ledgerAdapter.filter("", "", "", "", "", "", "", "")
        transactionAdapter.filter("", "", "", "", "", "", "", "")
    }

    private fun applyChartFilters() {
        val f1 = etFilterHead.text.toString().trim()
        val f2 = etFilterGl.text.toString().trim()
        val f3 = etFilterScheme.text.toString().trim()
        val f4 = etFilterAcctId.text.toString().trim()
        val f5 = etFilterAcctName.text.toString().trim()
        val f6 = etFilterCurrency.text.toString().trim()
        val f7 = etFilterCredits.text.toString().trim()
        val f8 = etFilterDebits.text.toString().trim()
        val f9 = etFilterBalance.text.toString().trim()
        val f10 = etFilterStatus.text.toString().trim()

        chartAdapter.filter(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    private fun applyLedgerFilters() {
        val f1 = etFilterLedgerHead.text.toString().trim()
        val f2 = etFilterLedgerAcctId.text.toString().trim()
        val f3 = etFilterLedgerAcctName.text.toString().trim()
        val f4 = etFilterLedgerCurrency.text.toString().trim()
        val f5 = etFilterLedgerCredits.text.toString().trim()
        val f6 = etFilterLedgerDebits.text.toString().trim()
        val f7 = etFilterLedgerBalance.text.toString().trim()
        val f8 = etFilterLedgerStatus.text.toString().trim()

        ledgerAdapter.filter(f1, f2, f3, f4, f5, f6, f7, f8)
    }

    private fun applyTransactionFilters() {
        val f1 = etFilterTranId.text.toString().trim()
        val f2 = etFilterTranEvent.text.toString().trim()
        val f3 = etFilterTranDebitNo.text.toString().trim()
        val f4 = etFilterTranDebitName.text.toString().trim()
        val f5 = etFilterTranCreditNo.text.toString().trim()
        val f6 = etFilterTranCreditName.text.toString().trim()
        val f7 = etFilterTranParticular.text.toString().trim()
        val f8 = etFilterTranType.text.toString().trim()

        transactionAdapter.filter(f1, f2, f3, f4, f5, f6, f7, f8)
    }

    private fun setupTabs() {
        btnTabChart.setOnClickListener { selectTab(btnTabChart, scrollTabChart) }
        btnTabLedger.setOnClickListener { selectTab(btnTabLedger, scrollTabLedger) }
        btnTabTransaction.setOnClickListener { selectTab(btnTabTransaction, scrollTabTransaction) }
    }

    private fun selectTab(selectedButton: Button, selectedScroll: HorizontalScrollView) {
        activeTabId = selectedButton.id

        // Reset all tab buttons
        listOf(btnTabChart, btnTabLedger, btnTabTransaction).forEach { btn ->
            btn.setBackgroundResource(R.drawable.tab_unselected)
            btn.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.cyanblue))
        }

        // Hide all scroll views
        listOf(scrollTabChart, scrollTabLedger, scrollTabTransaction).forEach { scroll ->
            scroll.visibility = View.GONE
        }

        // Highlight selected
        selectedButton.setBackgroundResource(R.drawable.tab_selected)
        selectedButton.setTextColor(Color.WHITE)
        selectedScroll.visibility = View.VISIBLE
        
        updateFilterVisibility()

        // UI changes per tab
        when (selectedButton.id) {
            R.id.btnTabChart -> {
                btnAdd.visibility = View.VISIBLE
                spinnerOffice.visibility = View.VISIBLE
                tvMainTitle.text = "CHART OF ACCOUNTS"
                updateSpinnerForTab(R.id.btnTabChart)
            }
            R.id.btnTabLedger -> {
                btnAdd.visibility = View.GONE
                spinnerOffice.visibility = View.VISIBLE
                tvMainTitle.text = "ACCOUNT LEDGER"
                updateSpinnerForTab(R.id.btnTabLedger)
            }
            R.id.btnTabTransaction -> {
                btnAdd.visibility = View.VISIBLE
                spinnerOffice.visibility = View.GONE
                tvMainTitle.text = "TRANSACTION ACCOUNTS"
                loadTransactionAccounts()
            }
        }
    }

    private fun updateSpinnerForTab(tabId: Int) {
        val options = when (tabId) {
            R.id.btnTabChart -> listOf("Office", "Customer", "Mirror")
            R.id.btnTabLedger -> listOf("Office", "Customer")
            else -> emptyList()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOffice.onItemSelectedListener = null
        spinnerOffice.adapter = adapter

        spinnerOffice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = options[position]
                when (tabId) {
                    R.id.btnTabChart -> loadChartAccounts(selected)
                    R.id.btnTabLedger -> loadLedgerAccounts(selected)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Load initial data
        if (options.isNotEmpty()) {
            when (tabId) {
                R.id.btnTabChart -> loadChartAccounts(options[0])
                R.id.btnTabLedger -> loadLedgerAccounts(options[0])
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            return
        }
        refreshActiveTabData()
    }

    private fun refreshActiveTabData() {
        when (activeTabId) {
            R.id.btnTabChart -> {
                val selected = spinnerOffice.selectedItem?.toString() ?: "Office"
                loadChartAccounts(selected)
            }
            R.id.btnTabLedger -> {
                val selected = spinnerOffice.selectedItem?.toString() ?: "Office"
                loadLedgerAccounts(selected)
            }
            R.id.btnTabTransaction -> {
                loadTransactionAccounts()
            }
        }
    }

    // ------------------------------------------------------------
    // Chart of Accounts Tab
    // ------------------------------------------------------------
    private fun loadChartAccounts(selectedOption: String) {
        val type = when (selectedOption) {
            "Office" -> "O"
            "Customer" -> "C"
            "Mirror" -> "M"
            else -> "O"
        }

        RetrofitClient.api.filterChartOfAccounts(type)
            .enqueue(object : Callback<List<ChartAccountItem>> {
                override fun onResponse(call: Call<List<ChartAccountItem>>, response: Response<List<ChartAccountItem>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.map { item ->
                            TabChartModel(
                                head = item.classification ?: "",
                                gl = item.gl_code ?: "",              // ✅ GL code from API
                                schemeCode = item.schm_code ?: "",     // ✅ Scheme Code from API
                                acctId = item.acct_num ?: "",
                                acctName = item.acct_name ?: "",
                                currency = item.acct_crncy ?: "",
                                credits = item.cr_amt ?: "0.00",
                                debits = item.dr_amt ?: "0.00",
                                balance = item.acct_bal ?: "0.00",
                                status = if (item.entity_flg == "Y") "Active" else "Inactive"
                            )
                        }
                        chartAdapter.updateList(list)
                        applyChartFilters() // re-apply filters if any
                    } else {
                        Toast.makeText(this@ChartOfAccountsActivity, "Error loading chart accounts", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ChartAccountItem>>, t: Throwable) {
                    Toast.makeText(this@ChartOfAccountsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ------------------------------------------------------------
    // Account Ledger Tab
    // ------------------------------------------------------------
    private fun loadLedgerAccounts(selectedOption: String) {
        val type = when (selectedOption) {
            "Office" -> "O"
            "Customer" -> "C"
            else -> "O"
        }

        RetrofitClient.api.filterChartOfAccounts(type)
            .enqueue(object : Callback<List<ChartAccountItem>> {
                override fun onResponse(call: Call<List<ChartAccountItem>>, response: Response<List<ChartAccountItem>>) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.map { item ->
                            TabLedgerModel(
                                head = item.classification ?: "",
                                acctId = item.acct_num ?: "",
                                acctName = item.acct_name ?: "",
                                currency = item.acct_crncy ?: "",
                                credits = item.cr_amt ?: "0.00",
                                debits = item.dr_amt ?: "0.00",
                                balance = item.acct_bal ?: "0.00",
                                status = if (item.entity_flg == "Y") "Active" else "Inactive"
                            )
                        }
                        ledgerAdapter.updateList(list)
                        applyLedgerFilters() // re-apply filters if any
                    } else {
                        Toast.makeText(this@ChartOfAccountsActivity, "Error loading ledger accounts", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ChartAccountItem>>, t: Throwable) {
                    Toast.makeText(this@ChartOfAccountsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ------------------------------------------------------------
    // Transaction Accounts Tab
    // ------------------------------------------------------------
    private fun loadTransactionAccounts() {
        RetrofitClient.api.getTransactionAccountsList("list")
            .enqueue(object : Callback<com.example.bgls.DataModels.TransactionAccountsResponse> {
                override fun onResponse(
                    call: Call<com.example.bgls.DataModels.TransactionAccountsResponse>,
                    response: Response<com.example.bgls.DataModels.TransactionAccountsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val transactions = response.body()!!.list ?: emptyList()
                        val tabModels = transactions.map { item ->
                            TabTransactionModel(
                                id = item.id ?: "",
                                event = item.event ?: "",
                                debitAccNo = item.debitAccountNumber ?: "",
                                debitAccName = item.debitAccountName ?: "",
                                creditAccNo = item.creditAccountNumber ?: "",
                                creditAccName = item.creditAccountName ?: "",
                                tranParticular = item.tranParticular ?: "",
                                type = item.accountType ?: ""
                            )
                        }
                        transactionAdapter.updateList(tabModels)
                        applyTransactionFilters() // re-apply filters if any
                    } else {
                        Toast.makeText(this@ChartOfAccountsActivity, "Error loading transaction accounts", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<com.example.bgls.DataModels.TransactionAccountsResponse>, t: Throwable) {
                    Toast.makeText(this@ChartOfAccountsActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}