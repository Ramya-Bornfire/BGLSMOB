package com.example.bgls.LoanSchedule

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.LoanScheduleViewActivity
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

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

    private var currentPage = 1
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_schedule_list)

        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupSpinner()
        setupButtons()
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

    private fun setupSpinner() {
        val filterOptions = listOf("Select Filter", "Loan Name", "Loan Id", "Retailer Name")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter
    }

    private fun setupButtons() {
        btnDownload.setOnClickListener {
            downloadExcel()
        }
        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                fetchLoanScheduleList()
            }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                fetchLoanScheduleList()
            }
        }
    }

    private fun fetchLoanScheduleList() {
        progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoanScheduleList("listschedule")
                if (response.isSuccessful) {
                    val body = response.body()
                    val list = body?.list ?: emptyList()
                    val mappedList = list.mapIndexed { idx, model ->
                        model.copy(sno = (idx + 1).toString())
                    }
                    adapter.updateList(mappedList)

                    // Simple pagination based on total count (no server pagination for this endpoint)
                    totalPages = 1
                    currentPage = 1
                    tvPageInfo.text = "Page 1 of 1 · Total: ${mappedList.size}"
                } else {
                    Toast.makeText(this@LoanScheduleListActivity, "Failed to load", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = android.view.View.GONE
            }
        }
    }

    private fun downloadExcel() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.downloadExcel("LoanSchedule") // adjust type as needed
                if (response.isSuccessful) {
                    saveFile(response.body(), "LoanSchedule.xlsx")
                } else {
                    Toast.makeText(this@LoanScheduleListActivity, "Download failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleListActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveFile(body: ResponseBody?, fileName: String) {
        if (body == null) return
        try {
            val file = File(getExternalFilesDir(null), fileName)
            val fos = FileOutputStream(file)
            fos.write(body.bytes())
            fos.close()
            Toast.makeText(this, "Downloaded to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }
}