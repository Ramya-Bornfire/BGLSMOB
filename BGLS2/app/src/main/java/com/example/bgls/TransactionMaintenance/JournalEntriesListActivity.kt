package com.example.bgls.TransactionMaintenance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import android.app.DatePickerDialog
import android.widget.ImageView
import com.example.bgls.MainActivity

import android.widget.EditText
import java.util.Calendar



class JournalEntriesListActivity : AppCompatActivity() {

    private lateinit var rvJournalList: RecyclerView
    private lateinit var adapter: JournalEntriesListAdapter
    private val journalList = mutableListOf<JournalEntryListModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entries_list)

        initViews()
        setupButtons()
        loadData()
    }

    private fun initViews() {
        rvJournalList = findViewById(R.id.rvJournalList)
        rvJournalList.layoutManager = LinearLayoutManager(this)
        adapter = JournalEntriesListAdapter(journalList) { action, item ->
            handleAction(action, item)
        }
        rvJournalList.adapter = adapter

        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            loadData() // Reload as simple filter for now
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntriesList("list1")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Try to get list from jour or tableparttran
                    val items = body.jour ?: body.tableparttran ?: emptyList()

                    journalList.clear()
                    journalList.addAll(items.map {
                        JournalEntryListModel(
                            tranDate = it.tran_date ?: "",
                            tranId = it.tran_id ?: "",
                            paTranTy = it.part_tran_type ?: "",
                            currency = it.acct_crncy ?: "",
                            amount = DecimalFormat("#,##0.00").format(it.tran_amt ?: 0.0),
                            acctId = it.acct_num ?: "",
                            acctName = it.acct_name ?: "",
                            tranParticular = it.tran_particular ?: "",
                            status = it.tran_status ?: ""
                        )
                    })
                    adapter.notifyDataSetChanged()

                    if (journalList.isEmpty()) {
                        Toast.makeText(
                            this@JournalEntriesListActivity,
                            "No journal entries found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Failed to load list",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@JournalEntriesListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun handleAction(action: String, item: JournalEntryListModel) {
        when (action) {
            "View" -> {
                val intent = android.content.Intent(
                    this@JournalEntriesListActivity,
                    JournalEntriesViewActivity::class.java
                )
                val parts = item.tranId.split("/")
                intent.putExtra("tran_id", parts[0])
                intent.putExtra("part_tran_id", if (parts.size > 1) parts[1] else "1")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            }


            "Delete" -> {
                deleteEntry(item)
            }
        }
    }


    private fun deleteEntry(item: JournalEntryListModel) {
        lifecycleScope.launch {
            try {
                // For delete, we need the actual part_tran_id if possible. 
                // Using "1" as a fallback or trying to parse it from tranId if it was composite.
                val response = RetrofitClient.api.deleteJournalEntry(
                    tranId = item.tranId,
                    partTranId = "1",
                    acctNum = item.acctId
                )
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadData()
                } else {
                    Toast.makeText(
                        this@JournalEntriesListActivity,
                        "Delete failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@JournalEntriesListActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupButtons() {
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etFilterDate = findViewById<EditText>(R.id.etFilterDate)
        etFilterDate.setOnClickListener {
            val c = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                etFilterDate.setText(String.format("%02d-%02d-%d", dayOfMonth, month + 1, year))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            datePickerDialog.show()
        }

    }

}
