package com.example.bgls.CustomerMaster

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class CustomerMasterListActivity : AppCompatActivity() {

    // ─── Views ───
    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: CustomerMasterAdapter
    private lateinit var btnBack: ImageView

    // ─── Column-filter views ───
    private lateinit var layoutDefault: LinearLayout
    private lateinit var layoutFilter: LinearLayout

    private lateinit var tvCustomerId: TextView
    private lateinit var etCustomerId: EditText
    private lateinit var tvCustomerName: TextView
    private lateinit var etCustomerName: EditText
    private lateinit var tvBranchName: TextView
    private lateinit var etBranchName: EditText
    private lateinit var tvMobileNo: TextView
    private lateinit var etMobileNo: EditText
    private lateinit var tvEmail: TextView
    private lateinit var etEmail: EditText

    private lateinit var allTvs: List<TextView>
    private lateinit var allEts: List<EditText>

    // ─── Pagination state (server-side) ───
    private val pageSize = 200
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

    // ─── Column-filter state ───
    private var isFilterMode = false
    private var fullList: List<CustomerMaster> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        initViews()
        setupColumnFilter()
        setupSpinners()
        setupRecyclerView()
        setupPagination()
        setupDownload()

        // Initial load – all customers, page 1
        loadCustomersFromApi(1)
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnDownload   = findViewById(R.id.btnDownload)
        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        tvPageInfo    = findViewById(R.id.tvPageInfo)
        recyclerView  = findViewById(R.id.recyclerViewCustomers)
        progressBar   = findViewById(R.id.progressBar)

        layoutDefault   = findViewById(R.id.layoutDefaultHeader)
        layoutFilter    = findViewById(R.id.layoutFilterHeader)

        tvCustomerId   = findViewById(R.id.tvHdrCustomerId)
        etCustomerId   = findViewById(R.id.etFilterCustomerId)
        tvCustomerName = findViewById(R.id.tvHdrCustomerName)
        etCustomerName = findViewById(R.id.etFilterCustomerName)
        tvBranchName   = findViewById(R.id.tvHdrBranchName)
        etBranchName   = findViewById(R.id.etFilterBranchName)
        tvMobileNo     = findViewById(R.id.tvHdrMobileNo)
        etMobileNo     = findViewById(R.id.etFilterMobileNo)
        tvEmail        = findViewById(R.id.tvHdrEmail)
        etEmail        = findViewById(R.id.etFilterEmail)

        allTvs = listOf(tvCustomerId, tvCustomerName, tvBranchName, tvMobileNo, tvEmail)
        allEts = listOf(etCustomerId, etCustomerName, etBranchName, etMobileNo, etEmail)
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
        adapter.updateList(fullList)
        spinnerFilter.setSelection(0, false)
    }

    private fun setupColumnFilter() {
        // Column header click listeners
        tvCustomerId.setOnClickListener   { activateColumn(tvCustomerId,   etCustomerId) }
        tvCustomerName.setOnClickListener { activateColumn(tvCustomerName, etCustomerName) }
        tvBranchName.setOnClickListener   { activateColumn(tvBranchName,   etBranchName) }
        tvMobileNo.setOnClickListener     { activateColumn(tvMobileNo,     etMobileNo) }
        tvEmail.setOnClickListener        { activateColumn(tvEmail,        etEmail) }

        // TextWatcher on every filter EditText
        allEts.forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString().trim()
                    searchJob?.cancel()

                    // Determine if this EditText is tied to an API search filter
                    val apiSearchType = when (et) {
                        etCustomerId -> "Customer Id"
                        etMobileNo   -> "Mobile No"
                        etEmail      -> "Email"
                        else         -> null
                    }

                    if (query.isEmpty()) {
                        applyColumnFilters() // Local fallback if cleared
                    } else if (apiSearchType != null) {
                        // This column supports API search, trigger debounced API search
                        searchJob = lifecycleScope.launch {
                            delay(400) // Debounce 400ms
                            selectedFilter = apiSearchType
                            // Sync spinner to match column
                            val pos = filterOptions.indexOf(apiSearchType)
                            if (pos >= 0 && spinnerFilter.selectedItemPosition != pos) {
                                spinnerFilter.setSelection(pos, false)
                            }
                            triggerSearch(query)
                        }
                    } else {
                        // This column is local-only (Name, Branch)
                        applyColumnFilters()
                    }
                }
            })
        }
    }

    /** Applies client-side filtering across all active column EditTexts */
    private fun applyColumnFilters() {
        val qId     = etCustomerId.text.toString().trim().lowercase()
        val qName   = etCustomerName.text.toString().trim().lowercase()
        val qBranch = etBranchName.text.toString().trim().lowercase()
        val qMobile = etMobileNo.text.toString().trim().lowercase()
        val qEmail  = etEmail.text.toString().trim().lowercase()

        val filtered = fullList.filter { c ->
            (qId.isEmpty()     || (c.customerId   ?: "").lowercase().contains(qId))     &&
            (qName.isEmpty()   || (c.customerName ?: "").lowercase().contains(qName))   &&
            (qBranch.isEmpty() || (c.branchName   ?: "").lowercase().contains(qBranch)) &&
            (qMobile.isEmpty() || (c.mobileNo     ?: "").lowercase().contains(qMobile)) &&
            (qEmail.isEmpty()  || (c.email        ?: "").lowercase().contains(qEmail))
        }
        adapter.updateList(filtered)
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

                if (selectedFilter == "Select Filter") {
                    // Only clear and reload if we were previously in filter mode
                    if (isFilterMode) {
                        clearAllFilters()
                        loadCustomersFromApi(1)
                    }
                } else {
                    // Automatically activate the corresponding column header
                    when (selectedFilter) {
                        "Customer Id" -> activateColumn(tvCustomerId, etCustomerId)
                        "Mobile No"   -> activateColumn(tvMobileNo, etMobileNo)
                        "Email"       -> activateColumn(tvEmail, etEmail)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                selectedStatus = statusOptions[pos]
                
                // Determine active query from the active API column
                val query = when {
                    etCustomerId.visibility == View.VISIBLE && etCustomerId.text.isNotEmpty() -> etCustomerId.text.toString().trim()
                    etMobileNo.visibility   == View.VISIBLE && etMobileNo.text.isNotEmpty()   -> etMobileNo.text.toString().trim()
                    etEmail.visibility      == View.VISIBLE && etEmail.text.isNotEmpty()      -> etEmail.text.toString().trim()
                    else -> ""
                }

                if (query.isNotEmpty() && selectedFilter != "Select Filter") {
                    triggerSearch(query)
                } else if (selectedStatus != "Select Status") {
                    loadByStatus(selectedStatus)
                } else {
                    loadCustomersFromApi(1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // ─── API Search ─────────────────────────────────────────────────────────

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
                    currentPage = 1
                    totalPages = 1
                    updatePaginationUI()
                    tvPageInfo.text = "${list.size} result(s)"
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
                    currentPage = 1
                    totalPages = 1
                    updatePaginationUI()
                    tvPageInfo.text = "${list.size} result(s)"
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

    private fun loadCustomersFromApi(page: Int) {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAllApprovedCust(page, pageSize)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        currentPage = body.currentPage
                        totalPages  = body.totalPages
                        fetchAndMapBranchNames(body.data)
                        updatePaginationUI()
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

    private fun updatePaginationUI() {
        tvPageInfo.text = "Page $currentPage of $totalPages"
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
        fullList = list
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
            if (currentPage > 1) loadCustomersFromApi(currentPage - 1)
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) loadCustomersFromApi(currentPage + 1)
        }
    }

    // ─── Download (Excel via API) ─────────────────────────────────────────────

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            downloadCustomerExcel()
        }
    }

    private fun downloadCustomerExcel() {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = RetrofitClient.api.downloadExcel("CUSTOMER")
                if (response.isSuccessful && response.body() != null) {
                    saveExcelFile(response.body()!!, "CustomerMaster.xlsx")
                    Toast.makeText(this@CustomerMasterListActivity, "Download complete", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@CustomerMasterListActivity, "Download failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMasterListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveExcelFile(body: ResponseBody, fileName: String) {
        try {
            val file = File(getExternalFilesDir(null), fileName)
            // Write file using buffered stream (avoids loading whole file into memory)
            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Use the correct authority (match manifest)
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",   // ✅ fixed
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open Excel"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Loading state ────────────────────────────────────────────────────────

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}
