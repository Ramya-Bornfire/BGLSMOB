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
import com.example.bgls.R
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Locale
import android.content.Intent
import android.widget.ImageView
import android.view.View
import com.example.bgls.MainActivity

class TransactionsReversalActivity : AppCompatActivity() {

    private lateinit var rvTransactionsReversal: RecyclerView
    //private lateinit var spinnerFilter: Spinner
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var tvPageInfo: TextView
    private lateinit var adapter: TransactionsReversalAdapter
    private var dataList = mutableListOf<ReversalTransactionModel>()

    private lateinit var btnFilter: Button
    private lateinit var headerRow: android.widget.LinearLayout
    private lateinit var filterRow: android.widget.LinearLayout

    // Filter EditTexts
    private lateinit var etFilterTranDate: android.widget.EditText
    private lateinit var etFilterTranId: android.widget.EditText
    private lateinit var etFilterPaTranTy: android.widget.EditText
    private lateinit var etFilterCurrency: android.widget.EditText
    private lateinit var etFilterAmount: android.widget.EditText
    private lateinit var etFilterAcctId: android.widget.EditText
    private lateinit var etFilterAcctName: android.widget.EditText
    private lateinit var etFilterTranParticular: android.widget.EditText
    private lateinit var etFilterStatus: android.widget.EditText

    private var isFilterVisible = false

    private var currentPage = 1
    private val pageSize = 200
    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_reversal)

        initViews()
        setupNavigation()
        //setupSpinner()
        setupFilterActions()
        
        adapter = TransactionsReversalAdapter(
            context = this,
            initialList = dataList,
            onAcctIdClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, TransactionsReversalViewActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            },
            onSelectClick = { position ->
                val item = dataList[position]
                val intent = android.content.Intent(this, TransactionsReversalEditActivity::class.java)
                intent.putExtra("tran_id", item.tranId.split("/")[0])
                intent.putExtra("part_tran_id", item.tranId.split("/").getOrNull(1) ?: "")
                intent.putExtra("acct_num", item.acctId)
                startActivity(intent)
            }
        )
        rvTransactionsReversal.layoutManager = LinearLayoutManager(this)
        rvTransactionsReversal.adapter = adapter

        fetchDataFromApi()

        btnPrev.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                fetchDataFromApi()
            } else {
                Toast.makeText(this, "Already on first page", Toast.LENGTH_SHORT).show()
            }
        }
        btnNext.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                fetchDataFromApi()
            } else {
                Toast.makeText(this, "No more pages", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        rvTransactionsReversal = findViewById(R.id.rvTransactionsReversal)
       // spinnerFilter = findViewById(R.id.spinnerFilter)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnFilter = findViewById(R.id.btnFilter)
        headerRow = findViewById(R.id.headerRow)
        filterRow = findViewById(R.id.filterRow)

        // Pre-cache filter fields
        etFilterTranDate = findViewById(R.id.etFilterTranDate)
        etFilterTranId = findViewById(R.id.etFilterTranId)
        etFilterPaTranTy = findViewById(R.id.etFilterPaTranTy)
        etFilterCurrency = findViewById(R.id.etFilterCurrency)
        etFilterAmount = findViewById(R.id.etFilterAmount)
        etFilterAcctId = findViewById(R.id.etFilterAcctId)
        etFilterAcctName = findViewById(R.id.etFilterAcctName)
        etFilterTranParticular = findViewById(R.id.etFilterTranParticular)
        etFilterStatus = findViewById(R.id.etFilterStatus)

        // Standardize filter fields
        val allFilters = listOf(
            etFilterTranDate, etFilterTranId, etFilterPaTranTy, etFilterCurrency,
            etFilterAmount, etFilterAcctId, etFilterAcctName, etFilterTranParticular, etFilterStatus
        )
        allFilters.forEach { et ->
            et.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            et.imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            et.setSingleLine(true)
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

//    private fun setupSpinner() {
//        val options = arrayOf("Select Filter", "Date", "ID", "Amount")
//        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
//        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerFilter.adapter = spinnerAdapter
//    }

    private fun setupFilterActions() {
        btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            headerRow.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            filterRow.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
            
            if (!isFilterVisible) {
                clearAllFilters()
            } else {
                applyFilters()
            }
        }

        val filters = listOf(
            etFilterTranDate, etFilterTranId, etFilterPaTranTy, etFilterCurrency,
            etFilterAmount, etFilterAcctId, etFilterAcctName, etFilterTranParticular, etFilterStatus
        )

        filters.forEach { et ->
            et.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyFilters()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun applyFilters() {
        adapter.filter(
            etFilterTranDate.text.toString().trim(),
            etFilterTranId.text.toString().trim(),
            etFilterPaTranTy.text.toString().trim(),
            etFilterCurrency.text.toString().trim(),
            etFilterAmount.text.toString().trim(),
            etFilterAcctId.text.toString().trim(),
            etFilterAcctName.text.toString().trim(),
            etFilterTranParticular.text.toString().trim(),
            etFilterStatus.text.toString().trim()
        )
    }

    private fun clearAllFilters() {
        etFilterTranDate.text.clear()
        etFilterTranId.text.clear()
        etFilterPaTranTy.text.clear()
        etFilterCurrency.text.clear()
        etFilterAmount.text.clear()
        etFilterAcctId.text.clear()
        etFilterAcctName.text.clear()
        etFilterTranParticular.text.clear()
        etFilterStatus.text.clear()
        applyFilters()
    }

    private fun fetchDataFromApi() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getReversalList(currentPage, pageSize)
                if (response.isSuccessful && response.body() != null) {
                    val reversalResponse = response.body()!!
                    totalPages = reversalResponse.totalPages
                    
                    val newData = reversalResponse.data.map { item ->
                        ReversalTransactionModel(
                            tranDate = formatDate(item.tran_date),
                            tranId = "${item.tran_id}/${item.part_tran_id}",
                            paTranTy = item.part_tran_type ?: "",
                            currency = item.acct_crncy ?: "",
                            amount = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0),
                            acctId = item.acct_num ?: "",
                            acctName = item.acct_name ?: "",
                            tranParticular = item.tran_particular ?: "",
                            status = item.tran_status ?: ""
                        )
                    }
                    dataList.clear()
                    dataList.addAll(newData)
                    adapter.updateList(newData)
                    tvPageInfo.text = "Page $currentPage of $totalPages"
                } else {
                    Toast.makeText(this@TransactionsReversalActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TransactionsReversalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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