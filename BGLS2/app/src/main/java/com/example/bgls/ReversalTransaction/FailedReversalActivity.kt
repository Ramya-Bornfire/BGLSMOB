package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R

class FailedReversalActivity : AppCompatActivity() {

    private lateinit var rvFailedReversal: RecyclerView
    private lateinit var adapter: TransactionsReversalAdapter
    private val dataList = mutableListOf<ReversalTransactionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_reversal)

        rvFailedReversal = findViewById(R.id.rvFailedReversal)

        setupSpinner()
        fetchDataFromApi()
        setupRecyclerView()

        // Handle pagination buttons (mock behavior)
        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            // Logic for previous page
        }
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            // Logic for next page
        }
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        val filterOptions = arrayOf("Select Filter", "Date", "Status", "Account")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
    }

    // ==========================================
    // API INTEGRATION POINTS
    // ==========================================

    /**
     * TODO: Replace with actual API call (e.g., Retrofit or Volley) to fetch the list.
     * Once you receive the response, map it to ReversalTransactionModel, clear `dataList`,
     * addAll() to `dataList`, and call `adapter.notifyDataSetChanged()`.
     */
    private fun fetchDataFromApi() {
        // Example:
        // ApiService.getFailedTransactionsList().enqueue(...)
        
        loadMockData() // Fallback to keep UI working until API is connected
    }

    private fun loadMockData() {
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09910/1", "Debit", "KES", "14,000.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09910/2", "Credit", "KES", "14,000.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09914/1", "Debit", "KES", "2,000.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09914/2", "Credit", "KES", "2,000.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09952/1", "Debit", "KES", "30,000.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09952/2", "Credit", "KES", "30,000.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09954/1", "Debit", "KES", "5,000.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("01/10/2025", "TR09954/2", "Credit", "KES", "5,000.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("02/10/2025", "TR09982/1", "Debit", "KES", "300.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("02/10/2025", "TR09982/2", "Credit", "KES", "300.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("03/10/2025", "TR10014/1", "Debit", "KES", "200.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("03/10/2025", "TR10014/2", "Credit", "KES", "200.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("03/10/2025", "TR10022/1", "Debit", "KES", "4,000.00", "1704120001", "Paybill Mambu clearing Account", "Receivable Failed Transaction", "POSTED"))
        dataList.add(ReversalTransactionModel("03/10/2025", "TR10022/2", "Credit", "KES", "4,000.00", "1644000001", "Debtors Adjustment Control", "Receivable Failed Transaction", "POSTED"))
    }

    private fun setupRecyclerView() {
        rvFailedReversal.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsReversalAdapter(
            context = this,
            list = dataList,
            onAcctIdClick = { position ->
                val intent = android.content.Intent(this, FailedReversalViewActivity::class.java)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val intent = android.content.Intent(this, FailedReversalEditActivity::class.java)
                startActivity(intent)
            }
        )
        rvFailedReversal.adapter = adapter
    }
}