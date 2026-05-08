package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.DepositFlowAdapter
import com.example.bgls.DataModels.DepositFlowModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.TransactionMaintenance.JournalEntriesActivity
import com.example.bgls.databinding.ActivityDepositAccountMaintenanceFlowBinding

class DepositAccountMaintenanceFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountMaintenanceFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountMaintenanceFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val custId = intent.getStringExtra("CUST_ID") ?: "CUST0000058601"
        val custName = intent.getStringExtra("CUST_NAME") ?: "LALITH"
        val status = intent.getStringExtra("STATUS") ?: "Verified"

        binding.etCustId.setText(custId)
        binding.etCustName.setText(custName)

        if (status.equals("Verified", ignoreCase = true)) {
            binding.btnVerify.text = "Modify"
        } else {
            binding.btnVerify.text = "Verify"
        }

        setupRecyclerView()
        setupTabs()

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLedger.setOnClickListener {
            val intent = Intent(this, AccountLedgerActivity::class.java)
            intent.putExtra("ACCT_ID", binding.etAccNo.text.toString())
            startActivity(intent)
        }

        binding.btnWallet.setOnClickListener {
            val intent = Intent(this, AccountLedgerActivity::class.java)
            intent.putExtra("ACCT_ID", "WA" + binding.etAccNo.text.toString().substring(2))
            startActivity(intent)
        }

        binding.btnVerify.setOnClickListener {
            when (binding.btnVerify.text) {
                "Modify" -> enterModifyMode()
                "Verify" -> enterVerifyMode()
                "Submit" -> {
                    val message = if (binding.tvTitle.text.toString().contains("Verify")) {
                        "Verified successfully"
                    } else {
                        "Updated successfully"
                    }
                    android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun enterVerifyMode() {
        binding.tvTitle.text = "DEPOSIT MAINTENANCE - Verify"
        binding.btnVerify.text = "Submit"
        // Fields remain disabled in Verify mode
    }

    private fun enterModifyMode() {
        binding.tvTitle.text = "DEPOSIT MAINTENANCE - Modify"
        binding.btnVerify.text = "Submit"

        // Enable and style all EditTexts
        val editTexts = listOf(
            binding.etBranchId, binding.etBranchDesc, binding.etCustId, binding.etCustName,
            binding.etDepositType, binding.etSchemeCode, binding.etGlshCode, binding.etGlshDesc,
            binding.etAccNo, binding.etDepositDate, binding.etPeriod, binding.etAmount,
            binding.etRoi, binding.etInterestAmount, binding.etFrequency, binding.etCurrency,
            binding.etMaturityDate, binding.etMaturityAmount
        )

        for (et in editTexts) {
            et.isEnabled = true
            et.setBackgroundResource(R.drawable.edittext_border) // White background with border
        }
    }

    private fun setupRecyclerView() {

        val dummyFlows = listOf(
            DepositFlowModel("1", "PI", "05-05-2026", "100,000.00", "100,000.00"),
            DepositFlowModel("2", "IO", "05-06-2026", "166.66", "100,000.00"),
            DepositFlowModel("3", "IO", "05-07-2026", "166.66", "100,000.00"),
            DepositFlowModel("4", "IO", "05-08-2026", "166.66", "100,000.00"),
            DepositFlowModel("5", "IO", "05-09-2026", "166.66", "100,000.00"),
            DepositFlowModel("6", "IO", "05-10-2026", "166.66", "100,000.00"),
            DepositFlowModel("7", "IO", "05-11-2026", "166.66", "100,000.00"),
            DepositFlowModel("8", "IO", "05-12-2026", "166.66", "100,000.00"),
            DepositFlowModel("9", "IO", "05-01-2027", "166.66", "100,000.00"),
            DepositFlowModel("10", "IO", "05-02-2027", "166.66", "100,000.00"),
            DepositFlowModel("11", "IO", "05-03-2027", "166.66", "100,000.00")
        )

        binding.rvFlows.layoutManager = LinearLayoutManager(this)

        binding.rvFlows.adapter = DepositFlowAdapter(dummyFlows) { selectedFlow ->
            val intent = Intent(this, JournalEntriesActivity::class.java)
            intent.putExtra("FLOW_ID", selectedFlow.flowId)
            intent.putExtra("FLOW_CODE", selectedFlow.flowCode)
            startActivity(intent)
        }
    }

    private fun setupTabs() {
        binding.tabFlows.setOnClickListener {
            switchTab(true)
        }
        binding.tabOperations.setOnClickListener {
            switchTab(false)
        }
        switchTab(true)
    }

    private fun switchTab(isFlows: Boolean) {
        if (isFlows) {
            binding.tabFlows.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
            binding.tabFlows.setTextColor(android.graphics.Color.WHITE)

            binding.tabOperations.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tabOperations.setTextColor(android.graphics.Color.parseColor("#333333"))

            binding.headerFlows.visibility = View.VISIBLE
            binding.headerOperations.visibility = View.GONE
            binding.rvFlows.visibility = View.VISIBLE
            binding.layoutNoRecords.visibility = View.GONE

        } else {
            binding.tabOperations.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
            binding.tabOperations.setTextColor(android.graphics.Color.WHITE)

            binding.tabFlows.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tabFlows.setTextColor(android.graphics.Color.parseColor("#333333"))

            binding.headerFlows.visibility = View.GONE
            binding.headerOperations.visibility = View.VISIBLE
            binding.rvFlows.visibility = View.GONE
            binding.layoutNoRecords.visibility = View.VISIBLE
        }
    }
}