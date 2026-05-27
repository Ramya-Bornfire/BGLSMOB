package com.example.bgls.LoanMaster

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class LoanMasterListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanMasterAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView

    // ─── Column-filter views ───
    private lateinit var layoutDefault: LinearLayout
    private lateinit var layoutFilter: LinearLayout

    private lateinit var tvLoanId: TextView
    private lateinit var etLoanId: EditText
    private lateinit var tvLoanType: TextView
    private lateinit var etLoanType: EditText
    private lateinit var tvLoanName: TextView
    private lateinit var etLoanName: EditText
    private lateinit var tvMobileNo: TextView
    private lateinit var etMobileNo: EditText
    private lateinit var tvRetailerBranchId: TextView
    private lateinit var etRetailerBranchId: EditText

    private lateinit var allTvs: List<TextView>
    private lateinit var allEts: List<EditText>

    // ─── Pagination ───
    private val pageSize = 200
    private var currentPage = 1
    private var totalPages = 1

    // ─── Search debounce & Filter state ───
    private var searchJob: Job? = null
    private var isFilterMode = false
    private var fullList: List<LoanMaster> = emptyList()
    private val filterOptions = listOf("Select Filter", "Loan Id", "Loan Type", "Mobile No")
    private val statusOptions = listOf("Select Status", "ACTIVE", "ACTIVE_IN_ARREARS", "APPROVED")

    private val TAG = "LoanMasterList"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_master_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

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
        setupColumnFilter()
        setupSpinners()
        setupRecyclerView()
        setupPagination()
        setupDownload()
        
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

        layoutDefault  = findViewById(R.id.layoutDefaultHeader)
        layoutFilter   = findViewById(R.id.layoutFilterHeader)

        tvLoanId = findViewById(R.id.tvHdrLoanId)
        etLoanId = findViewById(R.id.etFilterLoanId)
        tvLoanType = findViewById(R.id.tvHdrLoanType)
        etLoanType = findViewById(R.id.etFilterLoanType)
        tvLoanName = findViewById(R.id.tvHdrLoanName)
        etLoanName = findViewById(R.id.etFilterLoanName)
        tvMobileNo = findViewById(R.id.tvHdrMobileNo)
        etMobileNo = findViewById(R.id.etFilterMobileNo)
        tvRetailerBranchId = findViewById(R.id.tvHdrRetailerBranchId)
        etRetailerBranchId = findViewById(R.id.etFilterRetailerBranchId)

        allTvs = listOf(tvLoanId, tvLoanType, tvLoanName, tvMobileNo, tvRetailerBranchId)
        allEts = listOf(etLoanId, etLoanType, etLoanName, etMobileNo, etRetailerBranchId)
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
        tvLoanId.setOnClickListener { activateColumn(tvLoanId, etLoanId) }
        tvLoanType.setOnClickListener { activateColumn(tvLoanType, etLoanType) }
        tvLoanName.setOnClickListener { activateColumn(tvLoanName, etLoanName) }
        tvMobileNo.setOnClickListener { activateColumn(tvMobileNo, etMobileNo) }
        tvRetailerBranchId.setOnClickListener { activateColumn(tvRetailerBranchId, etRetailerBranchId) }

        allEts.forEach { et ->
            et.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString().trim()
                    searchJob?.cancel()

                    val apiSearchType = when (et) {
                        etLoanId -> "Loan Id"
                        etLoanType -> "Loan Type"
                        etMobileNo -> "Mobile No"
                        else -> null
                    }

                    if (query.isEmpty()) {
                        applyColumnFilters()
                    } else if (apiSearchType != null) {
                        // Trigger debounced API search
                        searchJob = lifecycleScope.launch {
                            delay(400)
                            val pos = filterOptions.indexOf(apiSearchType)
                            if (pos >= 0 && spinnerFilter.selectedItemPosition != pos) {
                                spinnerFilter.setSelection(pos, false)
                            }
                            when (apiSearchType) {
                                "Loan Id" -> searchByLoanId(query)
                                "Loan Type" -> searchByLoanType(query)
                                "Mobile No" -> searchByMobile(query)
                            }
                        }
                    } else {
                        // Local filter only (Loan Name, Branch Id)
                        applyColumnFilters()
                    }
                }
            })
        }
    }

    private fun applyColumnFilters() {
        val qId = etLoanId.text.toString().trim().lowercase()
        val qType = etLoanType.text.toString().trim().lowercase()
        val qName = etLoanName.text.toString().trim().lowercase()
        val qMobile = etMobileNo.text.toString().trim().lowercase()
        val qBranch = etRetailerBranchId.text.toString().trim().lowercase()

        val filtered = fullList.filter { c ->
            (qId.isEmpty() || (c.id ?: "").lowercase().contains(qId)) &&
            (qType.isEmpty() || (c.loanName ?: "").lowercase().contains(qType)) &&
            (qName.isEmpty() || c.customerName.lowercase().contains(qName)) &&
            (qMobile.isEmpty() || (c.mobilePhone ?: "").lowercase().contains(qMobile)) &&
            (qBranch.isEmpty() || (c.retailerBranch ?: "").lowercase().contains(qBranch))
        }
        adapter.updateList(filtered)
    }

    private fun setupSpinners() {
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedFilter = filterOptions[pos]
                if (selectedFilter == "Select Filter") {
                    if (isFilterMode) {
                        clearAllFilters()
                        loadLoansFromApi(1)
                    }
                } else {
                    when (selectedFilter) {
                        "Loan Id" -> activateColumn(tvLoanId, etLoanId)
                        "Loan Type" -> activateColumn(tvLoanType, etLoanType)
                        "Mobile No" -> activateColumn(tvMobileNo, etMobileNo)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selected = statusOptions[pos]
                
                val query = when {
                    etLoanId.visibility == View.VISIBLE && etLoanId.text.isNotEmpty() -> etLoanId.text.toString().trim()
                    etLoanType.visibility == View.VISIBLE && etLoanType.text.isNotEmpty() -> etLoanType.text.toString().trim()
                    etMobileNo.visibility == View.VISIBLE && etMobileNo.text.isNotEmpty() -> etMobileNo.text.toString().trim()
                    else -> ""
                }
                
                val apiSearchType = filterOptions.getOrNull(spinnerFilter.selectedItemPosition)
                if (query.isNotEmpty() && apiSearchType != null && apiSearchType != "Select Filter") {
                    when (apiSearchType) {
                        "Loan Id" -> searchByLoanId(query)
                        "Loan Type" -> searchByLoanType(query)
                        "Mobile No" -> searchByMobile(query)
                    }
                } else if (selected != "Select Status") {
                    searchByStatus(selected)
                } else {
                    loadLoansFromApi(1)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
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
                        fullList = body.data ?: emptyList()
                        adapter.updateList(fullList)
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
                fullList = list
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
        fullList = emptyList()
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
            downloadLoanExcel()
        }
    }

    private fun downloadLoanExcel() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.downloadExcel("LOAN")
                if (response.isSuccessful && response.body() != null) {
                    saveExcelFile(response.body()!!, "LoanMaster.xlsx")
                    Toast.makeText(this@LoanMasterListActivity, "Download complete", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@LoanMasterListActivity, "Download failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanMasterListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun saveExcelFile(body: ResponseBody, fileName: String) {
        try {
            val file = File(getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { fos ->
                fos.write(body.bytes())
            }
            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Optional: open the file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(
                        this@LoanMasterListActivity,
                        "${packageName}.fileprovider",
                        file
                    ),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open Excel"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}