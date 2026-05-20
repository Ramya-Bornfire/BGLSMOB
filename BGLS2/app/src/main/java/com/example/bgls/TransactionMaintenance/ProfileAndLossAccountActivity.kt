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
import android.content.Intent
import android.widget.ImageView
import com.example.bgls.MainActivity

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
        setupNavigation()
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

    private fun setupListeners() {
        llDatePicker.setOnClickListener { showDatePicker() }
        
        findViewById<android.widget.Button>(R.id.btnFilterIncome).setOnClickListener {
            val filters = findViewById<LinearLayout>(R.id.llIncomeFilters)
            val header = findViewById<LinearLayout>(R.id.incomeHeaderRow)
            val isVisible = filters.visibility == View.VISIBLE
            filters.visibility = if (isVisible) View.GONE else View.VISIBLE
            header.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) {
                // Closing filter: clear all income filter fields and reset list
                findViewById<android.widget.EditText>(R.id.etFilterIncomeGl).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterIncomeId).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterIncomeName).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterIncomeCurrency).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterIncomeAmount).text.clear()
                incomeList.clear()
                incomeList.addAll(fullIncomeList)
                updateUI()
            }
        }
        
        findViewById<android.widget.Button>(R.id.btnFilterExpenditure).setOnClickListener {
            val filters = findViewById<LinearLayout>(R.id.llExpenditureFilters)
            val header = findViewById<LinearLayout>(R.id.expenditureHeaderRow)
            val isVisible = filters.visibility == View.VISIBLE
            filters.visibility = if (isVisible) View.GONE else View.VISIBLE
            header.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) {
                // Closing filter: clear all expenditure filter fields and reset list
                findViewById<android.widget.EditText>(R.id.etFilterExpGl).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterExpId).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterExpName).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterExpCurrency).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterExpAmount).text.clear()
                expenditureList.clear()
                expenditureList.addAll(fullExpenditureList)
                updateUI()
            }
        }
    }

    private fun setupFilterListeners() {
        val incomeGl = findViewById<android.widget.EditText>(R.id.etFilterIncomeGl)
        val incomeId = findViewById<android.widget.EditText>(R.id.etFilterIncomeId)
        val incomeName = findViewById<android.widget.EditText>(R.id.etFilterIncomeName)
        val incomeCurrency = findViewById<android.widget.EditText>(R.id.etFilterIncomeCurrency)
        val incomeAmount = findViewById<android.widget.EditText>(R.id.etFilterIncomeAmount)
        
        val filterIncome = {
            val gl = incomeGl.text.toString().trim().lowercase()
            val id = incomeId.text.toString().trim().lowercase()
            val name = incomeName.text.toString().trim().lowercase()
            val cur = incomeCurrency.text.toString().trim().lowercase()
            val amt = incomeAmount.text.toString().trim()
            
            incomeList.clear()
            incomeList.addAll(fullIncomeList.filter { item ->
                (gl.isBlank() || item.glHead.lowercase().contains(gl)) &&
                (id.isBlank() || item.acctId.lowercase().contains(id)) &&
                (name.isBlank() || item.acctName.lowercase().contains(name)) &&
                (cur.isBlank() || item.currency.lowercase().contains(cur)) &&
                (amt.isBlank() || decimalFormat.format(item.amount).contains(amt))
            })
            updateUI()
        }
        
        incomeGl.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeId.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeName.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeCurrency.addTextChangedListener(SearchTextWatcher { filterIncome() })
        incomeAmount.addTextChangedListener(SearchTextWatcher { filterIncome() })
        
        val expGl = findViewById<android.widget.EditText>(R.id.etFilterExpGl)
        val expId = findViewById<android.widget.EditText>(R.id.etFilterExpId)
        val expName = findViewById<android.widget.EditText>(R.id.etFilterExpName)
        val expCurrency = findViewById<android.widget.EditText>(R.id.etFilterExpCurrency)
        val expAmount = findViewById<android.widget.EditText>(R.id.etFilterExpAmount)
        
        val filterExp = {
            val gl = expGl.text.toString().trim().lowercase()
            val id = expId.text.toString().trim().lowercase()
            val name = expName.text.toString().trim().lowercase()
            val cur = expCurrency.text.toString().trim().lowercase()
            val amt = expAmount.text.toString().trim()
            
            expenditureList.clear()
            expenditureList.addAll(fullExpenditureList.filter { item ->
                (gl.isBlank() || item.glHead.lowercase().contains(gl)) &&
                (id.isBlank() || item.acctId.lowercase().contains(id)) &&
                (name.isBlank() || item.acctName.lowercase().contains(name)) &&
                (cur.isBlank() || item.currency.lowercase().contains(cur)) &&
                (amt.isBlank() || decimalFormat.format(item.amount).contains(amt))
            })
            updateUI()
        }
        
        expGl.addTextChangedListener(SearchTextWatcher { filterExp() })
        expId.addTextChangedListener(SearchTextWatcher { filterExp() })
        expName.addTextChangedListener(SearchTextWatcher { filterExp() })
        expCurrency.addTextChangedListener(SearchTextWatcher { filterExp() })
        expAmount.addTextChangedListener(SearchTextWatcher { filterExp() })
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