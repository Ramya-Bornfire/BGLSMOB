package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

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

        // Load Initial Dummy Rows matching screenshot
        if (massEntryList.isEmpty()) {
            massEntryList.add(MassEntryModel(partTranId = "1"))
            massEntryList.add(MassEntryModel(partTranId = "2"))
            massEntryAdapter.notifyDataSetChanged()
        }
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
        findViewById<Button>(R.id.btnAddRow).setOnClickListener {
            massEntryAdapter.addRow()
        }

        findViewById<Button>(R.id.btnRemoveRow).setOnClickListener {
            massEntryAdapter.removeRow()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            // Trigger API Call here
            Toast.makeText(this, "Mass Entries Submitted Successfully", Toast.LENGTH_SHORT).show()
            finish() // Close Activity and return to previous
        }
    }
}
