package com.example.bgls.LoanSchedule

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanSchedule
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LoanScheduleActivity : AppCompatActivity() {

    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etAccountId: EditText
    private lateinit var etAccountName: EditText
    private lateinit var etLoanAmount: EditText
    private lateinit var etLoanDate: EditText

    private lateinit var btnUpload: Button
    private lateinit var btnList: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LoanScheduleActivityAdapter

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadFile(it) }
            ?: Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_schedule)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        initViews()
        setupRecyclerView()
        setupButtons()
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initViews() {
        etCustomerId   = findViewById(R.id.etCustomerId)
        etCustomerName = findViewById(R.id.etCustomerName)
        etAccountId    = findViewById(R.id.etAccountId)
        etAccountName  = findViewById(R.id.etAccountName)
        etLoanAmount   = findViewById(R.id.etLoanAmount)
        etLoanDate     = findViewById(R.id.etLoanDate)

        btnUpload = findViewById(R.id.btnUpload)
        btnList   = findViewById(R.id.btnList)

        recyclerView = findViewById(R.id.recyclerViewLoanSchedule)
    }

    private fun setupRecyclerView() {
        adapter = LoanScheduleActivityAdapter(this, emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")   // allow any file, or restrict to .xlsx
        }
        btnList.setOnClickListener {
            startActivity(Intent(this, LoanScheduleListActivity::class.java))
        }
    }

    private fun uploadFile(uri: Uri) {
        val file = getFileFromUri(uri)
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Unable to access file", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = when (file.extension.lowercase()) {
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> "application/vnd.ms-excel"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }

        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val fileInput = "loanSchedule"   // adjust to match backend expectation
        val overwrite = true

        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.uploadFileData(body, fileInput, overwrite)
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(this@LoanScheduleActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    populateFieldsFromResponse(result)
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@LoanScheduleActivity, "Upload failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
        }
    }

    /**
     * Fill form fields and schedule list from upload response.
     * Update keys to match your backend response.
     */
    private fun populateFieldsFromResponse(data: Map<String, Any>?) {
        if (data == null) return

        etCustomerId.setText(data["customerId"]?.toString() ?: "")
        etCustomerName.setText(data["customerName"]?.toString() ?: "")
        etAccountId.setText(data["accountId"]?.toString() ?: "")
        etAccountName.setText(data["accountName"]?.toString() ?: "")
        etLoanAmount.setText(data["loanAmount"]?.toString() ?: "")
        etLoanDate.setText(data["loanDate"]?.toString() ?: "")

        // Populate schedule list if present
        val scheduleList = data["scheduleList"] as? List<Map<String, Any>> ?: emptyList()
        val mapped = scheduleList.map {
            LoanSchedule(
                dueDate = it["dueDate"]?.toString() ?: "",
                principalExpenses = it["principalExpenses"]?.toString() ?: "",
                interestExpenses = it["interestExpenses"]?.toString() ?: "",
                feeExpenses = it["feeExpenses"]?.toString() ?: "",
                penaltyExpenses = it["penaltyExpenses"]?.toString() ?: "",
                repaidDate = it["repaidDate"]?.toString() ?: "",
                principalPaid = it["principalPaid"]?.toString() ?: "",
                interestPaid = it["interestPaid"]?.toString() ?: "",
                feePaid = it["feePaid"]?.toString() ?: "",
                penaltyPaid = it["penaltyPaid"]?.toString() ?: "",
                totalDues = it["totalDues"]?.toString() ?: ""
            )
        }
        adapter.updateList(mapped)
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex("_data")
                    if (columnIndex != -1) {
                        val path = it.getString(columnIndex)
                        return File(path)
                    }
                }
            }
            // Fallback: copy to cache
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val tempFile = File(cacheDir, "temp_upload_${System.currentTimeMillis()}")
            tempFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}