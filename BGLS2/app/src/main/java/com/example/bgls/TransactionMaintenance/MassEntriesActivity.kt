package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ChartAccountApiItem
import com.example.bgls.DataModels.ChartOfAccountsListResponse
import com.example.bgls.DataModels.MassEntryModel
import com.example.bgls.DataModels.MassEntryRequest
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MassEntriesActivity : AppCompatActivity() {

    private lateinit var tvMassTotal: TextView
    private val massEntryList = mutableListOf<MassEntryModel>()
    private lateinit var massEntryAdapter: MassEntryAdapter
    private var currentTranId: String = "TR0000"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mass_entries)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvMassTotal = findViewById(R.id.tvMassTotal)
        setupRecyclerView()
        setupButtons()

        loadInitialData()
    }

    private fun loadInitialData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntryAddScreen()
                if (response.isSuccessful && response.body() != null) {
                    currentTranId = response.body()!!.plusonetran2 ?: "TR0000"
                    // Add initial row with correct tranId
                    massEntryAdapter.addRow(currentTranId)
                } else {
                    massEntryAdapter.addRow(currentTranId)
                }
            } catch (e: Exception) {
                massEntryAdapter.addRow(currentTranId)
            }
        }
    }

    private fun setupRecyclerView() {
        val rvMassEntries = findViewById<RecyclerView>(R.id.rvMassEntries)
        rvMassEntries.layoutManager = LinearLayoutManager(this)

        massEntryAdapter = MassEntryAdapter(
            massEntryList,
            onAccountSearchRequested = { position -> openAccountSearchDialog(position) },
            onTotalCalculated = { credit, debit ->
                tvMassTotal.text = String.format("Total Credit: %.2f  |  Total Debit: %.2f", credit, debit)
            }
        )
        rvMassEntries.adapter = massEntryAdapter
    }

    private fun openAccountSearchDialog(position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val etSearchNo = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val etSearchName = dialogView.findViewById<EditText>(R.id.etSearchAccName)
        val btnFilter = dialogView.findViewById<Button>(R.id.btnFilter)
        val tlAccounts = dialogView.findViewById<TableLayout>(R.id.tlAccounts)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        fun populateTable(accounts: List<ChartAccountApiItem>) {
            tlAccounts.removeAllViews()
            // Header
            val header = TableRow(this)
            header.addView(TextView(this).apply { text = "Acc No"; setPadding(8, 8, 8, 8); setTextColor(resources.getColor(R.color.black)) })
            header.addView(TextView(this).apply { text = "Acc Name"; setPadding(8, 8, 8, 8); setTextColor(resources.getColor(R.color.black)) })
            tlAccounts.addView(header)

            accounts.forEach { acc ->
                val row = TableRow(this)
                row.addView(TextView(this).apply { text = acc.acct_num; setPadding(8, 8, 8, 8) })
                row.addView(TextView(this).apply { text = acc.acct_name; setPadding(8, 8, 8, 8) })
                row.setOnClickListener {
                    massEntryAdapter.updateAccount(position, acc.acct_num ?: "", acc.acct_name ?: "")
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        btnFilter.setOnClickListener {
            val queryNo = etSearchNo.text.toString().lowercase()
            val queryName = etSearchName.text.toString().lowercase()
            // Re-fetch or filter local list if cached
            fetchAccounts { allAccounts ->
                val filtered = allAccounts.filter {
                    (it.acct_num?.lowercase()?.contains(queryNo) == true) &&
                    (it.acct_name?.lowercase()?.contains(queryName) == true)
                }
                populateTable(filtered)
            }
        }

        // Initial load
        fetchAccounts { populateTable(it) }

        dialog.show()
    }

    private fun fetchAccounts(onSuccess: (List<ChartAccountApiItem>) -> Unit) {
        RetrofitClient.api.getChartOfAccountsList().enqueue(object : retrofit2.Callback<ChartOfAccountsListResponse> {
            override fun onResponse(call: retrofit2.Call<ChartOfAccountsListResponse>, response: retrofit2.Response<ChartOfAccountsListResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.chartaccount ?: emptyList())
                }
            }
            override fun onFailure(call: retrofit2.Call<ChartOfAccountsListResponse>, t: Throwable) {
                Toast.makeText(this@MassEntriesActivity, "Search failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAddRow).setOnClickListener { massEntryAdapter.addRow(currentTranId) }
        findViewById<Button>(R.id.btnRemoveRow).setOnClickListener { massEntryAdapter.removeRow() }
        findViewById<Button>(R.id.btnSubmit).setOnClickListener { submitMassEntries() }
    }

    private fun submitMassEntries() {
        val entries = massEntryAdapter.getEntries()
        if (entries.isEmpty()) {
            Toast.makeText(this, "No entries to submit", Toast.LENGTH_SHORT).show()
            return
        }

        // Validation: Credit and Debit must match
        var totalCredit = 0.0
        var totalDebit = 0.0
        entries.forEach {
            if (it.part_tran_type == "Credit") totalCredit += it.tran_amt
            else if (it.part_tran_type == "Debit") totalDebit += it.tran_amt
        }

        if (totalCredit != totalDebit || totalCredit == 0.0) {
            Toast.makeText(this, "Credit and Debit totals must match and be non-zero", Toast.LENGTH_LONG).show()
            return
        }

        val tranDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val tranType = "Transfer"

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.addMassEntries(entries, tranDate, tranType)
                if (response.isSuccessful) {
                    Toast.makeText(this@MassEntriesActivity, "Mass entries submitted successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Submission failed"
                    Toast.makeText(this@MassEntriesActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MassEntriesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}