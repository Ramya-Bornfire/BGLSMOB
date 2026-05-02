package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bgls.R
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.OpenableColumns

class JournalEntriesActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private var tvDialogFileName: android.widget.TextView? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = getFileName(it)
            tvDialogFileName?.text = fileName
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_journal_entries)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinners()
    }

    private fun setupSpinners() {
        val options = arrayOf("Select", "Add", "Mass Entries", "List", "Upload")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        
        val spinnerFunction = findViewById<android.widget.Spinner>(R.id.spinnerFunction)
        spinnerFunction?.adapter = adapter

        spinnerFunction?.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedOption = options[position]
                
                if (selectedOption.equals("Mass Entries", ignoreCase = true)) {
                    // Launch new Activity
                    val intent = android.content.Intent(this@JournalEntriesActivity, MassEntriesActivity::class.java)
                    startActivity(intent)
                    
                    // Reset spinner back to "Select" so it doesn't get stuck
                    spinnerFunction.setSelection(0)
                } else if (selectedOption.equals("List", ignoreCase = true)) {
                    // Launch List Activity
                    val intent = android.content.Intent(this@JournalEntriesActivity, JournalEntriesListActivity::class.java)
                    startActivity(intent)
                    
                    // Reset spinner back to "Select"
                    spinnerFunction.setSelection(0)
                } else if (selectedOption.equals("Upload", ignoreCase = true)) {
                    showUploadDialog()
                    spinnerFunction.setSelection(0)
                } else if (selectedOption.equals("Add", ignoreCase = true)) {
                    populateDefaultValues()
                } else {
                    clearValues()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun showUploadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_files, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        tvDialogFileName = dialogView.findViewById(R.id.tvFileName)
        val btnChoose = dialogView.findViewById<android.widget.Button>(R.id.btnChooseFile)
        
        btnChoose.setOnClickListener {
            filePickerLauncher.launch("*/*") // Allow picking any file type
        }

        val btnClose = dialogView.findViewById<android.widget.Button>(R.id.btnCloseUpload)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        val btnSubmit = dialogView.findViewById<android.widget.Button>(R.id.btnSubmitUpload)
        btnSubmit.setOnClickListener {
            if (selectedFileUri != null) {
                // Handle upload logic here (e.g., Multipart upload)
                android.widget.Toast.makeText(this, "File ${tvDialogFileName?.text} submitted successfully", android.widget.Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                android.widget.Toast.makeText(this, "Please choose a file first", android.widget.Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Unknown file"
    }

    // Data model representing the API response for single entry
    data class JournalEntryResponse(
        val tranId: String,
        val tranDate: String,
        val entryUser: String,
        val entryTime: String,
        val tranStatus: String,
        val partTranId: String,
        val valueDate: String
    )

    private fun populateDefaultValues() {
        val mockApiResponse = JournalEntryResponse(
            tranId = "TR8866",
            tranDate = "30/04/2026",
            entryUser = "EMP04",
            entryTime = "30/04/2026",
            tranStatus = "ENTERED",
            partTranId = "1",
            valueDate = "30/04/2026"
        )
        bindDataToView(mockApiResponse)
    }

    private fun bindDataToView(data: JournalEntryResponse) {
        findViewById<android.widget.EditText>(R.id.etTranId)?.setText(data.tranId)
        findViewById<android.widget.EditText>(R.id.etTranDate)?.setText(data.tranDate)
        findViewById<android.widget.EditText>(R.id.etEntryUser)?.setText(data.entryUser)
        findViewById<android.widget.EditText>(R.id.etEntryTime)?.setText(data.entryTime)
        findViewById<android.widget.EditText>(R.id.etTranStatus)?.setText(data.tranStatus)
        findViewById<android.widget.EditText>(R.id.etPartTranId)?.setText(data.partTranId)
        findViewById<android.widget.EditText>(R.id.etValueDate)?.setText(data.valueDate)
    }

    private fun clearValues() {
        findViewById<android.widget.EditText>(R.id.etTranId)?.setText("")
        findViewById<android.widget.EditText>(R.id.etTranDate)?.setText("")
        findViewById<android.widget.EditText>(R.id.etEntryUser)?.setText("")
        findViewById<android.widget.EditText>(R.id.etEntryTime)?.setText("")
        findViewById<android.widget.EditText>(R.id.etTranStatus)?.setText("")
        findViewById<android.widget.EditText>(R.id.etPartTranId)?.setText("")
        findViewById<android.widget.EditText>(R.id.etValueDate)?.setText("")
    }
}