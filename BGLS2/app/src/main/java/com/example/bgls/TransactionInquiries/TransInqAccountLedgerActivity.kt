package com.example.bgls.TransactionInquiries

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.AccountLedgerActivity
import com.example.bgls.DataModels.ChartAccountItem
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class TransInqAccountLedgerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AccountLedger"
    }

    // UI Components
    private lateinit var rvTransLedger: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var btnFilter: Button
    private lateinit var headerRow: View
    private lateinit var filterRow: View

    // Filter EditTexts
    private lateinit var etFilterHead: EditText
    private lateinit var etFilterAcctId: EditText
    private lateinit var etFilterAcctName: EditText
    private lateinit var etFilterCurrency: EditText
    private lateinit var etFilterCredits: EditText
    private lateinit var etFilterDebits: EditText
    private lateinit var etFilterBalance: EditText
    private lateinit var etFilterStatus: EditText

    // Data
    private lateinit var adapter: AccountLedgerAdapter
    private val fullList = mutableListOf<ChartAccountItem>()
    private var isFilterVisible = false
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trans_account_ledger)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupClickListeners()
        loadData()
    }

    private fun initViews() {
        // RecyclerView
        rvTransLedger = findViewById(R.id.rvTransLedger)
        rvTransLedger.layoutManager = LinearLayoutManager(this)

        // Progress and status
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)

        // Navigation
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)

        // Filter components
        btnFilter = findViewById(R.id.btnFilter)
        headerRow = findViewById(R.id.headerRow)
        filterRow = findViewById(R.id.filterRow)

        // Filter EditTexts
        etFilterHead = findViewById(R.id.etFilterHead)
        etFilterAcctId = findViewById(R.id.etFilterAcctId)
        etFilterAcctName = findViewById(R.id.etFilterAcctName)
        etFilterCurrency = findViewById(R.id.etFilterCurrency)
        etFilterCredits = findViewById(R.id.etFilterCredits)
        etFilterDebits = findViewById(R.id.etFilterDebits)
        etFilterBalance = findViewById(R.id.etFilterBalance)
        etFilterStatus = findViewById(R.id.etFilterStatus)

        // Setup IME action for search on keyboard
        setupImeActions()

        // Setup user info
        setupUserInfo()

        // Initialize adapter
        adapter = AccountLedgerAdapter(emptyList()) { item ->
            navigateToLedgerDetail(item)
        }
        rvTransLedger.adapter = adapter
    }

    private fun setupImeActions() {
        // Use TextView.OnEditorActionListener
        val onSearchListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilters()
                true
            } else false
        }

        etFilterHead.setOnEditorActionListener(onSearchListener)
        etFilterAcctId.setOnEditorActionListener(onSearchListener)
        etFilterAcctName.setOnEditorActionListener(onSearchListener)
        etFilterCurrency.setOnEditorActionListener(onSearchListener)
        etFilterCredits.setOnEditorActionListener(onSearchListener)
        etFilterDebits.setOnEditorActionListener(onSearchListener)
        etFilterBalance.setOnEditorActionListener(onSearchListener)
        etFilterStatus.setOnEditorActionListener(onSearchListener)
    }

    private fun setupUserInfo() {
        try {
            val currentTime = java.text.SimpleDateFormat(
                "dd-MM-yyyy HH:mm", java.util.Locale.getDefault()
            ).format(java.util.Date())
            findViewById<TextView>(R.id.txtLoginTimeInfo).text = currentTime
        } catch (e: Exception) {
            // Use default values
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener { finish() }

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        btnFilter.setOnClickListener {
            toggleFilterVisibility()
        }
    }

    private fun toggleFilterVisibility() {
        isFilterVisible = !isFilterVisible
        headerRow.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        filterRow.visibility = if (isFilterVisible) View.VISIBLE else View.GONE

        if (isFilterVisible) {
            attachFilterListeners()
            // Request focus on first filter field
            etFilterHead.postDelayed({
                etFilterHead.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etFilterHead, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        } else {
            clearAllFilters()
            // Hide keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
        }
    }

    private val filterTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isDataLoaded) {
                applyFilters()
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    private fun attachFilterListeners() {
        // Remove existing listeners to avoid duplicate calls, then add
        etFilterHead.removeTextChangedListener(filterTextWatcher)
        etFilterHead.addTextChangedListener(filterTextWatcher)

        etFilterAcctId.removeTextChangedListener(filterTextWatcher)
        etFilterAcctId.addTextChangedListener(filterTextWatcher)

        etFilterAcctName.removeTextChangedListener(filterTextWatcher)
        etFilterAcctName.addTextChangedListener(filterTextWatcher)

        etFilterCurrency.removeTextChangedListener(filterTextWatcher)
        etFilterCurrency.addTextChangedListener(filterTextWatcher)

        etFilterCredits.removeTextChangedListener(filterTextWatcher)
        etFilterCredits.addTextChangedListener(filterTextWatcher)

        etFilterDebits.removeTextChangedListener(filterTextWatcher)
        etFilterDebits.addTextChangedListener(filterTextWatcher)

        etFilterBalance.removeTextChangedListener(filterTextWatcher)
        etFilterBalance.addTextChangedListener(filterTextWatcher)

        etFilterStatus.removeTextChangedListener(filterTextWatcher)
        etFilterStatus.addTextChangedListener(filterTextWatcher)
    }

    private fun applyFilters() {
        adapter.applyFilters(
            head = etFilterHead.text.toString(),
            acctId = etFilterAcctId.text.toString(),
            acctName = etFilterAcctName.text.toString(),
            currency = etFilterCurrency.text.toString(),
            credits = etFilterCredits.text.toString(),
            debits = etFilterDebits.text.toString(),
            balance = etFilterBalance.text.toString(),
            status = etFilterStatus.text.toString()
        )
    }

    private fun clearAllFilters() {
        etFilterHead.text.clear()
        etFilterAcctId.text.clear()
        etFilterAcctName.text.clear()
        etFilterCurrency.text.clear()
        etFilterCredits.text.clear()
        etFilterDebits.text.clear()
        etFilterBalance.text.clear()
        etFilterStatus.text.clear()
    }

    private fun loadData() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.filterChartOfAccountsSuspend(type = "O")

                if (response.isSuccessful && response.body() != null) {
                    val accounts = response.body()!!
                    handleDataLoaded(accounts)
                } else {
                    handleError("API Error: ${response.code()}")
                }
            } catch (e: Exception) {
                handleError("Network error: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleDataLoaded(accounts: List<ChartAccountItem>) {
        fullList.clear()
        fullList.addAll(accounts)
        isDataLoaded = true

        if (fullList.isEmpty()) {
            showNoData("No accounts found")
        } else {
            hideNoData()
            adapter.setFullData(fullList)
        }
    }

    private fun handleError(message: String) {
        showNoData("Failed to load data")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLedgerDetail(item: ChartAccountItem) {
        val intent = Intent(this, AccountLedgerActivity::class.java).apply {
            putExtra("acct_num", item.acct_num ?: "")
        }
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showNoData(message: String) {
        tvNoData.visibility = View.VISIBLE
        tvNoData.text = message
        rvTransLedger.visibility = View.GONE
    }

    private fun hideNoData() {
        tvNoData.visibility = View.GONE
        rvTransLedger.visibility = View.VISIBLE
    }

    private fun parseAmount(value: String?): Double {
        if (value.isNullOrBlank()) return 0.0
        return try {
            value.replace(",", "").toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    // ==================== ADAPTER CLASS ====================

    inner class AccountLedgerAdapter(
        private var items: List<ChartAccountItem>,
        private val onItemClick: (ChartAccountItem) -> Unit
    ) : RecyclerView.Adapter<AccountLedgerAdapter.ViewHolder>() {

        private val decimalFormat = DecimalFormat("#,##0.00")
        private var allItems = mutableListOf<ChartAccountItem>()

        fun setFullData(data: List<ChartAccountItem>) {
            allItems.clear()
            allItems.addAll(data)
            items = data.toList()
            notifyDataSetChanged()
        }

        fun applyFilters(
            head: String,
            acctId: String,
            acctName: String,
            currency: String,
            credits: String,
            debits: String,
            balance: String,
            status: String
        ) {
            val filtered = allItems.filter { item ->
                matchFilter(item, head, acctId, acctName, currency, credits, debits, balance, status)
            }

            items = filtered
            notifyDataSetChanged()
            updateEmptyState(filtered.isEmpty())
        }

        private fun matchFilter(
            item: ChartAccountItem,
            head: String,
            acctId: String,
            acctName: String,
            currency: String,
            credits: String,
            debits: String,
            balance: String,
            status: String
        ): Boolean {
            // Head/Classification filter
            if (head.isNotBlank()) {
                val itemHead = (item.classification ?: "Asset").trim()
                if (!itemHead.contains(head.trim(), ignoreCase = true)) return false
            }

            // Account ID filter
            if (acctId.isNotBlank()) {
                val itemAcctId = (item.acct_num ?: "").trim()
                if (!itemAcctId.contains(acctId.trim(), ignoreCase = true)) return false
            }

            // Account Name filter
            if (acctName.isNotBlank()) {
                val itemAcctName = (item.acct_name ?: "").trim()
                if (!itemAcctName.contains(acctName.trim(), ignoreCase = true)) return false
            }

            // Currency filter
            if (currency.isNotBlank()) {
                val itemCurrency = (item.acct_crncy ?: "KES").trim()
                if (!itemCurrency.contains(currency.trim(), ignoreCase = true)) return false
            }

            // Credits filter
            if (credits.isNotBlank()) {
                val itemCredits = parseAmount(item.cr_amt).toString()
                if (!itemCredits.contains(credits.trim(), ignoreCase = true)) return false
            }

            // Debits filter
            if (debits.isNotBlank()) {
                val itemDebits = parseAmount(item.dr_amt).toString()
                if (!itemDebits.contains(debits.trim(), ignoreCase = true)) return false
            }

            // Balance filter
            if (balance.isNotBlank()) {
                val itemBalance = parseAmount(item.acct_bal).toString()
                if (!itemBalance.contains(balance.trim(), ignoreCase = true)) return false
            }

            // Status filter
            if (status.isNotBlank()) {
                val itemStatus = if (item.entity_flg == "Y") "Active" else "Inactive"
                if (!itemStatus.contains(status.trim(), ignoreCase = true)) return false
            }

            return true
        }

        private fun updateEmptyState(isEmpty: Boolean) {
            if (isEmpty && allItems.isNotEmpty()) {
                tvNoData.visibility = View.VISIBLE
                tvNoData.text = "No matching accounts found"
                rvTransLedger.visibility = View.GONE
            } else if (allItems.isEmpty()) {
                tvNoData.visibility = View.VISIBLE
                tvNoData.text = "No accounts found"
                rvTransLedger.visibility = View.GONE
            } else {
                tvNoData.visibility = View.GONE
                rvTransLedger.visibility = View.VISIBLE
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_trans_inq_ledger, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount(): Int = items.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvHead: TextView = itemView.findViewById(R.id.tvHead)
            private val tvAcctId: TextView = itemView.findViewById(R.id.tvAcctId)
            private val tvAcctName: TextView = itemView.findViewById(R.id.tvAcctName)
            private val tvCurrency: TextView = itemView.findViewById(R.id.tvCurrency)
            private val tvCredits: TextView = itemView.findViewById(R.id.tvCredits)
            private val tvDebits: TextView = itemView.findViewById(R.id.tvDebits)
            private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val ivAction: RadioButton = itemView.findViewById(R.id.ivAction)

            fun bind(item: ChartAccountItem, position: Int) {
                // Set text values
                tvHead.text = item.classification ?: "Asset"
                tvAcctId.text = item.acct_num ?: ""
                tvAcctName.text = item.acct_name ?: ""
                tvCurrency.text = item.acct_crncy ?: "KES"

                // Format numeric values
                val credits = parseAmount(item.cr_amt)
                tvCredits.text = decimalFormat.format(credits)

                val debits = parseAmount(item.dr_amt)
                tvDebits.text = decimalFormat.format(debits)

                val balance = parseAmount(item.acct_bal)
                tvBalance.text = decimalFormat.format(balance)
                tvBalance.setTextColor(if (balance < 0) Color.parseColor("#D32F2F") else Color.parseColor("#333333"))

                // Status with color
                val status = if (item.entity_flg == "Y") "Active" else "Inactive"
                tvStatus.text = status
                tvStatus.setTextColor(if (status == "Active") Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))

                // Zebra striping
                itemView.setBackgroundColor(if (position % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9"))

                // Click listeners - make entire item clickable
                ivAction.isChecked = false
                ivAction.isClickable = false
                itemView.setOnClickListener { onItemClick(item) }
                tvAcctId.setOnClickListener { onItemClick(item) }
                tvAcctName.setOnClickListener { onItemClick(item) }
            }
        }
    }
}