package com.example.bgls.CustomerMaster

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Calendar

class CustomerMasterActivity : AppCompatActivity() {

    // ─── Form fields ───
    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etGender: EditText
    private lateinit var etDateOfBirth: EditText
    private lateinit var etBranch: EditText
    private lateinit var etClientRoleKey: EditText
    private lateinit var etCreationDate: EditText
    private lateinit var etApprovalDate: EditText
    private lateinit var etLastModificationDate: EditText
    private lateinit var etActivationDate: EditText
    private lateinit var etMobileNo: EditText
    private lateinit var etEmailId: EditText
    private lateinit var etAddress1: EditText
    private lateinit var etAddress2: EditText
    private lateinit var etCity: EditText
    private lateinit var etSuburb: EditText
    private lateinit var etLoanCycle: EditText
    private lateinit var etGroupLoanCycle: EditText
    private lateinit var etAssignedUser: EditText
    private lateinit var etAsOnDate: EditText

    // ─── Buttons ───
    private lateinit var btnUpload: Button
    private lateinit var btnList: Button
    private lateinit var progressBar: ProgressBar

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadFile(it) }
            ?: Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_master)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        initViews()
        setupDatePickers()
        setupButtons()
        progressBar = findViewById(R.id.progressBar)
    }

    private fun initViews() {
        etCustomerId            = findViewById(R.id.etCustomerId)
        etCustomerName          = findViewById(R.id.etCustomerName)
        etFirstName             = findViewById(R.id.etFirstName)
        etLastName              = findViewById(R.id.etLastName)
        etGender                = findViewById(R.id.etGender)
        etDateOfBirth           = findViewById(R.id.etDateOfBirth)
        etBranch                = findViewById(R.id.etBranch)
        etClientRoleKey         = findViewById(R.id.etClientRoleKey)
        etCreationDate          = findViewById(R.id.etCreationDate)
        etApprovalDate          = findViewById(R.id.etApprovalDate)
        etLastModificationDate  = findViewById(R.id.etLastModificationDate)
        etActivationDate        = findViewById(R.id.etActivationDate)
        etMobileNo              = findViewById(R.id.etMobileNo)
        etEmailId               = findViewById(R.id.etEmailId)
        etAddress1              = findViewById(R.id.etAddress1)
        etAddress2              = findViewById(R.id.etAddress2)
        etCity                  = findViewById(R.id.etCity)
        etSuburb                = findViewById(R.id.etSuburb)
        etLoanCycle             = findViewById(R.id.etLoanCycle)
        etGroupLoanCycle        = findViewById(R.id.etGroupLoanCycle)
        etAssignedUser          = findViewById(R.id.etAssignedUser)
        etAsOnDate              = findViewById(R.id.etAsOnDate)

        btnUpload = findViewById(R.id.btnUpload)
        btnList   = findViewById(R.id.btnList)
    }

    private fun setupButtons() {
        btnUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")   // allow any file, or restrict to .xlsx
        }

        btnList.setOnClickListener {
            startActivity(Intent(this, CustomerMasterListActivity::class.java))
            finish()
        }
    }

    private fun uploadFile(uri: Uri) {
        val file = getFileFromUri(uri)
        if (file == null || !file.exists()) {
            Toast.makeText(this, "Unable to access file", Toast.LENGTH_SHORT).show()
            return
        }

        // MIME type based on file extension
        val mimeType = when (file.extension.lowercase()) {
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "xls" -> "application/vnd.ms-excel"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }

        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        val fileInput = "customer"   // adjust to what backend expects
        val overwrite = true

        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.uploadFileData(body, fileInput, overwrite)
                if (response.isSuccessful) {
                    val result = response.body()
                    Toast.makeText(this@CustomerMasterActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                    populateFieldsFromResponse(result)
                } else {
                    val error = response.errorBody()?.string() ?: "Unknown error"
                    Toast.makeText(this@CustomerMasterActivity, "Upload failed: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CustomerMasterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                btnUpload.isEnabled = true
            }
        }
    }

    /**
     * Fills the form fields with data returned from the upload API.
     * Update the key names to match your backend response.
     */
    private fun populateFieldsFromResponse(data: Map<String, Any>?) {
        if (data == null) {
            Toast.makeText(this, "No data returned from server", Toast.LENGTH_SHORT).show()
            return
        }

        // Map response keys to EditText fields – adjust keys as needed
        etCustomerId.setText(data["customerId"]?.toString() ?: "")
        etCustomerName.setText(data["customerName"]?.toString() ?: "")
        etFirstName.setText(data["firstName"]?.toString() ?: "")
        etLastName.setText(data["lastName"]?.toString() ?: "")
        etGender.setText(data["gender"]?.toString() ?: "")
        etDateOfBirth.setText(data["dateOfBirth"]?.toString() ?: "")
        etBranch.setText(data["branch"]?.toString() ?: "")
        etClientRoleKey.setText(data["clientRoleKey"]?.toString() ?: "")
        etCreationDate.setText(data["creationDate"]?.toString() ?: "")
        etApprovalDate.setText(data["approvalDate"]?.toString() ?: "")
        etLastModificationDate.setText(data["lastModificationDate"]?.toString() ?: "")
        etActivationDate.setText(data["activationDate"]?.toString() ?: "")
        etMobileNo.setText(data["mobileNo"]?.toString() ?: "")
        etEmailId.setText(data["emailId"]?.toString() ?: "")
        etAddress1.setText(data["address1"]?.toString() ?: "")
        etAddress2.setText(data["address2"]?.toString() ?: "")
        etCity.setText(data["city"]?.toString() ?: "")
        etSuburb.setText(data["suburb"]?.toString() ?: "")
        etLoanCycle.setText(data["loanCycle"]?.toString() ?: "")
        etGroupLoanCycle.setText(data["groupLoanCycle"]?.toString() ?: "")
        etAssignedUser.setText(data["assignedUser"]?.toString() ?: "")
        etAsOnDate.setText(data["asOnDate"]?.toString() ?: "")
    }

    // Helper: Convert content URI to a File
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

    // ─── Date picker on all date fields ───
    private fun setupDatePickers() {
        val dateFields = listOf(
            etDateOfBirth, etCreationDate, etApprovalDate,
            etLastModificationDate, etActivationDate, etAsOnDate
        )
        dateFields.forEach { field ->
            field.setOnClickListener { showDatePicker(field) }
            field.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) showDatePicker(field)
            }
        }
    }

    private fun showDatePicker(targetField: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                targetField.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}