package com.example.bgls.CustomerMaintenance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CustomerMaintenanceModel
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.DataModels.CustomerMasterPagedResponse
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import okhttp3.ResponseBody
import retrofit2.Response

class CustomerMaintenanceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMaintenanceAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView


    private var currentStatusFilter: String = "Select Status"
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 20

    // Server‑side pagination for ALL mode
    private var totalApiPages: Int = 1

    // Holds complete list when status is filtered (client‑side pagination)
    private var fullFilteredList: List<CustomerMaintenanceModel> = emptyList()

    // Cache for branch names (branchKey -> branchName)
    private val branchNameCache = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_maintenance)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    Toast.makeText(this, "Home Clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Logout Clicked", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        recyclerView = findViewById(R.id.recyclerViewCustomerMaintenance)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSpinners()
        setupPagination()

        // Initial load
        loadData()
    }

    private fun setupSpinners() {
        val filterOptions = listOf("Select Filter", "ID", "Name", "Mobile")
        val filterAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerFilter).adapter = filterAdapter

        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val statusOptions = listOf(
            "Select Status", "ACTIVE", "INACTIVE", "BLACKLIST",
            "EXITED", "PENDING_APPROVAL", "REJECTED"
        )
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                currentPage = 1
                loadData()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val allOptions = listOf("ALL", "VERIFIED", "NOTVERIFIED")
        val allAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, allOptions)
        allAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinnerAll).adapter = allAdapter
    }

    private fun setupPagination() {
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnNext = findViewById<Button>(R.id.btnNext)

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadData()
            }
        }

        btnNext.setOnClickListener {
            val totalPages = if (currentStatusFilter == "Select Status") {
                totalApiPages
            } else {
                if (fullFilteredList.isEmpty()) 1
                else (fullFilteredList.size + itemsPerPage - 1) / itemsPerPage
            }

            if (currentPage < totalPages) {
                currentPage++
                loadData()
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                if (currentStatusFilter == "Select Status") {
                    // Server‑side paginated endpoint
                    val response = RetrofitClient.api.getAllApprovedCust(currentPage, itemsPerPage)
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val list = body.data
                            totalApiPages = body.totalPages
                            // Enrich branch names
                            enrichCustomerListWithBranchNames(list)
                            val models = list.map { it.toMaintenanceModel() }
                            updateTable(models)
                        }
                    } else {
                        showToast("Failed to load data")
                    }
                } else {
                    // Status search – returns all records
                    val response = RetrofitClient.api.searchCustomersByStatus(currentStatusFilter)
                    if (response.isSuccessful) {
                        val list = response.body() ?: emptyList()
                        // Enrich branch names
                        enrichCustomerListWithBranchNames(list)
                        fullFilteredList = list.map { it.toMaintenanceModel() }
                        val totalPages = if (fullFilteredList.isEmpty()) 1
                        else (fullFilteredList.size + itemsPerPage - 1) / itemsPerPage
                        val start = (currentPage - 1) * itemsPerPage
                        val end = minOf(start + itemsPerPage, fullFilteredList.size)
                        val pageData = if (start < fullFilteredList.size) fullFilteredList.subList(start, end)
                        else emptyList()
                        updateTable(pageData)
                    } else {
                        showToast("Failed to load status data")
                    }
                }
            } catch (e: Exception) {
                showToast("Network error: ${e.message}")
            }
        }
    }

    private fun updateTable(pageData: List<CustomerMaintenanceModel>) {
        val totalPages = if (currentStatusFilter == "Select Status") totalApiPages
        else if (fullFilteredList.isEmpty()) 1
        else (fullFilteredList.size + itemsPerPage - 1) / itemsPerPage

        findViewById<TextView>(R.id.tvPageInfo).text = "Page $currentPage of $totalPages"

        adapter = CustomerMaintenanceAdapter(this, pageData)
        recyclerView.adapter = adapter
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // ─── Branch name resolution (cached) ─────────────────────────────────────

    private suspend fun enrichCustomerListWithBranchNames(list: List<CustomerMaster>) {
        if (list.isEmpty()) return

        // Collect unique branch keys that are not yet cached
        val keysToFetch = list.mapNotNull { it.branchKey?.takeIf { k -> k.isNotBlank() && !branchNameCache.containsKey(k) } }.toSet()

        // Fetch missing branch names
        keysToFetch.forEach { key ->
            val branchName = fetchBranchName(key)
            branchNameCache[key] = branchName
        }

        // Assign branchName to each customer
        list.forEach { customer ->
            val key = customer.branchKey
            customer.branchName = when {
                key.isNullOrBlank() -> "UNKNOWN"
                else -> branchNameCache[key] ?: "UNKNOWN"
            }
        }
    }

    private suspend fun fetchBranchName(branchKey: String): String {
        return try {
            val response: Response<ResponseBody> = RetrofitClient.api.getBranchNameByKey(branchKey)
            if (response.isSuccessful) {
                response.body()?.string()?.trim()?.takeIf {
                    it.isNotBlank() && !it.contains("<") && it.length < 200
                } ?: "UNKNOWN"
            } else {
                "UNKNOWN"
            }
        } catch (e: Exception) {
            "UNKNOWN"
        }
    }

    // ─── Convert CustomerMaster to UI model with formatted DOB ─────────────────

    private fun CustomerMaster.toMaintenanceModel(): CustomerMaintenanceModel {
        // Format DOB from timestamp (yyyy-MM-dd'T'HH:mm:ss) to dd/MM/yyyy
        val formattedDob = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(this.dob ?: "")
            if (date != null) outputFormat.format(date) else this.dob ?: ""
        } catch (e: Exception) {
            this.dob ?: ""
        }

        return CustomerMaintenanceModel(
            sNo = "",
            customerId = this.customerId ?: "",
            customerName = this.customerName,
            dob = formattedDob,
            branchName = this.branchName,   // already resolved by enrichment
            mobileNo = this.mobileNo ?: "",
            email = this.email ?: "",
            status = this.status ?: "",
            branchKey = this.branchKey ?: ""
        )
    }
}

// ─── Adapter class (add fallback for branch name) ─────────────────────────

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
        holder.tvSNo.text = (position + 1).toString()
        holder.tvCustomerId.text = item.customerId
        holder.tvCustomerName.text = item.customerName
        holder.tvDob.text = item.dob
        // Fallback to "UNKNOWN" if branch name is empty
        holder.tvBranchName.text = item.branchName.ifEmpty { "UNKNOWN" }
        holder.tvMobileNo.text = item.mobileNo
        holder.tvEmail.text = item.email
        holder.tvStatus.text = item.status
        
        // Zebra striping
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F9F9F9"))
        }

        // Make Customer ID look like a clickable link
        holder.tvCustomerId.paintFlags = holder.tvCustomerId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvCustomerId.setTextColor(Color.parseColor("#2196F3"))

        holder.tvCustomerId.setOnClickListener {
            val intent = Intent(context, CustomerMaintenanceViewActivity::class.java)
            intent.putExtra("CUSTOMER_ID", item.customerId)
            intent.putExtra("BRANCH_KEY", item.branchKey)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}