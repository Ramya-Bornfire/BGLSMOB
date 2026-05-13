package com.example.bgls.LoanMaintenance


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanMaster
import com.example.bgls.LoanMaster.LoanMasterAdapter
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class LoanMaintananceListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanMasterAdapter
    private lateinit var etSearchFilter: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    // ─── Pagination ───
    private val pageSize = 200
    private var currentPage = 1
    private var totalPages = 1

    private val TAG = "LoanMasterList"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_maintanance_list)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupSpinners()
        setupRecyclerView()
        setupPagination()
        setupDownload()
        setupColumnFilterLogic()
        loadLoansFromApi(1)
    }

    private fun initViews() {
        spinnerFilter  = findViewById(R.id.spinnerFilter)
        spinnerStatus  = findViewById(R.id.spinnerStatus)
        btnDownload    = findViewById(R.id.btnDownload)
        btnPrev        = findViewById(R.id.btnPrev)
        btnNext        = findViewById(R.id.btnNext)
        tvPageInfo     = findViewById(R.id.tvPageInfo)
        recyclerView   = findViewById(R.id.recyclerViewLoansList)
        progressBar    = findViewById(R.id.progressBar)
    }

    private var isFilterVisible = false
    private var allLoadedLoans: List<LoanMaster> = emptyList()

    private fun setupSpinners() {
        val filterOptions = listOf("Select Filter", "Loan Id", "Loan Type", "Mobile No")
        val filterAdapter = ArrayAdapter(this, R.layout.spinner_item_small, filterOptions)
        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_small)
        spinnerFilter.adapter = filterAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos > 0) {
                    showFilterHeader(true)
                    // Reset all first
                    findViewById<View>(R.id.etFilterLoanId).visibility = View.GONE
                    findViewById<View>(R.id.etFilterLoanType).visibility = View.GONE
                    findViewById<View>(R.id.etFilterLoanName).visibility = View.GONE
                    findViewById<View>(R.id.etFilterMobileNo).visibility = View.GONE

                    val targetEt = when (pos) {
                        1 -> findViewById<EditText>(R.id.etFilterLoanId)
                        2 -> findViewById<EditText>(R.id.etFilterLoanType)
                        3 -> findViewById<EditText>(R.id.etFilterMobileNo)
                        else -> null
                    }
                    targetEt?.apply {
                        visibility = View.VISIBLE
                        requestFocus()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val statusOptions = listOf("Select Status", "ACTIVE", "ACTIVE_IN_ARREARS", "APPROVED")
        val statusAdapter = ArrayAdapter(this, R.layout.spinner_item_small, statusOptions)
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_small)
        spinnerStatus.adapter = statusAdapter

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selected = statusOptions[pos]
                if (selected == "Select Status") {
                    loadLoansFromApi(1)
                } else {
                    searchByStatus(selected)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupColumnFilterLogic() {
        val filterIds = listOf(
            R.id.etFilterLoanId, R.id.etFilterLoanType,
            R.id.etFilterLoanName, R.id.etFilterMobileNo
        )

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        filterIds.forEach { id ->
            val et = findViewById<EditText>(id)
            et.addTextChangedListener(textWatcher)
            et.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || 
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    showFilterHeader(false)
                    true
                } else false
            }
        }
    }

    private fun showFilterHeader(show: Boolean) {
        isFilterVisible = show
        findViewById<View>(R.id.layoutDefaultHeader).visibility = if (show) View.GONE else View.VISIBLE
        findViewById<View>(R.id.layoutFilterHeader).visibility = if (show) View.VISIBLE else View.GONE
        if (!show) {
            // Reset spinner to "Select Filter"
            spinnerFilter.setSelection(0)
            // Hide all EditTexts
            findViewById<View>(R.id.etFilterLoanId).visibility = View.GONE
            findViewById<View>(R.id.etFilterLoanType).visibility = View.GONE
            findViewById<View>(R.id.etFilterLoanName).visibility = View.GONE
            findViewById<View>(R.id.etFilterMobileNo).visibility = View.GONE
        }
    }

    private fun clearFilters() {
        val filterIds = listOf(
            R.id.etFilterLoanId, R.id.etFilterLoanType,
            R.id.etFilterLoanName, R.id.etFilterMobileNo
        )
        filterIds.forEach { findViewById<EditText>(it).text.clear() }
    }

    private fun applyCombinedFilter() {
        val qId = findViewById<EditText>(R.id.etFilterLoanId).text.toString().trim()
        val qType = findViewById<EditText>(R.id.etFilterLoanType).text.toString().trim()
        val qName = findViewById<EditText>(R.id.etFilterLoanName).text.toString().trim()
        val qMobile = findViewById<EditText>(R.id.etFilterMobileNo).text.toString().trim()

        val filtered = allLoadedLoans.filter { item ->
            (qId.isEmpty() || (item.id ?: "").contains(qId, ignoreCase = true)) &&
            (qType.isEmpty() || (item.loanName ?: "").contains(qType, ignoreCase = true)) &&
            (qName.isEmpty() || item.customerName.contains(qName, ignoreCase = true)) &&
            (qMobile.isEmpty() || (item.mobilePhone ?: "").contains(qMobile, ignoreCase = true))
        }
        updateTable(filtered, isFiltering = true)
    }

    private fun setupRecyclerView() {
        adapter = LoanMasterAdapter(this, emptyList()) { loan ->
            // Navigate to Loan Detail View
            val intent = Intent(this, LoanMaintananceViewActivity::class.java)
            intent.putExtra("loanId", loan.id ?: "")
            intent.putExtra("holderKey", loan.accountHolderKey ?: "")
            intent.putExtra("branchKey", loan.assignedBranchKey ?: "")
            intent.putExtra("source", "LoanMaintenance")
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // ─── API CALLS ───

    private fun loadLoansFromApi(page: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoans(page = page, limit = pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentPage = body.currentPage
                        totalPages  = body.totalPages
                        allLoadedLoans = body.data
                        updateTable(allLoadedLoans)
                        updatePaginationUI()
                    } else {
                        showNoData()
                    }
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    showNoData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error", e)
                showNoData()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun searchByStatus(status: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchLoanByStatus(status)
                handleSearchResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Status search error", e)
                showNoData()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun handleSearchResponse(response: retrofit2.Response<List<LoanMaster>>) {
        if (response.isSuccessful) {
            val list = response.body()
            if (!list.isNullOrEmpty()) {
                allLoadedLoans = list
                updateTable(allLoadedLoans)
                // Hide pagination for search results
                totalPages = 1
                currentPage = 1
                updatePaginationUI()
            } else {
                showNoData()
            }
        } else {
            Log.e(TAG, "Search API error: ${response.code()}")
            showNoData()
        }
    }

    // ─── UI HELPERS ───

    private fun showNoData() {
        allLoadedLoans = emptyList()
        updateTable(emptyList())
        Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateTable(pageData: List<LoanMaster>, isFiltering: Boolean = false) {
        if (isFiltering) {
            tvPageInfo.text = "Showing ${pageData.size} results"
        } else {
            tvPageInfo.text = "Page $currentPage of $totalPages"
        }
        adapter.updateList(pageData)
    }

    private fun updatePaginationUI() {
        // tvPageInfo update moved to updateTable
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha = if (currentPage > 1) 1f else 0.5f
        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha = if (currentPage < totalPages) 1f else 0.5f
    }

    private fun setupPagination() {
        btnPrev.setOnClickListener {
            if (currentPage > 1) loadLoansFromApi(currentPage - 1)
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) loadLoansFromApi(currentPage + 1)
        }
    }

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            Toast.makeText(this, "Downloading loan list...", Toast.LENGTH_SHORT).show()
        }
    }
}