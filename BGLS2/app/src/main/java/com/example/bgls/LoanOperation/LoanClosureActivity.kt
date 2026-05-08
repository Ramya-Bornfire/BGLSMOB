package com.example.bgls.LoanOperation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.LoanClosureDataResponse
import com.example.bgls.DataModels.LoanFlowDetail
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LoanClosureActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvAccountLabel: TextView
    private lateinit var tvBalanceLabel: TextView
    private lateinit var btnScheduler: Button
    private lateinit var btnLedger: Button
    private lateinit var btnPreClosureMode: Button
    private lateinit var btnClosureMode: Button
    private lateinit var btnSubmit: Button
    private lateinit var ivSearchAccount: ImageView
    private lateinit var etAccountId: EditText
    private lateinit var etAccountName: EditText
    private lateinit var etDisbursement: EditText
    private lateinit var etBalance: EditText
    private lateinit var etClosureBalance: EditText
    private lateinit var llRows: LinearLayout
    private lateinit var ivAddRow: ImageView
    private lateinit var ivRemoveRow: ImageView
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button

    private var isPreClosureMode = true
    private var currentAccountNo = ""
    private var totalFlowAmtFromDb = 0.0
    private val TAG = "LoanClosure"
    private val sdfUI = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private var initialCollection: List<List<Any>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_closure)
        initViews()
        setupListeners()
        setMode(true)
        fetchInitialData()
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvAccountLabel = findViewById(R.id.tvAccountLabel)
        tvBalanceLabel = findViewById(R.id.tvBalanceLabel)
        btnScheduler = findViewById(R.id.btnScheduler)
        btnLedger = findViewById(R.id.btnLedger)
        btnPreClosureMode = findViewById(R.id.btnPreClosureMode)
        btnClosureMode = findViewById(R.id.btnClosureMode)
        btnSubmit = findViewById(R.id.btnSubmit)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        etAccountId = findViewById(R.id.etAccountId)
        etAccountName = findViewById(R.id.etAccountName)
        etDisbursement = findViewById(R.id.etDisbursement)
        etBalance = findViewById(R.id.etBalance)
        etClosureBalance = findViewById(R.id.etClosureBalance)
        llRows = findViewById(R.id.llRows)
        ivAddRow = findViewById(R.id.ivAddRow)
        ivRemoveRow = findViewById(R.id.ivRemoveRow)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnPreClosureMode.setOnClickListener { setMode(true) }
        btnClosureMode.setOnClickListener { setMode(false) }
        ivSearchAccount.setOnClickListener { openAccountSearchDialog() }
        ivAddRow.setOnClickListener { addNewRow() }
        ivRemoveRow.setOnClickListener { removeLastRow() }
        btnSubmit.setOnClickListener { submitData() }
        btnScheduler.setOnClickListener { navigateToScheduler() }
        btnLedger.setOnClickListener { navigateToLedger() }
        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun setMode(isPreClosure: Boolean) {
        isPreClosureMode = isPreClosure
        clearForm()
        if (isPreClosure) {
            tvTitle.text = "LOAN PRE - CLOSURE"
            tvAccountLabel.text = "Account ID"
            tvBalanceLabel.text = "Loan Balance"
            btnPreClosureMode.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#0056b3"))
            btnClosureMode.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
        } else {
            tvTitle.text = "LOAN CLOSURE"
            tvAccountLabel.text = "Account No"
            tvBalanceLabel.text = "Account Balance"
            btnPreClosureMode.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnClosureMode.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#0056b3"))
        }
    }

    private fun clearForm() {
        etAccountId.setText("")
        etAccountName.setText("")
        etDisbursement.setText("")
        etBalance.setText("")
        etClosureBalance.setText("")
        currentAccountNo = ""
        totalFlowAmtFromDb = 0.0
        // Remove all dynamic rows, keep the static placeholder row from XML
        while (llRows.childCount > 1) {
            llRows.removeViewAt(llRows.childCount - 1)
        }
        // Clear the static placeholder row too
        if (llRows.childCount > 0) llRows.removeAllViews()
        btnSubmit.visibility = View.GONE
    }

    private fun fetchInitialData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loanClosure("list")
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d(TAG, "Initial data loaded: formmode=${data?.formmode}")
                    initialCollection = data?.collection
                    Log.d(TAG, "Collection size: ${initialCollection?.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchInitialData error: ${e.message}")
            }
        }
    }

    // ── Account Search Dialog ──────────────────────────────────────────────
    private fun openAccountSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etSearchAccNo = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val btnFilter = dialogView.findViewById<Button>(R.id.btnFilter)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val tlAccounts = dialogView.findViewById<TableLayout>(R.id.tlAccounts)

        fun populateSearchTable(list: List<List<Any>>) {
            tlAccounts.removeAllViews()
            for (acc in list) {
                if (acc.size < 2) continue
                val row = TableRow(this)
                val tvNo = TextView(this).apply {
                    text = acc[0].toString(); textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvName = TextView(this).apply {
                    text = acc[1].toString(); textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                }
                row.addView(tvNo); row.addView(tvName)
                row.setOnClickListener {
                    onAccountSelected(acc[0].toString(), acc[1].toString())
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
            if (list.isEmpty()) {
                val emptyRow = TableRow(this)
                val tvEmpty = TextView(this).apply {
                    text = "No Records Found"; textSize = 12f; setTextColor(Color.RED)
                    setPadding(16, 16, 16, 16); gravity = Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT
                    )
                }
                emptyRow.addView(tvEmpty)
                tlAccounts.addView(emptyRow)
            }
        }

        // Initial load - show collection from loanClosure API
        if (initialCollection != null && initialCollection!!.isNotEmpty()) {
            populateSearchTable(initialCollection!!)
        } else {
            loadAccounts("") { populateSearchTable(it) }
        }

        btnFilter.setOnClickListener {
            etSearchAccNo.setText("")
            etSearchAccNo.isEnabled = true
            etSearchAccNo.requestFocus()
        }

        etSearchAccNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 2) {
                    loadAccounts(query) { populateSearchTable(it) }
                } else if (query.isEmpty() && initialCollection != null) {
                    populateSearchTable(initialCollection!!)
                }
            }
        })

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun loadAccounts(
        value: String,
        callback: (List<List<Any>>) -> Unit
    ) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.search(value)
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    Log.d(TAG, "Search returned ${list.size} results")
                    callback(list)
                } else {
                    Log.e(TAG, "Search failed: ${response.code()} ${response.errorBody()?.string()}")
                    callback(emptyList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Account search error: ${e.message}", e)
                callback(emptyList())
            }
        }
    }

    // ── Account Selected ───────────────────────────────────────────────────
    private fun onAccountSelected(accountNo: String, accountName: String) {
        currentAccountNo = accountNo
        etAccountId.setText(accountNo)
        etAccountName.setText(accountName)
        btnSubmit.visibility = View.VISIBLE
        fetchAccountData(accountNo)
    }

    private fun fetchAccountData(accountNo: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api

                // 1. Fetch account balance
                val balResp = api.fetchAccountBalance(accountNo)
                if (balResp.isSuccessful) {
                    val bal = balResp.body()?.toDoubleOrNull() ?: 0.0
                    etBalance.setText(String.format("%.2f", bal))
                }

                // 2. Fetch disbursement balance
                val disbResp = api.fetchDisbursementBalance(accountNo)
                if (disbResp.isSuccessful) {
                    val disb = disbResp.body()?.toDoubleOrNull() ?: 0.0
                    etDisbursement.setText(String.format("%.2f", disb))
                }

                // 3. Fetch flow data
                val flowResp = if (isPreClosureMode) {
                    api.getPreclosureFlowData(accountNo)
                } else {
                    api.getClosureFlowData(accountNo)
                }

                if (flowResp.isSuccessful) {
                    val data = flowResp.body()
                    if (data != null) {
                        totalFlowAmtFromDb = data.flowTotalAmt
                        etClosureBalance.setText(String.format("%.2f", data.flowTotalAmt))
                        populateTable(data.loanFlows)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAccountData error: ${e.message}")
                Toast.makeText(this@LoanClosureActivity, "Error loading account data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Populate Table ─────────────────────────────────────────────────────
    private fun populateTable(flows: List<LoanFlowDetail>?) {
        llRows.removeAllViews()
        if (flows.isNullOrEmpty()) return

        for (flow in flows) {
            val flowAmt = flow.flowAmt
            val formattedFlowAmt = String.format("%.2f", flowAmt)

            val row = createTableRow(
                flowDate = formatFlowDate(flow.flowDate),
                flowCode = flow.flowCode,
                flowAmtStr = formattedFlowAmt,
                tranAmtStr = formattedFlowAmt,
                waiverStr = "0.00",
                additionalStr = "0.00",
                isEditable = true,
                isDeletable = false
            )
            llRows.addView(row)
        }
    }

    private fun createTableRow(
        flowDate: String, flowCode: String, flowAmtStr: String,
        tranAmtStr: String, waiverStr: String, additionalStr: String,
        isEditable: Boolean, isDeletable: Boolean
    ): LinearLayout {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            tag = "data_row"
        }

        val weights = listOf(1f, 1f, 1.2f, 1.2f, 1.2f, 1.5f)
        val values = listOf(flowDate, flowCode, flowAmtStr, tranAmtStr, waiverStr, additionalStr)

        for ((index, w) in weights.withIndex()) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), w)
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 10f
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                gravity = if (index >= 2) Gravity.END else Gravity.CENTER
                setText(values[index])
                // flow_date (0), flow_code (1), flow_amt (2) are readonly
                // tran_amt (3) is editable
                // waiver (4) and additional (5) are readonly (calculated)
                isFocusable = (index == 3 && isEditable)
                isFocusableInTouchMode = (index == 3 && isEditable)
                isClickable = (index == 3 && isEditable)
            }

            // Tran Amt change listener → calculate waiver/additional
            if (index == 3 && isEditable) {
                et.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        calculateAmounts(row)
                    }
                })
            }
            row.addView(et)
        }
        return row
    }

    private fun calculateAmounts(row: LinearLayout) {
        val flowAmtEt = row.getChildAt(2) as EditText
        val tranAmtEt = row.getChildAt(3) as EditText
        val waiverEt = row.getChildAt(4) as EditText
        val additionalEt = row.getChildAt(5) as EditText

        val flowAmt = flowAmtEt.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
        val tranAmt = tranAmtEt.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0

        if (isPreClosureMode) {
            // Pre-closure: tran_amt cannot exceed flow_amt for existing rows
            if (flowAmt > 0 && tranAmt > flowAmt) {
                tranAmtEt.setText(String.format("%.2f", flowAmt))
                waiverEt.setText("0.00")
                additionalEt.setText("0.00")
                Toast.makeText(this, "Amount exceeds flow amount", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Calculate waiver and additional
        if (flowAmt == 0.0 && tranAmt > 0) {
            waiverEt.setText("0.00")
            additionalEt.setText(String.format("%.2f", tranAmt))
        } else if (tranAmt < flowAmt) {
            waiverEt.setText(String.format("%.2f", flowAmt - tranAmt))
            additionalEt.setText("0.00")
        } else if (tranAmt > flowAmt) {
            additionalEt.setText(String.format("%.2f", tranAmt - flowAmt))
            waiverEt.setText("0.00")
        } else {
            waiverEt.setText("0.00")
            additionalEt.setText("0.00")
        }
    }

    // ── Add / Remove Rows ──────────────────────────────────────────────────
    private fun addNewRow() {
        if (currentAccountNo.isEmpty()) {
            Toast.makeText(this, "Select an account first", Toast.LENGTH_SHORT).show()
            return
        }

        val deletableCount = (0 until llRows.childCount).count {
            (llRows.getChildAt(it) as? LinearLayout)?.tag == "deletable_row"
        }
        if (deletableCount >= 3) {
            Toast.makeText(this, "Cannot add more than 3 rows", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = if (isPreClosureMode) {
                    api.getLoanClosureDatas(currentAccountNo)
                } else {
                    api.getClosureAddRowData(currentAccountNo)
                }

                if (response.isSuccessful) {
                    val data = response.body()
                    val tranDate = formatFlowDate(data?.tranDate ?: sdfUI.format(Date()))

                    val row = createTableRow(
                        flowDate = tranDate,
                        flowCode = "",
                        flowAmtStr = "0.00",
                        tranAmtStr = "0.00",
                        waiverStr = "0.00",
                        additionalStr = "0.00",
                        isEditable = true,
                        isDeletable = true
                    )
                    row.tag = "deletable_row"

                    // Make flow_code editable with spinner-like behavior
                    val flowCodeEt = row.getChildAt(1) as EditText
                    flowCodeEt.isFocusable = false
                    flowCodeEt.setOnClickListener { showFlowCodePicker(flowCodeEt) }

                    // Make tran_amt fully editable
                    val tranAmtEt = row.getChildAt(3) as EditText
                    tranAmtEt.isFocusable = true
                    tranAmtEt.isFocusableInTouchMode = true
                    tranAmtEt.setText("")

                    llRows.addView(row)
                }
            } catch (e: Exception) {
                Log.e(TAG, "addNewRow error: ${e.message}")
                Toast.makeText(this@LoanClosureActivity, "Error adding row", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFlowCodePicker(target: EditText) {
        val codes = arrayOf("FEEDEM", "PENDEM", "INDEM")
        AlertDialog.Builder(this)
            .setTitle("Select Flow Code")
            .setItems(codes) { _, which -> target.setText(codes[which]) }
            .show()
    }

    private fun removeLastRow() {
        for (i in llRows.childCount - 1 downTo 0) {
            val child = llRows.getChildAt(i) as? LinearLayout
            if (child?.tag == "deletable_row") {
                llRows.removeViewAt(i)
                return
            }
        }
        Toast.makeText(this, "No rows to delete", Toast.LENGTH_SHORT).show()
    }

    // ── Submit ─────────────────────────────────────────────────────────────
    private fun submitData() {
        if (currentAccountNo.isEmpty()) {
            Toast.makeText(this, "Select an account first", Toast.LENGTH_SHORT).show()
            return
        }

        // Confirmation dialog
        AlertDialog.Builder(this)
            .setMessage("Do you want to continue?")
            .setPositiveButton("Yes") { _, _ -> performSubmit() }
            .setNegativeButton("No") { d, _ -> d.dismiss() }
            .show()
    }

    private fun performSubmit() {
        val rowDataList = mutableListOf<Map<String, Any>>()

        for (i in 0 until llRows.childCount) {
            val row = llRows.getChildAt(i) as? LinearLayout ?: continue
            val flowDate = (row.getChildAt(0) as EditText).text.toString()
            val flowCode = (row.getChildAt(1) as EditText).text.toString()
            val flowAmt = (row.getChildAt(2) as EditText).text.toString().replace(",", "")
            val tranAmt = (row.getChildAt(3) as EditText).text.toString().replace(",", "")
            val waiver = (row.getChildAt(4) as EditText).text.toString().replace(",", "")
            val additional = (row.getChildAt(5) as EditText).text.toString().replace(",", "")

            rowDataList.add(
                mapOf(
                    "flow_date" to flowDate,
                    "flow_code" to flowCode,
                    "flow_amt" to (flowAmt.ifEmpty { "0" }),
                    "tran_amt" to (tranAmt.ifEmpty { "0" }),
                    "waiver_total_amt" to (waiver.ifEmpty { "0" }),
                    "additional_amt" to (additional.ifEmpty { "0" })
                )
            )
        }

        val data = mapOf<String, Any>(
            "list" to rowDataList,
            "acct_num" to currentAccountNo,
            "total_flow_amt_db" to totalFlowAmtFromDb.toString()
        )

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = if (isPreClosureMode) {
                    api.saveLoanpreClosureDetails(data)
                } else {
                    api.saveLoanClosureDetails(data)
                }

                if (response.isSuccessful) {
                    val msg = response.body() ?: "Success"
                    AlertDialog.Builder(this@LoanClosureActivity)
                        .setMessage(msg)
                        .setPositiveButton("Close") { d, _ ->
                            d.dismiss()
                            clearForm()
                            setMode(isPreClosureMode)
                        }
                        .show()
                } else {
                    Toast.makeText(this@LoanClosureActivity,
                        "Error: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Submit error: ${e.message}")
                Toast.makeText(this@LoanClosureActivity,
                    "Error saving data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Scheduler Navigation ───────────────────────────────────────────────
    private fun navigateToScheduler() {
        val accNo = currentAccountNo
        if (accNo.isEmpty()) {
            Toast.makeText(this, "Account number is required!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.fetchLoanDetails(accNo)
                if (response.isSuccessful) {
                    val data = response.body()
                    val holderKey = data?.get("account_holderkey")?.toString() ?: ""
                    val encodedKey = data?.get("encoded_key")?.toString() ?: ""
                    val id = data?.get("id")?.toString() ?: accNo

                    if (holderKey.isNotEmpty() && encodedKey.isNotEmpty()) {
                        try {
                            val cls = Class.forName(
                                "com.example.bgls.LoanSchedule.LoanScheduleViewActivity"
                            )
                            val intent = Intent(this@LoanClosureActivity, cls).apply {
                                putExtra("LOAN_ID", id)
                                putExtra("HOLDER_KEY", holderKey)
                                putExtra("ENCODED_KEY", encodedKey)
                                putExtra("FORMMODE", "viewloanschedule1")
                            }
                            startActivity(intent)
                        } catch (e: ClassNotFoundException) {
                            Toast.makeText(this@LoanClosureActivity,
                                "Scheduler screen not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoanClosureActivity,
                            "No Data!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Scheduler error: ${e.message}")
                Toast.makeText(this@LoanClosureActivity,
                    "Error fetching loan details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Ledger Navigation ──────────────────────────────────────────────────
    private fun navigateToLedger() {
        val accNo = currentAccountNo
        if (accNo.isEmpty()) {
            Toast.makeText(this, "Account number is required!", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val cls = Class.forName(
                "com.example.bgls.AccountLedger.AccountLedgerDetailActivity"
            )
            val intent = Intent(this, cls).apply {
                putExtra("ACCT_NUM", accNo)
                putExtra("FORMMODE", "view")
            }
            startActivity(intent)
        } catch (e: ClassNotFoundException) {
            Toast.makeText(this, "Ledger screen not found", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private fun formatFlowDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            // Try ISO format first (yyyy-MM-dd)
            val isoFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = isoFmt.parse(dateStr)
            if (date != null) sdfUI.format(date) else dateStr
        } catch (e: Exception) {
            try {
                // Try backend date format (dd-MMM-yyyy)
                val backendFmt = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                val date = backendFmt.parse(dateStr)
                if (date != null) sdfUI.format(date) else dateStr
            } catch (e2: Exception) {
                dateStr
            }
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}
