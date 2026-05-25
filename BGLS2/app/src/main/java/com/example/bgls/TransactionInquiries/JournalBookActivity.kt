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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.JournalBookAdapter
import com.example.bgls.DataModels.JournalBookModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JournalBookActivity : AppCompatActivity() {

    private lateinit var etReportDate: EditText
    private lateinit var etSearch: EditText
    private lateinit var btnFilter: Button
    private lateinit var rvJournalBook: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var adapter: JournalBookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_book)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupListeners()
        setupRecyclerView()
        
        // Initial load
        loadInitialData()
    }

    private fun initViews() {
        etReportDate = findViewById(R.id.etReportDate)
        etSearch = findViewById(R.id.etSearch)
        btnFilter = findViewById(R.id.btnFilter)
        rvJournalBook = findViewById(R.id.rvJournalBook)
        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)

        etReportDate.setText(getTodayString())
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btnBackArrow).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        etReportDate.setOnClickListener { showDatePicker() }

        btnFilter.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                etSearch.visibility = View.GONE
                etSearch.setText("")
                adapter.filter("")
            } else {
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { adapter.filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView() {
        adapter = JournalBookAdapter(
            emptyList(),
            onViewClick = { item ->
                // Enquiry logic
                Toast.makeText(this, "Viewing ${item.tranId}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { item ->
                // Delete logic
                Toast.makeText(this, "Delete ${item.tranId}", Toast.LENGTH_SHORT).show()
            }
        )
        rvJournalBook.layoutManager = LinearLayoutManager(this)
        rvJournalBook.adapter = adapter
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(this, { _, year, month, day ->
            val formatted = String.format("%04d-%02d-%02d", year, month + 1, day)
            val displayDate = String.format("%02d-%02d-%04d", day, month + 1, year)
            etReportDate.setText(displayDate)
            loadJournalByDate(formatted)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun loadInitialData() {
        progressBar.visibility = View.VISIBLE
        tvNoData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getJournalBook("list1")

                if (response.isSuccessful && response.body() != null) {
                    val rawList = response.body()!!.jour
                    if (rawList != null) {
                        val models = rawList.map { mapToModel(it) }
                        updateUI(models)
                    } else {
                        updateUI(emptyList())
                    }
                } else {
                    updateUI(emptyList())
                }
            } catch (e: Exception) {
                updateUI(emptyList())
                Toast.makeText(this@JournalBookActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadJournalByDate(date: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getJournalBook2("list1", date)

                if (response.isSuccessful && response.body() != null) {
                    val rawList = response.body()!!
                    // Assuming journalbook2 returns List<List<Any>> based on some implementations
                    // But controller says List<TRAN_MAIN_TRM_WRK_ENTITY> which is List<Map>
                    val models = rawList.map { mapToModel(it as Map<String, Any>) }
                    updateUI(models)
                } else {
                    updateUI(emptyList())
                }
            } catch (e: Exception) {
                updateUI(emptyList())
            }
        }
    }

    private fun mapToModel(map: Map<String, Any>): JournalBookModel {
        return JournalBookModel(
            tranDate = formatApiDate(map["tran_date"]?.toString() ?: ""),
            tranId = map["tran_id"]?.toString() ?: "",
            partTranId = map["part_tran_id"]?.toString() ?: "",
            partTranType = map["part_tran_type"]?.toString() ?: "",
            currency = map["acct_crncy"]?.toString() ?: "",
            amount = formatAmount(map["tran_amt"]?.toString() ?: "0.00"),
            acctNum = map["acct_num"]?.toString() ?: "",
            acctName = map["acct_name"]?.toString() ?: "",
            particular = map["tran_particular"]?.toString() ?: "",
            status = map["tran_status"]?.toString() ?: ""
        )
    }

    private fun updateUI(models: List<JournalBookModel>) {
        progressBar.visibility = View.GONE
        if (models.isEmpty()) {
            tvNoData.visibility = View.VISIBLE
        } else {
            tvNoData.visibility = View.GONE
            adapter.updateData(models)
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

    private fun formatAmount(amt: String): String {
        return try {
            val value = amt.toDouble()
            String.format("%,.2f", value)
        } catch (e: Exception) {
            amt
        }
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
