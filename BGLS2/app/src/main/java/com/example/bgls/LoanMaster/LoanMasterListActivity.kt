package com.example.bgls.LoanMaster

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.LoanMaster.LoanMasterAdapter
import com.example.bgls.DataModels.LoanMaster
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.R

class LoanMasterListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanMasterAdapter

    // ─── Pagination ───
    private val pageSize = 16
    private var currentPage = 1
    private var totalPages = 1

    // ─── Full dummy data — replace with API later ───
    private val allLoans = mutableListOf(
        LoanMaster("1",  "LN000001", "PERSONAL LOAN", "JOHN DOE",       "254700000001", "BR001", "ACTIVE"),
        LoanMaster("2",  "LN000002", "BUSINESS LOAN", "JANE DOE",       "254700000002", "BR001", "ACTIVE"),
        LoanMaster("3",  "LN000003", "PERSONAL LOAN", "MERCY MACHUKA",  "254725661248", "BR002", "ACTIVE"),
        LoanMaster("4",  "LN000004", "EDUCATION LOAN", "BEATRICE OBWOCHA","254721169780", "BR002", "INACTIVE"),
        LoanMaster("5",  "LN000005", "PERSONAL LOAN", "JACKLYNE OBEGI", "254726678050", "BR001", "PENDING"),
        LoanMaster("6",  "LN000006", "BUSINESS LOAN", "NJOGU YUNA",     "254722663889", "BR003", "ACTIVE"),
        LoanMaster("7",  "LN000007", "PERSONAL LOAN", "HARISH KALYAN",  "3684308",      "BR004", "ACTIVE"),
        LoanMaster("8",  "LN000008", "PERSONAL LOAN", "SUNIL KUMAR",    "5887958",      "BR004", "ACTIVE"),
        LoanMaster("9",  "LN000009", "PERSONAL LOAN", "RAJILAKSHMI",    "5744541",      "BR001", "ACTIVE"),
        LoanMaster("10", "LN000010", "BUSINESS LOAN", "PON PRASANTH",   "5659769",      "BR002", "ACTIVE"),
        LoanMaster("11", "LN000011", "PERSONAL LOAN", "ELIZABETH NYAMBURA","254721480542", "BR001", "ACTIVE"),
        LoanMaster("12", "LN000012", "PERSONAL LOAN", "NDIWA PAUL",     "254703815518", "BR003", "ACTIVE"),
        LoanMaster("13", "LN000013", "BUSINESS LOAN", "CHERUIYOT ISAAC","254727938049", "BR001", "ACTIVE"),
        LoanMaster("14", "LN000014", "PERSONAL LOAN", "HESBON MUSILI",  "254703321017", "BR002", "ACTIVE"),
        LoanMaster("15", "LN000015", "PERSONAL LOAN", "SIMOTWO TOM",    "254728724194", "BR001", "ACTIVE"),
        LoanMaster("16", "LN000016", "EDUCATION LOAN", "HILLARY LUSENO", "254797828762", "BR003", "ACTIVE"),
        LoanMaster("17", "LN000017", "PERSONAL LOAN", "TEST USER 1",    "254711111111", "BR001", "ACTIVE"),
        LoanMaster("18", "LN000018", "BUSINESS LOAN", "TEST USER 2",    "254722222222", "BR002", "INACTIVE")
    )

    private var filteredLoans = allLoans.toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_master_list)
        
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
        loadPage(1)
    }

    private fun initViews() {
        spinnerFilter  = findViewById(R.id.spinnerFilter)
        spinnerStatus  = findViewById(R.id.spinnerStatus)
        btnDownload    = findViewById(R.id.btnDownload)
        btnPrev        = findViewById(R.id.btnPrev)
        btnNext        = findViewById(R.id.btnNext)
        tvPageInfo     = findViewById(R.id.tvPageInfo)
        recyclerView   = findViewById(R.id.recyclerViewLoansList)
    }

    private fun setupSpinners() {
        // Filter spinner
        val filterOptions = listOf("Select Filter", "Loan Id", "Loan Name", "Mobile No")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter

        // Status spinner
        val statusOptions = listOf("Select Status", "ACTIVE", "INACTIVE", "PENDING")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = statusAdapter

        // Filter by status when changed
        spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                val selected = statusOptions[pos]
                filteredLoans = if (selected == "Select Status") {
                    allLoans.toMutableList()
                } else {
                    allLoans.filter { it.status == selected }.toMutableList()
                }
                loadPage(1)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = LoanMasterAdapter(this, emptyList()) { loan ->
            // Navigate to Loan Detail screen (LoanMasterViewActivity)
            val intent = Intent(this, LoanMasterViewActivity::class.java)
            intent.putExtra("loanId", loan.loanId)
            intent.putExtra("loanName", loan.loanName)
            intent.putExtra("mobile", loan.mobileNo)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadPage(page: Int) {
        totalPages = Math.max(1, Math.ceil(filteredLoans.size.toDouble() / pageSize).toInt())
        currentPage = page.coerceIn(1, totalPages)

        val fromIndex = (currentPage - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, filteredLoans.size)
        val pageData = if (fromIndex < filteredLoans.size) {
            filteredLoans.subList(fromIndex, toIndex)
        } else emptyList()

        adapter.updateList(pageData)
        tvPageInfo.text = "Page $currentPage of $totalPages"

        // Disable Prev on first page, Next on last page
        btnPrev.isEnabled = currentPage > 1
        btnPrev.alpha = if (currentPage > 1) 1f else 0.5f
        btnNext.isEnabled = currentPage < totalPages
        btnNext.alpha = if (currentPage < totalPages) 1f else 0.5f
    }

    private fun setupPagination() {
        btnPrev.setOnClickListener {
            if (currentPage > 1) loadPage(currentPage - 1)
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) loadPage(currentPage + 1)
        }
    }

    private fun setupDownload() {
        btnDownload.setOnClickListener {
            Toast.makeText(this, "Downloading loan list...", Toast.LENGTH_SHORT).show()
        }
    }
}