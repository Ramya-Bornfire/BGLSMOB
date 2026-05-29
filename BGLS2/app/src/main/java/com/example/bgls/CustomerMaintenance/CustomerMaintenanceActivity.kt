package com.example.bgls.CustomerMaintenance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
class CustomerMaintenanceActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMaintenanceAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var homebtn: ImageView
    private lateinit var progressBar: ProgressBar

    private var currentStatusFilter: String = "Select Status"
    private var currentPage: Int = 1
    private val itemsPerPage: Int = 200

    // Server‑side pagination for ALL mode
    private var totalApiPages: Int = 1

    // Holds complete list when status is filtered (client‑side pagination)
    private var fullFilteredList: List<CustomerMaintenanceModel> = emptyList()

    // Cache for branch names (branchKey -> branchName)
    private val branchNameCache = mutableMapOf<String, String>()
    private var isFilterVisible = false
    private var allLoadedData: List<CustomerMaintenanceModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_customer_maintenance)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, com.example.bgls.MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }
        val btnDownload = findViewById<ImageView>(R.id.btnDownload)
        btnDownload.setOnClickListener {
            downloadCustomerExcel()
        }
        recyclerView = findViewById(R.id.recyclerViewCustomerMaintenance)
        recyclerView.layoutManager = LinearLayoutManager(this)
        progressBar = findViewById(R.id.progressBar)

        setupSpinners()
        setupPagination()
        setupColumnFilterLogic()

        // Initial load
        loadData()
    }
    private fun downloadCustomerExcel() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.downloadExcel("CUSTOMER")
                if (response.isSuccessful && response.body() != null) {
                    saveExcelFile(response.body()!!, "CustomerMaster.xlsx")
                    Toast.makeText(this@CustomerMaintenanceActivity, "Download complete", Toast.LENGTH_SHORT).show()
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@CustomerMaintenanceActivity, "Download failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMaintenanceActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun saveExcelFile(body: ResponseBody, fileName: String) {
        try {
            val file = File(getExternalFilesDir(null), fileName)
            FileOutputStream(file).use { fos ->
                fos.write(body.bytes())
            }
            Toast.makeText(this, "Saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // Optionally open the file
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    FileProvider.getUriForFile(
                        this@CustomerMaintenanceActivity,
                        "${packageName}.provider",
                        file
                    ),
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open Excel"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupSpinners() {
        val filterOptions = listOf("Select Filter", "ID", "Name", "Mobile")
        val filterSpinner = findViewById<Spinner>(R.id.spinnerFilter)
        val filterAdapter = ArrayAdapter(this, R.layout.spinner_item_small, filterOptions)
        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_small)
        filterSpinner.adapter = filterAdapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    showFilterHeader(true)
                    // Reset all first
                    findViewById<View>(R.id.etFilterCustomerId).visibility = View.GONE
                    findViewById<View>(R.id.etFilterCustomerName).visibility = View.GONE
                    findViewById<View>(R.id.etFilterMobileNo).visibility = View.GONE

                    val targetEt = when (filterOptions[position]) {
                        "ID" -> findViewById<EditText>(R.id.etFilterCustomerId)
                        "Name" -> findViewById<EditText>(R.id.etFilterCustomerName)
                        "Mobile" -> findViewById<EditText>(R.id.etFilterMobileNo)
                        else -> null
                    }
                    targetEt?.apply {
                        visibility = View.VISIBLE
                        requestFocus()
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val statusOptions = listOf(
            "Select Status", "ACTIVE", "INACTIVE", "BLACKLIST",
            "EXITED", "PENDING_APPROVAL", "REJECTED"
        )
        val statusAdapter = ArrayAdapter(this, R.layout.spinner_item_small, statusOptions)
        statusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_small)
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
        val allAdapter = ArrayAdapter(this, R.layout.spinner_item_small, allOptions)
        allAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_small)
        findViewById<Spinner>(R.id.spinnerAll).adapter = allAdapter
    }

    private fun setupColumnFilterLogic() {
        val filterIds = listOf(
            R.id.etFilterCustomerId, R.id.etFilterCustomerName, R.id.etFilterMobileNo
        )

        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyCombinedFilter()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        filterIds.forEach { id ->
            val et = findViewById<EditText>(id)
            et.addTextChangedListener(textWatcher)
            et.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || 
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    showFilterHeader(false)
                    true
                } else false
            }
        }
    }

    private fun showFilterHeader(show: Boolean) {
        isFilterVisible = show
        findViewById<View>(R.id.layoutDefaultHeader).visibility = if (show) View.GONE else View.VISIBLE
        findViewById<View>(R.id.layoutFilterHeader).visibility = if (show) View.VISIBLE else View.GONE
        if (!show) {
            // Reset spinner to "Select Filter"
            findViewById<Spinner>(R.id.spinnerFilter).setSelection(0)
            // Hide all EditTexts in the filter row
            findViewById<View>(R.id.etFilterCustomerId).visibility = View.GONE
            findViewById<View>(R.id.etFilterCustomerName).visibility = View.GONE
            findViewById<View>(R.id.etFilterMobileNo).visibility = View.GONE
        }
    }

    private fun clearFilters() {
        val filterIds = listOf(
            R.id.etFilterCustomerId, R.id.etFilterCustomerName, R.id.etFilterMobileNo
        )
        filterIds.forEach { findViewById<EditText>(it).text.clear() }
    }

    private fun applyCombinedFilter() {
        val qId = findViewById<EditText>(R.id.etFilterCustomerId).text.toString().trim()
        val qName = findViewById<EditText>(R.id.etFilterCustomerName).text.toString().trim()
        val qMobile = findViewById<EditText>(R.id.etFilterMobileNo).text.toString().trim()

        val filtered = allLoadedData.filter { item ->
            (qId.isEmpty() || (item.customerId ?: "").contains(qId, ignoreCase = true)) &&
            (qName.isEmpty() || item.customerName.contains(qName, ignoreCase = true)) &&
            (qMobile.isEmpty() || (item.mobileNo ?: "").contains(qMobile, ignoreCase = true))
        }
        updateTable(filtered, isFiltering = true)
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
                if (allLoadedData.isEmpty()) 1
                else (allLoadedData.size + itemsPerPage - 1) / itemsPerPage
            }

            if (currentPage < totalPages) {
                currentPage++
                loadData()
            }
        }
    }

    private fun loadData() {
        progressBar.visibility = View.VISIBLE
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
                            allLoadedData = list.map { it.toMaintenanceModel() }
                            updateTable(allLoadedData)
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
                        allLoadedData = list.map { it.toMaintenanceModel() }
                        
                        val totalPages = if (allLoadedData.isEmpty()) 1
                        else (allLoadedData.size + itemsPerPage - 1) / itemsPerPage
                        val start = (currentPage - 1) * itemsPerPage
                        val end = minOf(start + itemsPerPage, allLoadedData.size)
                        val pageData = if (start < allLoadedData.size) allLoadedData.subList(start, end)
                        else emptyList()
                        updateTable(pageData)
                    } else {
                        showToast("Failed to load status data")
                    }
                }
            } catch (e: Exception) {
                showToast("Network error: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateTable(pageData: List<CustomerMaintenanceModel>, isFiltering: Boolean = false) {
        val totalPages = if (currentStatusFilter == "Select Status") totalApiPages
        else if (allLoadedData.isEmpty()) 1
        else (allLoadedData.size + itemsPerPage - 1) / itemsPerPage

        val pageInfo = findViewById<TextView>(R.id.tvPageInfo)
        if (isFiltering) {
            pageInfo.text = "Showing ${pageData.size} results"
        } else {
            pageInfo.text = "Page $currentPage of $totalPages"
        }

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
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

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

