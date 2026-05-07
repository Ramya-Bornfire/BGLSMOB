package com.example.bgls.TransactionInquiries

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.InterestSummaryAdapter
import com.example.bgls.DataModels.InterestSummaryModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import java.util.*

class InterestSummaryActivity : AppCompatActivity() {

    private lateinit var rvInterestSummary: RecyclerView
    private lateinit var adapter: InterestSummaryAdapter
    private lateinit var tvTranDate: TextView
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interest_summary)

        setupHeader()
        setupRecyclerView()
        setupButtons()
        loadData()
    }

    private fun setupHeader() {
        tvTranDate = findViewById(R.id.tvTranDate)
        tvTranDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val date = String.format("%02d-%02d-%d", day, month + 1, year)
                tvTranDate.text = date
                // In real app, reload data for selected date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupRecyclerView() {
        rvInterestSummary = findViewById(R.id.rvInterestSummary)
        rvInterestSummary.layoutManager = LinearLayoutManager(this)
        adapter = InterestSummaryAdapter(emptyList()) { item ->
            val intent = Intent(this, LeaseLoanViewActivity::class.java)
            intent.putExtra("loanNo", item.loanNo)
            startActivity(intent)
        }
        rvInterestSummary.adapter = adapter
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        val dummyData = listOf(
            InterestSummaryModel("LA0001", "TIM DAVID", "22-05-2026", "10,000.00", "12", "0.00", "0", "0", "0"),
            InterestSummaryModel("LA0002", "JOHN DOE", "22-05-2026", "15,000.00", "10", "0.00", "0", "0", "0"),
            InterestSummaryModel("LA0003", "SARAH CONNOR", "22-05-2026", "20,000.00", "14", "0.00", "0", "0", "0"),
            InterestSummaryModel("LA0004", "MICHAEL JORDAN", "22-05-2026", "30,000.00", "11", "0.00", "0", "0", "0"),
            InterestSummaryModel("LA0005", "SERENA WILLIAMS", "22-05-2026", "25,000.00", "13", "0.00", "0", "0", "0")
        )
        adapter.updateData(dummyData)
    }
}