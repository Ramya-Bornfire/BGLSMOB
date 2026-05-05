package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.DataModels.ReversalTransactionModel

class TransactionsReversalActivity : AppCompatActivity() {

    private lateinit var rvTransactionsReversal: RecyclerView
    private lateinit var spinnerFilter: Spinner
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var adapter: TransactionsReversalAdapter
    private var dataList = mutableListOf<ReversalTransactionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_reversal)

        initViews()
        setupSpinner()
        loadMockData()
        
        adapter = TransactionsReversalAdapter(
            context = this,
            list = dataList,
            onAcctIdClick = { position ->
                val intent = android.content.Intent(this, TransactionsReversalViewActivity::class.java)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val intent = android.content.Intent(this, TransactionsReversalEditActivity::class.java)
                startActivity(intent)
            }
        )
        rvTransactionsReversal.layoutManager = LinearLayoutManager(this)
        rvTransactionsReversal.adapter = adapter

        btnPrev.setOnClickListener { Toast.makeText(this, "Previous Page", Toast.LENGTH_SHORT).show() }
        btnNext.setOnClickListener { Toast.makeText(this, "Next Page", Toast.LENGTH_SHORT).show() }
    }

    private fun initViews() {
        rvTransactionsReversal = findViewById(R.id.rvTransactionsReversal)
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvPageInfo = findViewById(R.id.tvPageInfo)
    }

    private fun setupSpinner() {
        val options = arrayOf("Select Filter", "Date", "ID", "Amount")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = spinnerAdapter
    }

    private fun loadMockData() {
        dataList.add(ReversalTransactionModel("10/04/2019", "TR00001/1523", "Debit", "KES", "33,600.00", "MGJJ129", "HAROLD OPICHO", "Loan Disbursement Amount", "POSTED"))
        dataList.add(ReversalTransactionModel("17/04/2019", "TR00001/1991", "Debit", "KES", "82,500.00", "e01fd50109fb4c5ca1d0fcc7a209e61e", "GEOFFREY NGUI", "Loan Disbursement Amount", "POSTED"))
        dataList.add(ReversalTransactionModel("07/05/2019", "TR00005/8493", "Credit", "KES", "2,800.00", "MGJJ129", "HAROLD OPICHO", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("07/05/2019", "TR00006/8611", "Credit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("11/05/2019", "TR00002/15885", "Debit", "KES", "1,344.00", "MGJJ129", "HAROLD OPICHO", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("18/05/2019", "TR00002/20609", "Debit", "KES", "3,300.00", "e01fd50109fb4c5ca1d0fcc7a209e61e", "GEOFFREY NGUI", "Interest Applied", "POSTED"))
        dataList.add(ReversalTransactionModel("23/05/2019", "TR00005/8592", "Credit", "KES", "6,875.00", "e01fd50109fb4c5ca1d0fcc7a209e61e", "GEOFFREY NGUI", "Principal Recovery", "POSTED"))
        dataList.add(ReversalTransactionModel("23/05/2019", "TR00006/8716", "Credit", "KES", "3,300.00", "e01fd50109fb4c5ca1d0fcc7a209e61e", "GEOFFREY NGUI", "Interest Recovery", "POSTED"))
    }
}