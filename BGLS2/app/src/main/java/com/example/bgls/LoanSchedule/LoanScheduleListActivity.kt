package com.example.bgls.LoanSchedule

import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.LoanScheduleListActivityAdapter
import com.example.bgls.CustomerMaster.LoanScheduleViewActivity
import com.example.bgls.DataModels.LoanScheduleListModel
import com.example.bgls.R

class LoanScheduleListActivity : AppCompatActivity() {

    private lateinit var spinnerFilter: Spinner
    private lateinit var btnDownload: Button
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanScheduleListActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loan_schedule_list)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        loadDummyData()
    }

    private fun initViews() {
        spinnerFilter = findViewById(R.id.spinnerFilter)
        btnDownload   = findViewById(R.id.btnDownload)
        btnPrev       = findViewById(R.id.btnPrev)
        btnNext       = findViewById(R.id.btnNext)
        tvPageInfo    = findViewById(R.id.tvPageInfo)
        recyclerView  = findViewById(R.id.recyclerViewLoanScheduleList)

        val filterOptions = listOf("Select Filter", "Loan Name", "Loan Id", "Retailer Name")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilter.adapter = filterAdapter
    }

    private fun setupRecyclerView() {
        adapter = LoanScheduleListActivityAdapter(this, emptyList()) { item ->
            val intent = android.content.Intent(this,LoanScheduleViewActivity::class.java)
            intent.putExtra("loanId", item.loanId)
            intent.putExtra("is_from_loan_schedule", true)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadDummyData() {
        val dummyData = listOf(
            LoanScheduleListModel("1", "Consumer Credit New Client", "CCNa4669c7aa0ce25ecd788", "Kingsway Tyres Limited (Nairobi)", "University Way", "ACTIVE_IN_ARREARS"),
            LoanScheduleListModel("2", "Consumer Credit Repeat Client", "CCR0eff4343df2c1c0f7c6e", "Buytec Stores Limited", "Nairobi", "ACTIVE_IN_ARREARS"),
            LoanScheduleListModel("3", "Boda Financing Monthly", "BFM190774592", "", "Pioneer House", "ACTIVE_IN_ARREARS"),
            LoanScheduleListModel("4", "Consumer Credit New Client", "CCNa8419916f871da6a6db1", "Kenyatronics Traders Limited", "CBD", "ACTIVE_IN_ARREARS"),
            LoanScheduleListModel("5", "Consumer Credit Repeat Client", "CCRfd3790fe5076d4d8dc95", "Victoria Courts Kenya", "Karen", "ACTIVE_IN_ARREARS"),
            LoanScheduleListModel("6", "Consumer Credit Repeat Client", "CCR43debe41450141d0e25d", "Appliance Zone Ltd", "Westlands", "ACTIVE_IN_ARREARS")
        )
        adapter.updateList(dummyData)
    }
}