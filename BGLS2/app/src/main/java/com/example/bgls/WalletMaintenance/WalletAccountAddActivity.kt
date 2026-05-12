package com.example.bgls.WalletMaintenance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityWalletAccountAddBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class WalletAccountAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAccountAddBinding
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAccountAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        setupDatePickers()

        // Fetch new account number
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getWalletMaintenance("add")
                if (response.isSuccessful) {
                    val accNo = response.body()?.loanaccountno
                    withContext(Dispatchers.Main) {
                        binding.etAccNo.setText(accNo ?: "")
                    }
                }
            } catch (e: Exception) {
                // handle error
            }
        }

        binding.btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun setupDatePickers() {
        val dateFields = listOf(
            binding.etOpenDate, binding.etCloseDate, binding.etBalanceDate
        )
        for (et in dateFields) {
            et.isFocusable = false
            et.setOnClickListener {
                showDatePicker(et)
            }
        }
    }

    private fun showDatePicker(editText: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = Calendar.getInstance()
            date.set(selectedYear, selectedMonth, selectedDay)
            editText.setText(dateFormatter.format(date.time))
        }, year, month, day).show()
    }

    private fun submitForm() {
        val fields = mapOf(
            "wallet_category" to binding.etWalletCategory.text.toString(),
            "branch_id" to binding.etBranchId.text.toString(),
            "wallet_type" to binding.etWalletType.text.toString(),
            "wallet_acct_num" to binding.etAccNo.text.toString(),
            "wallet_crncy" to binding.etCurrency.text.toString(),
            "act_cls_flg" to binding.etCloseFlag.text.toString(),
            "acct_bal" to binding.etBalance.text.toString(),
            "customer_limit" to binding.etCustLimit.text.toString(),
            "sms_flg" to binding.etSmsFlag.text.toString(),
            "email_flg" to binding.etEmailFlag.text.toString(),
            "customer_id" to binding.etCustId.text.toString(),
            "branch_name" to binding.etBranchName.text.toString(),
            "debit_limit" to binding.etDebitLimit.text.toString(),
            "wallet_acct_name" to binding.etAccName.text.toString(),
            "acct_open_date" to binding.etOpenDate.text.toString(),
            "act_cls_date" to binding.etCloseDate.text.toString(),
            "last_acct_bal_date" to binding.etBalanceDate.text.toString(),
            "wallet_limit" to binding.etWalletLimit.text.toString(),
            "mobile_no" to binding.etMobileNo.text.toString(),
            "email_id" to binding.etEmailId.text.toString(),
            "entity_flg" to "N"
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.addWalletScreenData(fields)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountAddActivity, "Wallet account created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountAddActivity, "Failed to create account", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletAccountAddActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}