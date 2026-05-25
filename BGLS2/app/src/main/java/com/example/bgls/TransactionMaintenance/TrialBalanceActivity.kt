package com.example.bgls.TransactionMaintenance

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ChartAccountApiItem
import com.example.bgls.DataModels.TrialBalanceItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Intent
import android.view.WindowManager
import android.widget.ImageView
import com.example.bgls.MainActivity

class TrialBalanceActivity : AppCompatActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var layoutDatePicker: LinearLayout
    private lateinit var rvTrialBalance: RecyclerView
    private lateinit var rvGLSHDetails: RecyclerView
    private lateinit var layoutGLSH: LinearLayout
    private lateinit var tvTotalCredits: TextView
    private lateinit var tvTotalDebits: TextView
    private lateinit var btnFilter: Button
    
    // Filter EditTexts
    private lateinit var etFilterGlCode: android.widget.EditText
    private lateinit var etFilterAcctName: android.widget.EditText
    private lateinit var etFilterOpeningBal: android.widget.EditText
    private lateinit var etFilterCredit: android.widget.EditText
    private lateinit var etFilterDebit: android.widget.EditText
    private lateinit var etFilterNetChange: android.widget.EditText
    private lateinit var etFilterClosingBal: android.widget.EditText

    private var isFilterVisible = false
    private var isDataLoaded = false

    private val filterTextWatcher = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isDataLoaded) {
                applyFilters()
            }
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    }
    
    private lateinit var adapter: TrialBalanceAdapter
    private lateinit var glshAdapter: GLSHDetailAdapter
    
    private var dataList = mutableListOf<TrialBalanceItem>()
    private var glshList = mutableListOf<ChartAccountApiItem>()
    
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial_balance)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupNavigation()
        setupRecyclerViews()
        setupDatePicker()
        
        // Load initial list (formmode=list)
        fetchInitialTrialBalance()
        
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            toggleFilterVisibility()
        }
    }

    private fun initViews() {
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        layoutDatePicker = findViewById(R.id.layoutDatePicker)
        rvTrialBalance = findViewById(R.id.rvTrialBalance)
        rvGLSHDetails = findViewById(R.id.rvGLSHDetails)
        layoutGLSH = findViewById(R.id.layoutGLSH)
        tvTotalCredits = findViewById(R.id.tvTotalCredits)
        tvTotalDebits = findViewById(R.id.tvTotalDebits)
        btnFilter = findViewById(R.id.btnFilter)
        
        etFilterGlCode = findViewById(R.id.etFilterGlCode)
        etFilterAcctName = findViewById(R.id.etFilterAcctName)
        etFilterOpeningBal = findViewById(R.id.etFilterOpeningBal)
        etFilterCredit = findViewById(R.id.etFilterCredit)
        etFilterDebit = findViewById(R.id.etFilterDebit)
        etFilterNetChange = findViewById(R.id.etFilterNetChange)
        etFilterClosingBal = findViewById(R.id.etFilterClosingBal)
    }

    private fun toggleFilterVisibility() {
        isFilterVisible = !isFilterVisible
        val filterRow = findViewById<View>(R.id.filterRow)
        val headerRow = findViewById<View>(R.id.headerRow)

        filterRow.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        headerRow.visibility = if (isFilterVisible) View.GONE else View.VISIBLE

        if (isFilterVisible) {
            etFilterGlCode.removeTextChangedListener(filterTextWatcher)
            etFilterGlCode.addTextChangedListener(filterTextWatcher)

            etFilterAcctName.removeTextChangedListener(filterTextWatcher)
            etFilterAcctName.addTextChangedListener(filterTextWatcher)

            etFilterOpeningBal.removeTextChangedListener(filterTextWatcher)
            etFilterOpeningBal.addTextChangedListener(filterTextWatcher)

            etFilterCredit.removeTextChangedListener(filterTextWatcher)
            etFilterCredit.addTextChangedListener(filterTextWatcher)

            etFilterDebit.removeTextChangedListener(filterTextWatcher)
            etFilterDebit.addTextChangedListener(filterTextWatcher)

            etFilterNetChange.removeTextChangedListener(filterTextWatcher)
            etFilterNetChange.addTextChangedListener(filterTextWatcher)

            etFilterClosingBal.removeTextChangedListener(filterTextWatcher)
            etFilterClosingBal.addTextChangedListener(filterTextWatcher)
        } else {
            // Clear all fields
            etFilterGlCode.text.clear()
            etFilterAcctName.text.clear()
            etFilterOpeningBal.text.clear()
            etFilterCredit.text.clear()
            etFilterDebit.text.clear()
            etFilterNetChange.text.clear()
            etFilterClosingBal.text.clear()
            
            applyFilters()

            // Hide keyboard
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
        }
    }

    private fun applyFilters() {
        adapter.applyFilters(
            glCode = etFilterGlCode.text.toString(),
            acctName = etFilterAcctName.text.toString(),
            openingBal = etFilterOpeningBal.text.toString(),
            credit = etFilterCredit.text.toString(),
            debit = etFilterDebit.text.toString(),
            netChange = etFilterNetChange.text.toString(),
            closingBal = etFilterClosingBal.text.toString()
        )
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

    private fun setupRecyclerViews() {
        adapter = TrialBalanceAdapter(dataList) { item ->
            fetchGLSHDetails(item.glCode)
        }
        rvTrialBalance.layoutManager = LinearLayoutManager(this)
        rvTrialBalance.adapter = adapter

        glshAdapter = GLSHDetailAdapter(glshList)
        rvGLSHDetails.layoutManager = LinearLayoutManager(this)
        rvGLSHDetails.adapter = glshAdapter
    }

    private fun setupDatePicker() {
        layoutDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            try {
                val current = displayDateFormat.parse(tvSelectedDate.text.toString())
                if (current != null) calendar.time = current
            } catch (e: Exception) {}

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val cal = Calendar.getInstance()
                cal.set(selectedYear, selectedMonth, selectedDay)
                tvSelectedDate.text = displayDateFormat.format(cal.time)
                fetchTrialBalance(apiDateFormat.format(cal.time))
            }, year, month, day)
            
            // Prevent future dates as in web
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun fetchInitialTrialBalance() {
        layoutGLSH.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getTrialBalanceList("list")
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    body.trialbal?.let { processRawData(it) }
                } else {
                    // If initial list fails, try fetching for today
                    val today = Calendar.getInstance()
                    fetchTrialBalance(apiDateFormat.format(today.time))
                }
            } catch (e: Exception) {
                Log.e("TrialBalance", "Initial Load Error", e)
                val today = Calendar.getInstance()
                fetchTrialBalance(apiDateFormat.format(today.time))
            }
        }
    }

    private fun fetchTrialBalance(date: String) {
        layoutGLSH.visibility = View.GONE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getTrialBalanceReports(date)
                if (response.isSuccessful && response.body() != null) {
                    processRawData(response.body()!!)
                } else {
                    Toast.makeText(this@TrialBalanceActivity, "Failed to fetch data for $date", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TrialBalance", "API Error", e)
                Toast.makeText(this@TrialBalanceActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processRawData(rawData: List<List<Any>>) {
        dataList.clear()
        var totalCredits = 0.0
        var totalDebits = 0.0

        if (rawData.isEmpty()) {
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show()
        }

        for (row in rawData) {
            try {
                // 0: gl_code, 1: acct_name, 2: opening_bal, 3: credit, 4: debit, 5: net_change, 6: closing_bal
                val item = TrialBalanceItem(
                    glCode = row[0].toString(),
                    acctName = row[1].toString(),
                    openingBal = row[2].toString().toDoubleOrNull() ?: 0.0,
                    credit = row[3].toString().toDoubleOrNull() ?: 0.0,
                    debit = row[4].toString().toDoubleOrNull() ?: 0.0,
                    netChange = row[5].toString().toDoubleOrNull() ?: 0.0,
                    closingBal = row[6].toString().toDoubleOrNull() ?: 0.0
                )
                dataList.add(item)
                totalCredits += item.credit
                totalDebits += item.debit
            } catch (e: Exception) {
                Log.e("TrialBalance", "Error parsing row: $row", e)
            }
        }

        isDataLoaded = true
        adapter.setFullData(dataList)
        applyFilters()
    }

    // Called by the adapter to update totals after filtering
    fun updateTotals(filteredList: List<TrialBalanceItem>) {
        var totalCredits = 0.0
        var totalDebits = 0.0
        for (item in filteredList) {
            totalCredits += item.credit
            totalDebits += item.debit
        }
        tvTotalCredits.text = decimalFormat.format(totalCredits)
        tvTotalDebits.text = decimalFormat.format(totalDebits)
    }

    private fun fetchGLSHDetails(glshCode: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getGLSHListData(glshCode)
                if (response.isSuccessful && response.body() != null) {
                    glshList.clear()
                    glshList.addAll(response.body()!!)
                    glshAdapter.notifyDataSetChanged()
                    layoutGLSH.visibility = View.VISIBLE
                    
                    // Scroll to GLSH section
                    layoutGLSH.requestFocus()
                } else {
                    Toast.makeText(this@TrialBalanceActivity, "No data found for GLSH: $glshCode", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TrialBalance", "GLSH API Error", e)
                Toast.makeText(this@TrialBalanceActivity, "Error fetching GLSH details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class TrialBalanceAdapter(
        private var list: List<TrialBalanceItem>,
        private val onItemClick: (TrialBalanceItem) -> Unit
    ) : RecyclerView.Adapter<TrialBalanceAdapter.ViewHolder>() {
        
        private var allItems = list.toList()

        fun setFullData(newList: List<TrialBalanceItem>) {
            allItems = newList.toList()
            list = newList
            notifyDataSetChanged()
        }

        fun applyFilters(
            glCode: String,
            acctName: String,
            openingBal: String,
            credit: String,
            debit: String,
            netChange: String,
            closingBal: String
        ) {
            val filtered = allItems.filter { item ->
                val matchGl = glCode.isBlank() || item.glCode.contains(glCode.trim(), true)
                val matchName = acctName.isBlank() || item.acctName.contains(acctName.trim(), true)
                
                val itemOpenStr = decimalFormat.format(item.openingBal)
                val matchOpen = openingBal.isBlank() || itemOpenStr.contains(openingBal.trim(), true)
                
                val itemCreditStr = decimalFormat.format(item.credit)
                val matchCredit = credit.isBlank() || itemCreditStr.contains(credit.trim(), true)
                
                val itemDebitStr = decimalFormat.format(item.debit)
                val matchDebit = debit.isBlank() || itemDebitStr.contains(debit.trim(), true)
                
                val itemNetStr = decimalFormat.format(item.netChange)
                val matchNet = netChange.isBlank() || itemNetStr.contains(netChange.trim(), true)
                
                val itemCloseStr = decimalFormat.format(item.closingBal)
                val matchClose = closingBal.isBlank() || itemCloseStr.contains(closingBal.trim(), true)

                matchGl && matchName && matchOpen && matchCredit && matchDebit && matchNet && matchClose
            }
            
            list = filtered
            notifyDataSetChanged()
            updateTotals(filtered)
        }
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlCode: TextView = view.findViewById(R.id.tvGlCode)
            val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
            val tvOpeningBal: TextView = view.findViewById(R.id.tvOpeningBal)
            val tvCredit: TextView = view.findViewById(R.id.tvCredit)
            val tvDebit: TextView = view.findViewById(R.id.tvDebit)
            val tvNetChange: TextView = view.findViewById(R.id.tvNetChange)
            val tvClosingBal: TextView = view.findViewById(R.id.tvClosingBal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trial_balance, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlCode.text = item.glCode
            holder.tvAccountName.text = item.acctName
            
            // Format to millions if needed, or just format decimal. 
            // Web divides by 1,000,000. Let's do the same for consistency if values are large.
            // But usually trial balance shows full values. I'll stick to full values for now.
            holder.tvOpeningBal.text = decimalFormat.format(item.openingBal)
            holder.tvCredit.text = decimalFormat.format(item.credit)
            holder.tvDebit.text = decimalFormat.format(item.debit)
            holder.tvNetChange.text = decimalFormat.format(item.netChange)
            holder.tvClosingBal.text = decimalFormat.format(item.closingBal)
            
            // Zebra striping
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
            
            holder.tvGlCode.setOnClickListener { onItemClick(item) }
        }

        override fun getItemCount() = list.size
    }

    inner class GLSHDetailAdapter(private val list: List<ChartAccountApiItem>) : RecyclerView.Adapter<GLSHDetailAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlshCode: TextView = view.findViewById(R.id.tvGlshCode)
            val tvAcctNum: TextView = view.findViewById(R.id.tvAcctNum)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvCredit: TextView = view.findViewById(R.id.tvCredit)
            val tvDebit: TextView = view.findViewById(R.id.tvDebit)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_glsh_detail, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlshCode.text = item.glsh_code
            holder.tvAcctNum.text = item.acct_num
            holder.tvAcctName.text = item.acct_name
            
            val bal = item.acct_bal?.toDoubleOrNull() ?: 0.0
            if (bal > 0) {
                holder.tvCredit.text = decimalFormat.format(bal)
                holder.tvDebit.text = "0.00"
            } else {
                holder.tvCredit.text = "0.00"
                holder.tvDebit.text = decimalFormat.format(Math.abs(bal))
            }
            
            // Zebra striping
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
        }

        override fun getItemCount() = list.size
    }
}

