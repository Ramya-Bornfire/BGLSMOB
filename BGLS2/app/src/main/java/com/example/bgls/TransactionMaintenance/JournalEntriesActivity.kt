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

class JournalEntriesActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private var tvDialogFileName: TextView? = null

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
        setupSpinners()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
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
    }

    private fun loadAddScreenData() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntryAddScreen()
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    findViewById<EditText>(R.id.etTranId).setText(data.plusonetran2)
                    findViewById<EditText>(R.id.etPartTranId).setText(data.partTranId)
                    findViewById<EditText>(R.id.etEntryUser).setText(data.user)
                    findViewById<EditText>(R.id.etTranStatus).setText(data.tranStatus)
                    findViewById<EditText>(R.id.etTranDate).setText(data.currentDate)
                    findViewById<EditText>(R.id.etValueDate).setText(data.currentDate)
                    findViewById<EditText>(R.id.etEntryTime).setText(data.currentDate)
                    // Also update the header date if present
                    findViewById<EditText>(R.id.etHeaderDate)?.setText(data.currentDate)
                } else {
                    Toast.makeText(this@JournalEntriesActivity, "Failed to load default values", Toast.LENGTH_SHORT).show()
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
        findViewById<EditText>(R.id.etTranId)?.setText("")
        findViewById<EditText>(R.id.etPartTranId)?.setText("")
        findViewById<EditText>(R.id.etEntryUser)?.setText("")
        findViewById<EditText>(R.id.etTranStatus)?.setText("")
        findViewById<EditText>(R.id.etTranDate)?.setText("")
        findViewById<EditText>(R.id.etValueDate)?.setText("")
        findViewById<EditText>(R.id.etEntryTime)?.setText("")
    }
}