package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.MassEntryRequest
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MassEntriesActivity : AppCompatActivity() {

    private lateinit var tvMassTotal: TextView
    private val massEntryList = mutableListOf<MassEntryModel>()
    private lateinit var massEntryAdapter: MassEntryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mass_entries)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvMassTotal = findViewById(R.id.tvMassTotal)
        setupRecyclerView()
        setupButtons()

    }

    private fun setupRecyclerView() {
        val rvMassEntries = findViewById<RecyclerView>(R.id.rvMassEntries)
        rvMassEntries.layoutManager = LinearLayoutManager(this)

        massEntryAdapter = MassEntryAdapter(massEntryList) { totalCredit, totalDebit ->
            tvMassTotal.text = String.format("Total Credit: %.2f  |  Total Debit: %.2f", totalCredit, totalDebit)
        }
        rvMassEntries.adapter = massEntryAdapter
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAddRow).setOnClickListener { massEntryAdapter.addRow() }
        findViewById<Button>(R.id.btnRemoveRow).setOnClickListener { massEntryAdapter.removeRow() }
        findViewById<Button>(R.id.btnSubmit).setOnClickListener { submitMassEntries() }
    }
    private fun submitMassEntries() {
        val entries = massEntryAdapter.getEntries()
        if (entries.isEmpty()) {
            Toast.makeText(this, "No entries to submit", Toast.LENGTH_SHORT).show()
            return
        }

        val tranDate = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
        val tranType = "Transfer"  // you may allow user selection

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.addMassEntries(entries, tranDate, tranType)
                if (response.isSuccessful) {
                    Toast.makeText(this@MassEntriesActivity, "Mass entries submitted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@MassEntriesActivity, "Submission failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MassEntriesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

}