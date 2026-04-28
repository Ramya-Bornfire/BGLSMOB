package com.example.bgls.CustomerMaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

class AccountLedgerActivity : AppCompatActivity() {

    private lateinit var recyclerViewAccountLedger: RecyclerView
    private lateinit var adapter: AccountLedgerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_ledger)

        recyclerViewAccountLedger = findViewById(R.id.recyclerViewAccountLedger)

        setupTable()
    }

    private fun setupTable() {
        val dummyData = listOf(
            LedgerItem("19-06-2023", "TR00001/5", "Loan Disbursement Amount", "KES", "0.00", "127,285.00", "-127,285.00"),
            LedgerItem("19-06-2023", "TR00002/98", "Interest Applied", "KES", "0.00", "7,001.00", "-134,286.00"),
            LedgerItem("19-06-2023", "TR00003/20002", "Fees Applied", "KES", "0.00", "535.00", "-134,821.00"),
            LedgerItem("19-06-2023", "TR00005/9813", "Principal Recovery", "KES", "10,607.00", "0.00", "-124,214.00"),
            LedgerItem("19-06-2023", "TR00006/9965", "Interest Recovery", "KES", "7,001.00", "0.00", "-117,213.00"),
            LedgerItem("19-06-2023", "TR00007/9845", "Fees Recovery", "KES", "535.00", "0.00", "-116,678.00"),
            LedgerItem("19-07-2023", "TR00002/99", "Interest Applied", "KES", "0.00", "7,001.00", "-123,679.00"),
            LedgerItem("19-07-2023", "TR00003/20003", "Fees Applied", "KES", "0.00", "535.00", "-124,214.00"),
            LedgerItem("02-08-2023", "TR00005/9814", "Principal Recovery", "KES", "10,607.00", "0.00", "-113,607.00"),
            LedgerItem("02-08-2023", "TR00006/9966", "Interest Recovery", "KES", "7,001.00", "0.00", "-106,606.00"),
            LedgerItem("02-08-2023", "TR00007/9846", "Fees Recovery", "KES", "535.00", "0.00", "-106,071.00"),
            LedgerItem("18-08-2023", "TR00002/100", "Interest Applied", "KES", "0.00", "7,001.00", "-113,072.00"),
            LedgerItem("18-08-2023", "TR00003/20004", "Fees Applied", "KES", "0.00", "535.00", "-113,607.00"),
            LedgerItem("18-08-2023", "TR00004/8433", "Penalty Applied", "KES", "0.00", "2,378.00", "-115,985.00"),
            LedgerItem("17-09-2023", "TR00002/101", "Interest Applied", "KES", "0.00", "7,001.00", "-122,986.00"),
            LedgerItem("17-09-2023", "TR00003/20005", "Fees Applied", "KES", "0.00", "3,662.00", "-126,648.00")
        )

        recyclerViewAccountLedger.layoutManager = LinearLayoutManager(this)
        adapter = AccountLedgerAdapter(this, dummyData)
        recyclerViewAccountLedger.adapter = adapter
    }
}