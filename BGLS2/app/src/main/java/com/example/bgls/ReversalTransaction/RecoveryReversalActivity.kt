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
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Intent
import android.widget.ImageView
import com.example.bgls.MainActivity

class RecoveryReversalActivity : AppCompatActivity() {

    private lateinit var rvRecoveryReversal: RecyclerView
    private lateinit var adapter: TransactionsReversalAdapter
    private val dataList = mutableListOf<ReversalTransactionModel>()

    private var currentPage = 1
    private val pageSize = 200
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_reversal)

        rvRecoveryReversal = findViewById(R.id.rvRecoveryReversal)

        setupNavigation()
        setupSpinner()
        setupRecyclerView()
        fetchDataFromApi()

        // Handle pagination buttons (mock behavior for now, same as TransactionsReversal)
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
                // The backend RecoveryReversal(formmode="list") returns Map<String, Object>
                val response = RetrofitClient.api.getRecoveryReversal("list1")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val gson = Gson()
                    
                  // val dataJson = gson.toJson(body["data"])
                    val dataJson = gson.toJson(body["jour"])
                    val type = object : TypeToken<List<JournalEntryItem>>() {}.type
                    val items: List<JournalEntryItem>? = gson.fromJson(dataJson, type)
                    
                    dataList.clear()
                    items?.forEach { item ->
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
                    
                    if (items == null || items.isEmpty()) {
                        Toast.makeText(this@RecoveryReversalActivity, "No recovery transactions found", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Update total pages if available in response
                    totalPages = (body["totalPages"] as? Double)?.toInt() ?: 1
                    findViewById<TextView>(R.id.tvPageInfo).text = "Page $currentPage of $totalPages"
                    
                } else {
                    Toast.makeText(this@RecoveryReversalActivity, "Failed to load recovery list", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@RecoveryReversalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        rvRecoveryReversal.layoutManager = LinearLayoutManager(this)
        adapter = TransactionsReversalAdapter(
            context = this,
            list = dataList,
            onAcctIdClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, RecoveryReversalViewActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, RecoveryReversalEditActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            }
        )
        rvRecoveryReversal.adapter = adapter
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