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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountBalanceActivity : AppCompatActivity() {

    private lateinit var etLeaseDatePicker: EditText
    private lateinit var btnLeaseFilter: Button
    private lateinit var etLeaseSearch: EditText
    private lateinit var rvLeaseAccounts: RecyclerView
    private lateinit var progressLease: ProgressBar
    private lateinit var tvLeaseNoData: TextView

    private lateinit var leaseAdapter: AccountBalanceLeaseAdapter
    private var isFilterVisible = false
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_balance)

        bindViews()
        setupRecyclerView()
        setupDatePicker()
        setupFilterButton()
        setupSearchListener()
        setupBottomButtons()

        // Pre-load today's date and data
        val today = getTodayString()
        selectedDate = today
        etLeaseDatePicker.setText(today)
        loadLeaseData(today)
    }

    // ──────────────────────────────────────────────
    // View binding
    // ──────────────────────────────────────────────
    private fun bindViews() {
        etLeaseDatePicker = findViewById(R.id.etLeaseDatePicker)
        btnLeaseFilter    = findViewById(R.id.btnLeaseFilter)
        etLeaseSearch     = findViewById(R.id.etLeaseSearch)
        rvLeaseAccounts   = findViewById(R.id.rvLeaseAccounts)
        progressLease     = findViewById(R.id.progressLease)
        tvLeaseNoData     = findViewById(R.id.tvLeaseNoData)

        findViewById<ImageView>(R.id.btnBackArrow).setOnClickListener { finish() }
    }

    // ──────────────────────────────────────────────
    // RecyclerView
    // ──────────────────────────────────────────────
    private fun setupRecyclerView() {
        leaseAdapter = AccountBalanceLeaseAdapter(
            emptyList(),
            onCustomerClick = { item ->
                val intent = Intent(this, CustomerMasterViewActivity::class.java)
                intent.putExtra("customerId", item.customerId)
                intent.putExtra("customerName", item.accountName) // Using account name as customer name if available
                startActivity(intent)
            },
            onAccountClick = { item ->
                val intent = Intent(this, LoanMasterViewActivity::class.java)
                intent.putExtra("loanId", item.accountId)
                startActivity(intent)
            }
        )
        rvLeaseAccounts.layoutManager = LinearLayoutManager(this)
        rvLeaseAccounts.adapter = leaseAdapter
    }

    // ──────────────────────────────────────────────
    // Date picker — no future dates allowed
    // ──────────────────────────────────────────────
    private fun setupDatePicker() {
        etLeaseDatePicker.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val formatted = String.format("%02d-%02d-%04d", day, month + 1, year)
                    selectedDate = formatted
                    etLeaseDatePicker.setText(formatted)
                    loadLeaseData(formatted)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
                show()
            }
        }
    }

    // ──────────────────────────────────────────────
    // Filter button — toggles search bar
    // ──────────────────────────────────────────────
    private fun setupFilterButton() {
        btnLeaseFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            etLeaseSearch.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
            if (!isFilterVisible) {
                etLeaseSearch.setText("")
                leaseAdapter.filter("")
            } else {
                etLeaseSearch.requestFocus()
            }
        }
    }

    // ──────────────────────────────────────────────
    // Live search
    // ──────────────────────────────────────────────
    private fun setupSearchListener() {
        etLeaseSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { leaseAdapter.filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ──────────────────────────────────────────────
    // Data loading (dummy — replace with API call)
    // ──────────────────────────────────────────────
    private fun loadLeaseData(date: String) {
        progressLease.visibility  = View.VISIBLE
        tvLeaseNoData.visibility  = View.GONE

        val dummyData = listOf(
            AccountBalanceLeaseModel(1,  "30304459",        "8878c9751e394855a1ef174520a5142e", "", "10-08-2019", "15,000.00",       "-6,268.57"),
            AccountBalanceLeaseModel(2,  "PVT-NXYUME",      "BFM190701417",                    "", "22-06-2023", "89,348.00",       "-212,381.32"),
            AccountBalanceLeaseModel(3,  "PVT-2016-033816", "BFM190701451",                    "", "30-01-2024", "375,500.00",      "-553,969.98"),
            AccountBalanceLeaseModel(4,  "24342923",        "BFM190701931",                    "", "10-08-2023", "1,000,000.00",    "-2,291,705.75"),
            AccountBalanceLeaseModel(5,  "CPR-2012-85398",  "BFM190702838",                    "", "03-07-2024", "1,750,000.00",    "-811,578.62"),
            AccountBalanceLeaseModel(6,  "PVT-ZQUMGYL",     "BFM190706075",                    "", "07-02-2024", "1,500,000.00",    "-703,623.46"),
            AccountBalanceLeaseModel(7,  "CPR-2012-64984",  "BFM190709009",                    "", "18-11-2023", "65,000.00",       "-80,170.20"),
            AccountBalanceLeaseModel(8,  "CPR-2011-62600",  "BFM190709177",                    "", "03-07-2024", "150,000.00",      "-71,664.24"),
            AccountBalanceLeaseModel(9,  "BN-2015-384088",  "BFM190714580",                    "", "08-10-2024", "844,828.00",      "-760,944.16"),
            AccountBalanceLeaseModel(10, "BN-MXCJY67",      "BFM190715452",                    "", "15-05-2024", "1,000,000.00",    "-629,162.29"),
            AccountBalanceLeaseModel(11, "PVT-8LUJVQV",     "BFM190722628",                    "", "20-06-2024", "538,500.00",      "-1,174,267.72"),
            AccountBalanceLeaseModel(12, "C.168184",         "BFM190723449",                    "", "27-05-2024", "153,855.00",      "-102,366.18"),
            AccountBalanceLeaseModel(13, "BN-01NY8",         "BFM190725796",                    "", "02-04-2024", "440,140.00",      "-217,386.90"),
            AccountBalanceLeaseModel(14, "PVT-ZQULLKZV",    "BFM190726255",                    "", "24-06-2024", "1,343,000.00",    "-924,704.04"),
            AccountBalanceLeaseModel(15, "PVT-RXUZ8V3",     "BFM190730351",                    "", "10-01-2024", "2,000,000.00",    "-623,643.98"),
            AccountBalanceLeaseModel(16, "PVT-2016-033816", "BFM190732087",                    "", "21-09-2023", "674,030.00",      "-762,233.24"),
            AccountBalanceLeaseModel(17, "697262804",        "BFM190732627",                    "", "03-02-2024", "729,610.00",      "-80,905.48"),
            AccountBalanceLeaseModel(18, "PVT-MKU7LP",      "BFM190732658",                    "", "24-06-2024", "1,305,517.00",    "-1,067,686.78"),
            AccountBalanceLeaseModel(19, "PVT-AJU6MK2",     "BFM190734631",                    "", "26-03-2024", "2,000,000.00",    "-416,214.40"),
            AccountBalanceLeaseModel(20, "BN-WLS20Y6Y",      "BFM190737415",                    "", "19-07-2024", "511,554.00",      "-488,985.38")
        )

        progressLease.visibility = View.GONE
        if (dummyData.isEmpty()) {
            tvLeaseNoData.visibility = View.VISIBLE
        } else {
            tvLeaseNoData.visibility = View.GONE
            leaseAdapter.updateData(dummyData)
        }
    }

    // ──────────────────────────────────────────────
    // Bottom buttons
    // ──────────────────────────────────────────────
    private fun setupBottomButtons() {
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    // ──────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────
    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
