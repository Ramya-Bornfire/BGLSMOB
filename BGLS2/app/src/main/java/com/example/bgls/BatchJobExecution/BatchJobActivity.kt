package com.example.bgls.BatchJobExecution

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.ConsistencyCheckResponse
import com.example.bgls.DataModels.DabAccountModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BatchJobActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private lateinit var txtUserIdInfo: TextView
    private lateinit var txtUserNameInfo: TextView
    private lateinit var txtLoginTimeInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_job)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing...")
            setCancelable(false)
        }

        val etCurrentDate = findViewById<EditText>(R.id.etCurrentDate)
        val etNextWorkingDate = findViewById<EditText>(R.id.etNextWorkingDate)

        // Set current date
        etCurrentDate.setText(dateFormat.format(Date()))
        etNextWorkingDate.setOnClickListener { showDatePicker(etNextWorkingDate) }

        // Job row clicks
        findViewById<View>(R.id.rowHolidayCheck).setOnClickListener { showHolidayCheckDialog() }
        findViewById<View>(R.id.rowDailyAccountBalance).setOnClickListener { showDabSelectionDialog() }
        findViewById<View>(R.id.rowConsistencyCheck).setOnClickListener { showConsistencyCheckDialog() }
        findViewById<View>(R.id.rowDateChange).setOnClickListener { showDateChangeDialog() }
        findViewById<View>(R.id.rowGlUpdation).setOnClickListener { runGlConsolidation() }
        findViewById<View>(R.id.rowPenaltyAccrual).setOnClickListener { showAccrualDialog("Penalty") }
        findViewById<View>(R.id.rowInterestAccrual).setOnClickListener { showAccrualDialog("Interest") }

        // Header icons
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnHome).setOnClickListener { finish() }

        txtUserIdInfo = findViewById(R.id.txtUserIdInfo)
        txtUserNameInfo = findViewById(R.id.txtUserNameInfo)
        txtLoginTimeInfo = findViewById(R.id.txtLoginTimeInfo)

        // Bottom Buttons
        findViewById<Button>(R.id.btnHomeBottom).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(this, "Batch jobs refreshed", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnBackBottom).setOnClickListener { finish() }
    }

    // ----------------------------- Helper: Date picker -----------------------------
    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            editText.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun convertToApiFormat(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr)
            apiDateFormat.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }

    // ----------------------------- 1. Holiday Check -----------------------------
    private fun showHolidayCheckDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_holiday_check, null)
        dialog.setView(view)

        val etDate = view.findViewById<EditText>(R.id.etFromDate)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvResult = view.findViewById<TextView>(R.id.tvValidationMessage)
        val layoutContent = view.findViewById<LinearLayout>(R.id.layoutDialogContent)
        val etToDate = view.findViewById<EditText>(R.id.etToDate)

        // Set To Date as current date
        etToDate.setText(dateFormat.format(Date()))
        etDate.setOnClickListener { showDatePicker(etDate) }

        btnSubmit.setOnClickListener {
            val dateStr = etDate.text.toString()
            if (dateStr.isEmpty()) {
                Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    val apiDate = convertToApiFormat(dateStr)
                    val response = RetrofitClient.api.holidayCheckBatchJob(apiDate)
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!.string()
                        Log.d("HolidayCheck", "Result: $result")
                        tvResult.text = result
                        tvResult.visibility = View.VISIBLE
                        layoutContent.visibility = View.GONE
                        btnSubmit.visibility = View.GONE
                    } else {
                        Log.e("HolidayCheck", "API Error: ${response.code()}")
                        Toast.makeText(this@BatchJobActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e("HolidayCheck", "Error: ${e.message}", e)
                    Toast.makeText(this@BatchJobActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // ----------------------------- 2. Daily Account Balance -----------------------------
    private fun showDabSelectionDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_dab_selection, null)
        dialog.setView(view)

        val etAccountId = view.findViewById<EditText>(R.id.etAccountId)
        val etFromDate = view.findViewById<EditText>(R.id.etFromDate)
        val etToDate = view.findViewById<EditText>(R.id.etToDate)
        val btnSearch = view.findViewById<ImageButton>(R.id.btnSearchAccount)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val layoutSelectionContent = view.findViewById<LinearLayout>(R.id.layoutSelectionContent)
        val layoutProcessing = view.findViewById<LinearLayout>(R.id.layoutProcessing)

        etFromDate.setOnClickListener { showDatePicker(etFromDate) }
        etToDate.setOnClickListener { showDatePicker(etToDate) }

        btnSearch.setOnClickListener {
            fetchDabAccountList { selectedAccount ->
                etAccountId.setText(selectedAccount)
            }
        }

        btnSubmit.setOnClickListener {
            val accountNo = etAccountId.text.toString()
            val fromDate = etFromDate.text.toString()
            val toDate = etToDate.text.toString()
            if (accountNo.isEmpty() || fromDate.isEmpty() || toDate.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.doaDabRun(accountNo, fromDate, toDate)
                    progressDialog.dismiss()
                    if (response.isSuccessful) {
                        val message = response.body() ?: "Success"
                        Log.d("DAB", "Success: $message")
                        AlertDialog.Builder(this@BatchJobActivity)
                            .setTitle("Daily Account Balance")
                            .setMessage(message)
                            .setPositiveButton("OK") { _, _ -> dialog.dismiss() }
                            .show()
                    } else {
                        Log.e("DAB", "Failed: ${response.code()}")
                        AlertDialog.Builder(this@BatchJobActivity)
                            .setTitle("Daily Account Balance")
                            .setMessage("Failed: ${response.code()}")
                            .setPositiveButton("OK") { _, _ -> dialog.dismiss() }
                            .show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e("DAB", "Error: ${e.message}", e)
                    AlertDialog.Builder(this@BatchJobActivity)
                        .setTitle("Daily Account Balance")
                        .setMessage("Error: ${e.message}")
                        .setPositiveButton("OK") { _, _ -> dialog.dismiss() }
                        .show()
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun fetchDabAccountList(onSelected: (String) -> Unit) {
        progressDialog.show()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDabAcctList()
                progressDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    showAccountListDialog(response.body()!!, onSelected)
                } else {
                    Log.e("DAB", "No accounts found")
                    Toast.makeText(this@BatchJobActivity, "No accounts found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e("DAB", "Error fetching account list: ${e.message}", e)
                Toast.makeText(this@BatchJobActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAccountListDialog(accounts: List<DabAccountModel>, onSelected: (String) -> Unit) {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_dab_account_list, null)
        dialog.setView(view)

        val tableAccounts = view.findViewById<TableLayout>(R.id.tableAccounts)
        val btnFilter = view.findViewById<Button>(R.id.btnFilter)
        val etFilterAccountNumber = view.findViewById<EditText>(R.id.etFilterAccountNumber)
        val etFilterAccountName = view.findViewById<EditText>(R.id.etFilterAccountName)

        btnFilter.setOnClickListener {
            val isVisible = etFilterAccountNumber.visibility == View.VISIBLE
            etFilterAccountNumber.visibility = if (isVisible) View.GONE else View.VISIBLE
            etFilterAccountName.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        fun populateTable(filterNum: String = "", filterName: String = "") {
            tableAccounts.removeAllViews()
            for (account in accounts) {
                val acctNum = account.acctNum ?: continue
                val acctName = account.acctName ?: ""
                if (filterNum.isNotEmpty() && !acctNum.contains(filterNum, ignoreCase = true)) continue
                if (filterName.isNotEmpty() && !acctName.contains(filterName, ignoreCase = true)) continue

                val row = TableRow(this)
                val tvNum = TextView(this).apply { text = acctNum; setPadding(16, 8, 16, 8) }
                val tvName = TextView(this).apply { text = acctName; setPadding(16, 8, 16, 8) }
                row.addView(tvNum)
                row.addView(tvName)
                row.isClickable = true
                row.setOnClickListener {
                    onSelected(acctNum)
                    dialog.dismiss()
                }
                tableAccounts.addView(row)
            }
        }

        etFilterAccountNumber.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                populateTable(s.toString(), etFilterAccountName.text.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        etFilterAccountName.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                populateTable(etFilterAccountNumber.text.toString(), s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        populateTable()

        view.findViewById<Button>(R.id.btnClose).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnBack).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ----------------------------- 3. Consistency Check -----------------------------
    private fun showConsistencyCheckDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_consistency_check, null)
        dialog.setView(view)

        val tvTrmCredit = view.findViewById<TextView>(R.id.tvTrmCredit)
        val tvTrmDebit = view.findViewById<TextView>(R.id.tvTrmDebit)
        val tvCoaCredit = view.findViewById<TextView>(R.id.tvCoaCredit)
        val tvCoaDebit = view.findViewById<TextView>(R.id.tvCoaDebit)
        val tvDabCredit = view.findViewById<TextView>(R.id.tvDabCredit)
        val tvDabDebit = view.findViewById<TextView>(R.id.tvDabDebit)
        val tvResult = view.findViewById<TextView>(R.id.tvValidationResult)
        val btnClose = view.findViewById<Button>(R.id.btnClose)

        val currentDate = findViewById<EditText>(R.id.etCurrentDate).text.toString()
        if (currentDate.isEmpty()) {
            Toast.makeText(this, "No current date", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        progressDialog.show()
        lifecycleScope.launch {
            try {
                val apiDate = convertToApiFormat(currentDate)
                val response = RetrofitClient.api.consistencyCheck(apiDate)
                progressDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Log.d("ConsistencyCheck", "Response: $result")
                    tvTrmCredit.text = formatNumber(result.trmRow.second)
                    tvTrmDebit.text = formatNumber(result.trmRow.third)
                    tvCoaCredit.text = formatNumber(result.coaRow.second)
                    tvCoaDebit.text = formatNumber(result.coaRow.third)
                    tvDabCredit.text = formatNumber(result.dabRow.second)
                    tvDabDebit.text = formatNumber(result.dabRow.third)
                    tvResult.text = "Validation Completed"
                    tvResult.setTextColor(resources.getColor(android.R.color.holo_green_dark))

                    AlertDialog.Builder(this@BatchJobActivity)
                        .setTitle("Consistency Check")
                        .setMessage("Validation Completed Successfully")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                } else {
                    Log.e("ConsistencyCheck", "Validation Failed")
                    tvResult.text = "Validation Failed"
                    tvResult.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    AlertDialog.Builder(this@BatchJobActivity)
                        .setTitle("Consistency Check")
                        .setMessage("Validation Failed")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e("ConsistencyCheck", "Error: ${e.message}", e)
                tvResult.text = "Error: ${e.message}"
                tvResult.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                AlertDialog.Builder(this@BatchJobActivity)
                    .setTitle("Consistency Check")
                    .setMessage("Error: ${e.message}")
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // ----------------------------- 4. Date Change Process -----------------------------
    private fun showDateChangeDialog() {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_holiday_check, null)
        dialog.setView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        tvTitle.text = "Date Change Process"

        val layoutContent = view.findViewById<LinearLayout>(R.id.layoutDialogContent)
        val etCurrentDate = EditText(this).apply {
            hint = "Current Date"
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { showDatePicker(this) }
        }
        val etNextDate = EditText(this).apply {
            hint = "Next Date"
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener { showDatePicker(this) }
        }
        layoutContent.addView(etCurrentDate, 0)
        layoutContent.addView(etNextDate, 1)

        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvResult = view.findViewById<TextView>(R.id.tvValidationMessage)

        btnSubmit.setOnClickListener {
            val current = etCurrentDate.text.toString()
            val next = etNextDate.text.toString()
            if (current.isEmpty() || next.isEmpty()) {
                Toast.makeText(this, "Both dates required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.dateChangeProcess(next, current)
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        val respBody = response.body()!!.string()  // read plain string
                        Log.d("DateChange", "Response: $respBody")
                        tvResult.text = respBody
                        tvResult.visibility = View.VISIBLE
                        layoutContent.visibility = View.GONE
                        btnSubmit.visibility = View.GONE
                        findViewById<EditText>(R.id.etCurrentDate).setText(current)
                        findViewById<EditText>(R.id.etNextWorkingDate).setText(next)
                    } else {
                        Log.e("DateChange", "Date change failed: ${response.code()}")
                        Toast.makeText(this@BatchJobActivity, "Date change failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e("DateChange", "Error: ${e.message}", e)
                    Toast.makeText(this@BatchJobActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // ----------------------------- 5. GL Consolidation -----------------------------
    private fun runGlConsolidation() {
        progressDialog.show()
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.glConsolidation()
                progressDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val respBody = response.body()!!.string()
                    Log.d("GL", "Response: $respBody")
                    AlertDialog.Builder(this@BatchJobActivity)
                        .setTitle("GL Consolidation")
                        .setMessage("GL Consolidation Successful")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                } else {
                    Log.e("GL", "GL Consolidation Failed: ${response.code()}")
                    AlertDialog.Builder(this@BatchJobActivity)
                        .setTitle("GL Consolidation")
                        .setMessage("GL Consolidation Failed")
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Log.e("GL", "Error: ${e.message}", e)
                AlertDialog.Builder(this@BatchJobActivity)
                    .setTitle("GL Consolidation")
                    .setMessage("Error: ${e.message}")
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
        }
    }

    // ----------------------------- 6. Accrual (Interest / Penalty) -----------------------------
    private fun showAccrualDialog(type: String) {
        val dialog = AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_penalty_accrual, null)
        dialog.setView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvDialogTitle)
        tvTitle.text = "$type Accrual Run"

        val etDate = view.findViewById<EditText>(R.id.etAccrualDate)
        etDate.setText(dateFormat.format(Date()))
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<Button>(R.id.btnClose)
        val tvSuccess = view.findViewById<TextView>(R.id.tvSuccessMessage)
        val layoutContent = view.findViewById<LinearLayout>(R.id.layoutDialogContent)

        etDate.setOnClickListener { showDatePicker(etDate) }

        btnSubmit.setOnClickListener {
            val dateStr = etDate.text.toString()
            if (dateStr.isEmpty()) {
                Toast.makeText(this, "Select date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressDialog.show()
            lifecycleScope.launch {
                try {
                    val response = if (type == "Interest") {
                        RetrofitClient.api.interestAccrual(mapOf("date" to dateStr))
                    } else {
                        RetrofitClient.api.penaltyAccrual(mapOf("date" to dateStr))
                    }
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        val respBody = response.body()!!.string()
                        Log.d("${type}Accrual", "Response: $respBody")
                        tvSuccess.text = respBody
                        tvSuccess.visibility = View.VISIBLE
                        layoutContent.visibility = View.GONE
                        btnSubmit.visibility = View.GONE
                    } else {
                        Log.e("${type}Accrual", "Failed: ${response.code()}")
                        Toast.makeText(this@BatchJobActivity, "$type Accrual failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    Log.e("${type}Accrual", "Error: ${e.message}", e)
                    Toast.makeText(this@BatchJobActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    // ----------------------------- Helper: format currency numbers -----------------------------
    private fun formatNumber(value: String): String {
        return try {
            val num = value.toDoubleOrNull() ?: 0.0
            String.format(Locale.US, "%,.2f", num)
        } catch (e: Exception) {
            value
        }
    }
}