package com.example.bgls.TransactionReports

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

class CreditFacilityReportActivity : AppCompatActivity() {

    private lateinit var btnDetails: Button
    private lateinit var btnSchedule: Button
    private lateinit var ivSearchAccount: ImageView
    private lateinit var etAccountNo: EditText
    private lateinit var etAccountName: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button
    private lateinit var progressBar: ProgressBar   // add this to your layout (see Step 4)

    // true = Details mode, false = Schedule mode
    private var isDetailsMode = true

    // Data loaded from API
    private var loanvalues: List<List<Any?>> = emptyList()   // for Schedule modal
    private var loanDetails: List<List<Any?>> = emptyList()  // for Details modal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_facility_report)

        initViews()
        setupListeners()
        loadAccountLists()   // fetch both lists from server on open
    }

    // ------------------------------------------------------------------
    // Init
    // ------------------------------------------------------------------

    private fun initViews() {
        btnDetails      = findViewById(R.id.btnDetails)
        btnSchedule     = findViewById(R.id.btnSchedule)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        etAccountNo     = findViewById(R.id.etAccountNo)
        etAccountName   = findViewById(R.id.etAccountName)
        btnSubmit       = findViewById(R.id.btnSubmit)
        btnHome         = findViewById(R.id.btnHome)
        btnBack         = findViewById(R.id.btnBack)
        progressBar     = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnDetails.setOnClickListener  { setMode(true) }
        btnSchedule.setOnClickListener { setMode(false) }

        ivSearchAccount.setOnClickListener { openAccountSearchDialog() }

        btnSubmit.setOnClickListener {
            val acctNo = etAccountNo.text.toString().trim()
            if (acctNo.isEmpty()) {
                Toast.makeText(this, "Please select an account first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            downloadPdf(acctNo)
        }

        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
    }

    // ------------------------------------------------------------------
    // Mode toggle (Details / Schedule)
    // ------------------------------------------------------------------

    private fun setMode(detailsMode: Boolean) {
        isDetailsMode = detailsMode
        // Clear previous selection when switching tabs
        etAccountNo.setText("")
        etAccountName.setText("")

        if (detailsMode) {
            btnDetails.backgroundTintList  = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnDetails.setTextColor(Color.WHITE)
            btnSchedule.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            btnSchedule.setTextColor(Color.parseColor("#333333"))
        } else {
            btnDetails.backgroundTintList  = android.content.res.ColorStateList.valueOf(Color.WHITE)
            btnDetails.setTextColor(Color.parseColor("#333333"))
            btnSchedule.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnSchedule.setTextColor(Color.WHITE)
        }
    }

    // ------------------------------------------------------------------
    // Load account lists from API
    // ------------------------------------------------------------------

    private fun loadAccountLists() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getCreditFacilityReport()
                if (response.isSuccessful) {
                    val body = response.body()
                    loanvalues  = body?.loanvalues  ?: emptyList()
                    loanDetails = body?.loanDetails ?: emptyList()
                    
                    val total = loanvalues.size + loanDetails.size
                    android.util.Log.d("CreditFacility", "Loaded $total accounts (Schedule: ${loanvalues.size}, Details: ${loanDetails.size})")
                    toast("Loaded $total accounts")
                } else {
                    android.util.Log.e("CreditFacility", "API Error: ${response.code()}")
                    toast("Failed to load accounts: ${response.code()}")
                }
            } catch (e: Exception) {
                toast("Network error: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // ------------------------------------------------------------------
    // Account Search Dialog
    // ------------------------------------------------------------------

    private fun openAccountSearchDialog() {
        val accounts: List<Pair<String, String>> = if (isDetailsMode) {
            loanDetails.map { row ->
                val no   = row.getOrNull(0)?.toString() ?: ""
                val name = row.getOrNull(1)?.toString() ?: ""
                Pair(no, name)
            }
        } else {
            loanvalues.map { row ->
                val no   = row.getOrNull(0)?.toString() ?: ""
                val name = row.getOrNull(1)?.toString() ?: ""
                Pair(no, name)
            }
        }

        if (accounts.isEmpty()) {
            toast("No accounts loaded. Re-fetching...")
            loadAccountLists()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etSearchAccNo   = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val etSearchAccName = dialogView.findViewById<EditText>(R.id.etSearchAccName)
        val btnFilter       = dialogView.findViewById<Button>(R.id.btnFilter)
        val btnClose        = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val tlAccounts      = dialogView.findViewById<TableLayout>(R.id.tlAccounts)

        fun populateTable(list: List<Pair<String, String>>) {
            tlAccounts.removeAllViews()
            
            // Add Header Row
            val header = TableRow(this).apply {
                setBackgroundColor(Color.LTGRAY)
                setPadding(2, 2, 2, 2)
            }
            header.addView(TextView(this).apply { 
                text = "Acc No"
                setPadding(8, 8, 8, 8)
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
                layoutParams = TableRow.LayoutParams(0, -2, 1f) 
            })
            header.addView(TextView(this).apply { 
                text = "Acc Name"
                setPadding(8, 8, 8, 8)
                setTypeface(null, Typeface.BOLD)
                textSize = 12f
                layoutParams = TableRow.LayoutParams(0, -2, 2f) 
            })
            tlAccounts.addView(header)

            // Limit results to prevent UI freeze (e.g., first 50)
            val displayList = list.take(100)
            if (list.size > 100) {
                tlAccounts.addView(TextView(this).apply {
                    text = "Showing first 100 of ${list.size} accounts. Please use filter."
                    setTextColor(Color.RED)
                    setPadding(16, 8, 16, 8)
                    textSize = 10f
                    setTypeface(null, Typeface.ITALIC)
                })
            }

            displayList.forEach { acc ->
                val row = TableRow(this).apply {
                    setPadding(2, 2, 2, 2)
                    isClickable = true
                    isFocusable = true
                    setBackgroundResource(android.R.drawable.list_selector_background)
                }

                row.addView(TextView(this).apply { text = acc.first; setPadding(12, 12, 12, 12); textSize = 11f; layoutParams = TableRow.LayoutParams(0, -2, 1f) })
                row.addView(TextView(this).apply { text = acc.second; setPadding(12, 12, 12, 12); textSize = 11f; layoutParams = TableRow.LayoutParams(0, -2, 2f) })
                
                row.setOnClickListener {
                    etAccountNo.setText(acc.first)
                    etAccountName.setText(acc.second)
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        populateTable(accounts)

        fun filterAction() {
            val qNo = etSearchAccNo.text.toString().trim().lowercase()
            val qName = etSearchAccName.text.toString().trim().lowercase()
            
            val filtered = if (qNo.isEmpty() && qName.isEmpty()) {
                accounts
            } else {
                accounts.filter {
                    it.first.lowercase().contains(qNo) || it.second.lowercase().contains(qName)
                }
            }
            populateTable(filtered)
        }

        btnFilter.setOnClickListener { filterAction() }

        etSearchAccNo.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { filterAction() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        etSearchAccName.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { filterAction() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    // ------------------------------------------------------------------
    // PDF Download
    // ------------------------------------------------------------------

    private fun downloadPdf(acctNo: String) {
        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        lifecycleScope.launch {
            try {
                // Details tab → downloadDetailsPdf  |  Schedule tab → downloadSchedulePdf
                val response: retrofit2.Response<ResponseBody> = if (isDetailsMode) {
                    RetrofitClient.api.downloadDetailsPdf(acctNo = acctNo)
                } else {
                    RetrofitClient.api.downloadSchedulePdf(acctNo = acctNo)
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Check if server returned PDF or Excel (based on your backend code)
                        val contentType = response.headers()["Content-Type"] ?: ""
                        val isExcel = contentType.contains("spreadsheet") || contentType.contains("excel")
                        val extension = if (isExcel) ".xlsx" else ".pdf"
                        val mimeType = if (isExcel) "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" else "application/pdf"

                        val fileName = if (isDetailsMode) "Details_$acctNo$extension" else "Schedule_$acctNo$extension"
                        val saved = saveFile(body, fileName)
                        if (saved != null) {
                            toast("Saved: $fileName")
                            openFile(saved, mimeType)
                        } else {
                            toast("Error: Could not save file to storage")
                        }
                    } else {
                        toast("Error: Server returned empty data")
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    toast("Server Error ${response.code()}: $errorMsg")
                }
            } catch (e: Exception) {
                toast("Network/App Error: ${e.message}")
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
            }
        }
    }

    // Save to Downloads folder
    private suspend fun saveFile(body: ResponseBody, fileName: String): File? =
        withContext(Dispatchers.IO) {
            try {
                // Try public Downloads first
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                
                var file = File(downloadsDir, fileName)
                
                FileOutputStream(file).use { fos ->
                    body.byteStream().use { it.copyTo(fos) }
                }
                file
            } catch (e: Exception) {
                // Fallback to internal storage if public fails
                try {
                    val fallbackDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val fallbackFile = File(fallbackDir, fileName)
                    FileOutputStream(fallbackFile).use { fos ->
                        body.byteStream().use { it.copyTo(fos) }
                    }
                    fallbackFile
                } catch (e2: Exception) {
                    null
                }
            }
        }

    // Open the file with device viewer
    private fun openFile(file: File, mimeType: String) {
        try {
            val authority = "${applicationContext.packageName}.provider"
            val uri = androidx.core.content.FileProvider.getUriForFile(this, authority, file)
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(android.content.Intent.createChooser(intent, "Open Report"))
        } catch (e: Exception) {
            toast("No app found to open this file. File saved in Downloads.")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}