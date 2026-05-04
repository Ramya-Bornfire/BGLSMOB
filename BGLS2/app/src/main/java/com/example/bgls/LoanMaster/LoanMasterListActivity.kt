package com.example.bgls.LoanMaster

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
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.DataModels.LoanMaster
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class LoanMasterListActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_loan_master_list)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

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
        setupSearch()
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
        etSearchFilter = findViewById(R.id.etSearchFilter)
        progressBar    = findViewById(R.id.progressBar)
    }

    private fun setupSpinners() {
        // Filter spinner
        val filterOptions = listOf("Select Filter", "Loan Id", "Loan Type", "Mobile No")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        // Status spinner – matches web's status options
        val statusOptions = listOf("Select Status", "ACTIVE", "ACTIVE_IN_ARREARS", "APPROVED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // When a filter is selected, show/hide the search box
        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    etSearchFilter.visibility = View.GONE
                    etSearchFilter.setText("")
                    loadLoansFromApi(1)
                } else {
                    etSearchFilter.visibility = View.VISIBLE
                    etSearchFilter.hint = filterOptions[pos]
                    etSearchFilter.setText("")
                    etSearchFilter.requestFocus()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Filter by status when changed
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

    private fun setupSearch() {
        etSearchFilter.setOnEditorActionListener { _, _, _ ->
            val query = etSearchFilter.text.toString().trim()
            if (query.isEmpty()) {
                loadLoansFromApi(1)
                return@setOnEditorActionListener true
            }

            when (spinnerFilter.selectedItemPosition) {
                1 -> searchByLoanId(query)      // Loan Id
                2 -> searchByLoanType(query)    // Loan Type
                3 -> searchByMobile(query)      // Mobile No
            }
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = LoanMasterAdapter(this, emptyList()) { loan ->
            // Navigate to Loan Detail View
            val intent = Intent(this, LoanMasterViewActivity::class.java)
            intent.putExtra("loanId", loan.id ?: "")
            intent.putExtra("holderKey", loan.accountHolderKey ?: "")
            intent.putExtra("branchKey", loan.assignedBranchKey ?: "")
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
                        adapter.updateList(body.data)
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

    private fun searchByLoanId(loanId: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchLoanById(loanId)
                handleSearchResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                showNoData()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun searchByLoanType(loanType: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchLoanByType(loanType)
                handleSearchResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                showNoData()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun searchByMobile(mobileNumber: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchLoanByMobile(mobileNumber)
                handleSearchResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
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
                adapter.updateList(list)
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
        adapter.updateList(emptyList())
        Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updatePaginationUI() {
        tvPageInfo.text = "Page $currentPage of $totalPages"
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