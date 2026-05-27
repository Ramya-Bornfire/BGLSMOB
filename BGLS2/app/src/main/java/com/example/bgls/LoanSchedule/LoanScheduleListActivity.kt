package com.example.bgls.LoanSchedule

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.bgls.CustomerMaster.LoanScheduleViewActivity
import com.example.bgls.DataModels.LoanScheduleListModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class LoanScheduleListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: LoanScheduleListActivityAdapter

    // ─── Column-filter views ───
    private lateinit var layoutDefault: LinearLayout
    private lateinit var layoutFilter: LinearLayout

    private lateinit var tvLoanName: TextView
    private lateinit var etLoanName: EditText
    private lateinit var tvLoanId: TextView
    private lateinit var etLoanId: EditText
    private lateinit var tvRetailerName: TextView
    private lateinit var etRetailerName: EditText
    private lateinit var tvRetailerBranchId: TextView
    private lateinit var etRetailerBranchId: EditText

    private lateinit var allTvs: List<TextView>
    private lateinit var allEts: List<EditText>

    // ─── Pagination State ───
    private val pageSize = 200
    private var currentPage = 1
    private var totalPages = 1
    
    // ─── Filter State ───
    private var isFilterMode = false
    private var fullList: List<LoanScheduleListModel> = emptyList()
    private var currentFilteredList: List<LoanScheduleListModel> = emptyList()
    private val filterOptions = listOf("Select Filter", "Loan Name", "Loan Id", "Retailer Name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_schedule_list)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupColumnFilter()
        setupRecyclerView()
        setupSpinner()
        setupButtons()
        fetchLoanScheduleList()
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnDownload   = findViewById(R.id.btnDownload)
        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        tvPageInfo    = findViewById(R.id.tvPageInfo)
        recyclerView  = findViewById(R.id.recyclerViewLoanScheduleList)
        progressBar   = findViewById(R.id.progressBar)

        layoutDefault  = findViewById(R.id.layoutDefaultHeader)
        layoutFilter   = findViewById(R.id.layoutFilterHeader)

        tvLoanName = findViewById(R.id.tvHdrLoanName)
        etLoanName = findViewById(R.id.etFilterLoanName)
        tvLoanId = findViewById(R.id.tvHdrLoanId)
        etLoanId = findViewById(R.id.etFilterLoanId)
        tvRetailerName = findViewById(R.id.tvHdrRetailerName)
        etRetailerName = findViewById(R.id.etFilterRetailerName)
        tvRetailerBranchId = findViewById(R.id.tvHdrRetailerBranchId)
        etRetailerBranchId = findViewById(R.id.etFilterRetailerBranchId)

        allTvs = listOf(tvLoanName, tvLoanId, tvRetailerName, tvRetailerBranchId)
        allEts = listOf(etLoanName, etLoanId, etRetailerName, etRetailerBranchId)
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
        
        currentFilteredList = fullList
        currentPage = 1
        updatePaginationUI()
        
        spinnerFilter.setSelection(0, false)
    }

    private fun setupColumnFilter() {
        tvLoanName.setOnClickListener { activateColumn(tvLoanName, etLoanName) }
        tvLoanId.setOnClickListener { activateColumn(tvLoanId, etLoanId) }
        tvRetailerName.setOnClickListener { activateColumn(tvRetailerName, etRetailerName) }
        tvRetailerBranchId.setOnClickListener { activateColumn(tvRetailerBranchId, etRetailerBranchId) }

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
        val qName = etLoanName.text.toString().trim().lowercase()
        val qId = etLoanId.text.toString().trim().lowercase()
        val qRetailerName = etRetailerName.text.toString().trim().lowercase()
        val qBranch = etRetailerBranchId.text.toString().trim().lowercase()

        currentFilteredList = fullList.filter { c ->
            (qName.isEmpty() || (c.loanName ?: "").lowercase().contains(qName)) &&
            (qId.isEmpty() || (c.loanId ?: "").lowercase().contains(qId)) &&
            (qRetailerName.isEmpty() || (c.retailerName ?: "").lowercase().contains(qRetailerName)) &&
            (qBranch.isEmpty() || (c.retailerBranchId ?: "").lowercase().contains(qBranch))
        }
        
        currentPage = 1
        updatePaginationUI()
    }

    private fun updatePaginationUI() {
        totalPages = Math.ceil(currentFilteredList.size.toDouble() / pageSize).toInt()
        if (totalPages < 1) totalPages = 1
        if (currentPage > totalPages) currentPage = totalPages

        tvPageInfo.text = "Page $currentPage of $totalPages"
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha = if (currentPage > 1) 1f else 0.5f
        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha = if (currentPage < totalPages) 1f else 0.5f

        val startIndex = (currentPage - 1) * pageSize
        var endIndex = startIndex + pageSize
        if (endIndex > currentFilteredList.size) {
            endIndex = currentFilteredList.size
        }

        if (startIndex < endIndex) {
            val pageData = currentFilteredList.subList(startIndex, endIndex)
            adapter.updateList(pageData)
        } else {
            adapter.updateList(emptyList())
        }
    }

    private fun setupRecyclerView() {
        adapter = LoanScheduleListActivityAdapter(this, emptyList()) { item ->
            val intent = Intent(this, LoanScheduleViewActivity::class.java)
            intent.putExtra("loanId", item.loanId)
            intent.putExtra("holder_key", item.accountHolderKey)
            intent.putExtra("encoded_key", item.encodedKey)
            intent.putExtra("is_from_loan_schedule", true)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
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
                        "Loan Name" -> activateColumn(tvLoanName, etLoanName)
                        "Loan Id" -> activateColumn(tvLoanId, etLoanId)
                        "Retailer Name" -> activateColumn(tvRetailerName, etRetailerName)
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupButtons() {
        btnDownload.setOnClickListener {
            downloadExcel()
        }
        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updatePaginationUI()
            }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                updatePaginationUI()
            }
        }
    }

    private fun fetchLoanScheduleList() {
        progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoanScheduleList("listschedule")
                if (response.isSuccessful) {
                    val body = response.body()
                    val list = body?.list ?: emptyList()
                    val mappedList = list.mapIndexed { idx, model ->
                        model.copy(sno = (idx + 1).toString())
                    }
                    fullList = mappedList
                    currentFilteredList = fullList
                    currentPage = 1
                    updatePaginationUI()
                } else {
                    Toast.makeText(this@LoanScheduleListActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun downloadExcel() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.downloadExcel("LoanSchedule") // adjust type as needed
                if (response.isSuccessful) {
                    saveFile(response.body(), "LoanSchedule.xlsx")
                } else {
                    Toast.makeText(this@LoanScheduleListActivity, "Download failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveFile(body: ResponseBody?, fileName: String) {
        if (body == null) return
        try {
            val file = File(getExternalFilesDir(null), fileName)
            val fos = FileOutputStream(file)
            fos.write(body.bytes())
            fos.close()
            Toast.makeText(this, "Downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }
}