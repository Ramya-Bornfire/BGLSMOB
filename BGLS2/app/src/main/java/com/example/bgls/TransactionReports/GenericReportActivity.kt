package com.example.bgls.TransactionReports

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R
import java.util.Calendar

class GenericReportActivity : AppCompatActivity() {

    private lateinit var tvReportTitle: TextView
    private lateinit var etAsonDate: EditText
    private lateinit var ivCalendar: ImageView
    private lateinit var spinnerFormat: Spinner
    private lateinit var btnHome: Button
    private lateinit var btnDownload: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_report)

        initViews()
        setupListeners()
        
        // Get report details from intent
        val reportTitle = intent.getStringExtra("REPORT_TITLE") ?: "Report"
        val showSpinner = intent.getBooleanExtra("SHOW_SPINNER", false)
        
        tvReportTitle.text = reportTitle
        
        if (showSpinner) {
            spinnerFormat.visibility = View.VISIBLE
            val formats = arrayOf("Excel", "PDF", "CSV")
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
    }

    private fun setupListeners() {
        ivCalendar.setOnClickListener {
            showDatePicker()
        }

        btnDownload.setOnClickListener {
            val date = etAsonDate.text.toString()
            val format = if (spinnerFormat.visibility == View.VISIBLE) {
                " as ${spinnerFormat.selectedItem}"
            } else ""
            
            Toast.makeText(this, "Downloading ${tvReportTitle.text}$format for $date...", Toast.LENGTH_LONG).show()
        }

        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
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
}
