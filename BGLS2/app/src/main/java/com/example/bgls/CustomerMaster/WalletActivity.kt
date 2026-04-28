package com.example.bgls.CustomerMaster

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

class WalletActivity : AppCompatActivity() {

    private lateinit var tvNoRecords: TextView
    private lateinit var recyclerViewWallet: RecyclerView
    private lateinit var adapter: AccountLedgerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        tvNoRecords = findViewById(R.id.tvNoRecords)
        recyclerViewWallet = findViewById(R.id.recyclerViewWallet)

        // When you integrate with your API, you will pass your dynamic list here.
        // For example, passing an empty list shows "No records":
        val apiData = emptyList<LedgerItem>()
        // To test with data, you could pass something like:
        // val apiData = listOf(LedgerItem("27-04-2026", "TR001", "Wallet Deposit", "KES", "1000", "0", "1000"))

        updateTableData(apiData)
    }

    // Call this method whenever your API data updates
    private fun updateTableData(dataList: List<LedgerItem>) {
        if (dataList.isEmpty()) {
            tvNoRecords.visibility = View.VISIBLE
            recyclerViewWallet.visibility = View.GONE
        } else {
            tvNoRecords.visibility = View.GONE
            recyclerViewWallet.visibility = View.VISIBLE

            recyclerViewWallet.layoutManager = LinearLayoutManager(this)
            adapter = AccountLedgerAdapter(this, dataList)
            recyclerViewWallet.adapter = adapter
        }
    }
}