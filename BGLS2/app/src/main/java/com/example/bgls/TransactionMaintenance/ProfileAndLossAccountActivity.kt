package com.example.bgls.TransactionMaintenance

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ChartAccountApiItem
import com.example.bgls.DataModels.DABItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ProfitLossUIModel(
    val glHead: String,
    val acctId: String,
    val acctName: String,
    val currency: String,
    val amount: Double
)

class ProfileAndLossAccountActivity : AppCompatActivity() {

    private lateinit var rvIncome: RecyclerView
    private lateinit var rvExpenditure: RecyclerView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenditure: TextView
    private lateinit var tvReportStatus: TextView
    private lateinit var tvProfitAmount: TextView
    private lateinit var tvProfitAmountLabel: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var llDatePicker: LinearLayout

    private lateinit var incomeAdapter: ProfitLossAdapter
    private lateinit var expenditureAdapter: ProfitLossAdapter
    private var incomeList = mutableListOf<ProfitLossUIModel>()
    private var expenditureList = mutableListOf<ProfitLossUIModel>()
    
    private var fullIncomeList = mutableListOf<ProfitLossUIModel>()
    private var fullExpenditureList = mutableListOf<ProfitLossUIModel>()
    
    private val decimalFormat = DecimalFormat("#,##0.00")
    private val sdfApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val sdfUI = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_and_loss_account)

        initViews()
        setupAdapters()
        setupListeners()
        setupFilterListeners()

        // Initial fetch
        fetchInitialProfitLoss()
    }

    private fun initViews() {
        rvIncome = findViewById(R.id.rvIncome)
        rvExpenditure = findViewById(R.id.rvExpenditure)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenditure = findViewById(R.id.tvTotalExpenditure)
        tvReportStatus = findViewById(R.id.tvReportStatus)
        tvProfitAmount = findViewById(R.id.tvProfitAmount)
        tvProfitAmountLabel = findViewById(R.id.tvProfitAmountLabel)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        llDatePicker = findViewById(R.id.llDatePicker)
    }

    private fun setupAdapters() {
        incomeAdapter = ProfitLossAdapter(incomeList)
        rvIncome.layoutManager = LinearLayoutManager(this)
        rvIncome.adapter = incomeAdapter

        expenditureAdapter = ProfitLossAdapter(expenditureList)
        rvExpenditure.layoutManager = LinearLayoutManager(this)
        rvExpenditure.adapter = expenditureAdapter
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        
        llDatePicker.setOnClickListener { showDatePicker() }
        
        findViewById<View>(R.id.btnFilterIncome).setOnClickListener {
            val filters = findViewById<LinearLayout>(R.id.llIncomeFilters)
            filters.visibility = if (filters.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        
        findViewById<View>(R.id.btnFilterExpenditure).setOnClickListener {
            val filters = findViewById<LinearLayout>(R.id.llExpenditureFilters)
            filters.visibility = if (filters.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    private fun setupFilterListeners() {
        val incomeGl = findViewById<android.widget.EditText>(R.id.etFilterIncomeGl)
        val incomeId = findViewById<android.widget.EditText>(R.id.etFilterIncomeId)
        val incomeName = findViewById<android.widget.EditText>(R.id.etFilterIncomeName)
        
        val filterIncome = {
            val gl = incomeGl.text.toString().lowercase()
            val id = incomeId.text.toString().lowercase()
            val name = incomeName.text.toString().lowercase()
            
            incomeList.clear()
            incomeList.addAll(fullIncomeList.filter { 
                it.glHead.lowercase().contains(gl) &&
                it.acctId.lowercase().contains(id) &&
                it.acctName.lowercase().contains(name)
            })
            incomeAdapter.notifyDataSetChanged()
            updateUI()
        }
        
        incomeGl.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeId.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeName.addTextChangedListener(SearchTextWatcher { filterIncome() })
        
        val expGl = findViewById<android.widget.EditText>(R.id.etFilterExpGl)
        val expId = findViewById<android.widget.EditText>(R.id.etFilterExpId)
        val expName = findViewById<android.widget.EditText>(R.id.etFilterExpName)
        
        val filterExp = {
            val gl = expGl.text.toString().lowercase()
            val id = expId.text.toString().lowercase()
            val name = expName.text.toString().lowercase()
            
            expenditureList.clear()
            expenditureList.addAll(fullExpenditureList.filter { 
                it.glHead.lowercase().contains(gl) &&
                it.acctId.lowercase().contains(id) &&
                it.acctName.lowercase().contains(name)
            })
            expenditureAdapter.notifyDataSetChanged()
            updateUI()
        }
        
        expGl.addTextChangedListener(SearchTextWatcher { filterExp() })
        expId.addTextChangedListener(SearchTextWatcher { filterExp() })
        expName.addTextChangedListener(SearchTextWatcher { filterExp() })
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, y, m, d ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(y, m, d)
            
            val apiDate = sdfApi.format(selectedCalendar.time)
            val uiDate = sdfUI.format(selectedCalendar.time)
            
            tvSelectedDate.text = uiDate
            tvSelectedDate.tag = apiDate // Store API format in tag
            
            // Automatic fetch like web
            fetchFilteredProfitLoss(apiDate)
            
        }, year, month, day)
        
        dpd.datePicker.maxDate = System.currentTimeMillis()
        dpd.show()
    }

    private fun fetchInitialProfitLoss() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getProfitAndLossAccount()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    fullIncomeList.clear()
                    data.balancesheet3?.forEach { 
                        fullIncomeList.add(mapChartItemToUI(it))
                    }
                    
                    fullExpenditureList.clear()
                    data.balancesheet4?.forEach { 
                        fullExpenditureList.add(mapChartItemToUI(it))
                    }
                    
                    incomeList.clear()
                    incomeList.addAll(fullIncomeList)
                    expenditureList.clear()
                    expenditureList.addAll(fullExpenditureList)
                    
                    updateUI()
                } else {
                    Toast.makeText(this@ProfileAndLossAccountActivity, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PLActivity", "Error: ${e.message}")
                Toast.makeText(this@ProfileAndLossAccountActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchFilteredProfitLoss(date: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getIncomeExpenditure(date)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    fullIncomeList.clear()
                    data.msg?.forEach { 
                        fullIncomeList.add(mapDABItemToUI(it))
                    }
                    
                    fullExpenditureList.clear()
                    data.msg1?.forEach { 
                        fullExpenditureList.add(mapDABItemToUI(it))
                    }
                    
                    incomeList.clear()
                    incomeList.addAll(fullIncomeList)
                    expenditureList.clear()
                    expenditureList.addAll(fullExpenditureList)
                    
                    updateUI()
                } else {
                    Toast.makeText(this@ProfileAndLossAccountActivity, "Failed to fetch filtered data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("PLActivity", "Error: ${e.message}")
                Toast.makeText(this@ProfileAndLossAccountActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapChartItemToUI(item: ChartAccountApiItem): ProfitLossUIModel {
        val amount = item.acct_bal?.replace(",", "")?.toDoubleOrNull() ?: 0.0
        return ProfitLossUIModel(
            glHead = "${item.glsh_code} - ${item.glsh_desc}",
            acctId = item.acct_num ?: "",
            acctName = item.acct_name ?: "",
            currency = item.acct_crncy ?: "",
            amount = Math.abs(amount)
        )
    }

    private fun mapDABItemToUI(item: DABItem): ProfitLossUIModel {
        return ProfitLossUIModel(
            glHead = "${item.glsh_code} - ${item.glsh_desc}",
            acctId = item.acct_num ?: "",
            acctName = item.acct_name ?: "",
            currency = item.acct_crncy ?: "",
            amount = Math.abs(item.tran_date_bal ?: 0.0)
        )
    }

    private fun updateUI() {
        incomeAdapter.notifyDataSetChanged()
        expenditureAdapter.notifyDataSetChanged()
        
        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpenditure = expenditureList.sumOf { it.amount }
        
        tvTotalIncome.text = decimalFormat.format(totalIncome)
        tvTotalExpenditure.text = decimalFormat.format(totalExpenditure)
        
        val difference = totalIncome - totalExpenditure
        if (difference >= 0) {
            tvReportStatus.text = "Profit"
            tvReportStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            tvProfitAmountLabel.text = "Profit Amount: "
            tvProfitAmount.text = decimalFormat.format(difference)
        } else {
            tvReportStatus.text = "Loss"
            tvReportStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            tvProfitAmountLabel.text = "Loss Amount: "
            tvProfitAmount.text = decimalFormat.format(Math.abs(difference))
        }
    }

    inner class ProfitLossAdapter(private val list: List<ProfitLossUIModel>) : RecyclerView.Adapter<ProfitLossAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlHead: TextView = view.findViewById(R.id.tvGlHead)
            val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profit_loss, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlHead.text = item.glHead
            holder.tvAcctId.text = item.acctId
            holder.tvAcctName.text = item.acctName
            holder.tvCurrency.text = item.currency
            holder.tvAmount.text = decimalFormat.format(item.amount)
        }

        override fun getItemCount() = list.size
    }
}

class SearchTextWatcher(val onSearch: () -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onSearch() }
    override fun afterTextChanged(s: android.text.Editable?) {}
}