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
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getInterestSummary("list")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val listValues = data.fewvalues ?: emptyList<Any>()
                    val accBalances = data.allAccBalances ?: emptyList<Any>()

                    val summaryList = listValues.mapIndexed { index: Int, row: Any ->
                        val entity = row as? Map<*, *>
                        val balance = accBalances.getOrNull(index)?.toString() ?: "0.00"
                        
                        InterestSummaryModel(
                            loanNo = entity?.get("loan_accountno")?.toString() ?: "",
                            name = entity?.get("customer_name")?.toString() ?: "",
                            dateOfLoan = entity?.get("date_of_loan")?.toString() ?: "",
                            loanAmt = entity?.get("loan_sanctioned")?.toString() ?: "0.00",
                            interestRate = entity?.get("effective_interest_rate")?.toString() ?: "0",
                            liability = balance,
                            accruedInterest = "0.00",
                            bookedInterest = "0.00",
                            appliedInterest = "0.00"
                        )
                    }
                    adapter.updateData(summaryList)
                }
            } catch (e: Exception) {}
        }
    }
}