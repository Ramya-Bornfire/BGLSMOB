package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountLedgerPostingModel
import com.example.bgls.R

class AccountLedgerPositingActivity : AppCompatActivity() {

    private lateinit var rvAccountLedger: RecyclerView
    private lateinit var adapter: AccountLedgerPositingAdapter
    private var postingList = mutableListOf<AccountLedgerPostingModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_ledger_positing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadMockData()
    }

    private fun initViews() {
        rvAccountLedger = findViewById(R.id.rvAccountLedger)

        // ✅ Fix 1: Disable item change animation — prevents visual left-shift on selection
        rvAccountLedger.itemAnimator = null

        // ✅ Fix 2: Fixed size prevents full relayout when only checked state changes
        rvAccountLedger.setHasFixedSize(true)

        // ✅ Fix 3: Disable nested scrolling — stops RecyclerView fighting HorizontalScrollView
        rvAccountLedger.isNestedScrollingEnabled = false

        val layoutManager = LinearLayoutManager(this)
        rvAccountLedger.layoutManager = layoutManager

        adapter = AccountLedgerPositingAdapter(postingList) { position ->
            val item = postingList[position]
            val intent = android.content.Intent(this, AccountLedgerDetailActivity::class.java)
            intent.putExtra("tranId", item.tranId)
            intent.putExtra("partTranId", item.partTranId)
            intent.putExtra("acctId", item.acctId)
            intent.putExtra("acctName", item.acctName)
            intent.putExtra("amount", item.amount)
            intent.putExtra("tranDate", item.tranDate)
            intent.putExtra("partTranType", item.partTranType)
            intent.putExtra("currency", item.currency)
            intent.putExtra("tranParticular", item.tranParticular)
            intent.putExtra("status", item.status)
            startActivity(intent)
        }
        rvAccountLedger.adapter = adapter
    }

    private fun loadMockData() {
        val mockData = listOf(
            AccountLedgerPostingModel("27-04-2026", "TR8798", "1", "Debit",  "SCR", "500,000.00", "LA0019", "PRAKASH", "LA0019 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("27-04-2026", "TR8798", "2", "Credit", "SCR", "500,000.00", "WA0019", "PRAKASH", "LA0019 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("27-04-2026", "TR8799", "1", "Debit",  "SCR", "600,000.00", "LA0024", "SOWMIYA", "LA0024 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("27-04-2026", "TR8799", "2", "Credit", "SCR", "600,000.00", "WA0024", "SOWMIYA", "LA0024 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("27-04-2026", "TR8806", "1", "Debit",  "SCR", "300,000.00", "LA0025", "VINAY",   "LA0025 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("27-04-2026", "TR8806", "2", "Credit", "SCR", "300,000.00", "WA0025", "VINAY",   "LA0025 Loan Disbursement", "ENTERED"),
            AccountLedgerPostingModel("29-04-2026", "TR8842", "1", "Credit", "SCR", "100,000.00", "TD0039", "",        "TD0039 Principal Deposit",  "ENTERED"),
            AccountLedgerPostingModel("29-04-2026", "TR8842", "2", "Debit",  "SCR", "100,000.00", "TD0039", "",        "TD0039 Principal Deposit",  "ENTERED"),
            AccountLedgerPostingModel("30-04-2026", "TR8853", "1", "Credit", "SCR", "500,000.00", "TD0046", "SHASHA",  "TD0046 Principal Deposit",  "ENTERED"),
            AccountLedgerPostingModel("30-04-2026", "TR8901", "1", "Credit", "SCR", "400,000.00", "TD0049", "MOHAN",   "TD0049 Principal Deposit",  "ENTERED")
        )
        postingList.addAll(mockData)

        // ✅ Fix 4: Use notifyItemRangeInserted instead of notifyDataSetChanged
        // — avoids a full rebind + scroll reset when data is first loaded
        adapter.notifyItemRangeInserted(0, mockData.size)
    }
}