package com.example.bgls.DepositAccount

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityDepositAccountOpeningBinding
import java.util.*

class DepositAccountOpeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountOpeningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupDatePickers()

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnDeposit.setOnClickListener {
            showDepositForm()
        }
        
        binding.btnGenerateFlow.setOnClickListener {
            showGenerateFlow()
        }

        binding.btnFlowSubmit.setOnClickListener {
            // Logic for submitting the flow
            android.widget.Toast.makeText(this, "Flow Submitted Successfully", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDepositForm() {
        binding.layoutDepositForm.visibility = android.view.View.VISIBLE
        binding.layoutGenerateFlow.visibility = android.view.View.GONE
        
        // Update tab styles
        binding.btnDeposit.setBackgroundResource(0) // Clear background
        binding.btnDeposit.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
        binding.btnDeposit.setTextColor(android.graphics.Color.WHITE)
        binding.btnDeposit.setTypeface(null, android.graphics.Typeface.BOLD)
        
        binding.btnGenerateFlow.setBackgroundResource(0)
        binding.btnGenerateFlow.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.btnGenerateFlow.setTextColor(android.graphics.Color.parseColor("#333333"))
        binding.btnGenerateFlow.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun showGenerateFlow() {
        binding.layoutDepositForm.visibility = android.view.View.GONE
        binding.layoutGenerateFlow.visibility = android.view.View.VISIBLE
        
        // Update tab styles
        binding.btnGenerateFlow.setBackgroundResource(0)
        binding.btnGenerateFlow.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
        binding.btnGenerateFlow.setTextColor(android.graphics.Color.WHITE)
        binding.btnGenerateFlow.setTypeface(null, android.graphics.Typeface.BOLD)
        
        binding.btnDeposit.setBackgroundResource(0)
        binding.btnDeposit.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.btnDeposit.setTextColor(android.graphics.Color.parseColor("#333333"))
        binding.btnDeposit.setTypeface(null, android.graphics.Typeface.NORMAL)
    }

    private fun setupSpinners() {
        val customerIds = arrayOf("Select", "C001", "C002", "C003")
        val depositTypes = arrayOf("Fixed", "Reinvestment", "Recurring")
        val frequencies = arrayOf("Monthly", "Quarterly", "Yearly")

        binding.spCustomerId.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, customerIds)
        binding.spDepositType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, depositTypes)
        binding.spFrequency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, frequencies)
    }

    private fun setupDatePickers() {
        binding.etMaturityDate.setOnClickListener { showDatePicker { date -> binding.etMaturityDate.setText(date) } }
        binding.etDateOfDeposit.setOnClickListener { showDatePicker { date -> binding.etDateOfDeposit.setText(date) } }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
            onDateSelected(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }
}
