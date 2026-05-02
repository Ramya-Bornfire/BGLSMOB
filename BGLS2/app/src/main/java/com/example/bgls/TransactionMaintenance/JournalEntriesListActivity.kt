package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

class JournalEntriesListActivity : AppCompatActivity() {

    private lateinit var rvJournalList: RecyclerView
    private lateinit var adapter: JournalEntriesListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_journal_entries_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        rvJournalList = findViewById(R.id.rvJournalList)
        rvJournalList.layoutManager = LinearLayoutManager(this)

        val mockData = getMockData()
        
        adapter = JournalEntriesListAdapter(mockData) { action, item ->
            when (action) {
                "View" -> {
                    val intent = android.content.Intent(this@JournalEntriesListActivity, JournalEntriesViewActivity::class.java)
                    startActivity(intent)
                }
                "Delete" -> {
                    Toast.makeText(this, "Deleting: ${item.tranId}", Toast.LENGTH_SHORT).show()
                    // Add API delete call here later
                }
            }
        }
        
        rvJournalList.adapter = adapter
    }

    private fun getMockData(): List<JournalEntryListModel> {
        return listOf(
            JournalEntryListModel("27-04-2026", "TR8798/1", "Debit", "SCR", "500,000.00", "LA0019", "PRAKASH", "LA0019 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("27-04-2026", "TR8798/2", "Credit", "SCR", "500,000.00", "WA0019", "PRAKASH", "LA0019 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("27-04-2026", "TR8799/1", "Debit", "SCR", "600,000.00", "LA0024", "SOWMIYA", "LA0024 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("27-04-2026", "TR8799/2", "Credit", "SCR", "600,000.00", "WA0024", "SOWMIYA", "LA0024 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("27-04-2026", "TR8806/1", "Debit", "SCR", "300,000.00", "LA0025", "VINAY", "LA0025 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("27-04-2026", "TR8806/2", "Credit", "SCR", "300,000.00", "WA0025", "VINAY", "LA0025 Loan Disbursement", "ENTERED"),
            JournalEntryListModel("29-04-2026", "TR8842/1", "Credit", "SCR", "100,000.00", "TD0039", "", "TD0039 Principal Deposit", "ENTERED"),
            JournalEntryListModel("29-04-2026", "TR8842/2", "Debit", "SCR", "100,000.00", "TD0039", "", "TD0039 Principal Deposit", "ENTERED")
        )
    }
}
