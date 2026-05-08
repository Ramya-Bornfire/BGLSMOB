package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.databinding.ActivityWalletAccountFlowBinding

class WalletAccountFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAccountFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAccountFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val custId = intent.getStringExtra("CUST_ID") ?: "CUST0000052001"
        val custName = intent.getStringExtra("CUST_NAME") ?: "MOHAN"
        val accNo = intent.getStringExtra("ACC_NO") ?: "WA0049"

        binding.etCustId.setText(custId)
        binding.etAccName.setText(custName)
        binding.etAccNo.setText(accNo)

        binding.btnAction.setOnClickListener {
            if (binding.btnAction.text == "Modify") {
                enterModifyMode()
            } else {
                Toast.makeText(this, "Wallet account updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun enterModifyMode() {
        binding.tvTitle.text = "Wallet Account Master - Modify"
        binding.btnAction.text = "Submit"

        val editTexts = listOf(
            binding.etWalletCategory, binding.etBranchId, binding.etWalletType, binding.etAccNo,
            binding.etCurrency, binding.etCloseFlag, binding.etBalance, binding.etAvailableBalance,
            binding.etCustLimit, binding.etSmsFlag, binding.etEmailFlag, binding.etCustId,
            binding.etBranchName, binding.etDebitLimit, binding.etAccName, binding.etOpenDate,
            binding.etCloseDate, binding.etBalanceDate, binding.etCurrentDate, binding.etWalletLimit,
            binding.etMobileNo, binding.etEmailId
        )

        for (et in editTexts) {
            et.isEnabled = true
            et.setBackgroundResource(R.drawable.edittext_border)
        }
    }
}
