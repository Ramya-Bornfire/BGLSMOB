package com.example.bgls.TransactionReports

import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
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
import java.util.Calendar
import android.content.Intent
import com.example.bgls.MainActivity

class GenericReportActivity : AppCompatActivity() {

    private lateinit var tvReportTitle: TextView
    private lateinit var etAsonDate: EditText
    private lateinit var ivCalendar: ImageView
    private lateinit var spinnerFormat: Spinner
    private lateinit var btnHome: ImageView
    private lateinit var btnDownload: Button
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_report)

        initViews()
        setupNavigation()
        setupListeners()
        
        // Get report details from intent
        val reportTitle = intent.getStringExtra("REPORT_TITLE") ?: "Report"
        val showSpinner = intent.getBooleanExtra("SHOW_SPINNER", false)
        
        tvReportTitle.text = reportTitle
        
        if (showSpinner) {
            findViewById<LinearLayout>(R.id.llFormat).visibility = View.VISIBLE
            val formats = arrayOf("EXCEL", "PDF")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFormat.adapter = adapter
        }
    }

    private fun initViews() {
        tvReportTitle = findViewById(R.id.tvReportTitle)
        etAsonDate = findViewById(R.id.etAsonDate)
        ivCalendar = findViewById(R.id.ivCalendar)
        spinnerFormat = findViewById(R.id.spinnerFormat)
        btnHome = findViewById(R.id.btnHome)
        btnDownload = findViewById(R.id.btnDownload)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupListeners() {
        ivCalendar.setOnClickListener {
            showDatePicker()
        }

        btnDownload.setOnClickListener {
            val date = etAsonDate.text.toString().trim()
            if (date.isEmpty()) {
                toast("Please select a date")
                return@setOnClickListener
            }
            
            startDownload(date)
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
            val selectedDate = String.format("%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year)
            etAsonDate.setText(selectedDate)
        }, year, month, day)

        dpd.show()
    }

    private fun startDownload(date: String) {
        progressBar.visibility = View.VISIBLE
        btnDownload.isEnabled = false

        lifecycleScope.launch {
            try {
                val reportTitleText = tvReportTitle.text.toString()
                val response = when {
                    reportTitleText.contains("Consolidated Loan", true) -> {
                        RetrofitClient.api.downloadConsolidatedLoanReport(date)
                    }
                    reportTitleText.contains("Transaction", true) && !reportTitleText.contains("Recovery", true) -> {
                        RetrofitClient.api.downloadTransactionReport(date)
                    }
                    reportTitleText.contains("Recovery", true) -> {
                        val format = spinnerFormat.selectedItem.toString()
                        RetrofitClient.api.downloadRecoveryReport(date, "Mpesa", format)
                    }
                    reportTitleText.contains("Demand Generation", true) -> {
                        RetrofitClient.api.downloadDemandGenerationReport(date)
                    }
                    reportTitleText.contains("Interest Accrual", true) -> {
                        RetrofitClient.api.downloadInterestAccrualReport(date)
                    }
                    reportTitleText.contains("Penalty Accrual", true) -> {
                        RetrofitClient.api.downloadPenaltyAccrualReport(date)
                    }
                    else -> null
                }

                if (response != null && response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val extension = when {
                            reportTitleText.contains("Recovery", true) -> {
                                if (spinnerFormat.selectedItem.toString().equals("EXCEL", true)) ".xlsx" else ".pdf"
                            }
                            reportTitleText.contains("Interest Accrual", true) || 
                            reportTitleText.contains("Penalty Accrual", true) || 
                            reportTitleText.contains("Consolidated", true) -> ".xlsx"
                            else -> ".pdf"
                        }
                        
                        val fileName = "${reportTitleText.replace(" ", "_")}_$date$extension"
                        val savedFile = saveFile(body, fileName)
                        if (savedFile != null) {
                            toast("Saved: $fileName")
                            val mime = if (extension == ".pdf") "application/pdf" else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            openFile(savedFile, mime)
                        } else {
                            toast("Error: Could not save file to storage")
                        }
                    } else {
                        toast("Error: Server returned empty data")
                    }
                } else {
                    val errorMsg = response?.errorBody()?.string() ?: "Unknown error"
                    toast("Server Error ${response?.code()}: $errorMsg")
                }
            } catch (e: Exception) {
                toast("Network/App Error: ${e.message}")
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
                btnDownload.isEnabled = true
            }
        }
    }

    private suspend fun saveFile(body: ResponseBody, fileName: String): File? =
        withContext(Dispatchers.IO) {
            try {
                // Try public Downloads first
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                
                var file = File(downloadsDir, fileName)
                
                // If public fails or we want to be safe, we could also try app-specific
                // But for now, with legacy storage, public should work.
                
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
            toast("No app found to open this file type. File saved in Downloads.")
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
