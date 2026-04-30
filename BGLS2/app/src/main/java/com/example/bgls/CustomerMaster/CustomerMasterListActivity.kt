package com.example.bgls.CustomerMaster

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomerMasterListActivity : AppCompatActivity() {

    // ─── Views ───
    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var etSearch: EditText
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: CustomerMasterAdapter

    // ─── Pagination state (server-side) ───
    private val pageLimit = 200
    private var currentPage = 1
    private var totalPages = 1

    // ─── Search debounce ───
    private var searchJob: Job? = null

    // ─── Filter state ───
    private var selectedFilter = "Select Filter"   // Customer Id / Mobile No / Email
    private var selectedStatus = "Select Status"   // ACTIVE / INACTIVE / PENDING

    // ─── Filter options (must match spinner positions) ───
    private val filterOptions = listOf("Select Filter", "Customer Id", "Mobile No", "Email")
    private val statusOptions = listOf("Select Status", "ACTIVE", "INACTIVE", "BLACKLISTED", "EXITED", "PENDING_APPROVAL", "REJECTED")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master_list)

        initViews()
        setupSpinners()
        setupSearchBox()
        setupRecyclerView()
        setupPagination()
        setupDownload()

        // Initial load – all customers, page 1
        loadPage(1)
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        etSearch      = findViewById(R.id.etSearch)
        btnDownload   = findViewById(R.id.btnDownload)
        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        tvPageInfo    = findViewById(R.id.tvPageInfo)
        recyclerView  = findViewById(R.id.recyclerViewCustomers)
        progressBar   = findViewById(R.id.progressBar)
    }

    // ─── Spinners ────────────────────────────────────────────────────────────

    private fun setupSpinners() {
        // Filter spinner
        spinnerFilter.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, filterOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Status spinner
        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, statusOptions
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedFilter = filterOptions[pos]
                etSearch.hint = when (selectedFilter) {
                    "Customer Id" -> "Search by Customer ID…"
                    "Mobile No"   -> "Search by Mobile Number…"
                    "Email"       -> "Search by Email…"
                    else          -> "Select a filter above to search"
                }
                etSearch.isEnabled = selectedFilter != "Select Filter"
                etSearch.setText("")
                // Reset to full list when filter type changes
                if (selectedFilter == "Select Filter") loadPage(1)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedStatus = statusOptions[pos]
                val query = etSearch.text.toString().trim()
                if (query.isNotEmpty() && selectedFilter != "Select Filter") {
                    triggerSearch(query)
                } else if (selectedStatus != "Select Status") {
                    loadByStatus(selectedStatus)
                } else {
                    loadPage(1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ─── Search box (debounced 400 ms) ───────────────────────────────────────

    private fun setupSearchBox() {
        etSearch.isEnabled = false   // enabled once a filter is selected
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    loadPage(1)
                    return
                }
                searchJob = lifecycleScope.launch {
                    delay(400)            // debounce
                    triggerSearch(query)
                }
            }
        })
    }

    private fun triggerSearch(query: String) {
        val statusParam = if (selectedStatus == "Select Status") null else selectedStatus
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = when (selectedFilter) {
                    "Customer Id" -> RetrofitClient.api.searchCustomersById(query, statusParam)
                    "Mobile No"   -> RetrofitClient.api.searchCustomersByMobile(query, statusParam)
                    "Email"       -> RetrofitClient.api.searchCustomersByEmail(query, statusParam)
                    else          -> null
                }
                if (response != null && response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    fetchAndMapBranchNames(list)
                    tvPageInfo.text = "${list.size} result(s)"
                    btnPrev.isEnabled = false; btnPrev.alpha = 0.5f
                    btnNext.isEnabled = false; btnNext.alpha = 0.5f
                } else {
                    Toast.makeText(this@CustomerMasterListActivity,
                        "Search failed: ${response?.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMasterListActivity,
                    "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun loadByStatus(status: String) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.searchCustomersByStatus(status)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    fetchAndMapBranchNames(list)
                    tvPageInfo.text = "${list.size} result(s)"
                    btnPrev.isEnabled = false; btnPrev.alpha = 0.5f
                    btnNext.isEnabled = false; btnNext.alpha = 0.5f
                } else {
                    Toast.makeText(this@CustomerMasterListActivity,
                        "Filter failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMasterListActivity,
                    "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    // ─── Paginated full-list load ─────────────────────────────────────────────

    private fun loadPage(page: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAllApprovedCust(page, pageLimit)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentPage = body.currentPage
                        totalPages  = body.totalPages
                        fetchAndMapBranchNames(body.data)
                        tvPageInfo.text = "Page $currentPage of $totalPages"
                        updatePaginationButtons()
                    }
                } else {
                    Toast.makeText(this@CustomerMasterListActivity,
                        "Load failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMasterListActivity,
                    "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updatePaginationButtons() {
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha     = if (currentPage > 1) 1f else 0.5f
        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha     = if (currentPage < totalPages) 1f else 0.5f
    }

    private suspend fun fetchAndMapBranchNames(list: List<CustomerMaster>) {
        val uniqueBranchKeys = list.mapNotNull { it.branchKey }.toSet()
        val branchNameMap = mutableMapOf<String, String>()

        for (key in uniqueBranchKeys) {
            try {
                val res = RetrofitClient.api.getBranchNameByKey(key)
                if (res.isSuccessful) {
                    val responseStr = res.body()?.string()?.trim() ?: ""
                    // A valid branch name is short plain text.
                    // If the response contains HTML tags or is suspiciously long,
                    // the endpoint returned a login/error page instead – skip it.
                    val isValidBranchName = responseStr.isNotEmpty()
                            && !responseStr.contains("<", ignoreCase = false)
                            && responseStr.length < 200
                    if (isValidBranchName) {
                        branchNameMap[key] = responseStr
                    }
                }
            } catch (e: Exception) {
                // Ignore failure for individual branch names
            }
        }

        list.forEach {
            val resolvedName = branchNameMap[it.branchKey]
            it.branchName = if (!resolvedName.isNullOrEmpty()) resolvedName else "UNKNOWN"
        }
        adapter.updateList(list)
    }

    // ─── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = CustomerMasterAdapter(this, emptyList()) { customer ->
            val intent = Intent(this, CustomerMasterViewActivity::class.java).apply {
                putExtra("customerId",  customer.customerId  ?: "")
                putExtra("branchKey",   customer.branchKey   ?: "")
                // Pass cached fields so the view screen can display immediately
                // while the full-detail API call completes in the background.
                putExtra("customerName",         customer.customerName         ?: "")
                putExtra("firstName",            customer.firstName            ?: "")
                putExtra("lastName",             customer.lastName             ?: "")
                putExtra("gender",               customer.gender               ?: "")
                putExtra("dob",                  customer.dob                  ?: "")
                putExtra("branchName",           customer.branchName           ?: "")
                putExtra("clientRoleKey",        customer.clientRoleKey        ?: "")
                putExtra("creationDate",         customer.creationDate         ?: "")
                putExtra("approvalDate",         customer.approvalDate         ?: "")
                putExtra("lastModificationDate", customer.lastModificationDate ?: "")
                putExtra("activationDate",       customer.activationDate       ?: "")
                putExtra("mobileNo",             customer.mobileNo             ?: "")
                putExtra("email",                customer.email                ?: "")
                putExtra("address1",             customer.address1             ?: "")
                putExtra("address2",             customer.address2             ?: "")
                putExtra("city",                 customer.city                 ?: "")
                putExtra("suburb",               customer.suburb               ?: "")
                putExtra("loanCycle",            customer.loanCycle            ?: "")
                putExtra("groupLoanCycle",       customer.groupLoanCycle       ?: "")
                putExtra("assignedUser",         customer.assignedUser         ?: "")
            }
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // ─── Pagination buttons ───────────────────────────────────────────────────

    private fun setupPagination() {
        btnPrev.setOnClickListener {
            if (currentPage > 1) loadPage(currentPage - 1)
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) loadPage(currentPage + 1)
        }
    }

    // ─── Download ────────────────────────────────────────────────────────────

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            Toast.makeText(this, "Downloading customer list…", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Loading state ────────────────────────────────────────────────────────

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}