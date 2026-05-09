package com.example.bgls.WalletMaintenance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.databinding.ActivityWalletAccountFlowBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.app.DatePickerDialog
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

class WalletAccountFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAccountFlowBinding
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAccountFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("MODE") ?: "VIEW"
        val custId = intent.getStringExtra("CUST_ID") ?: ""
        val custName = intent.getStringExtra("CUST_NAME") ?: ""
        val accNo = intent.getStringExtra("ACC_NO") ?: ""
        val internalAccNo = intent.getStringExtra("INTERNAL_ACC_NO") ?: accNo
        val status = intent.getStringExtra("STATUS") ?: ""

        setupMode(mode, status)
        setupDatePickers()

        // Fetch account details
        fetchAccountDetails(internalAccNo, accNo, custId, custName)

        binding.btnAction.setOnClickListener {
            val actionText = binding.btnAction.text.toString()
            when (actionText) {
                "Modify" -> enterModifyMode()
                "Submit" -> submitModification(internalAccNo)
                "Verify" -> submitVerification(internalAccNo)
            }
        }




    }

    private fun setupMode(mode: String, status: String) {
        when (mode) {
            "VIEW" -> {
                binding.tvTitle.text = "Wallet Account Master - View"
                binding.btnAction.text = "Modify"
                disableEditing()
            }
            "MODIFY" -> {
                binding.tvTitle.text = "Wallet Account Master - Modify"
                binding.btnAction.text = "Submit"
                enableEditing()
            }
            "VERIFY" -> {
                binding.tvTitle.text = "Wallet Account Master - Verify"
                binding.btnAction.text = "Verify"
                disableEditing()
            }
        }
        
        // Overriding based on status if needed
        if (mode == "VIEW" && status == "Unverified") {
            binding.btnAction.text = "Verify"
        }
    }

    private fun setupDatePickers() {
        val dateFields = listOf(
            binding.etOpenDate, binding.etCloseDate, binding.etBalanceDate
        )
        for (et in dateFields) {
            et.isFocusable = false
            et.setOnClickListener {
                if (et.isEnabled) showDatePicker(et)
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

    private fun fetchAccountDetails(internalAccNo: String, displayAccNo: String, custId: String, custName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getWalletMaintenance("view", internalAccNo)
                if (response.isSuccessful) {
                    val wallet = response.body()?.wallet
                    withContext(Dispatchers.Main) {
                        wallet?.let {
                            binding.etWalletCategory.setText(it.wallet_category ?: "")
                            binding.etBranchId.setText(it.branch_id ?: "")
                            binding.etWalletType.setText(it.wallet_type ?: "")
                            binding.etAccNo.setText(it.bips_acct_num ?: (it.wallet_acct_num ?: displayAccNo))
                            binding.etCurrency.setText(it.wallet_crncy ?: "")
                            binding.etCloseFlag.setText(it.act_cls_flg ?: "")
                            binding.etBalance.setText(it.acct_bal ?: "")
                            binding.etCustLimit.setText(it.customer_limit ?: "")
                            binding.etSmsFlag.setText(it.sms_flg ?: "")
                            binding.etEmailFlag.setText(it.email_flg ?: "")
                            binding.etCustId.setText(it.customer_id ?: custId)
                            binding.etBranchName.setText(it.branch_name ?: "")
                            binding.etDebitLimit.setText(it.debit_limit ?: "")
                            binding.etAccName.setText(it.wallet_acct_name ?: custName)
                            
                            // Format dates for UI
                            binding.etOpenDate.setText(formatDateForUI(it.acct_open_date))
                            binding.etCloseDate.setText(formatDateForUI(it.act_cls_date))
                            binding.etBalanceDate.setText(formatDateForUI(it.last_acct_bal_date))
                            
                            binding.etWalletLimit.setText(it.wallet_limit ?: "")
                            binding.etMobileNo.setText(it.mobile_no ?: "")
                            binding.etEmailId.setText(it.email_id ?: "")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletAccountFlowActivity, "Error fetching details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDateForUI(dateStr: String?): String {
        if (dateStr == null) return ""
        return if (dateStr.contains("T")) {
            val parts = dateStr.split("T")[0].split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else {
            dateStr
        }
    }

    private fun disableEditing() {
        val editTexts = getAllEditTexts()
        for (et in editTexts) {
            et.isEnabled = false
            // REMOVED: et.setBackgroundResource(android.R.color.transparent)
        }
    }

    private fun enableEditing() {
        val editTexts = getAllEditTexts()
        for (et in editTexts) {
            et.isEnabled = true
            et.setBackgroundResource(R.drawable.edittext_border)
        }
        // Account Number and Customer ID should usually stay disabled
        binding.etAccNo.isEnabled = false
        binding.etCustId.isEnabled = false
    }

    private fun getAllEditTexts(): List<android.widget.EditText> {
        return listOf(
            binding.etWalletCategory, binding.etBranchId, binding.etWalletType, binding.etAccNo,
            binding.etCurrency, binding.etCloseFlag, binding.etBalance,
            binding.etCustLimit, binding.etSmsFlag, binding.etEmailFlag, binding.etCustId,
            binding.etBranchName, binding.etDebitLimit, binding.etAccName, binding.etOpenDate,
            binding.etCloseDate, binding.etBalanceDate, binding.etWalletLimit,
            binding.etMobileNo, binding.etEmailId
        )
    }

    private fun enterModifyMode() {
        binding.tvTitle.text = "Wallet Account Master - Modify"
        binding.btnAction.text = "Submit"
        enableEditing()
    }

    private fun submitModification(accNo: String) {
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
                val response = RetrofitClient.api.modifyWalletScreenData(accNo, fields)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountFlowActivity, "Wallet account updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountFlowActivity, "Failed to update account", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletAccountFlowActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun submitVerification(accNo: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.verifyWalletScreenData(accNo)
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountFlowActivity, "Wallet account verified successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletAccountFlowActivity, "Failed to verify account", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletAccountFlowActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}