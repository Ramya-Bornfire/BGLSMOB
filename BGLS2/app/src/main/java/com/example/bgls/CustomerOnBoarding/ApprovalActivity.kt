package com.example.bgls.CustomerOnBoarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ApprovalModel
import com.example.bgls.R

class ApprovalActivity : AppCompatActivity() {

    private lateinit var adapter: ApprovalAdapter
    private var allData = mutableListOf<ApprovalModel>()
    private var isFilterVisible = false

    // Track current filter state
    private var currentStatusFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_approval)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSpinnerFilter()
        setupColumnFilterLogic()
        loadMockData()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = ApprovalAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, KYCComplianceViewActivity::class.java)
            intent.putExtra("appRefNo", selectedItem.appRefNo)
            intent.putExtra("customerName", selectedItem.customerName)
            intent.putExtra("customerGroup", selectedItem.custGroup)
            intent.putExtra("isFromApproval", true)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        // ✅ Prevents animation jump on radio selection
        recyclerView.itemAnimator = null
        // ✅ Prevents layout recalculation on item change
        recyclerView.setHasFixedSize(true)
    }

    private fun setupSpinnerFilter() {
        val spFilter = findViewById<Spinner>(R.id.spFilter)
        val filters = listOf("ALL", "APPROVED", "NOT APPROVED")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilter.adapter = filterAdapter

        // Spinner selection triggers combined filter
        spFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = filters[position]
                applyCombinedFilter()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupColumnFilterLogic() {
        val btnFilter = findViewById<Button>(R.id.btnFilter)
        val layoutDefaultHeader = findViewById<LinearLayout>(R.id.layoutDefaultHeader)
        val layoutFilterHeader = findViewById<LinearLayout>(R.id.layoutFilterHeader)

        val etFilterSrNo = findViewById<EditText>(R.id.etFilterSrNo)
        val etFilterCustGroup = findViewById<EditText>(R.id.etFilterCustGroup)
        val etFilterAppRefNo = findViewById<EditText>(R.id.etFilterAppRefNo)
        val etFilterAccountType = findViewById<EditText>(R.id.etFilterAccountType)
        val etFilterCustomerName = findViewById<EditText>(R.id.etFilterCustomerName)
        val etFilterNationalId = findViewById<EditText>(R.id.etFilterNationalId)
        val etFilterStatus = findViewById<EditText>(R.id.etFilterStatus)

        val allCapsFilter = arrayOf<InputFilter>(InputFilter.AllCaps())
        listOf(etFilterSrNo, etFilterCustGroup, etFilterAppRefNo, etFilterAccountType,
            etFilterCustomerName, etFilterNationalId, etFilterStatus).forEach {
            it.filters = allCapsFilter
        }

        btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            layoutDefaultHeader.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            layoutFilterHeader.visibility = if (isFilterVisible) View.VISIBLE else View.GONE

            if (isFilterVisible) {
                applyCombinedFilter()
            } else {
                clearColumnFilters()
                applyCombinedFilter()
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFilterVisible) applyCombinedFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        listOf(etFilterSrNo, etFilterCustGroup, etFilterAppRefNo, etFilterAccountType,
            etFilterCustomerName, etFilterNationalId, etFilterStatus).forEach {
            it.addTextChangedListener(textWatcher)
        }

        val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                applyCombinedFilter()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                true
            } else false
        }

        listOf(etFilterSrNo, etFilterCustGroup, etFilterAppRefNo, etFilterAccountType,
            etFilterCustomerName, etFilterNationalId, etFilterStatus).forEach {
            it.setOnEditorActionListener(editorActionListener)
        }
    }

    /**
     * Applies both Spinner (status) filter and column-based filter together.
     */
    private fun applyCombinedFilter() {
        val etSrNo = findViewById<EditText>(R.id.etFilterSrNo).text.toString().trim()
        val etGroup = findViewById<EditText>(R.id.etFilterCustGroup).text.toString().trim()
        val etAppRef = findViewById<EditText>(R.id.etFilterAppRefNo).text.toString().trim()
        val etType = findViewById<EditText>(R.id.etFilterAccountType).text.toString().trim()
        val etName = findViewById<EditText>(R.id.etFilterCustomerName).text.toString().trim()
        val etNatId = findViewById<EditText>(R.id.etFilterNationalId).text.toString().trim()
        val etStatus = findViewById<EditText>(R.id.etFilterStatus).text.toString().trim()

        val filtered = allData.filter { item ->
            // Spinner status filter
            val matchesSpinner = currentStatusFilter == "ALL" || item.status == currentStatusFilter
            // Column-based filter
            val matchesColumns = (etSrNo.isEmpty() || item.slNo.toString().contains(etSrNo, ignoreCase = true)) &&
                (etGroup.isEmpty() || item.custGroup.contains(etGroup, ignoreCase = true)) &&
                (etAppRef.isEmpty() || item.appRefNo.contains(etAppRef, ignoreCase = true)) &&
                (etType.isEmpty() || item.accountType.contains(etType, ignoreCase = true)) &&
                (etName.isEmpty() || item.customerName.contains(etName, ignoreCase = true)) &&
                (etNatId.isEmpty() || item.nationalId.contains(etNatId, ignoreCase = true)) &&
                (etStatus.isEmpty() || item.status.contains(etStatus, ignoreCase = true))

            matchesSpinner && matchesColumns
        }
        adapter.updateData(filtered)
    }

    private fun clearColumnFilters() {
        listOf(R.id.etFilterSrNo, R.id.etFilterCustGroup, R.id.etFilterAppRefNo,
            R.id.etFilterAccountType, R.id.etFilterCustomerName,
            R.id.etFilterNationalId, R.id.etFilterStatus).forEach {
            findViewById<EditText>(it).text.clear()
        }
    }

    private fun loadMockData() {
        allData = mutableListOf(
            ApprovalModel(1, "RETAIL", "ARN0586", "INDIVIDUAL", "GIVI", "785412369", "NOT APPROVED"),
            ApprovalModel(2, "RETAIL", "ARN0613", "INDIVIDUAL", "LALITH KUMAR", "CUTFTFIUYGFIU", "NOT APPROVED"),
            ApprovalModel(3, "RETAIL", "ARN0831", "INDIVIDUAL", "HARISH KALYAN", "DIYFUOGOIGOI", "NOT APPROVED"),
            ApprovalModel(4, "RETAIL", "ARN0625", "INDIVIDUAL", "RAJILAKSHMI", "785421699988", "NOT APPROVED"),
            ApprovalModel(5, "RETAIL", "ARN0542", "INDIVIDUAL", "ABI", "FCFGGFBKJHJ", "NOT APPROVED"),
            ApprovalModel(6, "RETAIL", "ARN0644", "INDIVIDUAL", "TIM DAVID", "TDTURYUOOOGO", "NOT APPROVED"),
            ApprovalModel(7, "RETAIL", "ARN0674", "INDIVIDUAL", "VIGNESH", "CBDHBVIUDENSL", "NOT APPROVED"),
            ApprovalModel(8, "RETAIL", "ARN0090", "INDIVIDUAL", "PRAKASH", "74172852963", "NOT APPROVED"),
            ApprovalModel(9, "RETAIL", "ARN0841", "INDIVIDUAL", "JACKIE JHAN", "GHHGPEIGEPHGP", "NOT APPROVED"),
            ApprovalModel(10, "RETAIL", "ARN0878", "INDIVIDUAL", "VIJAY", "VBCBHIVDAVCHU", "NOT APPROVED"),
            ApprovalModel(11, "RETAIL", "ARN0863", "INDIVIDUAL", "KUMARAN RAJENDERAN", "YTGIRUFHOIFHR", "NOT APPROVED")
        )
        adapter.updateData(allData)
    }
}