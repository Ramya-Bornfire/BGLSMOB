package com.example.bgls.DepositAccount

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.DepositFlowItem
import com.example.bgls.MainActivity
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.databinding.ActivityDepositAccountOpeningBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DepositAccountOpeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountOpeningBinding
    private lateinit var generatedAccountNo: String
    private var flowList: List<DepositFlowItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
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
        fetchOpeningData()

        binding.btnGenerateFlow.setOnClickListener {
            generateFlow()
            showGenerateFlow()

        }
        binding.btnUpdate.setOnClickListener {
            // Logic for submitting the flow
            submitDeposit()
            android.widget.Toast.makeText(this, "Flow Submitted Successfully", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.btnFlowSubmit.setOnClickListener {
            // Logic for submitting the flow
            submitDeposit()
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

//    private fun showDatePicker(onDateSelected: (String) -> Unit) {
//        val calendar = Calendar.getInstance()
//        val year = calendar.get(Calendar.YEAR)
//        val month = calendar.get(Calendar.MONTH)
//        val day = calendar.get(Calendar.DAY_OF_MONTH)
//
//        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
//            val formattedDate = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
//            onDateSelected(formattedDate)
//        }, year, month, day)
//        datePickerDialog.show()
//    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // ✅ Change to yyyy-MM-dd
            val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            onDateSelected(formattedDate)
        }, year, month, day)
        datePickerDialog.show()
    }

    private fun fetchOpeningData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getDepositOpenScreen("list")
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    generatedAccountNo = data.depoActNo ?: ""
                    val customerIds = data.deposit ?: emptyList()
                    withContext(Dispatchers.Main) {
                        binding.etDepositAccountNo.setText(generatedAccountNo)
                        // populate customer spinner
                        val adapter = ArrayAdapter(this@DepositAccountOpeningActivity,
                            android.R.layout.simple_spinner_item, customerIds)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spCustomerId.adapter = adapter
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountOpeningActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    private fun generateFlow() {
//        val depositDate = binding.etDateOfDeposit.text.toString()
//        val depositType = binding.spDepositType.selectedItem.toString()
//        val depositPeriod = binding.etDepositPeriod.text.toString()
//        val depositAmt = binding.etDepositAmount.text.toString()
//        val rateOfInt = binding.etRateOfInterest.text.toString()
//        val frequency = binding.spFrequency.selectedItem.toString()
//        // deposit_frequency is same as frequency for now (adjust as needed)
//        val depositFrequency = frequency
//
//        if (depositDate.isEmpty() || depositPeriod.isEmpty() || depositAmt.isEmpty()) {
//            Toast.makeText(this, "Please fill mandatory fields", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val response = RetrofitClient.api.getDepositFlow(
//                    depositDate, depositType, generatedAccountNo,
//                    depositPeriod, depositAmt, rateOfInt, frequency, depositFrequency
//                )
//                if (response.isSuccessful && response.body() != null) {
//                    flowList = response.body()!!
//                    withContext(Dispatchers.Main) {
//                        displayFlowTable(flowList)
//                    }
//                } else {
//                    withContext(Dispatchers.Main) {
//                        Toast.makeText(this@DepositAccountOpeningActivity,
//                            "Failed to generate flow", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@DepositAccountOpeningActivity,
//                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }




    private fun generateFlow() {
        val depositDateRaw = binding.etDateOfDeposit.text.toString() // now in yyyy-MM-dd
        // Convert to dd-MM-yyyy for the flow API
        val depositDateForFlow = try {
            val parts = depositDateRaw.split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else depositDateRaw
        } catch (e: Exception) { depositDateRaw }

        val depositType = binding.spDepositType.selectedItem.toString()
        val depositPeriod = binding.etDepositPeriod.text.toString()
        val depositAmt = binding.etDepositAmount.text.toString()
        val rateOfInt = binding.etRateOfInterest.text.toString()
        val frequency = binding.spFrequency.selectedItem.toString()
        val depositFrequency = frequency

        if (depositDateRaw.isEmpty() || depositPeriod.isEmpty() || depositAmt.isEmpty()) {
            Toast.makeText(this, "Please fill mandatory fields", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getDepositFlow(
                    depositDateForFlow, depositType, generatedAccountNo,
                    depositPeriod, depositAmt, rateOfInt, frequency, depositFrequency
                )
                if (response.isSuccessful && response.body() != null) {
                    flowList = response.body()!!
                    withContext(Dispatchers.Main) {
                        displayFlowTable(flowList)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountOpeningActivity,
                            "Failed to generate flow", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountOpeningActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayFlowTable(flows: List<DepositFlowItem>) {
        // Show the flow table in layoutGenerateFlow
        // You need to create a RecyclerView inside layoutGenerateFlow and populate it.
        // For simplicity, we'll just log and enable submit button.
        binding.btnFlowSubmit.isEnabled = true
        Toast.makeText(this, "Flow generated: ${flows.size} entries", Toast.LENGTH_SHORT).show()
    }

    private fun submitDeposit() {
        // Collect all fields from the form
        val fields = mutableMapOf<String, String>()
        fields["branch_id"] = binding.etBranchId.text.toString()
        fields["branch_desc"] = binding.etBranchDescription.text.toString()
        fields["cust_id"] = binding.spCustomerId.selectedItem.toString()
        fields["cust_name"] = binding.etCustomerName.text.toString()
        fields["deposit_type"] = binding.spDepositType.selectedItem.toString()
        fields["scheme_code"] = binding.etSchemeCode.text.toString()
        fields["glsh_code"] = binding.etGlshCode.text.toString()
        fields["glsh_desc"] = binding.etGlshDescription.text.toString()
        fields["depo_actno"] = generatedAccountNo
        fields["deposit_date"] = binding.etDateOfDeposit.text.toString()
        fields["deposit_period"] = binding.etDepositPeriod.text.toString()
        fields["deposit_amt"] = binding.etDepositAmount.text.toString().replace(",", "")
        fields["rate_of_int"] = binding.etRateOfInterest.text.toString()
        fields["int_amt"] = binding.etInterestAmount.text.toString().replace(",", "")
        fields["frequency"] = binding.spFrequency.selectedItem.toString()
        fields["currency"] = binding.etCurrency.text.toString()
        fields["maturity_amt"] = binding.etMaturityAmount.text.toString().replace(",", "")
        fields["maturity_date"] = binding.etMaturityDate.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.addDeposit(fields)
                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "Deposit added successfully"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountOpeningActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountOpeningActivity,
                            "Add failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountOpeningActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
