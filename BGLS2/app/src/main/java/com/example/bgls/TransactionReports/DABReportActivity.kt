package com.example.bgls.TransactionReports

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R
import java.util.Calendar

class DABReportActivity : AppCompatActivity() {

    private lateinit var etDate: EditText
    private lateinit var ivCalendar: ImageView
    private lateinit var btnHome: Button
    private lateinit var btnDownload: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dab_report)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etDate = findViewById(R.id.etDate)
        ivCalendar = findViewById(R.id.ivCalendar)
        btnHome = findViewById(R.id.btnHome)
        btnDownload = findViewById(R.id.btnDownload)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        ivCalendar.setOnClickListener {
            showDatePicker()
        }

        btnDownload.setOnClickListener {
            val date = etDate.text.toString()
            Toast.makeText(this, "Downloading DAB Report for $date...", Toast.LENGTH_LONG).show()
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
            etDate.setText(selectedDate)
        }, year, month, day)

        dpd.show()
    }
}
