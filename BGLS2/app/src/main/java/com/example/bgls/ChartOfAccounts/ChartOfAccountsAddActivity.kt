package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

import android.widget.ArrayAdapter
import android.widget.Spinner

class ChartOfAccountsAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_of_accounts_add)

        val btnSubmitAdd = findViewById<Button>(R.id.btnSubmitAdd)
        
        btnSubmitAdd.setOnClickListener {
            Toast.makeText(this, "Account Added Successfully", Toast.LENGTH_SHORT).show()
            finish() // Close screen on submit
        }
        
        setupSpinners()
    }

    private fun setupSpinners() {
        val dummyOptions = listOf("Select", "Option 1", "Option 2")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dummyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        findViewById<Spinner>(R.id.spinClassification).adapter = adapter
        findViewById<Spinner>(R.id.spinAccountType).adapter = adapter
        findViewById<Spinner>(R.id.spinSchemeType).adapter = adapter
        findViewById<Spinner>(R.id.spinAdditionalDetails).adapter = adapter
        findViewById<Spinner>(R.id.spinAccountPartitioning).adapter = adapter
        findViewById<Spinner>(R.id.spinOwnership).adapter = adapter
        
        val statusOptions = listOf("Select", "Active", "Inactive")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        findViewById<Spinner>(R.id.spinAccountStatus).adapter = statusAdapter
    }
}
