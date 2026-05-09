package com.example.bgls.LoanOperation

import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.LoanClosureDataResponse
import com.example.bgls.DataModels.LoanFlowDetail
import com.example.bgls.DataModels.LoanFlowTransactionRequest
import com.example.bgls.DataModels.MultipleTransactionRequest
import com.example.bgls.DataModels.SettlementRecord
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class LoanOperationActivity : AppCompatActivity() {

    private lateinit var rgOperationType: RadioGroup
    private lateinit var rgAccountType: RadioGroup
    private lateinit var rgCollectionType: RadioGroup
    private lateinit var layoutCollectionType: View
    private lateinit var tvTranDateLabel: TextView
    private lateinit var etTranDate: EditText
    private lateinit var tvFromDateLabel: TextView
    private lateinit var etFromDate: EditText
    private lateinit var tvToDateLabel: TextView
    private lateinit var etToDate: EditText
    private lateinit var etAccountNo: EditText
    private lateinit var etAcctName: EditText
    private lateinit var tvAcctBalanceLabel: TextView
    private lateinit var etAcctBalance: EditText
    private lateinit var tvRecoveryPaidLabel: TextView
    private lateinit var etRecoveryPaid: EditText
    private lateinit var tvRoutingAcctLabel: TextView
    private lateinit var etRoutingAcct: EditText
    private lateinit var tvColTranAmt: TextView
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button
    private lateinit var ivSearchAccount: ImageView
    
    // Upload mode views
    private lateinit var layoutStandardOperation: View
    private lateinit var layoutFileUpload: View
    private lateinit var layoutTableArea: View
    private lateinit var tlTableContent: LinearLayout // The container inside layoutTableArea ScrollView
    private lateinit var tvNoRecords: TextView
    private lateinit var btnChooseFile: Button
    private lateinit var tvFileName: TextView
    private lateinit var btnList: Button
    private lateinit var btnList1: Button
    private lateinit var btnUpload: Button

    // Bulk Collection views
    private lateinit var layoutBulkCollection: View
    private lateinit var llBulkRows: LinearLayout
    private lateinit var btnBulkUpload: Button
    private lateinit var btnBulkAdd: Button
    private lateinit var btnBulkSubmit: Button
    private lateinit var btnBulkHome: Button
    private lateinit var btnBulkBack: Button

    private var selectedFileUri: Uri? = null
    private val sdfUI = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val sdfBackend = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
    private val sdfISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var btnSubmit: Button
    private var bookingDays: Int = 0
    private var interestPercentage: Double = 0.0
    private var initialCollection: List<List<Any>>? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileName(it)
            tvFileName.text = fileName
            Toast.makeText(this, "Selected: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_operation)

        initViews()
        setupListeners()
        fetchInitialData()
    }

    private fun initViews() {
        rgOperationType = findViewById(R.id.rgOperationType)
        rgAccountType = findViewById(R.id.rgAccountType)
        rgCollectionType = findViewById(R.id.rgCollectionType)
        layoutCollectionType = findViewById(R.id.layoutCollectionType)
        
        tvTranDateLabel = findViewById(R.id.tvTranDateLabel)
        etTranDate = findViewById(R.id.etTranDate)
        tvFromDateLabel = findViewById(R.id.tvFromDateLabel)
        etFromDate = findViewById(R.id.etFromDate)
        tvToDateLabel = findViewById(R.id.tvToDateLabel)
        etToDate = findViewById(R.id.etToDate)
        
        etAccountNo = findViewById(R.id.etAccountNo)
        etAcctName = findViewById(R.id.etAcctName)
        tvAcctBalanceLabel = findViewById(R.id.tvAcctBalanceLabel)
        etAcctBalance = findViewById(R.id.etAcctBalance)
        tvRecoveryPaidLabel = findViewById(R.id.tvRecoveryPaidLabel)
        etRecoveryPaid = findViewById(R.id.etRecoveryPaid)
        
        tvRoutingAcctLabel = findViewById(R.id.tvRoutingAcctLabel)
        etRoutingAcct = findViewById(R.id.etRoutingAcct)
        tvColTranAmt = findViewById(R.id.tvColTranAmt)
        
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        
        layoutStandardOperation = findViewById(R.id.layoutStandardOperation)
        layoutFileUpload = findViewById(R.id.layoutFileUpload)
        layoutTableArea = findViewById(R.id.layoutTableArea)
        tlTableContent = (layoutTableArea as HorizontalScrollView).getChildAt(0) as LinearLayout
        tvNoRecords = findViewById(R.id.tvNoRecords)
        
        btnChooseFile = findViewById(R.id.btnChooseFile)
        tvFileName = findViewById(R.id.tvFileName)
        btnList = findViewById(R.id.btnList)
        btnList1 = findViewById(R.id.btnList1)
        btnUpload = findViewById(R.id.btnUpload)

        layoutBulkCollection = findViewById(R.id.layoutBulkCollection)
        llBulkRows = findViewById(R.id.llBulkRows)
        btnBulkUpload = findViewById(R.id.btnBulkUpload)
        btnBulkAdd = findViewById(R.id.btnBulkAdd)
        btnBulkSubmit = findViewById(R.id.btnBulkSubmit)
        btnBulkHome = findViewById(R.id.btnBulkHome)
        btnBulkBack = findViewById(R.id.btnBulkBack)

        // Set default dates
        val today = sdfUI.format(Date())
        etTranDate.setText(today)
        etFromDate.setText(today)
        etToDate.setText(today)

        addSubmitButton()
    }

    private fun addSubmitButton() {
        btnSubmit = findViewById(R.id.btnSubmit)
        btnSubmit.setOnClickListener { handleGlobalSubmit() }
    }

    private fun handleGlobalSubmit() {
        val accNo = etAccountNo.text.toString()
        if (accNo.isEmpty()) {
            Toast.makeText(this, "Select an account", Toast.LENGTH_SHORT).show()
            return
        }

        val isCollection = rgOperationType.checkedRadioButtonId == R.id.rbCollection
        val operation = when(rgOperationType.checkedRadioButtonId) {
            R.id.rbCollection -> "Collection"
            R.id.rbInterest -> "Interest"
            R.id.rbFees -> "Fees"
            R.id.rbPenalty -> "Penalty"
            R.id.rbBooking -> "Booking"
            else -> ""
        }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                if (isCollection) {
                    // Bulk submission for Collection
                    val records = mutableListOf<SettlementRecord>()
                    for (i in 2 until tlTableContent.childCount) {
                        val row = tlTableContent.getChildAt(i) as? LinearLayout ?: continue
                        val tranAmtStr = (row.getChildAt(4) as TextView).text.toString().replace(",", "")
                        val loanAcctNo = (row.getChildAt(5) as TextView).text.toString()
                        val amt = tranAmtStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            records.add(SettlementRecord(loanAcctNo, amt))
                        }
                    }
                    
                    if (records.isEmpty()) {
                        Toast.makeText(this@LoanOperationActivity, "No allocation to submit", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    
                    val response = api.settlementCollection(records)
                    if (response.isSuccessful) {
                        Toast.makeText(this@LoanOperationActivity, "Success: ${response.body()}", Toast.LENGTH_LONG).show()
                        refreshFlows()
                    } else {
                        Toast.makeText(this@LoanOperationActivity, "Error: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Sequential submission for other modes
                    var successCount = 0
                    var totalRows = 0
                    for (i in 2 until tlTableContent.childCount) {
                        val row = tlTableContent.getChildAt(i) as? LinearLayout ?: continue
                        totalRows++
                        
                        val flowDate = (row.getChildAt(0) as TextView).text.toString()
                        val flowId = (row.getChildAt(1) as TextView).text.toString()
                        val flowCode = (row.getChildAt(2) as TextView).text.toString()
                        val flowAmt = (row.getChildAt(3) as TextView).text.toString().replace(",", "")
                        val loanAcctNo = (row.getChildAt(4) as TextView).text.toString()
                        val acctName = (row.getChildAt(5) as TextView).text.toString()
                        
                        val response = api.transactionInterest(
                            flowCode = flowCode,
                            flowDate = flowDate,
                            flowAmount = flowAmt,
                            flowId = flowId,
                            accountNo = loanAcctNo,
                            accountName = acctName,
                            operation = operation
                        )
                        
                        if (response.isSuccessful) {
                            successCount++
                        }
                    }
                    
                    if (successCount == totalRows && totalRows > 0) {
                        Toast.makeText(this@LoanOperationActivity, "All transactions processed successfully", Toast.LENGTH_SHORT).show()
                        refreshFlows()
                    } else if (totalRows > 0) {
                        Toast.makeText(this@LoanOperationActivity, "Processed $successCount of $totalRows rows", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanOperationActivity, "Submission Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTableValueSequentially() {
        val totalAmount = etRecoveryPaid.text.toString().toDoubleOrNull() ?: 0.0
        var remaining = totalAmount
        
        for (i in 2 until tlTableContent.childCount) {
            val row = tlTableContent.getChildAt(i) as? LinearLayout ?: continue
            val flowAmt = (row.getChildAt(3) as TextView).text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val tranAmtTv = row.getChildAt(4) as TextView
            
            val applied = minOf(remaining, flowAmt)
            tranAmtTv.text = String.format("%.2f", applied)
            remaining -= applied
        }
    }

    private fun setupListeners() {
        rgOperationType.setOnCheckedChangeListener { _, checkedId ->
            updateOperationMode(checkedId)
            refreshFlows()
        }

        rgCollectionType.setOnCheckedChangeListener { _, checkedId ->
            updateCollectionSubMode(checkedId)
            refreshFlows()
        }

        btnChooseFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        btnBulkUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        btnBulkAdd.setOnClickListener {
            addBulkRow()
        }

        btnBulkSubmit.setOnClickListener {
            submitBulkTransactions()
        }

        btnBulkHome.setOnClickListener { finish() }
        btnBulkBack.setOnClickListener { onBackPressed() }

        ivSearchAccount.setOnClickListener {
            openAccountSearchDialog()
        }

        btnHome.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnUpload.setOnClickListener {
            uploadProcessFile()
        }

        btnList.setOnClickListener { fetchInitialData("uploadlist") }
        btnList1.setOnClickListener { fetchInitialData("list1") }

        etRecoveryPaid.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (rgOperationType.checkedRadioButtonId == R.id.rbCollection) {
                    updateTableValueSequentially()
                }
            }
        })

        etAccountNo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && etAccountNo.text.isNotEmpty()) {
                fetchAccountBalance(etAccountNo.text.toString())
                refreshFlows()
            }
        }

        setupDatePickers()
    }

    private fun setupDatePickers() {
        val listener = { editText: EditText ->
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                val dateStr = sdfUI.format(calendar.time)
                editText.setText(dateStr)
                if (editText.id == R.id.etTranDate) {
                    refreshFlows()
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        etTranDate.setOnClickListener { listener(etTranDate) }
        etFromDate.setOnClickListener { listener(etFromDate) }
        etToDate.setOnClickListener { listener(etToDate) }
    }

    private fun updateOperationMode(checkedId: Int) {
        if (checkedId == R.id.rbCollection) {
            layoutCollectionType.visibility = View.VISIBLE
            tvTranDateLabel.visibility = View.VISIBLE
            etTranDate.visibility = View.VISIBLE
            tvFromDateLabel.visibility = View.GONE
            etFromDate.visibility = View.GONE
            tvToDateLabel.visibility = View.GONE
            etToDate.visibility = View.GONE
            tvAcctBalanceLabel.visibility = View.VISIBLE
            etAcctBalance.visibility = View.VISIBLE
            tvRecoveryPaidLabel.visibility = View.VISIBLE
            etRecoveryPaid.visibility = View.VISIBLE
            tvColTranAmt.visibility = View.VISIBLE
            updateCollectionSubMode(rgCollectionType.checkedRadioButtonId)
        } else {
            layoutCollectionType.visibility = View.GONE
            tvTranDateLabel.visibility = View.GONE
            etTranDate.visibility = View.GONE
            tvFromDateLabel.visibility = View.VISIBLE
            etFromDate.visibility = View.VISIBLE
            tvToDateLabel.visibility = View.VISIBLE
            etToDate.visibility = View.VISIBLE
            tvAcctBalanceLabel.visibility = View.GONE
            etAcctBalance.visibility = View.GONE
            tvRecoveryPaidLabel.visibility = View.GONE
            etRecoveryPaid.visibility = View.GONE
            tvColTranAmt.visibility = View.GONE
            
            layoutStandardOperation.visibility = View.VISIBLE
            layoutFileUpload.visibility = View.GONE
            layoutBulkCollection.visibility = View.GONE
            layoutTableArea.visibility = View.VISIBLE
            btnSubmit.visibility = View.VISIBLE
        }
    }

    private fun updateCollectionSubMode(checkedId: Int) {
        if (rgOperationType.checkedRadioButtonId != R.id.rbCollection) return

        tvRoutingAcctLabel.visibility = View.GONE
        etRoutingAcct.visibility = View.GONE
        layoutStandardOperation.visibility = View.VISIBLE
        layoutFileUpload.visibility = View.GONE
        layoutBulkCollection.visibility = View.VISIBLE
        layoutTableArea.visibility = View.VISIBLE

        when (checkedId) {
            R.id.rbCash -> {
                layoutBulkCollection.visibility = View.GONE
            }
            R.id.rbOfficeRouting -> {
                tvRoutingAcctLabel.visibility = View.VISIBLE
                etRoutingAcct.visibility = View.VISIBLE
                layoutBulkCollection.visibility = View.GONE
            }
            R.id.rbStandingInstruction -> {
                layoutStandardOperation.visibility = View.GONE
                layoutFileUpload.visibility = View.VISIBLE
                layoutBulkCollection.visibility = View.GONE
                layoutTableArea.visibility = View.GONE
            }
            R.id.rbMultipleEntries -> {
                layoutStandardOperation.visibility = View.GONE
                layoutFileUpload.visibility = View.GONE
                layoutBulkCollection.visibility = View.VISIBLE
                layoutTableArea.visibility = View.GONE
                btnSubmit.visibility = View.GONE
            }
        }
    }

    private fun refreshFlows() {
        val accNo = etAccountNo.text.toString()
        val toDateUI = if (rgOperationType.checkedRadioButtonId == R.id.rbCollection) etTranDate.text.toString() else etToDate.text.toString()
        val fromDateUI = etFromDate.text.toString()
        if (accNo.isEmpty()) return

        val toDateISO = try { val d = sdfUI.parse(toDateUI); sdfISO.format(d!!) } catch (e: Exception) { toDateUI }
        val fromDateISO = try { val d = sdfUI.parse(fromDateUI); sdfISO.format(d!!) } catch (e: Exception) { fromDateUI }

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = when (rgOperationType.checkedRadioButtonId) {
                    R.id.rbCollection, R.id.rbFees, R.id.rbInterest, R.id.rbPenalty -> {
                        api.loanFlowDetails11(toDateISO, accNo)
                    }
                    R.id.rbBooking -> {
                        api.getLoanClosureDatas(accNo) // Using closure datas as fallback for Booking if specific endpoint missing
                    }
                    else -> null
                }

                if (response?.isSuccessful == true) {
                    val body = response.body()
                    if (body is LoanClosureDataResponse) {
                        if (rgOperationType.checkedRadioButtonId == R.id.rbCollection) {
                            etAcctBalance.setText(String.format("%.2f", body.flowTotalAmt ?: 0.0))
                        }
                        populateTable(body.loanFlows)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoanOp", "Refresh flows error: ${e.message}")
            }
        }
    }

    private fun fetchInitialData(formmode: String = "list") {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loanOperation(formmode)
                if (response.isSuccessful) {
                    val data = response.body()
                    data?.TRANDATE?.let { tDate ->
                        try {
                            val date = if (tDate.contains("-")) {
                                val datePart = if (tDate.length >= 10) tDate.substring(0, 10) else tDate
                                if (tDate.indexOf("-") == 4) sdfISO.parse(datePart) else sdfUI.parse(datePart)
                            } else null
                            
                            date?.let {
                                val formatted = sdfUI.format(it)
                                etTranDate.setText(formatted)
                                etFromDate.setText(formatted)
                                etToDate.setText(formatted)
                            }
                        } catch (e: Exception) {
                            Log.e("LoanOp", "Date parse error: ${e.message}")
                        }
                    }
                    initialCollection = data?.collection
                    if (formmode == "list1") {
                        populateBulkRows(data?.getlist)
                    }
                }
            } catch (e: Exception) {
                Log.e("LoanOp", "Error: ${e.message}")
            }
        }
    }

    private fun populateTable(flows: List<LoanFlowDetail>?) {
        // Keep header (index 0) and tvNoRecords (index 1)
        while (tlTableContent.childCount > 2) {
            tlTableContent.removeViewAt(2)
        }
        tvNoRecords.visibility = if (flows.isNullOrEmpty()) View.VISIBLE else View.GONE
        if (flows.isNullOrEmpty()) return

        val isCollection = rgOperationType.checkedRadioButtonId == R.id.rbCollection
        tvColTranAmt.visibility = if (isCollection) View.VISIBLE else View.GONE

        flows.forEach { flow ->
            val rowIndex = tlTableContent.childCount - 2
            val bgColor = if (rowIndex % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")
            
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setBackgroundColor(bgColor)
            }
            
            val fields = mutableListOf(
                Pair(1.2f, flow.flowDate), Pair(1.0f, flow.flowId), Pair(1.0f, flow.flowCode), 
                Pair(1.2f, String.format("%.2f", flow.flowAmt))
            )
            if (isCollection) {
                fields.add(Pair(1.2f, String.format("%.2f", flow.tranAmt ?: 0.0)))
            }
            fields.add(Pair(1.2f, flow.loanAcctNo))
            fields.add(Pair(1.5f, flow.acctName))

            fields.forEach { (weight, value) ->
                row.addView(TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
                    text = value
                    textSize = 10f
                    setPadding(10, 12, 10, 12)
                    gravity = if (value.toDoubleOrNull() != null) Gravity.END else Gravity.CENTER
                    setBackgroundColor(Color.TRANSPARENT)
                    setTextColor(Color.BLACK)
                })
            }
            
            row.setOnClickListener {
                if (rgOperationType.checkedRadioButtonId != R.id.rbCollection) {
                    performTransactionInterest(flow)
                }
            }
            tlTableContent.addView(row)
        }
    }

    private fun performTransactionInterest(flow: LoanFlowDetail) {
        val operation = when (rgOperationType.checkedRadioButtonId) {
            R.id.rbInterest -> "Interest"
            R.id.rbFees -> "Fees"
            R.id.rbPenalty -> "Penalty"
            R.id.rbBooking -> "Booking"
            else -> ""
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.transactionInterest(
                    flowCode = flow.flowCode,
                    flowDate = flow.flowDate,
                    flowAmount = flow.flowAmt.toString(),
                    flowId = flow.flowId,
                    accountNo = flow.loanAcctNo,
                    accountName = flow.acctName,
                    operation = operation
                )
                if (response.isSuccessful) {
                    Toast.makeText(this@LoanOperationActivity, "Transaction Success: ${response.body()?.get("tranId")}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("LoanOp", "Interest error: ${e.message}")
            }
        }
    }

    private fun addBulkRow(data: Map<String, Any>? = null) {
        val rowIndex = llBulkRows.childCount
        val bgColor = if (rowIndex % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")

        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(bgColor)
        }

        val weights = listOf(1f, 1.5f, 1f, 1.2f, 1f, 1f, 1.2f, 1.2f)
        val defaultValues = listOf(
            data?.get("tran_id")?.toString() ?: "",
            data?.get("names")?.toString() ?: "",
            data?.get("reference")?.toString() ?: "",
            data?.get("mobile_number")?.toString() ?: "",
            data?.get("amount")?.toString() ?: "",
            data?.get("allocated_amount")?.toString() ?: "0",
            data?.get("trans_time")?.toString() ?: sdfUI.format(Date()),
            data?.get("status")?.toString() ?: "UNALLOCATED"
        )

        for (i in weights.indices) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), weights[i])
                setBackgroundColor(Color.TRANSPARENT)
                textSize = 10f
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                setText(defaultValues[i])
                gravity = Gravity.CENTER
            }
            row.addView(et)
        }

        val rb = RadioButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), 0.8f)
            setBackgroundColor(Color.TRANSPARENT)
            gravity = Gravity.CENTER
            isChecked = (data?.get("status")?.toString() == "ALLOCATED")
        }
        row.addView(rb)

        val ivDelete = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx())
            setImageResource(android.R.drawable.ic_menu_delete)
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            setColorFilter(Color.RED)
            setOnClickListener { llBulkRows.removeView(row) }
        }
        row.addView(ivDelete)

        llBulkRows.addView(row)
    }

    private fun populateBulkRows(list: List<Any>?) {
        llBulkRows.removeAllViews()
        list?.forEach { item ->
            if (item is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                addBulkRow(item as Map<String, Any>)
            }
        }
    }

    private fun submitBulkTransactions() {
        val transactions = mutableListOf<MultipleTransactionRequest>()
        for (i in 0 until llBulkRows.childCount) {
            val row = llBulkRows.getChildAt(i) as LinearLayout
            val tranId = (row.getChildAt(0) as EditText).text.toString()
            val name = (row.getChildAt(1) as EditText).text.toString()
            val ref = (row.getChildAt(2) as EditText).text.toString()
            val mobile = (row.getChildAt(3) as EditText).text.toString()
            val amt = (row.getChildAt(4) as EditText).text.toString()
            
            transactions.add(MultipleTransactionRequest(
                acctNamedata = name,
                tranId = tranId,
                transactionDate = sdfUI.format(Date()),
                tranParticulardata = amt,
                acctNum = ref,
                tranRemarks = "",
                globalAuthUser = "SYSTEM"
            ))
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.saveMultipleTransactions1(transactions)
                if (response.isSuccessful) {
                    Toast.makeText(this@LoanOperationActivity, "Bulk collection submitted: ${response.body()?.get("message")}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanOperationActivity, "Submission failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadProcessFile() {
        val uri = selectedFileUri ?: return
        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                val response = RetrofitClient.api.leaseUploadExcel(body, "LOAN_OP", "SYSTEM")
                if (response.isSuccessful) {
                    Toast.makeText(this@LoanOperationActivity, "File uploaded successfully", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanOperationActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                val rowIndex = tlAccounts.childCount
                val bgColor = if (rowIndex % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")
                val row = TableRow(this@LoanOperationActivity).apply { setBackgroundColor(bgColor) }
                val tvNo = TextView(this@LoanOperationActivity).apply {
                    text = acc[0].toString()
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(Color.TRANSPARENT)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvName = TextView(this).apply {
                    text = acc[1].toString()
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(Color.TRANSPARENT)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                }
                row.addView(tvNo); row.addView(tvName)
                row.setOnClickListener {
                    etAccountNo.setText(acc[0].toString())
                    etAcctName.setText(acc[1].toString())
                    etRecoveryPaid.setText("0.00")
                    fetchAccountBalance(acc[0].toString())
                    refreshFlows()
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        btnFilter.setOnClickListener {
            val query = etSearchAccNo.text.toString()
            if (query.isEmpty() && rgOperationType.checkedRadioButtonId == R.id.rbCollection && initialCollection != null) {
                populateSearchTable(initialCollection!!)
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.api
                    val response = api.search(query)
                    if (response.isSuccessful) {
                        response.body()?.let { populateSearchTable(it) }
                    }
                } catch (e: Exception) { Log.e("LoanOp", "Search error: ${e.message}") }
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        
        if (rgOperationType.checkedRadioButtonId == R.id.rbCollection && initialCollection != null) {
            populateSearchTable(initialCollection!!)
        }
        
        dialog.show()
    }

    private fun fetchAccountBalance(accNo: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.fetchAccountBalance(accNo)
                if (response.isSuccessful) {
                    etAcctBalance.setText(response.body())
                }
            } catch (e: Exception) { Log.e("LoanOp", "Balance error: ${e.message}") }
        }
    }

    private fun getFileName(uri: Uri): String {
        return uri.path?.substringAfterLast('/') ?: "Selected File"
    }

    private suspend fun uriToFile(uri: Uri): File = withContext(Dispatchers.IO) {
        val file = File(cacheDir, "upload_file.xlsx")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
    private fun View.setAllPadding(padding: Int) = setPadding(padding, padding, padding, padding)
}
