package com.example.bgls.TransactionInquiries

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.AccountBalanceLeaseAdapter
import com.example.bgls.CustomerMaster.CustomerMasterViewActivity
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.DataModels.AccountBalanceLeaseModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountBalanceActivity : AppCompatActivity() {

    private lateinit var etLeaseDatePicker: EditText
    private lateinit var btnLeaseFilter: Button
    private lateinit var rvLeaseAccounts: RecyclerView
    private lateinit var progressLease: ProgressBar
    private lateinit var tvLeaseNoData: TextView
    private lateinit var btnHome: ImageView
    private lateinit var btnBack: ImageView

    private lateinit var headerRow: android.widget.LinearLayout
    private lateinit var filterRow: android.widget.LinearLayout

    // Filter EditTexts
    private lateinit var etFilterSrl: EditText
    private lateinit var etFilterCustId: EditText
    private lateinit var etFilterAcctId: EditText
    private lateinit var etFilterName: EditText
    private lateinit var etFilterDate: EditText
    private lateinit var etFilterLoanAmt: EditText
    private lateinit var etFilterDisbAmt: EditText

    private var isFilterVisible = false

    private lateinit var leaseAdapter: AccountBalanceLeaseAdapter
    private var currentMode = "Lease" // "Lease" or "Deposit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_balance)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupRecyclerView()
        setupNavigation()
        setupListeners()
        setupFilterActions()
        
        // Load initial full list (Account_Balances endpoint)
        loadInitialData()
    }

    private fun initViews() {
        etLeaseDatePicker = findViewById(R.id.etLeaseDatePicker)
        btnLeaseFilter    = findViewById(R.id.btnLeaseFilter)
        rvLeaseAccounts   = findViewById(R.id.rvLeaseAccounts)
        progressLease     = findViewById(R.id.progressLease)
        tvLeaseNoData     = findViewById(R.id.tvLeaseNoData)
        btnHome           = findViewById(R.id.btnHome)
        btnBack           = findViewById(R.id.btnBack)
        headerRow         = findViewById(R.id.headerRow)
        filterRow         = findViewById(R.id.filterRow)

        // Pre-cache filter fields
        etFilterSrl     = findViewById(R.id.etFilterSrl)
        etFilterCustId  = findViewById(R.id.etFilterCustId)
        etFilterAcctId  = findViewById(R.id.etFilterAcctId)
        etFilterName    = findViewById(R.id.etFilterName)
        etFilterDate    = findViewById(R.id.etFilterDate)
        etFilterLoanAmt = findViewById(R.id.etFilterLoanAmt)
        etFilterDisbAmt = findViewById(R.id.etFilterDisbAmt)

        // Standardize filter fields
        val allFilters = listOf(
            etFilterSrl, etFilterCustId, etFilterAcctId, etFilterName,
            etFilterDate, etFilterLoanAmt, etFilterDisbAmt
        )
        allFilters.forEach { et ->
            et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            et.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            et.setSingleLine(true)
        }

        etLeaseDatePicker.setText(getTodayString())
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        etLeaseDatePicker.setOnClickListener { showDatePicker() }
    }

    private fun setupFilterActions() {
        btnLeaseFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            headerRow.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            filterRow.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
            
            if (!isFilterVisible) {
                clearAllFilters()
            } else {
                applyFilters()
            }
        }

        val filters = listOf(
            etFilterSrl, etFilterCustId, etFilterAcctId, etFilterName,
            etFilterDate, etFilterLoanAmt, etFilterDisbAmt
        )

        filters.forEach { et ->
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyFilters()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    private fun applyFilters() {
        leaseAdapter.filter(
            etFilterSrl.text.toString().trim(),
            etFilterCustId.text.toString().trim(),
            etFilterAcctId.text.toString().trim(),
            etFilterName.text.toString().trim(),
            etFilterDate.text.toString().trim(),
            etFilterLoanAmt.text.toString().trim(),
            etFilterDisbAmt.text.toString().trim()
        )
    }

    private fun clearAllFilters() {
        etFilterSrl.text.clear()
        etFilterCustId.text.clear()
        etFilterAcctId.text.clear()
        etFilterName.text.clear()
        etFilterDate.text.clear()
        etFilterLoanAmt.text.clear()
        etFilterDisbAmt.text.clear()
        applyFilters()
    }

    private fun setupRecyclerView() {
        leaseAdapter = AccountBalanceLeaseAdapter(
            emptyList(),
            onCustomerClick = { item ->
                val intent = Intent(this, CustomerMasterViewActivity::class.java)
                intent.putExtra("customerId", item.customerId)
                intent.putExtra("holderKey", item.holderKey)
                intent.putExtra("branchKey", item.branchKey)
                intent.putExtra("module", "module1")
                intent.putExtra("formmode", "view")
                startActivity(intent)
            },
            onAccountClick = { item ->
                val intent = Intent(this, LoanMasterViewActivity::class.java)
                intent.putExtra("loanId", item.id)
                intent.putExtra("holderKey", item.holderKey)
                intent.putExtra("branchKey", item.branchKey)
                intent.putExtra("formmode", "viewloan")
                startActivity(intent)
            }
        )
        rvLeaseAccounts.layoutManager = LinearLayoutManager(this)
        rvLeaseAccounts.adapter = leaseAdapter
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, yr, mt, dy ->
            val selectedDate = String.format("%02d-%02d-%d", dy, mt + 1, yr)
            etLeaseDatePicker.setText(selectedDate)
            
            if (currentMode == "Lease") {
                loadLeaseData(selectedDate)
            } else {
                loadDepositDataByDate(selectedDate)
            }
        }, year, month, day)
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun loadInitialData() {
        progressLease.visibility = View.VISIBLE
        tvLeaseNoData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getAccountBalances(currentMode)

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val rawList = if (currentMode == "Lease") data.leaseaccount else data.depsoitaccount
                    
                    if (rawList != null) {
                        val models = rawList.mapIndexed { index, row ->
                            AccountBalanceLeaseModel(
                                srlNo = index + 1,
                                customerId = row.getOrNull(0)?.toString() ?: "",
                                accountId = row.getOrNull(1)?.toString() ?: "",
                                accountName = row.getOrNull(2)?.toString() ?: "",
                                dateOfLoan = formatApiDate(row.getOrNull(3)?.toString() ?: ""),
                                loanAmount = row.getOrNull(4)?.toString() ?: "0.00",
                                disbursedAmount = row.getOrNull(5)?.toString() ?: "0.00",
                                holderKey = row.getOrNull(6)?.toString() ?: "",
                                branchKey = row.getOrNull(7)?.toString() ?: "",
                                id = row.getOrNull(8)?.toString() ?: ""
                            )
                        }
                        updateUI(models)
                    } else {
                        updateUI(emptyList())
                    }
                } else {
                    updateUI(emptyList())
                }
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun loadLeaseData(date: String) {
        progressLease.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getLeaseBalance(date)
                if (response.isSuccessful && response.body() != null) {
                    val models = response.body()!!.mapIndexed { index, row ->
                        AccountBalanceLeaseModel(
                            srlNo = index + 1,
                            customerId = row.getOrNull(0)?.toString() ?: "",
                            accountId = row.getOrNull(1)?.toString() ?: "",
                            accountName = row.getOrNull(2)?.toString() ?: "",
                            dateOfLoan = formatApiDate(row.getOrNull(3)?.toString() ?: ""),
                            loanAmount = row.getOrNull(4)?.toString() ?: "0.00",
                            disbursedAmount = row.getOrNull(5)?.toString() ?: "0.00",
                            holderKey = row.getOrNull(6)?.toString() ?: "",
                            branchKey = row.getOrNull(7)?.toString() ?: "",
                            id = row.getOrNull(8)?.toString() ?: ""
                        )
                    }
                    updateUI(models)
                } else {
                    updateUI(emptyList())
                }
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun loadDepositDataByDate(date: String) {
        progressLease.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getDepositBalance(date)
                if (response.isSuccessful && response.body() != null) {
                    val models = response.body()!!.mapIndexed { index, row ->
                        AccountBalanceLeaseModel(
                            srlNo = index + 1,
                            customerId = row.getOrNull(0)?.toString() ?: "",
                            accountId = row.getOrNull(1)?.toString() ?: "",
                            accountName = row.getOrNull(2)?.toString() ?: "",
                            dateOfLoan = formatApiDate(row.getOrNull(3)?.toString() ?: ""),
                            loanAmount = row.getOrNull(4)?.toString() ?: "0.00",
                            disbursedAmount = row.getOrNull(10)?.toString() ?: row.getOrNull(8)?.toString() ?: "0.00",
                            id = row.getOrNull(1)?.toString() ?: ""
                        )
                    }
                    updateUI(models)
                } else {
                    updateUI(emptyList())
                }
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun updateUI(models: List<AccountBalanceLeaseModel>) {
        progressLease.visibility = View.GONE
        if (models.isEmpty()) {
            tvLeaseNoData.visibility = View.VISIBLE
            leaseAdapter.updateData(emptyList())
        } else {
            tvLeaseNoData.visibility = View.GONE
            leaseAdapter.updateData(models)
        }
    }

    private fun formatApiDate(rawDate: String): String {
        return try {
            if (rawDate.contains("T")) {
                val parts = rawDate.split("T")[0].split("-")
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else {
                rawDate
            }
        } catch (e: Exception) {
            rawDate
        }
    }




    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
