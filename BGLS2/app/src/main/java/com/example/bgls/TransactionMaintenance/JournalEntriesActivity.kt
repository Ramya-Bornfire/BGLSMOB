package com.example.bgls.TransactionMaintenance

import android.content.Intent
import com.example.bgls.MainActivity

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class JournalEntriesActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private var tvDialogFileName: TextView? = null

    // Form fields
    private lateinit var etTranId: EditText
    private lateinit var etPartTranId: EditText
    private lateinit var etAcctId: EditText
    private lateinit var etAcctName: EditText
    private lateinit var spinnerTranType: Spinner
    private lateinit var spinnerPartTranType: Spinner
    private lateinit var spinnerAcctCcy: Spinner
    private lateinit var etTranAmt: EditText
    private lateinit var etTranParticulars: EditText
    private lateinit var etTranRemarks: EditText
    private lateinit var etFlowCode: EditText
    private lateinit var etFlowDate: EditText
    private lateinit var etTranDate: EditText
    private lateinit var etValueDate: EditText
    private lateinit var etTranCode: EditText
    private lateinit var etTranReportCode: EditText
    private lateinit var etTranRefNo: EditText
    private lateinit var etAdditionalDetails: EditText
    private lateinit var etPartitionDetails: EditText
    private lateinit var etPartitionType: EditText
    private lateinit var etInstrumentNo: EditText
    private lateinit var etInstrumentDate: EditText
    private lateinit var spinnerRefCcy: Spinner
    private lateinit var etRefCcyAmt: EditText
    private lateinit var etRateCode: EditText
    private lateinit var etRate: EditText
    private lateinit var etEntryUser: EditText
    private lateinit var etPostUser: EditText
    private lateinit var etEntryTime: EditText
    private lateinit var etPostTime: EditText
    private lateinit var etTranStatus: EditText
    private lateinit var etDeleted: EditText

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFileUri = it
            tvDialogFileName?.text = getFileName(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_journal_entries)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initFormViews()
        setupFormSpinners()
        setupSpinners()
        setupNavigationAndButtons()
    }

    private fun initFormViews() {
        etTranId = findViewById(R.id.etTranId)
        etPartTranId = findViewById(R.id.etPartTranId)
        etAcctId = findViewById(R.id.etAcctId)
        etAcctName = findViewById(R.id.etAcctName)
        spinnerTranType = findViewById(R.id.spinnerTranType)
        spinnerPartTranType = findViewById(R.id.spinnerPartTranType)
        spinnerAcctCcy = findViewById(R.id.spinnerAcctCcy)
        etTranAmt = findViewById(R.id.etTranAmt)
        etTranParticulars = findViewById(R.id.etTranParticulars)
        etTranRemarks = findViewById(R.id.etTranRemarks)
        etFlowCode = findViewById(R.id.etFlowCode)
        etFlowDate = findViewById(R.id.etFlowDate)
        etTranDate = findViewById(R.id.etTranDate)
        etValueDate = findViewById(R.id.etValueDate)
        etTranCode = findViewById(R.id.etTranCode)
        etTranReportCode = findViewById(R.id.etTranReportCode)
        etTranRefNo = findViewById(R.id.etTranRefNo)
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails)
        etPartitionDetails = findViewById(R.id.etPartitionDetails)
        etPartitionType = findViewById(R.id.etPartitionType)
        etInstrumentNo = findViewById(R.id.etInstrumentNo)
        etInstrumentDate = findViewById(R.id.etInstrumentDate)
        spinnerRefCcy = findViewById(R.id.spinnerRefCcy)
        etRefCcyAmt = findViewById(R.id.etRefCcyAmt)
        etRateCode = findViewById(R.id.etRateCode)
        etRate = findViewById(R.id.etRate)
        etEntryUser = findViewById(R.id.etEntryUser)
        etPostUser = findViewById(R.id.etPostUser)
        etEntryTime = findViewById(R.id.etEntryTime)
        etPostTime = findViewById(R.id.etPostTime)
        etTranStatus = findViewById(R.id.etTranStatus)
        etDeleted = findViewById(R.id.etDeleted)

        // Setup dynamic totals text
        etTranAmt.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateTotals()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        spinnerPartTranType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                updateTotals()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFormSpinners() {
        val tranTypeOptions = arrayOf("Select", "Transfer", "Cash", "Clearing")
        val tranTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tranTypeOptions)
        spinnerTranType.adapter = tranTypeAdapter

        val partTranTypeOptions = arrayOf("Select", "Debit", "Credit")
        val partTranTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, partTranTypeOptions)
        spinnerPartTranType.adapter = partTranTypeAdapter

        val ccyOptions = arrayOf("Select", "USD", "EUR", "GBP", "INR", "AED", "CAD", "AUD")
        val ccyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ccyOptions)
        spinnerAcctCcy.adapter = ccyAdapter
        spinnerRefCcy.adapter = ccyAdapter
    }

    private fun updateTotals() {
        val amt = etTranAmt.text.toString().toDoubleOrNull() ?: 0.0
        val partType = spinnerPartTranType.selectedItem?.toString() ?: ""
        var totalCredit = 0.0
        var totalDebit = 0.0
        if (partType == "Credit") {
            totalCredit = amt
        } else if (partType == "Debit") {
            totalDebit = amt
        }
        findViewById<TextView>(R.id.tvTotals)?.text = 
            String.format(Locale.US, "Total Credit: %,.2f  |  Total Debit: %,.2f", totalCredit, totalDebit)
    }

    private fun setupNavigationAndButtons() {
        findViewById<Button>(R.id.btnBackBottom).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnHomeBottom).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnSaveTransaction).setOnClickListener {
            submitTransaction()
        }

        findViewById<ImageView>(R.id.btnSearchAcctId).setOnClickListener {
            openAccountSearchDialog()
        }
    }

    private fun openAccountSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val etSearchNo = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val etSearchName = dialogView.findViewById<EditText>(R.id.etSearchAccName)
        val btnFilter = dialogView.findViewById<Button>(R.id.btnFilter)
        val tlAccounts = dialogView.findViewById<TableLayout>(R.id.tlAccounts)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        btnClose.setOnClickListener { dialog.dismiss() }

        fun populateTable(accounts: List<com.example.bgls.DataModels.ChartAccountApiItem>) {
            tlAccounts.removeAllViews()
            // Header
            val header = TableRow(this)
            header.addView(TextView(this).apply { text = "Acc No"; setPadding(8, 8, 8, 8); setTextColor(resources.getColor(R.color.black)); setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f) })
            header.addView(TextView(this).apply { text = "Acc Name"; setPadding(8, 8, 8, 8); setTextColor(resources.getColor(R.color.black)); setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f) })
            tlAccounts.addView(header)

            accounts.forEach { acc ->
                val row = TableRow(this)
                row.addView(TextView(this).apply { text = acc.acct_num; setPadding(8, 8, 8, 8); setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f) })
                row.addView(TextView(this).apply { text = acc.acct_name; setPadding(8, 8, 8, 8); setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 11f) })
                row.setOnClickListener {
                    etAcctId.setText(acc.acct_num ?: "")
                    etAcctName.setText(acc.acct_name ?: "")
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        btnFilter.setOnClickListener {
            val queryNo = etSearchNo.text.toString().lowercase()
            val queryName = etSearchName.text.toString().lowercase()
            fetchAccounts { allAccounts ->
                val filtered = allAccounts.filter {
                    (it.acct_num?.lowercase()?.contains(queryNo) == true) &&
                    (it.acct_name?.lowercase()?.contains(queryName) == true)
                }
                populateTable(filtered)
            }
        }

        // Initial load
        fetchAccounts { populateTable(it) }

        dialog.show()
    }

    private fun fetchAccounts(onSuccess: (List<com.example.bgls.DataModels.ChartAccountApiItem>) -> Unit) {
        RetrofitClient.api.getChartOfAccountsList().enqueue(object : retrofit2.Callback<com.example.bgls.DataModels.ChartOfAccountsListResponse> {
            override fun onResponse(call: retrofit2.Call<com.example.bgls.DataModels.ChartOfAccountsListResponse>, response: retrofit2.Response<com.example.bgls.DataModels.ChartOfAccountsListResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.chartaccount ?: emptyList())
                }
            }
            override fun onFailure(call: retrofit2.Call<com.example.bgls.DataModels.ChartOfAccountsListResponse>, t: Throwable) {
                Toast.makeText(this@JournalEntriesActivity, "Search failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupSpinners() {
        val options = arrayOf("Select", "Add", "Mass Entries", "List", "Upload")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        val spinner = findViewById<Spinner>(R.id.spinnerFunction)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                when (options[position].lowercase()) {
                    "mass entries" -> {
                        startActivity(android.content.Intent(this@JournalEntriesActivity, MassEntriesActivity::class.java))
                        spinner.setSelection(0)
                    }
                    "list" -> {
                        startActivity(android.content.Intent(this@JournalEntriesActivity, JournalEntriesListActivity::class.java))
                        spinner.setSelection(0)
                    }
                    "upload" -> {
                        showUploadDialog()
                        spinner.setSelection(0)
                    }
                    "add" -> loadAddScreenData()
                    else -> clearForm()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // If navigated from migration, auto-select "Add"
        if (intent.getBooleanExtra("from_migration", false)) {
            spinner.setSelection(1) // "Add"
        }
    }

    private fun loadAddScreenData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntryAddScreen()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    etTranId.setText(data.plusonetran2)
                    etPartTranId.setText(data.partTranId)
                    etEntryUser.setText(data.user)
                    etTranStatus.setText(data.tranStatus)
                    etTranDate.setText(data.currentDate)
                    etValueDate.setText(data.currentDate)
                    etEntryTime.setText(data.currentDate)
                    etFlowDate.setText(data.currentDate)
                    findViewById<EditText>(R.id.etHeaderDate)?.setText(data.currentDate)

                    // If from migration, override with intent values!
                    if (intent.getBooleanExtra("from_migration", false)) {
                        intent.getStringExtra("flow_id")?.let { etTranId.setText(it) }
                        intent.getStringExtra("account_number")?.let { etAcctId.setText(it) }
                        intent.getStringExtra("account_name")?.let { etAcctName.setText(it) }
                        intent.getStringExtra("flow_code")?.let { etFlowCode.setText(it) }
                        intent.getStringExtra("flow_amount")?.let { etTranAmt.setText(it.replace(",", "")) }
                        intent.getStringExtra("flow_date")?.let { date ->
                            etTranDate.setText(date)
                            etValueDate.setText(date)
                            etEntryTime.setText(date)
                            etFlowDate.setText(date)
                            findViewById<EditText>(R.id.etHeaderDate)?.setText(date)
                        }

                        // Set default spinner options if matching
                        val flowCode = intent.getStringExtra("flow_code") ?: ""
                        if (flowCode.contains("DISB")) {
                            spinnerPartTranType.setSelection(1) // Debit
                        } else if (flowCode.contains("COLL") || flowCode.contains("RECOVERY")) {
                            spinnerPartTranType.setSelection(2) // Credit
                        }
                        spinnerTranType.setSelection(1) // Transfer
                        spinnerAcctCcy.setSelection(4)  // INR
                        spinnerRefCcy.setSelection(4)   // INR
                    }
                } else {
                    Toast.makeText(this@JournalEntriesActivity, "Failed to load default values", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntriesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun submitTransaction() {
        val tranId = etTranId.text.toString().trim()
        val partTranId = etPartTranId.text.toString().trim()
        val acctNum = etAcctId.text.toString().trim()
        val acctName = etAcctName.text.toString().trim()
        val tranAmtVal = etTranAmt.text.toString().trim().toDoubleOrNull() ?: 0.0
        val tranType = spinnerTranType.selectedItem?.toString() ?: "Select"
        val partTranType = spinnerPartTranType.selectedItem?.toString() ?: "Select"
        val acctCcy = spinnerAcctCcy.selectedItem?.toString() ?: "Select"

        if (tranId.isEmpty() || acctNum.isEmpty() || tranAmtVal <= 0.0 || tranType == "Select" || partTranType == "Select" || acctCcy == "Select") {
            Toast.makeText(this, "Please fill all required fields and select valid dropdown options", Toast.LENGTH_SHORT).show()
            return
        }

        val request = com.example.bgls.DataModels.TransactionRequest(
            tran_id = tranId,
            part_tran_id = partTranId,
            acct_num = acctNum,
            acct_name = acctName,
            tran_type = tranType,
            part_tran_type = partTranType,
            acct_crncy = acctCcy,
            tran_amt = tranAmtVal,
            tran_particular = etTranParticulars.text.toString(),
            tran_remarks = etTranRemarks.text.toString(),
            flow_code = etFlowCode.text.toString(),
            flow_date = etFlowDate.text.toString(),
            tran_date = etTranDate.text.toString(),
            value_date = etValueDate.text.toString(),
            tran_code = etTranCode.text.toString(),
            tran_rpt_code = etTranReportCode.text.toString(),
            tran_ref_no = etTranRefNo.text.toString(),
            add_details = etAdditionalDetails.text.toString(),
            partition_type = etPartitionType.text.toString(),
            partition_det = etPartitionDetails.text.toString(),
            instr_num = etInstrumentNo.text.toString(),
            instr_date = etInstrumentDate.text.toString(),
            ref_crncy = spinnerRefCcy.selectedItem?.toString() ?: "",
            ref_crncy_amt = etRefCcyAmt.text.toString().toDoubleOrNull() ?: 0.0,
            rate_code = etRateCode.text.toString(),
            rate = etRate.text.toString().toDoubleOrNull() ?: 0.0,
            entry_user = etEntryUser.text.toString(),
            post_user = etPostUser.text.toString(),
            entry_time = etEntryTime.text.toString(),
            post_time = etPostTime.text.toString(),
            tran_status = etTranStatus.text.toString(),
            del_flg = etDeleted.text.toString(),
            srl_no = "1"
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.addTransaction(listOf(request))
                if (response.isSuccessful) {
                    Toast.makeText(this@JournalEntriesActivity, "Transaction Added Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Submission failed"
                    Toast.makeText(this@JournalEntriesActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntriesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showUploadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_files, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        tvDialogFileName = dialogView.findViewById(R.id.tvFileName)
        val btnChoose = dialogView.findViewById<Button>(R.id.btnChooseFile)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseUpload)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitUpload)

        btnChoose.setOnClickListener { filePickerLauncher.launch("*/*") }
        btnClose.setOnClickListener { dialog.dismiss() }
        btnSubmit.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadFile(uri, dialog)
            } ?: Toast.makeText(this, "Select a file first", Toast.LENGTH_SHORT).show()
        }
        dialog.show()
    }

    private fun uploadFile(uri: Uri, dialog: AlertDialog) {
        val file = uriToFile(uri)
        val requestFile = file.asRequestBody("application/vnd.ms-excel".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestFile)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.uploadFile(part)
                if (response.isSuccessful) {
                    Toast.makeText(this@JournalEntriesActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@JournalEntriesActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@JournalEntriesActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val tempFile = File(cacheDir, "temp_upload_${System.currentTimeMillis()}")
        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return tempFile
    }

    private fun getFileName(uri: Uri): String {
        var fileName: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: uri.path?.substringAfterLast('/') ?: "unknown"
    }

    private fun clearForm() {
        etTranId.setText("")
        etPartTranId.setText("")
        etEntryUser.setText("")
        etTranStatus.setText("")
        etTranDate.setText("")
        etValueDate.setText("")
        etEntryTime.setText("")
        etFlowDate.setText("")
        etAcctId.setText("")
        etAcctName.setText("")
        etTranAmt.setText("")
        etTranParticulars.setText("")
        etTranRemarks.setText("")
        etFlowCode.setText("")
        etTranCode.setText("")
        etTranRefNo.setText("")
        etPartitionType.setText("")
        etPartitionDetails.setText("")
        etInstrumentNo.setText("")
        etInstrumentDate.setText("")
        etRefCcyAmt.setText("")
        etRateCode.setText("")
        etRate.setText("")
        etPostUser.setText("")
        etPostTime.setText("")
        etDeleted.setText("")
        spinnerTranType.setSelection(0)
        spinnerPartTranType.setSelection(0)
        spinnerAcctCcy.setSelection(0)
        spinnerRefCcy.setSelection(0)
    }
}