package com.example.bgls.CustomerOnBoarding

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ApprovalListResponse
import com.example.bgls.DataModels.KycItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KYCComplianceActivity : AppCompatActivity() {
    private lateinit var adapter: KYCComplianceAdapter
    private lateinit var fullData: List<KYCItem>
    private var isFilterVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kyccompliance)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupFilterLogic()
        loadKycData()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = KYCComplianceAdapter(emptyList()) { item ->
            val intent = Intent(this, KYCComplianceViewActivity::class.java).apply {
                putExtra("appRefNo", item.applRefNo)
                putExtra("customerName", item.customerName)
                putExtra("customerGroup", item.customerGroup)
                putExtra("isFromCompliance", true)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun loadKycData() {
        // Use the working endpoint
        RetrofitClient.api.getApprovalList().enqueue(object : Callback<ApprovalListResponse> {
            override fun onResponse(
                call: Call<ApprovalListResponse>,
                response: Response<ApprovalListResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val apiList = response.body()!!.customerRequest
                    fullData = apiList.mapIndexed { idx, item ->
                        KYCItem(
                            srNo = (idx + 1).toString(),
                            customerGroup = item.custGroup,        // from ApprovalApiItem
                            applRefNo = item.applRefNo,
                            accountType = item.accountType,
                            customerName = item.preferredName ?: item.fullName ?: "",
                            nationalId = item.nationalId
                        )
                    }
                    adapter.updateList(fullData)
                } else {
                    Toast.makeText(this@KYCComplianceActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApprovalListResponse>, t: Throwable) {
                Toast.makeText(this@KYCComplianceActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // ---------- Column filter logic (unchanged, works on fullData) ----------
    private fun setupFilterLogic() {
        val btnFilter = findViewById<Button>(R.id.btnFilter)
        val layoutDefaultHeader = findViewById<LinearLayout>(R.id.layoutDefaultHeader)
        val layoutFilterHeader = findViewById<LinearLayout>(R.id.layoutFilterHeader)

        val etFilterSrNo = findViewById<EditText>(R.id.etFilterSrNo)
        val etFilterCustomerGroup = findViewById<EditText>(R.id.etFilterCustomerGroup)
        val etFilterApplRefNo = findViewById<EditText>(R.id.etFilterApplRefNo)
        val etFilterAccountType = findViewById<EditText>(R.id.etFilterAccountType)
        val etFilterCustomerName = findViewById<EditText>(R.id.etFilterCustomerName)
        val etFilterNationalId = findViewById<EditText>(R.id.etFilterNationalId)

        val allCapsFilter = arrayOf<InputFilter>(InputFilter.AllCaps())
        etFilterSrNo.filters = allCapsFilter
        etFilterCustomerGroup.filters = allCapsFilter
        etFilterApplRefNo.filters = allCapsFilter
        etFilterAccountType.filters = allCapsFilter
        etFilterCustomerName.filters = allCapsFilter
        etFilterNationalId.filters = allCapsFilter

        val filterAction = {
            performFilter(
                etFilterSrNo.text.toString(),
                etFilterCustomerGroup.text.toString(),
                etFilterApplRefNo.text.toString(),
                etFilterAccountType.text.toString(),
                etFilterCustomerName.text.toString(),
                etFilterNationalId.text.toString()
            )
        }

        btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            layoutDefaultHeader.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            layoutFilterHeader.visibility = if (isFilterVisible) View.VISIBLE else View.GONE

            if (isFilterVisible) {
                filterAction()
            } else {
                clearFilters()
                adapter.updateList(fullData)
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFilterVisible) filterAction()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etFilterSrNo.addTextChangedListener(textWatcher)
        etFilterCustomerGroup.addTextChangedListener(textWatcher)
        etFilterApplRefNo.addTextChangedListener(textWatcher)
        etFilterAccountType.addTextChangedListener(textWatcher)
        etFilterCustomerName.addTextChangedListener(textWatcher)
        etFilterNationalId.addTextChangedListener(textWatcher)

        val editorActionListener = TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                filterAction()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                true
            } else {
                false
            }
        }

        etFilterSrNo.setOnEditorActionListener(editorActionListener)
        etFilterCustomerGroup.setOnEditorActionListener(editorActionListener)
        etFilterApplRefNo.setOnEditorActionListener(editorActionListener)
        etFilterAccountType.setOnEditorActionListener(editorActionListener)
        etFilterCustomerName.setOnEditorActionListener(editorActionListener)
        etFilterNationalId.setOnEditorActionListener(editorActionListener)
    }

    private fun performFilter(srNo: String, group: String, appRef: String, type: String, name: String, natId: String) {
        val qSrNo = srNo.trim()
        val qGroup = group.trim()
        val qAppRef = appRef.trim()
        val qType = type.trim()
        val qName = name.trim()
        val qNatId = natId.trim()

        val filteredList = fullData.filter { item ->
            (qSrNo.isEmpty() || item.srNo.contains(qSrNo, ignoreCase = true)) &&
                    (qGroup.isEmpty() || item.customerGroup.contains(qGroup, ignoreCase = true)) &&
                    (qAppRef.isEmpty() || item.applRefNo.contains(qAppRef, ignoreCase = true)) &&
                    (qType.isEmpty() || item.accountType.contains(qType, ignoreCase = true)) &&
                    (qName.isEmpty() || item.customerName.contains(qName, ignoreCase = true)) &&
                    (qNatId.isEmpty() || item.nationalId.contains(qNatId, ignoreCase = true))
        }
        adapter.updateList(filteredList)
    }

    private fun clearFilters() {
        findViewById<EditText>(R.id.etFilterSrNo).text.clear()
        findViewById<EditText>(R.id.etFilterCustomerGroup).text.clear()
        findViewById<EditText>(R.id.etFilterApplRefNo).text.clear()
        findViewById<EditText>(R.id.etFilterAccountType).text.clear()
        findViewById<EditText>(R.id.etFilterCustomerName).text.clear()
        findViewById<EditText>(R.id.etFilterNationalId).text.clear()
    }

}