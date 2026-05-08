package com.example.bgls.TransactionReports

import android.graphics.Color
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
                    loanvalues  = body?.loanvalues  ?: emptyList()   // Schedule list
                    loanDetails = body?.loanDetails ?: emptyList()   // Details list
                } else {
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
        // Pick the correct list depending on current tab
        val accounts: List<Pair<String, String>> = if (isDetailsMode) {
            // Details tab uses loanDetails  (maps to myModal2 / openModal1 in web)
            loanDetails.map { row ->
                val no   = row.getOrNull(0)?.toString() ?: ""
                val name = row.getOrNull(1)?.toString() ?: ""
                Pair(no, name)
            }
        } else {
            // Schedule tab uses loanvalues  (maps to myModal / openModal in web)
            loanvalues.map { row ->
                val no   = row.getOrNull(0)?.toString() ?: ""
                val name = row.getOrNull(1)?.toString() ?: ""
                Pair(no, name)
            }
        }

        if (accounts.isEmpty()) {
            toast("No accounts loaded yet. Please wait…")
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etSearchAccNo   = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val btnFilter       = dialogView.findViewById<Button>(R.id.btnFilter)
        val btnClose        = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val tlAccounts      = dialogView.findViewById<TableLayout>(R.id.tlAccounts)

        var filterEnabled = false

        fun populateTable(list: List<Pair<String, String>>) {
            tlAccounts.removeAllViews()
            list.forEach { acc ->
                val row = TableRow(this)

                val tvNo = TextView(this).apply {
                    text = acc.first
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvName = TextView(this).apply {
                    text = acc.second
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                }
                row.addView(tvNo)
                row.addView(tvName)
                row.setOnClickListener {
                    etAccountNo.setText(acc.first)
                    etAccountName.setText(acc.second)
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        populateTable(accounts)

        // Filter button toggles search field (mirrors web Filter button)
        btnFilter.setOnClickListener {
            filterEnabled = !filterEnabled
            etSearchAccNo.isEnabled = filterEnabled
            btnFilter.text = if (filterEnabled) "Clear Filter" else "Filter"
            if (filterEnabled) etSearchAccNo.requestFocus()
            else {
                etSearchAccNo.setText("")
                populateTable(accounts)
            }
        }

        // Live filter as user types
        etSearchAccNo.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                val query = s.toString().lowercase()
                val filtered = accounts.filter {
                    it.first.lowercase().contains(query) || it.second.lowercase().contains(query)
                }
                populateTable(filtered)
            }
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
                        val fileName = if (isDetailsMode) "Details_$acctNo.pdf" else "Schedule_$acctNo.pdf"
                        val saved = saveFile(body, fileName)
                        if (saved != null) {
                            toast("Saved: $fileName")
                            openPdf(saved)
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

    // Open the PDF with device viewer
    private fun openPdf(file: File) {
        try {
            val authority = "${applicationContext.packageName}.provider"
            val uri = androidx.core.content.FileProvider.getUriForFile(this, authority, file)
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(android.content.Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            toast("No PDF viewer found. File saved in Downloads.")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}