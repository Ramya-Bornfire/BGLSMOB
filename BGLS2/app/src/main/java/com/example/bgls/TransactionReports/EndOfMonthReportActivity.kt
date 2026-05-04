package com.example.bgls.TransactionReports

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class EndOfMonthReportActivity : AppCompatActivity() {

    private lateinit var etAsonDate: EditText
    private lateinit var btnHome: Button
    private lateinit var btnDownload: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_of_month_report)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etAsonDate = findViewById(R.id.etAsonDate)
        btnHome = findViewById(R.id.btnHome)
        btnDownload = findViewById(R.id.btnDownload)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnDownload.setOnClickListener {
            val date = etAsonDate.text.toString()
            Toast.makeText(this, "Downloading End of Month Report for $date...", Toast.LENGTH_LONG).show()
        }

        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
    }
}
