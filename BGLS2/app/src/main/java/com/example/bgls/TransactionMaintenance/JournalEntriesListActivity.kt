package com.example.bgls.TransactionMaintenance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.app.DatePickerDialog
import android.view.WindowManager
import android.widget.ImageView
import com.example.bgls.MainActivity

import android.widget.EditText
import java.util.Calendar



class JournalEntriesListActivity : AppCompatActivity() {

    private lateinit var rvJournalList: RecyclerView
    private lateinit var adapter: JournalEntriesListAdapter
    private val journalList = mutableListOf<JournalEntryListModel>()
    
    private var isFilterVisible = false
    private var isDataLoaded = false

    // Filter EditTexts
    private lateinit var etFilterColDate: EditText
    private lateinit var etFilterId: EditText
    private lateinit var etFilterTy: EditText
    private lateinit var etFilterCur: EditText
    private lateinit var etFilterAmt: EditText
    private lateinit var etFilterAcctId: EditText
    private lateinit var etFilterAcctName: EditText
    private lateinit var etFilterPart: EditText
    private lateinit var etFilterStatus: EditText

    private val filterTextWatcher = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isDataLoaded) {
                applyFilters()
            }
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entries_list)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupButtons()
        loadData()
    }

    private fun initViews() {
        rvJournalList = findViewById(R.id.rvJournalList)
        rvJournalList.layoutManager = LinearLayoutManager(this)
        adapter = JournalEntriesListAdapter(journalList) { action, item ->
            handleAction(action, item)
        }
        rvJournalList.adapter = adapter

        // Init Filter Views
        etFilterColDate = findViewById(R.id.etFilterColDate)
        etFilterId = findViewById(R.id.etFilterId)
        etFilterTy = findViewById(R.id.etFilterTy)
        etFilterCur = findViewById(R.id.etFilterCur)
        etFilterAmt = findViewById(R.id.etFilterAmt)
        etFilterAcctId = findViewById(R.id.etFilterAcctId)
        etFilterAcctName = findViewById(R.id.etFilterAcctName)
        etFilterPart = findViewById(R.id.etFilterPart)
        etFilterStatus = findViewById(R.id.etFilterStatus)

        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            toggleFilterVisibility()
        }
    }

    private fun toggleFilterVisibility() {
        isFilterVisible = !isFilterVisible
        val filterRow = findViewById<View>(R.id.filterRow)
        val headerRow = findViewById<View>(R.id.headerRow)
        
        filterRow.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        headerRow.visibility = if (isFilterVisible) View.GONE else View.VISIBLE

        if (isFilterVisible) {
            etFilterColDate.removeTextChangedListener(filterTextWatcher)
            etFilterColDate.addTextChangedListener(filterTextWatcher)
            
            etFilterId.removeTextChangedListener(filterTextWatcher)
            etFilterId.addTextChangedListener(filterTextWatcher)
            
            etFilterTy.removeTextChangedListener(filterTextWatcher)
            etFilterTy.addTextChangedListener(filterTextWatcher)
            
            etFilterCur.removeTextChangedListener(filterTextWatcher)
            etFilterCur.addTextChangedListener(filterTextWatcher)
            
            etFilterAmt.removeTextChangedListener(filterTextWatcher)
            etFilterAmt.addTextChangedListener(filterTextWatcher)
            
            etFilterAcctId.removeTextChangedListener(filterTextWatcher)
            etFilterAcctId.addTextChangedListener(filterTextWatcher)
            
            etFilterAcctName.removeTextChangedListener(filterTextWatcher)
            etFilterAcctName.addTextChangedListener(filterTextWatcher)
            
            etFilterPart.removeTextChangedListener(filterTextWatcher)
            etFilterPart.addTextChangedListener(filterTextWatcher)
            
            etFilterStatus.removeTextChangedListener(filterTextWatcher)
            etFilterStatus.addTextChangedListener(filterTextWatcher)
        } else {
            // Clear all fields
            etFilterColDate.text.clear()
            etFilterId.text.clear()
            etFilterTy.text.clear()
            etFilterCur.text.clear()
            etFilterAmt.text.clear()
            etFilterAcctId.text.clear()
            etFilterAcctName.text.clear()
            etFilterPart.text.clear()
            etFilterStatus.text.clear()
            
            applyFilters()
            
            // Hide keyboard
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
        }
    }

    private fun applyFilters() {
        adapter.applyFilters(
            tranDate = etFilterColDate.text.toString(),
            tranId = etFilterId.text.toString(),
            paTranTy = etFilterTy.text.toString(),
            currency = etFilterCur.text.toString(),
            amount = etFilterAmt.text.toString(),
            acctId = etFilterAcctId.text.toString(),
            acctName = etFilterAcctName.text.toString(),
            tranParticular = etFilterPart.text.toString(),
            status = etFilterStatus.text.toString()
        )
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntriesList("list1")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Try to get list from jour or tableparttran
                    val items = body.jour ?: body.tableparttran ?: emptyList()

                    journalList.clear()
                    journalList.addAll(items.map {
                        JournalEntryListModel(
                            tranDate = it.tran_date ?: "",
                            tranId = it.tran_id ?: "",
                            paTranTy = it.part_tran_type ?: "",
                            currency = it.acct_crncy ?: "",
                            amount = DecimalFormat("#,##0.00").format(it.tran_amt ?: 0.0),
                            acctId = it.acct_num ?: "",
                            acctName = it.acct_name ?: "",
                            tranParticular = it.tran_particular ?: "",
                            status = it.tran_status ?: ""
                        )
                    })
                    
                    adapter.setFullData(journalList)
                    isDataLoaded = true
                    applyFilters()

                    if (journalList.isEmpty()) {
                        Toast.makeText(
                            this@JournalEntriesListActivity,
                            "No journal entries found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Failed to load list",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@JournalEntriesListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleAction(action: String, item: JournalEntryListModel) {
        when (action) {
            "View" -> {
                val intent = android.content.Intent(
                    this@JournalEntriesListActivity,
                    JournalEntriesViewActivity::class.java
                )
                val parts = item.tranId.split("/")
                intent.putExtra("tran_id", parts[0])
                intent.putExtra("part_tran_id", if (parts.size > 1) parts[1] else "1")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            }


            "Delete" -> {
                deleteEntry(item)
            }
        }
    }


    private fun deleteEntry(item: JournalEntryListModel) {
        lifecycleScope.launch {
            try {
                // For delete, we need the actual part_tran_id if possible. 
                // Using "1" as a fallback or trying to parse it from tranId if it was composite.
                val response = RetrofitClient.api.deleteJournalEntry(
                    tranId = item.tranId,
                    partTranId = "1",
                    acctNum = item.acctId
                )
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadData()
                } else {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Delete failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@JournalEntriesListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etFilterDate = findViewById<EditText>(R.id.etFilterDate)
        etFilterDate.setOnClickListener {
            val c = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                etFilterDate.setText(String.format("%02d-%02d-%d", dayOfMonth, month + 1, year))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

    }

}
