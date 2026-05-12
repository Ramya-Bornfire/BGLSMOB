package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Intent
import android.widget.ImageView
import com.example.bgls.MainActivity

class FailedReversalActivity : AppCompatActivity() {

    private lateinit var rvFailedReversal: RecyclerView
    private lateinit var adapter: TransactionsReversalAdapter
    private val dataList = mutableListOf<ReversalTransactionModel>()

    private var currentPage = 1
    private val pageSize = 10
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_reversal)

        rvFailedReversal = findViewById(R.id.rvFailedReversal)

        setupNavigation()
        setupSpinner()
        setupRecyclerView()
        fetchDataFromApi()

        // Handle pagination buttons
        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                fetchDataFromApi()
            }
        }
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                fetchDataFromApi()
            }
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerFilter)
        val filterOptions = arrayOf("Select Filter", "Date", "Status", "Account")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
    }

    private fun fetchDataFromApi() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getFailedTransactions(currentPage, pageSize)
                if (response.isSuccessful && response.body() != null) {
                    val reversalResponse = response.body()!!
                    totalPages = reversalResponse.totalPages
                    
                    dataList.clear()
                    reversalResponse.data.forEach { item ->
                        dataList.add(ReversalTransactionModel(
                            tranDate = formatDate(item.tran_date),
                            tranId = "${item.tran_id}/${item.part_tran_id}",
                            paTranTy = item.part_tran_type ?: "",
                            currency = item.acct_crncy ?: "",
                            amount = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0),
                            acctId = item.acct_num ?: "",
                            acctName = item.acct_name ?: "",
                            tranParticular = item.tran_particular ?: "",
                            status = item.tran_status ?: ""
                        ))
                    }
                    adapter.notifyDataSetChanged()
                    findViewById<TextView>(R.id.tvPageInfo).text = "Page $currentPage of $totalPages"
                } else {
                    Toast.makeText(this@FailedReversalActivity, "Failed to load failed transactions", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FailedReversalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        rvFailedReversal.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsReversalAdapter(
            context = this,
            list = dataList,
            onAcctIdClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, FailedReversalViewActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, FailedReversalEditActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            }
        )
        rvFailedReversal.adapter = adapter
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            if (dateStr.contains(" ")) {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val date = sdfInput.parse(dateStr)
                val sdfOutput = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.US)
                sdfOutput.format(date!!)
            } else if (dateStr.contains("-") && dateStr.split("-")[0].length == 4) {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdfInput.parse(dateStr)
                val sdfOutput = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.US)
                sdfOutput.format(date!!)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}