package com.example.bgls.TransactionMaintenance

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.ChartOfAccounts.TabLedgerAdapter
import com.example.bgls.DataModels.TabLedgerModel
import com.example.bgls.R

class TransAccountLedgerActivity : AppCompatActivity() {

    private lateinit var rvTransLedger: RecyclerView
    private lateinit var ledgerAdapter: TabLedgerAdapter
    private var ledgerList = mutableListOf<TabLedgerModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trans_account_ledger)

        rvTransLedger = findViewById(R.id.rvTransLedger)
        rvTransLedger.layoutManager = LinearLayoutManager(this)
        
        loadMockData()
        
        ledgerAdapter = TabLedgerAdapter(this, ledgerList)
        rvTransLedger.adapter = ledgerAdapter
    }

    private fun loadMockData() {
        ledgerList.add(TabLedgerModel("ASSET", "OFF_ACC_001", "Office Cash Account", "KES", "10000", "5000", "CR", "Active"))
        ledgerList.add(TabLedgerModel("LIABILITY", "OFF_ACC_002", "Office Loan Account", "KES", "2000", "8000", "DR", "Active"))
        ledgerList.add(TabLedgerModel("INCOME", "CUST_ACC_001", "Customer Interest Income", "KES", "15000", "2000", "CR", "Active"))
    }
}