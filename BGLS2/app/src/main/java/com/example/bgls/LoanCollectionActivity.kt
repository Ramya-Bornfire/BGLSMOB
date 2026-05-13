package com.example.bgls

import android.app.ProgressDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.DataModels.MultipleTransactionRequest
import com.example.bgls.Retrofit.RetrofitClient
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

class LoanCollectionActivity : AppCompatActivity() {

    private lateinit var layoutBulkCollection: LinearLayout
    private lateinit var llBulkRows: LinearLayout
    private lateinit var btnBulkAdd: Button
    private lateinit var btnBulkUpload: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var txtUserIdInfo: TextView
    private lateinit var txtUserNameInfo: TextView
    private lateinit var txtLoginTimeInfo: TextView
    // Weights (must match header)
    private val W_TRAN_ID   = 1.2f
    private val W_NAMES     = 1.5f
    private val W_REF       = 1.2f
    private val W_MOBILE    = 1.5f
    private val W_AMOUNT    = 1.0f
    private val W_ALLOC_AMT = 1.2f
    private val W_TIME      = 1.5f
    private val W_STATUS    = 1.2f
    private val W_ALLOC_RB  = 0.8f
    private val W_DELETE    = 0.6f

    private lateinit var progressDialog: ProgressDialog

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_collection)

        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing...")
            setCancelable(false)
        }

        layoutBulkCollection = findViewById(R.id.layoutBulkCollection)
        llBulkRows = findViewById(R.id.llBulkRows)
        btnBulkAdd = findViewById(R.id.btnBulkAdd)
        btnBulkUpload = findViewById(R.id.btnBulkUpload)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)
        txtUserIdInfo = findViewById(R.id.txtUserIdInfo)
        txtUserNameInfo = findViewById(R.id.txtUserNameInfo)
        txtLoginTimeInfo = findViewById(R.id.txtLoginTimeInfo)

        // Optionally set session data here if available
        // txtUserIdInfo.text = session.userId
        layoutBulkCollection.visibility = LinearLayout.VISIBLE

        btnBulkAdd.setOnClickListener { addRow() }
        btnBulkUpload.setOnClickListener { filePickerLauncher.launch("*/*") }
        btnSubmit.setOnClickListener { submitManualRows() }
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnHome.setOnClickListener {
            finishAffinity()
        }
    }

    // ----------------------------------------------------------------------
    // UI Helpers
    // ----------------------------------------------------------------------
    private fun addRow() {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40))
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
        }

        val columns = listOf(
            Pair(W_TRAN_ID,   ""),
            Pair(W_NAMES,     ""),
            Pair(W_REF,       ""),
            Pair(W_MOBILE,    ""),
            Pair(W_AMOUNT,    ""),
            Pair(W_ALLOC_AMT, "0"),
            Pair(W_TIME,      getCurrentDateTime()),
            Pair(W_STATUS,    "UNALLOCATED")
        )

        for ((weight, defaultText) in columns) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 9f
                setPadding(dp(4), dp(2), dp(4), dp(2))
                setText(defaultText)
                gravity = Gravity.CENTER
                if (weight == W_AMOUNT || weight == W_ALLOC_AMT) {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
            }
            row.addView(et)
        }

        // RadioButton cell (Allocated)
        val rbCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, W_ALLOC_RB)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
            addView(RadioButton(this@LoanCollectionActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
        row.addView(rbCell)

        // Delete cell
        val deleteCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, W_DELETE)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
            addView(ImageView(this@LoanCollectionActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(20))
                setImageResource(android.R.drawable.ic_menu_delete)
                setColorFilter(Color.RED)
                setOnClickListener { llBulkRows.removeView(row) }
            })
        }
        row.addView(deleteCell)

        llBulkRows.addView(row)
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    // ----------------------------------------------------------------------
    // Manual Bulk Collection – triggered by Submit button
    // ----------------------------------------------------------------------
    private fun submitManualRows() {
        val rows = mutableListOf<MultipleTransactionRequest>()
        for (i in 0 until llBulkRows.childCount) {
            val row = llBulkRows.getChildAt(i) as LinearLayout
            val views = mutableListOf<View>()
            for (j in 0 until row.childCount) {
                views.add(row.getChildAt(j))
            }

            val tranId = (views[0] as EditText).text.toString().trim()
            val names = (views[1] as EditText).text.toString().trim()
            val reference = (views[2] as EditText).text.toString().trim()
            val mobile = (views[3] as EditText).text.toString().trim()
            val amountStr = (views[4] as EditText).text.toString().trim()
            val allocatedAmtStr = (views[5] as EditText).text.toString().trim()
            val transTime = (views[6] as EditText).text.toString().trim()
            val status = (views[7] as EditText).text.toString().trim()

            // Skip completely empty rows (all fields empty)
            if (names.isEmpty() && reference.isEmpty() && amountStr.isEmpty()) {
                continue
            }

            // Validate required fields
            if (names.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill Names, Reference and Amount in row ${i+1}", Toast.LENGTH_SHORT).show()
                return
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0

            rows.add(
                MultipleTransactionRequest(
                    acctNamedata = names,
                    tranId = tranId,
                    transactionDate = transTime,
                    tranParticulardata = amount.toString(),
                    acctNum = reference,
                    tranRemarks = allocatedAmtStr,
                    globalAuthUser = null
                )
            )
        }

        if (rows.isEmpty()) {
            Toast.makeText(this, "No valid rows to submit", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.saveMultipleTransactions1(rows)
                }
                progressDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val status = result["status"] as? String
                    if (status == "success") {
                        val message = result["message"] as? String ?: "Transactions saved"
                        val totalCust = result["number_of_customers"] ?: 0
                        val totalAmt = result["total_transaction_amount"] ?: 0.0
                        val alertMsg = "$message\nCustomers: $totalCust\nTotal Amount: $totalAmt"
                        AlertDialog.Builder(this@LoanCollectionActivity)
                            .setTitle("Success")
                            .setMessage(alertMsg)
                            .setPositiveButton("OK") { _, _ -> finish() }  // optional: clear or finish
                            .show()
                    } else {
                        val errMsg = result["message"] as? String ?: "Unknown error"
                        Toast.makeText(this@LoanCollectionActivity, "Error: $errMsg", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@LoanCollectionActivity, "Server error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@LoanCollectionActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ----------------------------------------------------------------------
    // File Upload – triggered by Upload button
    // ----------------------------------------------------------------------
    private fun uploadFile(uri: Uri) {
        val fileName = getFileNameFromUri(uri) ?: "upload.xlsx"
        val tempFile = File(cacheDir, fileName)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = tempFile.asRequestBody("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

        val uploadProgress = ProgressDialog(this).apply {
            setMessage("Uploading...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.uploadFileData(body, "TRANSACTION", false)
                }
                uploadProgress.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val status = result["status"] as? String
                    val message = result["message"] as? String ?: "Upload completed"
                    val succeeded = result["TotalSucceeded"] ?: 0
                    val failed = result["TotalFailed"] ?: 0
                    val duplicate = result["TotalDuplicate"] ?: 0
                    val processed = result["TotalProcessed"] ?: 0

                    val alertMsg = when (status) {
                        "success" -> "$message\nProcessed: $processed\nSucceeded: $succeeded\nFailed: $failed\nDuplicate: $duplicate"
                        "duplicate" -> "Duplicate file detected.\nDuplicate records: $duplicate\nUpload cancelled."
                        else -> "Upload error: $message"
                    }
                    AlertDialog.Builder(this@LoanCollectionActivity)
                        .setTitle(if (status == "success") "Upload Successful" else "Upload Issue")
                        .setMessage(alertMsg)
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(this@LoanCollectionActivity, "Upload failed: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                uploadProgress.dismiss()
                Toast.makeText(this@LoanCollectionActivity, "Upload error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }
}