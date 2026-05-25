package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.DepositFlowAdapter
import com.example.bgls.Adapter.DepositOperationAdapter
import com.example.bgls.DataModels.DepositAccountDetail
import com.example.bgls.DataModels.DepositFlow
import com.example.bgls.DataModels.DepositFlowModel
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.databinding.ActivityDepositAccountMaintenanceFlowBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DepositAccountMaintenanceFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountMaintenanceFlowBinding
    private var currentActNo: String? = null
    private var flowList: List<DepositFlow> = emptyList()
    private var operationList: List<List<Any?>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountMaintenanceFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        currentActNo = intent.getStringExtra("ACCT_ID")
        val status = intent.getStringExtra("STATUS") ?: "UnVerified"

        if (status.equals("Verified", ignoreCase = true)) {
            binding.btnVerify.text = "Modify"
        } else {
            binding.btnVerify.text = "Verify"
        }

        setupTabs()

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLedger.setOnClickListener {
            val intent = Intent(this, com.example.bgls.CustomerMaster.AccountLedgerActivity::class.java)
            intent.putExtra("acct_num", binding.etAccNo.text.toString())
            intent.putExtra("branch_id", binding.etBranchId.text.toString())
            startActivity(intent)
        }

        binding.etCustId.setOnClickListener {
            val intent = Intent(this, com.example.bgls.CustomerMaster.CustomerMasterViewActivity::class.java)
            val trimmedId = binding.etCustId.text.toString().trim()
            intent.putExtra("customerId", trimmedId)
            intent.putExtra("CUSTOMER_ID", trimmedId)
            intent.putExtra("branchKey", binding.etBranchId.text.toString())
            startActivity(intent)
        }

        binding.btnWallet.setOnClickListener {
            val intent = Intent(this, com.example.bgls.WalletMaintenance.WalletAccountFlowActivity::class.java)
            val accNo = binding.etAccNo.text.toString()
            val walletAccNo = if (accNo.length >= 2) "WA" + accNo.substring(2) else accNo
            intent.putExtra("ACC_NO", walletAccNo)
            intent.putExtra("INTERNAL_ACC_NO", walletAccNo)
            intent.putExtra("CUST_ID", binding.etCustId.text.toString())
            intent.putExtra("CUST_NAME", binding.etCustName.text.toString())
            intent.putExtra("MODE", "VIEW")
            startActivity(intent)
        }

        binding.btnVerify.setOnClickListener {
            when (binding.btnVerify.text) {
                "Modify" -> enterModifyMode()
                "Verify" -> enterVerifyMode()
                "Submit" -> {
                    if (binding.tvTitle.text.toString().contains("Verify")) {
                        performVerify()
                    } else {
                        performModify()
                    }
                }
            }
        }

        fetchDepositDetails()
    }

    private fun fetchDepositDetails() {
        if (currentActNo == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getDepositMaintenance("view", currentActNo)
                if (response.isSuccessful) {
                    val body = response.body()
                    val detail = body?.customerdata
                    flowList = body?.listact ?: emptyList()
                    operationList = body?.listacts ?: emptyList()

                    withContext(Dispatchers.Main) {
                        detail?.let { populateFields(it) }
                        updateFlowsTab()
                        updateOperationsTab()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateFields(detail: DepositAccountDetail) {
        val df = java.text.DecimalFormat("#,##0.00")
        binding.etBranchId.setText(detail.branch_id ?: "")
        binding.etBranchDesc.setText(detail.branch_desc ?: "")
        binding.etCustId.setText(detail.cust_id ?: "")
        binding.etCustName.setText(detail.cust_name ?: "")
        binding.etDepositType.setText(detail.deposit_type ?: "")
        binding.etSchemeCode.setText(detail.scheme_code ?: "")
        binding.etGlshCode.setText(detail.glsh_code ?: "")
        binding.etGlshDesc.setText(detail.glsh_desc ?: "")
        binding.etAccNo.setText(detail.depo_actno ?: "")
        binding.etDepositDate.setText(formatDate(detail.deposit_date))
        binding.etPeriod.setText(detail.deposit_period ?: "")
        binding.etAmount.setText(df.format(detail.deposit_amt ?: 0.0))
        binding.etRoi.setText(detail.rate_of_int ?: "")
        binding.etInterestAmount.setText(df.format(detail.int_amt ?: 0.0))
        binding.etFrequency.setText(detail.frequency ?: "")
        binding.etCurrency.setText(detail.currency ?: "")
        binding.etMaturityDate.setText(formatDate(detail.maturity_date))
        
        val maturityAmt = detail.maturity_amt
        val maturityAmtStr = when (maturityAmt) {
            is Double -> df.format(maturityAmt)
            is String -> maturityAmt
            else -> ""
        }
        binding.etMaturityAmount.setText(maturityAmtStr)
    }

    private fun performVerify() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.verifyDepositMaintenance(currentActNo!!)
                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "Verified successfully"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceFlowActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Verification failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performModify() {
        val fields = mutableMapOf<String, String>()
        fields["branch_desc"] = binding.etBranchDesc.text.toString()
        fields["branch_id"] = binding.etBranchId.text.toString()
        fields["currency"] = binding.etCurrency.text.toString()
        fields["cust_id"] = binding.etCustId.text.toString()
        fields["cust_name"] = binding.etCustName.text.toString()
        fields["deposit_amt"] = binding.etAmount.text.toString().replace(",", "")
        fields["int_amt"] = binding.etInterestAmount.text.toString().replace(",", "")
        fields["maturity_amt"] = binding.etMaturityAmount.text.toString().replace(",", "")
        fields["deposit_date"] = binding.etDepositDate.text.toString()
        fields["deposit_period"] = binding.etPeriod.text.toString()
        fields["deposit_type"] = binding.etDepositType.text.toString()
        fields["frequency"] = binding.etFrequency.text.toString()
        fields["rate_of_int"] = binding.etRoi.text.toString()
        fields["scheme_code"] = binding.etSchemeCode.text.toString()
        fields["glsh_code"] = binding.etGlshCode.text.toString()
        fields["glsh_desc"] = binding.etGlshDesc.text.toString()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.modifyDepositMaintenance(currentActNo!!, fields)
                if (response.isSuccessful) {
                    val msg = response.body()?.string() ?: "Updated successfully"
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceFlowActivity, msg, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Modification failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountMaintenanceFlowActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun enterVerifyMode() {
        binding.tvTitle.text = "DEPOSIT MAINTENANCE - Verify"
        binding.btnVerify.text = "Submit"
    }

    private fun enterModifyMode() {
        binding.tvTitle.text = "DEPOSIT MAINTENANCE - Modify"
        binding.btnVerify.text = "Submit"

        val editTexts = listOf(
            binding.etBranchId, binding.etBranchDesc, binding.etCustId, binding.etCustName,
            binding.etDepositType, binding.etSchemeCode, binding.etGlshCode, binding.etGlshDesc,
            binding.etAccNo, binding.etDepositDate, binding.etPeriod, binding.etAmount,
            binding.etRoi, binding.etInterestAmount, binding.etFrequency, binding.etCurrency,
            binding.etMaturityDate, binding.etMaturityAmount
        )

        for (et in editTexts) {
            et.isEnabled = true
            et.setBackgroundResource(R.drawable.edittext_border)
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
            binding.layoutNoRecords.visibility = if (flowList.isEmpty()) View.VISIBLE else View.GONE
            updateFlowsTab()
        } else {
            binding.tabOperations.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
            binding.tabOperations.setTextColor(android.graphics.Color.WHITE)

            binding.tabFlows.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tabFlows.setTextColor(android.graphics.Color.parseColor("#333333"))

            binding.headerFlows.visibility = View.GONE
            binding.headerOperations.visibility = View.VISIBLE
            binding.rvFlows.visibility = View.VISIBLE // Reusing the same RV
            binding.layoutNoRecords.visibility = if (operationList.isEmpty()) View.VISIBLE else View.GONE
            updateOperationsTab()
        }
    }

    private fun updateFlowsTab() {
        if (binding.tabFlows.currentTextColor == android.graphics.Color.WHITE) {
            val df = java.text.DecimalFormat("#,##0.00")
            val mappedFlows = flowList.map {
                DepositFlowModel(
                    flowId = it.flow_id ?: "",
                    flowCode = it.flow_code ?: "",
                    flowDate = formatDate(it.flow_date),
                    flowAmount = df.format(it.flow_amt ?: 0.0),
                    outstandingBalance = df.format(it.clr_bal_amt ?: 0.0)
                )
            }
            binding.rvFlows.layoutManager = LinearLayoutManager(this)
            binding.rvFlows.adapter = DepositFlowAdapter(mappedFlows) { selectedFlow ->
                // Handle click if needed
            }
        }
    }

    private fun updateOperationsTab() {
        if (binding.tabOperations.currentTextColor == android.graphics.Color.WHITE) {
            binding.rvFlows.layoutManager = LinearLayoutManager(this)
            binding.rvFlows.adapter = DepositOperationAdapter(operationList) { tranId ->
                // Handle tranId click
            }
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null || dateStr == "null" || dateStr.isEmpty()) return ""
        return if (dateStr.contains("T")) {
            val parts = dateStr.split("T")[0].split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else if (dateStr.contains("-") && dateStr.length >= 10) {
            val parts = dateStr.substring(0, 10).split("-")
            if (parts.size == 3 && parts[0].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else {
            dateStr
        }
    }
}