package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat

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
        loadEntries()
    }

    private fun setupRecyclerView() {
        rvJournalList = findViewById(R.id.rvJournalList)
        rvJournalList.layoutManager = LinearLayoutManager(this)

        adapter = JournalEntriesListAdapter(emptyList()) { action, item ->
            when (action) {
                "View" -> {
                    val intent = android.content.Intent(this, JournalEntriesViewActivity::class.java)
                    intent.putExtra("tran_id", item.tranId.split("/")[0])
                    intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "1")
                    intent.putExtra("acct_num", item.acctId)
                    startActivity(intent)
                }
                "Delete" -> {
                    lifecycleScope.launch {
                        try {
                            val tranIdMain = item.tranId.split("/")[0]
                            val partTranId = item.tranId.split("/").getOrNull(1) ?: "1"
                            val response = RetrofitClient.api.deleteJournalEntry(tranIdMain, partTranId, item.acctId)
                            if (response.isSuccessful) {
                                Toast.makeText(this@JournalEntriesListActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                loadEntries()
                            } else {
                                Toast.makeText(this@JournalEntriesListActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@JournalEntriesListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        rvJournalList.adapter = adapter
    }

    private fun loadEntries() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntriesList()
                if (response.isSuccessful && response.body() != null) {
                    val apiList = response.body()?.jour ?: emptyList()
                    val modelList = apiList.map {
                        JournalEntryListModel(
                            tranDate = it.tran_date ?: "",
                            tranId = "${it.tran_id}/${it.part_tran_id}",
                            paTranTy = it.part_tran_type ?: "",
                            currency = it.acct_crncy ?: "",
                            amount = DecimalFormat("#,##0.00").format(it.tran_amt ?: 0.0),
                            acctId = it.acct_num ?: "",
                            acctName = it.acct_name ?: "",
                            tranParticular = it.tran_particular ?: "",
                            status = it.tran_status ?: ""
                        )
                    }
                    adapter.updateData(modelList)
                } else {
                    Toast.makeText(this@JournalEntriesListActivity, "Failed to load list", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntriesListActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}