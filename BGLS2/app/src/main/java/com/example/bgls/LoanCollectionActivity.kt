package com.example.bgls

import android.app.ProgressDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.util.Log
import android.view.WindowManager
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


    private lateinit var progressDialog: ProgressDialog

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_collection)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
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
                LinearLayout.LayoutParams.MATCH_PARENT, dp(45))
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
        }

        // Weights matching XML header
        val columnWeights = listOf(1.2f, 1.5f, 1.2f, 1.5f, 1.0f, 1.4f, 1.5f, 1.2f)
        val defaultTexts = listOf("", "", "", "", "", "0", getCurrentDateTime(), "UNALLOCATED")

        for (i in columnWeights.indices) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, columnWeights[i])
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 9f
                setPadding(dp(4), dp(2), dp(4), dp(2))
                setText(defaultTexts[i])
                gravity = Gravity.CENTER
                if (i == 4 || i == 5) {
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                }
            }
            row.addView(et)
        }

        // RadioButton cell (Allocated) - weight 0.9
        val rbCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.9f)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
            addView(RadioButton(this@LoanCollectionActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
        row.addView(rbCell)

        // Delete cell - weight 0.8
        val deleteCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f)
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
//    private fun submitManualRows() {
//        val newRows = mutableListOf<MultipleTransactionRequest>()
//        val updateRows = mutableListOf<Map<String, Any>>()
//
//        for (i in 0 until llBulkRows.childCount) {
//            val row = llBulkRows.getChildAt(i) as LinearLayout
//            val views = mutableListOf<View>()
//            for (j in 0 until row.childCount) {
//                views.add(row.getChildAt(j))
//            }
//
//            val tranId = (views[0] as EditText).text.toString().trim()
//            val names = (views[1] as EditText).text.toString().trim()
//            val reference = (views[2] as EditText).text.toString().trim()
//            val mobile = (views[3] as EditText).text.toString().trim()
//            val amountStr = (views[4] as EditText).text.toString().trim()
//            val allocatedAmtStr = (views[5] as EditText).text.toString().trim()
//            val transTime = (views[6] as EditText).text.toString().trim()
//            val status = (views[7] as EditText).text.toString().trim()
//
//            // Skip completely empty rows
//            if (names.isEmpty() && reference.isEmpty() && amountStr.isEmpty()) {
//                continue
//            }
//
//            // Validate required fields
//            if (names.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
//                Toast.makeText(this, "Please fill Names, Reference and Amount in row ${i+1}", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            val amount = amountStr.toDoubleOrNull() ?: 0.0
//            val srlNo = row.tag as? String
//
//            if (srlNo != null) {
//                // Existing row to update
//                val map = mutableMapOf<String, Any>()
//                map["srl_no"] = srlNo
//                map["names"] = names
//                map["reference"] = reference
//                map["mobile_number"] = mobile
//                map["amount"] = amountStr
//                map["allocated_amount"] = allocatedAmtStr
//                map["status"] = status
//                updateRows.add(map)
//            } else {
//                // New row
//                newRows.add(
//                    MultipleTransactionRequest(
//                        acctNamedata = names,
//                        tranId = tranId,
//                        transactionDate = transTime,
//                        tranParticulardata = amount.toString(),
//                        acctNum = reference,
//                        tranRemarks = allocatedAmtStr,
//                        globalAuthUser = null
//                    )
//                )
//            }
//        }
//
//        if (newRows.isEmpty() && updateRows.isEmpty()) {
//            Toast.makeText(this, "No valid rows to submit", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        progressDialog.show()
//        lifecycleScope.launch {
//            try {
//                var newRowsSuccess = true
//                var updateRowsSuccess = true
//                var msg = ""
//
//                // 1) Send New Rows
//                if (newRows.isNotEmpty()) {
//                    val response = withContext(Dispatchers.IO) {
//                        RetrofitClient.api.saveMultipleTransactions1(newRows)
//                    }
//                    if (response.isSuccessful && response.body() != null) {
//                        val result = response.body()!!
//                        if (result["status"] == "success") {
//                            msg += "New rows saved. "
//                        } else {
//                            newRowsSuccess = false
//                            msg += "New rows error: ${result["message"]}. "
//                        }
//                    } else {
//                        newRowsSuccess = false
//                        msg += "New rows failed (${response.code()}). "
//                    }
//                }
//
//                // 2) Send Updated Rows
//                if (updateRows.isNotEmpty()) {
//                    val response = withContext(Dispatchers.IO) {
//                        RetrofitClient.api.updateMultipleTransactions(updateRows)
//                    }
//                    if (response.isSuccessful && response.body() != null) {
//                        val result = response.body()!!
//                        if (result["status"] == "success") {
//                            msg += "Modified rows updated. "
//                        } else {
//                            updateRowsSuccess = false
//                            msg += "Update error: ${result["message"]}. "
//                        }
//                    } else {
//                        updateRowsSuccess = false
//                        msg += "Update failed (${response.code()}). "
//                    }
//                }
//
//                progressDialog.dismiss()
//                if (newRowsSuccess && updateRowsSuccess) {
//                    AlertDialog.Builder(this@LoanCollectionActivity)
//                        .setTitle("Success")
//                        .setMessage(msg)
//                        .setPositiveButton("OK") { _, _ -> fetchTransactionsAndPopulate() }
//                        .show()
//                } else {
//                    Toast.makeText(this@LoanCollectionActivity, msg, Toast.LENGTH_LONG).show()
//                }
//            } catch (e: Exception) {
//                progressDialog.dismiss()
//                Toast.makeText(this@LoanCollectionActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//    private fun submitManualRows() {
//        val newRows = mutableListOf<MultipleTransactionRequest>()
//        val updateRows = mutableListOf<Map<String, Any>>()
//
//        for (i in 0 until llBulkRows.childCount) {
//            val row = llBulkRows.getChildAt(i) as LinearLayout
//            val views = mutableListOf<View>()
//            for (j in 0 until row.childCount) {
//                views.add(row.getChildAt(j))
//            }
//
//            val tranId = (views[0] as EditText).text.toString().trim()
//            val names = (views[1] as EditText).text.toString().trim()
//            val reference = (views[2] as EditText).text.toString().trim()
//            val mobile = (views[3] as EditText).text.toString().trim()
//            val amountStr = (views[4] as EditText).text.toString().trim()
//            val allocatedAmtStr = (views[5] as EditText).text.toString().trim()
//            val transTime = (views[6] as EditText).text.toString().trim()
//            val status = (views[7] as EditText).text.toString().trim()
//
//            // Skip completely empty rows
//            if (names.isEmpty() && reference.isEmpty() && amountStr.isEmpty()) {
//                Log.d("LoanCollection", "Row ${i+1} is empty, skipping")
//                continue
//            }
//
//            // Validate required fields
//            if (names.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
//                Toast.makeText(this, "Please fill Names, Reference and Amount in row ${i+1}", Toast.LENGTH_SHORT).show()
//                Log.w("LoanCollection", "Row ${i+1} missing required fields: names=$names, reference=$reference, amount=$amountStr")
//                return
//            }
//
//            val amount = amountStr.toDoubleOrNull() ?: 0.0
//            val srlNo = row.tag as? String
//
//            if (srlNo != null) {
//                // Existing row to update
//                val map = mutableMapOf<String, Any>()
//                map["srl_no"] = srlNo
//                map["names"] = names
//                map["reference"] = reference
//                map["mobile_number"] = mobile
//                map["amount"] = amountStr
//                map["allocated_amount"] = allocatedAmtStr
//                map["status"] = status
//                updateRows.add(map)
//                Log.d("LoanCollection", "Row ${i+1} added to update list: srlNo=$srlNo, names=$names")
//            } else {
//                // New row
//                newRows.add(
//                    MultipleTransactionRequest(
//                        acctNamedata = names,
//                        tranId = tranId,
//                        transactionDate = transTime,
//                        tranParticulardata = amount.toString(),
//                        acctNum = reference,
//                        tranRemarks = allocatedAmtStr,
//                        globalAuthUser = null
//                    )
//                )
//                Log.d("LoanCollection", "Row ${i+1} added to new rows list: names=$names, amount=$amount")
//            }
//        }
//
//        if (newRows.isEmpty() && updateRows.isEmpty()) {
//            Toast.makeText(this, "No valid rows to submit", Toast.LENGTH_SHORT).show()
//            Log.w("LoanCollection", "No rows to submit")
//            return
//        }
//
//        Log.d("LoanCollection", "Submitting: newRows=${newRows.size}, updateRows=${updateRows.size}")
//
//        progressDialog.show()
//        lifecycleScope.launch {
//            try {
//                var newRowsSuccess = true
//                var updateRowsSuccess = true
//                var msg = ""
//
//                // 1) Send New Rows
//                if (newRows.isNotEmpty()) {
//                    Log.d("LoanCollection", "Calling saveMultipleTransactions1 with ${newRows.size} rows")
//                    val response = withContext(Dispatchers.IO) {
//                        RetrofitClient.api.saveMultipleTransactions1(newRows)
//                    }
//                    Log.d("LoanCollection", "saveMultipleTransactions1 response code: ${response.code()}")
//                    if (response.isSuccessful && response.body() != null) {
//                        val result = response.body()!!
//                        Log.d("LoanCollection", "saveMultipleTransactions1 response body: $result")
//                        if (result["status"] == "success") {
//                            msg += "New rows saved. "
//                            Log.d("LoanCollection", "New rows saved successfully")
//                        } else {
//                            newRowsSuccess = false
//                            msg += "New rows error: ${result["message"]}. "
//                            Log.e("LoanCollection", "API error from saveMultipleTransactions1: ${result["message"]}")
//                        }
//                    } else {
//                        newRowsSuccess = false
//                        val errorBody = response.errorBody()?.string()
//                        msg += "New rows failed (${response.code()}). "
//                        Log.e("LoanCollection", "HTTP error ${response.code()} - saveMultipleTransactions1, body: $errorBody")
//                    }
//                }
//
//                // 2) Send Updated Rows
//                if (updateRows.isNotEmpty()) {
//                    Log.d("LoanCollection", "Calling updateMultipleTransactions with ${updateRows.size} rows")
//                    val response = withContext(Dispatchers.IO) {
//                        RetrofitClient.api.updateMultipleTransactions(updateRows)
//                    }
//                    Log.d("LoanCollection", "updateMultipleTransactions response code: ${response.code()}")
//                    if (response.isSuccessful && response.body() != null) {
//                        val result = response.body()!!
//                        Log.d("LoanCollection", "updateMultipleTransactions response body: $result")
//                        if (result["status"] == "success") {
//                            msg += "Modified rows updated. "
//                            Log.d("LoanCollection", "Update rows saved successfully")
//                        } else {
//                            updateRowsSuccess = false
//                            msg += "Update error: ${result["message"]}. "
//                            Log.e("LoanCollection", "API error from updateMultipleTransactions: ${result["message"]}")
//                        }
//                    } else {
//                        updateRowsSuccess = false
//                        val errorBody = response.errorBody()?.string()
//                        msg += "Update failed (${response.code()}). "
//                        Log.e("LoanCollection", "HTTP error ${response.code()} - updateMultipleTransactions, body: $errorBody")
//                    }
//                }
//
//                progressDialog.dismiss()
//                if (newRowsSuccess && updateRowsSuccess) {
//                    AlertDialog.Builder(this@LoanCollectionActivity)
//                        .setTitle("Success")
//                        .setMessage(msg)
//                        .setPositiveButton("OK") { _, _ -> fetchTransactionsAndPopulate() }
//                        .show()
//                    Log.d("LoanCollection", "All operations completed successfully")
//                } else {
//                    Toast.makeText(this@LoanCollectionActivity, msg, Toast.LENGTH_LONG).show()
//                    Log.w("LoanCollection", "Partial or complete failure: $msg")
//                }
//            } catch (e: Exception) {
//                progressDialog.dismiss()
//                // Print full stack trace
//                e.printStackTrace()
//                Log.e("LoanCollection", "Exception in submitManualRows", e)
//
//                // Extract detailed error information
//                val errorDetail = when (e) {
//                    is retrofit2.HttpException -> {
//                        val errorBody = e.response()?.errorBody()?.string()
//                        "HTTP ${e.code()}: ${e.message()}\nBody: $errorBody"
//                    }
//                    is java.net.UnknownHostException -> "No internet connection: ${e.message}"
//                    is java.net.SocketTimeoutException -> "Connection timeout: ${e.message}"
//                    is java.io.IOException -> "IO Error: ${e.message}"
//                    is kotlinx.coroutines.TimeoutCancellationException -> "Coroutine timeout: ${e.message}"
//                    else -> e.message ?: "Unknown error"
//                }
//                Log.e("LoanCollection", "Detailed error info: $errorDetail")
//
//                Toast.makeText(
//                    this@LoanCollectionActivity,
//                    "Network error: ${e.message}",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
//    }

    private fun submitManualRows() {
        val newRows = mutableListOf<MultipleTransactionRequest>()
        val updateRows = mutableListOf<Map<String, Any>>()

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

            // Skip completely empty rows
            if (names.isEmpty() && reference.isEmpty() && amountStr.isEmpty()) {
                Log.d("LoanCollection", "Row ${i+1} is empty, skipping")
                continue
            }

            // Validate required fields
            if (names.isEmpty() || reference.isEmpty() || amountStr.isEmpty()) {
                Toast.makeText(this, "Please fill Names, Reference and Amount in row ${i+1}", Toast.LENGTH_SHORT).show()
                Log.w("LoanCollection", "Row ${i+1} missing required fields: names=$names, reference=$reference, amount=$amountStr")
                return
            }

            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val srlNo = row.tag as? String

            if (srlNo != null) {
                // Existing row to update
                val map = mutableMapOf<String, Any>()
                map["srl_no"] = srlNo
                map["names"] = names
                map["reference"] = reference
                map["mobile_number"] = mobile
                map["amount"] = amountStr
                map["allocated_amount"] = allocatedAmtStr
                map["status"] = status
                updateRows.add(map)
                Log.d("LoanCollection", "Row ${i+1} added to update list: srlNo=$srlNo, names=$names")
            } else {
                // New row
                newRows.add(
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
                Log.d("LoanCollection", "Row ${i+1} added to new rows list: names=$names, amount=$amount")
            }
        }

        if (newRows.isEmpty() && updateRows.isEmpty()) {
            Toast.makeText(this, "No valid rows to submit", Toast.LENGTH_SHORT).show()
            Log.w("LoanCollection", "No rows to submit")
            return
        }

        Log.d("LoanCollection", "Submitting: newRows=${newRows.size}, updateRows=${updateRows.size}")

        progressDialog.show()
        lifecycleScope.launch {
            try {
                var newRowsSuccess = true
                var updateRowsSuccess = true
                var msg = ""

                // 1) Send New Rows
                if (newRows.isNotEmpty()) {
                    Log.d("LoanCollection", "Calling saveMultipleTransactions1 with ${newRows.size} rows")
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.saveMultipleTransactions1(newRows)
                    }
                    Log.d("LoanCollection", "saveMultipleTransactions1 response code: ${response.code()}")
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        Log.d("LoanCollection", "saveMultipleTransactions1 response body: $result")
                        if (result["status"] == "success") {
                            msg += "New rows saved. "
                            Log.d("LoanCollection", "New rows saved successfully")
                        } else {
                            newRowsSuccess = false
                            msg += "New rows error: ${result["message"]}. "
                            Log.e("LoanCollection", "API error from saveMultipleTransactions1: ${result["message"]}")
                        }
                    } else {
                        newRowsSuccess = false
                        val errorBody = response.errorBody()?.string()
                        msg += "New rows failed (${response.code()}). "
                        Log.e("LoanCollection", "HTTP error ${response.code()} - saveMultipleTransactions1, body: $errorBody")
                    }
                }

                // 2) Send Updated Rows
                if (updateRows.isNotEmpty()) {
                    Log.d("LoanCollection", "Calling updateMultipleTransactions with ${updateRows.size} rows")
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.updateMultipleTransactions(updateRows)
                    }
                    Log.d("LoanCollection", "updateMultipleTransactions response code: ${response.code()}")
                    if (response.isSuccessful && response.body() != null) {
                        val result = response.body()!!
                        Log.d("LoanCollection", "updateMultipleTransactions response body: $result")
                        if (result["status"] == "success") {
                            msg += "Modified rows updated. "
                            Log.d("LoanCollection", "Update rows saved successfully")
                        } else {
                            updateRowsSuccess = false
                            msg += "Update error: ${result["message"]}. "
                            Log.e("LoanCollection", "API error from updateMultipleTransactions: ${result["message"]}")
                        }
                    } else {
                        updateRowsSuccess = false
                        val errorBody = response.errorBody()?.string()
                        msg += "Update failed (${response.code()}). "
                        Log.e("LoanCollection", "HTTP error ${response.code()} - updateMultipleTransactions, body: $errorBody")
                    }
                }

                progressDialog.dismiss()
                if (newRowsSuccess && updateRowsSuccess) {
                    AlertDialog.Builder(this@LoanCollectionActivity)
                        .setTitle("Success")
                        .setMessage(msg)
                        .setPositiveButton("OK") { _, _ -> fetchTransactionsAndPopulate() }
                        .show()
                    Log.d("LoanCollection", "All operations completed successfully")
                } else {
                    Toast.makeText(this@LoanCollectionActivity, msg, Toast.LENGTH_LONG).show()
                    Log.w("LoanCollection", "Partial or complete failure: $msg")
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                // Print full stack trace
                e.printStackTrace()
                Log.e("LoanCollection", "Exception in submitManualRows", e)

                // Extract detailed error information
                val errorDetail = when (e) {
                    is retrofit2.HttpException -> {
                        val errorBody = e.response()?.errorBody()?.string()
                        "HTTP ${e.code()}: ${e.message()}\nBody: $errorBody"
                    }
                    is java.net.UnknownHostException -> "No internet connection: ${e.message}"
                    is java.net.SocketTimeoutException -> "Connection timeout: ${e.message}"
                    is java.io.IOException -> "IO Error: ${e.message}"
                    is kotlinx.coroutines.TimeoutCancellationException -> "Coroutine timeout: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                Log.e("LoanCollection", "Detailed error info: $errorDetail")

                Toast.makeText(
                    this@LoanCollectionActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
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
                    val succeeded = (result["TotalSucceeded"] as? Number)?.toInt() ?: 0
                    val failed = (result["TotalFailed"] as? Number)?.toInt() ?: 0
                    val duplicate = (result["TotalDuplicate"] as? Number)?.toInt() ?: 0
                    val processed = (result["TotalProcessed"] as? Number)?.toInt() ?: 0

                    val alertMsg = when (status) {
                        "success" -> "$message\nProcessed: $processed\nSucceeded: $succeeded\nFailed: $failed\nDuplicate: $duplicate"
                        "duplicate" -> "Duplicate file detected.\nDuplicate records: $duplicate\nUpload cancelled."
                        else -> "Upload error: $message"
                    }
                    AlertDialog.Builder(this@LoanCollectionActivity)
                        .setTitle(if (status == "success") "Upload Successful" else "Upload Issue")
                        .setMessage(alertMsg)
                        .setPositiveButton("OK") { _, _ ->
                            if (status == "success") {
                                fetchTransactionsAndPopulate()
                            }
                        }
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

    private fun fetchTransactionsAndPopulate() {
        val loadProgress = ProgressDialog(this).apply {
            setMessage("Loading data...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.loanOperation("view2")
                }
                loadProgress.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val list = result.tranData
                    if (list != null) {
                        populateBulkRows(list as List<Map<String, Any>>)
                    } else {
                        Toast.makeText(this@LoanCollectionActivity, "No data available", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@LoanCollectionActivity, "Failed to load data: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                loadProgress.dismiss()
                Toast.makeText(this@LoanCollectionActivity, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateBulkRows(transactions: List<Map<String, Any>>) {
        llBulkRows.removeAllViews()

        for (item in transactions) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(45))
                orientation = LinearLayout.HORIZONTAL
                setBackgroundColor(Color.WHITE)
            }
            
            // Store srl_no for updates
            row.tag = item["srl_no"]?.toString()

            val columnWeights = listOf(1.2f, 1.5f, 1.2f, 1.5f, 1.0f, 1.4f, 1.5f, 1.2f)
            
            val tranId = item["transaction_id"]?.toString() ?: ""
            val names = item["names"]?.toString() ?: ""
            val reference = item["reference"]?.toString() ?: ""
            val mobile = item["mobile_number"]?.toString() ?: ""
            val amount = item["amount"]?.toString() ?: "0"
            val allocated = item["allocated_amount"]?.toString() ?: "0"
            val transTime = item["trans_time"]?.toString() ?: getCurrentDateTime()
            val status = item["status"]?.toString() ?: "UNALLOCATED"

            val defaultTexts = listOf(tranId, names, reference, mobile, amount, allocated, transTime, status)

            for (i in columnWeights.indices) {
                val et = EditText(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, columnWeights[i])
                    setBackgroundResource(R.drawable.table_cell_bg)
                    textSize = 9f
                    setPadding(dp(4), dp(2), dp(4), dp(2))
                    setText(defaultTexts[i])
                    gravity = Gravity.CENTER
                    if (i == 4 || i == 5) {
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }
                }
                row.addView(et)
            }

            // RadioButton cell (Allocated)
            val rbCell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.9f)
                setBackgroundResource(R.drawable.table_cell_bg)
                gravity = Gravity.CENTER
                val rb = RadioButton(this@LoanCollectionActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                    isChecked = status.equals("ALLOCATED", ignoreCase = true) || status.equals("PARTIAL ALLOCATED", ignoreCase = true)
                    setOnClickListener {
                        val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                        showAllocationDialog(names, parsedAmt)
                    }
                }
                addView(rb)
            }
            row.addView(rbCell)

            // Delete cell
            val deleteCell = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0.8f)
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
    }

    private fun showAllocationDialog(customerId: String, transactionAmt: Double) {
        if (customerId.isBlank()) {
            Toast.makeText(this, "Customer ID/Name is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val pDialog = ProgressDialog(this).apply {
            setMessage("Fetching allocation details...")
            setCancelable(false)
            show()
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.getAccountDetails(customerId, transactionAmt)
                }
                pDialog.dismiss()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    showAllocationDialogUI(data)
                } else {
                    Toast.makeText(this@LoanCollectionActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                pDialog.dismiss()
                Toast.makeText(this@LoanCollectionActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAllocationDialogUI(flows: List<Map<String, Any>>) {
        val context = this
        val scrollView = ScrollView(context)
        val tableLayout = TableLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setStretchAllColumns(true)
        }
        
        // Add Header Row
        val headerRow = TableRow(context).apply { setBackgroundColor(Color.LTGRAY) }
        val headers = listOf("Flow Date", "Flow ID", "Flow Code", "Flow Amt", "Acct No", "Acct Name")
        for (h in headers) {
            val tv = TextView(context).apply {
                text = h
                setPadding(dp(8), dp(8), dp(8), dp(8))
                textSize = 12f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
            }
            headerRow.addView(tv)
        }
        tableLayout.addView(headerRow)

        // Add Data Rows
        for (flow in flows) {
            val row = TableRow(context)
            val rowData = listOf(
                flow["dueDate"]?.toString() ?: "",
                flow["flowId"]?.toString() ?: "",
                flow["flowCode"]?.toString() ?: "",
                flow["flowAmt"]?.toString() ?: "",
                flow["loanAcctNo"]?.toString() ?: "",
                flow["acctName"]?.toString() ?: ""
            )
            for (d in rowData) {
                val tv = TextView(context).apply {
                    text = d
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                    textSize = 12f
                    gravity = Gravity.CENTER
                }
                row.addView(tv)
            }
            tableLayout.addView(row)
        }
        
        scrollView.addView(tableLayout)

        AlertDialog.Builder(context)
            .setTitle("Allocation Details")
            .setView(scrollView)
            .setPositiveButton("Close", null)
            .show()
    }
}