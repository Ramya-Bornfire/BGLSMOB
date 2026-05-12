package com.example.bgls.CustomerOnBoarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ApprovalListResponse
import com.example.bgls.DataModels.ApprovalModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ApprovalActivity : AppCompatActivity() {

    private lateinit var adapter: ApprovalAdapter
    private var allData = mutableListOf<ApprovalModel>()
    private var isFilterVisible = false
    private var currentStatusFilter = "NOT APPROVED"

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
        loadDataFromApi()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ApprovalAdapter(emptyList()) { selectedItem ->
            val intent = Intent(this, KYCComplianceViewActivity::class.java).apply {
                putExtra("appRefNo", selectedItem.appRefNo)
                putExtra("customerName", selectedItem.customerName)
                putExtra("customerGroup", selectedItem.custGroup)
                putExtra("isFromApproval", true)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)
    }

    private fun setupSpinnerFilter() {
        val spFilter = findViewById<Spinner>(R.id.spFilter)
        val filters = listOf("ALL", "APPROVED", "NOT APPROVED")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filters)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spFilter.adapter = filterAdapter

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = filters[position]
                applyCombinedFilter()   // client‑side filter only
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDataFromApi() {
        RetrofitClient.api.getApprovalList().enqueue(object : Callback<ApprovalListResponse> {
            override fun onResponse(call: Call<ApprovalListResponse>, response: Response<ApprovalListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val apiList = response.body()!!.customerRequest
                    allData = apiList.mapIndexed { idx, item ->
                        ApprovalModel(
                            slNo = idx + 1,
                            custGroup = item.custGroup,
                            appRefNo = item.applRefNo,
                            accountType = item.accountType,
                            customerName = item.preferredName ?: item.fullName ?: "",
                            nationalId = item.nationalId,
                            status = item.status
                        )
                    }.toMutableList()
                    applyCombinedFilter()
                } else {
                    Toast.makeText(this@ApprovalActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApprovalListResponse>, t: Throwable) {
                Toast.makeText(this@ApprovalActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // ---------- Column filter logic (unchanged, works on allData) ----------
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
            val matchesSpinner = currentStatusFilter == "ALL" || item.status == currentStatusFilter

            val matchesColumns = (etSrNo.isEmpty() || (item.slNo?.toString()?.contains(etSrNo, ignoreCase = true) == true)) &&
                    (etGroup.isEmpty() || (item.custGroup?.contains(etGroup, ignoreCase = true) == true)) &&
                    (etAppRef.isEmpty() || (item.appRefNo?.contains(etAppRef, ignoreCase = true) == true)) &&
                    (etType.isEmpty() || (item.accountType?.contains(etType, ignoreCase = true) == true)) &&
                    (etName.isEmpty() || (item.customerName?.contains(etName, ignoreCase = true) == true)) &&
                    (etNatId.isEmpty() || (item.nationalId?.contains(etNatId, ignoreCase = true) == true)) &&
                    (etStatus.isEmpty() || (item.status?.contains(etStatus, ignoreCase = true) == true))

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

}