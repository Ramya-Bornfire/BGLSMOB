package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R

class RecoveryReversalActivity : AppCompatActivity() {

    private lateinit var rvRecoveryReversal: RecyclerView
    private lateinit var adapter: TransactionsReversalAdapter
    private val dataList = mutableListOf<ReversalTransactionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_reversal)

        rvRecoveryReversal = findViewById(R.id.rvRecoveryReversal)

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
        // ApiService.getRecoveryList().enqueue(...)
        
        loadMockData() // Fallback to keep UI working until API is connected
    }

    private fun loadMockData() {
        dataList.add(ReversalTransactionModel("10/04/2019", "TR00001/1523", "Debit", "KES", "33,600.00", "MGJJ129", "HAROLD OPICHO", "Loan Disbursement Amount", "POSTED"))
        dataList.add(ReversalTransactionModel("17/04/2019", "TR00001/1991", "Debit", "KES", "82,500.00", "e01fd50109fb...", "GEOFFREY NGUI", "Loan Disbursement Amount", "POSTED"))
        dataList.add(ReversalTransactionModel("07/05/2019", "TR00005/8493", "Credit", "KES", "2,800.00", "MGJJ129", "HAROLD OPICHO", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("07/05/2019", "TR00006/8611", "Credit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("11/05/2019", "TR00002/15885", "Debit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("18/05/2019", "TR00002/20609", "Debit", "KES", "3,300.00", "e01fd50109fb...", "GEOFFREY NGUI", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("23/05/2019", "TR00005/8592", "Credit", "KES", "6,875.00", "e01fd50109fb...", "GEOFFREY NGUI", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("23/05/2019", "TR00006/8716", "Credit", "KES", "3,300.00", "e01fd50109fb...", "GEOFFREY NGUI", "Interest Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("07/06/2019", "TR00005/8494", "Credit", "KES", "2,800.00", "MGJJ129", "HAROLD OPICHO", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("07/06/2019", "TR00006/8612", "Credit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("10/06/2019", "TR00002/15886", "Debit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("17/06/2019", "TR00002/20610", "Debit", "KES", "3,300.00", "e01fd50109fb...", "GEOFFREY NGUI", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("22/06/2019", "TR00005/8593", "Credit", "KES", "6,875.00", "e01fd50109fb...", "GEOFFREY NGUI", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("22/06/2019", "TR00006/8717", "Credit", "KES", "3,300.00", "e01fd50109fb...", "GEOFFREY NGUI", "Interest Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("10/07/2019", "TR00002/15887", "Debit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("17/07/2019", "TR00002/20611", "Debit", "KES", "3,300.00", "e01fd50109fb...", "GEOFFREY NGUI", "Interest Applied", "POSTED"))
    }

    private fun setupRecyclerView() {
        rvRecoveryReversal.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsReversalAdapter(
            context = this,
            list = dataList,
            onAcctIdClick = { position ->
                val intent = android.content.Intent(this, RecoveryReversalViewActivity::class.java)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val intent = android.content.Intent(this, RecoveryReversalEditActivity::class.java)
                startActivity(intent)
            }
        )
        rvRecoveryReversal.adapter = adapter
    }
}