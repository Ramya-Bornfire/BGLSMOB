package com.example.bgls.CustomerMaintenance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaintenance.CustomerMaintenanceAdapter
import com.example.bgls.DataModels.CustomerMaintenanceModel
import com.example.bgls.R

class CustomerMaintenanceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMaintenanceAdapter
    
    private var currentStatusFilter: String = "Select Status"
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 20

    // Extended dummy data with mixed statuses for testing the filter
    private val allData = (1..100).map { i ->
        val status = when (i % 7) {
            0 -> "ACTIVE"
            1 -> "INACTIVE"
            2 -> "BLACKLIST"
            3 -> "EXITED"
            4 -> "PENDING_APPROVAL"
            5 -> "REJECTED"
            else -> "ACTIVE"
        }
        CustomerMaintenanceModel(
            sNo = i.toString(),
            customerId = "CUST${1000 + i}",
            customerName = "CUSTOMER $i",
            dob = "01-01-1990",
            branchName = "NAIROBI HEAD OFFICE",
            mobileNo = "2547000000${i.toString().padStart(2, '0')}",
            email = "customer$i@gmail.com",
            status = status
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_maintenance)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewCustomerMaintenance)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSpinners()
        setupPagination()
        
        // Show all data initially
        updateTableData("Select Status")
    }

    private fun setupSpinners() {
        val filterOptions = listOf("Select Filter", "ID", "Name", "Mobile")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerFilter).adapter = filterAdapter

        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val statusOptions = listOf("Select Status", "ACTIVE", "INACTIVE", "BLACKLIST", "EXITED", "PENDING_APPROVAL", "REJECTED")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                currentPage = 1
                updateTableData(currentStatusFilter)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val allOptions = listOf("ALL", "VERIFIED", "NOTVERIFIED")
        val allAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allOptions)
        allAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerAll).adapter = allAdapter
    }

    private fun setupPagination() {
        val btnPrev = findViewById<android.widget.Button>(R.id.btnPrev)
        val btnNext = findViewById<android.widget.Button>(R.id.btnNext)
        
        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateTableData(currentStatusFilter)
            }
        }
        
        btnNext.setOnClickListener {
            val filteredData = getFilteredData(currentStatusFilter)
            val totalPages = (filteredData.size + itemsPerPage - 1) / itemsPerPage
            
            if (currentPage < totalPages) {
                currentPage++
                updateTableData(currentStatusFilter)
            }
        }
    }
    
    private fun getFilteredData(statusFilter: String): List<CustomerMaintenanceModel> {
        return if (statusFilter == "Select Status" || statusFilter == "ALL") {
            allData
        } else {
            allData.filter { it.status.equals(statusFilter, ignoreCase = true) }
        }
    }

    private fun updateTableData(statusFilter: String) {
        val filteredData = getFilteredData(statusFilter)
        
        val totalPages = (filteredData.size + itemsPerPage - 1) / itemsPerPage
        val maxPage = if (totalPages > 0) totalPages else 1
        
        if (currentPage > maxPage) {
            currentPage = maxPage
        }
        
        val tvPageInfo = findViewById<android.widget.TextView>(R.id.tvPageInfo)
        tvPageInfo.text = "Page $currentPage of $maxPage"
        
        val startIndex = (currentPage - 1) * itemsPerPage
        val endIndex = Math.min(startIndex + itemsPerPage, filteredData.size)
        
        val pageData = if (startIndex < filteredData.size) {
            filteredData.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
        
        adapter = CustomerMaintenanceAdapter(this, pageData)
        recyclerView.adapter = adapter
    }
}

class CustomerMaintenanceAdapter(
    private val context: Context,
    private val list: List<CustomerMaintenanceModel>
) : RecyclerView.Adapter<CustomerMaintenanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSNo: TextView = view.findViewById(R.id.tvSNo)
        val tvCustomerId: TextView = view.findViewById(R.id.tvCustomerId)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvDob: TextView = view.findViewById(R.id.tvDob)
        val tvBranchName: TextView = view.findViewById(R.id.tvBranchName)
        val tvMobileNo: TextView = view.findViewById(R.id.tvMobileNo)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_customer_maintenance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSNo.text = item.sNo
        holder.tvCustomerId.text = item.customerId
        holder.tvCustomerName.text = item.customerName
        holder.tvDob.text = item.dob
        holder.tvBranchName.text = item.branchName
        holder.tvMobileNo.text = item.mobileNo
        holder.tvEmail.text = item.email
        holder.tvStatus.text = item.status

        // Make Customer ID look like a clickable link
        holder.tvCustomerId.paintFlags = holder.tvCustomerId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvCustomerId.setTextColor(Color.parseColor("#2196F3"))

        holder.tvCustomerId.setOnClickListener {
            val intent = Intent(context, CustomerMaintenanceViewActivity::class.java)
            intent.putExtra("CUSTOMER_ID", item.customerId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}