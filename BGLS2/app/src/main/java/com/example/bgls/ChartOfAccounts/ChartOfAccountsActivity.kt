package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chart_of_accounts)

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