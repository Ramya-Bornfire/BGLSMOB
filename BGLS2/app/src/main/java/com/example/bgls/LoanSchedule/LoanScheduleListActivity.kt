package com.example.bgls.LoanSchedule

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

import android.view.View
import android.widget.ProgressBar
import com.example.bgls.CustomerMaster.LoanScheduleViewActivity

class LoanScheduleListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: LoanScheduleListActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_schedule_list)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        fetchLoanScheduleList()
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnDownload   = findViewById(R.id.btnDownload)
        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        tvPageInfo    = findViewById(R.id.tvPageInfo)
        recyclerView  = findViewById(R.id.recyclerViewLoanScheduleList)
        progressBar   = findViewById(R.id.progressBar)

        val filterOptions = listOf("Select Filter", "Loan Name", "Loan Id", "Retailer Name")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter
    }

    private fun setupRecyclerView() {
        adapter = LoanScheduleListActivityAdapter(this, emptyList()) { item ->
            val intent = Intent(this, LoanScheduleViewActivity::class.java)
            intent.putExtra("loanId", item.loanId)
            intent.putExtra("holder_key", item.accountHolderKey)
            intent.putExtra("encoded_key", item.encodedKey)
            intent.putExtra("is_from_loan_schedule", true)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun fetchLoanScheduleList() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoanScheduleList()
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val list = responseBody?.list ?: emptyList()
                    // Map SNO based on index
                    val mappedList = list.mapIndexed { index, model ->
                        model.copy(sno = (index + 1).toString())
                    }
                    adapter.updateList(mappedList)
                    tvPageInfo.text = "Total: ${mappedList.size}"
                    Toast.makeText(this@LoanScheduleListActivity, "Loaded ${mappedList.size} schedules", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = "Failed to load schedule: ${response.code()} ${response.message()}"
                    Toast.makeText(this@LoanScheduleListActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleListActivity, 
                    "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}