package com.example.bgls.TransactionInquiries

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
    private lateinit var etLeaseSearch: EditText
    private lateinit var btnLeaseFilter: Button
    private lateinit var rvLeaseAccounts: RecyclerView
    private lateinit var progressLease: ProgressBar
    private lateinit var tvLeaseNoData: TextView
    private lateinit var rgAccountType: RadioGroup

    private lateinit var leaseAdapter: AccountBalanceLeaseAdapter
    private var currentMode = "Lease" // "Lease" or "Deposit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_balance)

        initViews()
        setupRecyclerView()
        setupListeners()
        
        // Load initial full list (Account_Balances endpoint)
        loadInitialData()
    }

    private fun initViews() {
        etLeaseDatePicker = findViewById(R.id.etLeaseDatePicker)
        etLeaseSearch     = findViewById(R.id.etLeaseSearch)
        btnLeaseFilter    = findViewById(R.id.btnLeaseFilter)
        rvLeaseAccounts   = findViewById(R.id.rvLeaseAccounts)
        progressLease     = findViewById(R.id.progressLease)
        tvLeaseNoData     = findViewById(R.id.tvLeaseNoData)
        rgAccountType     = findViewById(R.id.rgAccountType)

        etLeaseDatePicker.setText(getTodayString())
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBackArrow).setOnClickListener { finish() }
        
        etLeaseDatePicker.setOnClickListener { showDatePicker() }
        
        btnLeaseFilter.setOnClickListener {
            if (etLeaseSearch.visibility == View.VISIBLE) {
                etLeaseSearch.visibility = View.GONE
                etLeaseSearch.setText("")
                leaseAdapter.filter("")
            } else {
                etLeaseSearch.visibility = View.VISIBLE
                etLeaseSearch.requestFocus()
            }
        }


        setupSearchListener()
        setupBottomButtons()
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

    private fun setupSearchListener() {
        etLeaseSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { leaseAdapter.filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupBottomButtons() {
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
