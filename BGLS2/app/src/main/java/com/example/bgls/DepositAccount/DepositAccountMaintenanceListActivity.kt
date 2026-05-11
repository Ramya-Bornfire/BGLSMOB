package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.DepositAccountAdapter
import com.example.bgls.DataModels.DepositAccountModel
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityDepositAccountMaintenanceListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.bgls.Retrofit.RetrofitClient
import android.widget.Toast

class DepositAccountMaintenanceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountMaintenanceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountMaintenanceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvDepositAccounts.layoutManager = LinearLayoutManager(this)

        binding.btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, DepositAccountOpeningActivity::class.java)
            startActivity(intent)
        }

        fetchDepositList()
    }

    private fun fetchDepositList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getDepositMaintenance("list")
                if (response.isSuccessful) {
                    val body = response.body()
                    val depositList = body?.getdata ?: emptyList()

                    val mappedList = depositList.map { item ->
                        DepositAccountModel(
                            custId = item.cust_id ?: "",
                            custName = item.cust_name ?: "",
                            actNo = item.depo_actno ?: "",
                            dateOfDeposit = formatDate(item.deposit_date),
                            depositAmount = java.text.DecimalFormat("#,##0.00").format(item.deposit_amt ?: 0.0),
                            status = if (item.entity_flg == "Y") "Verified" else "UnVerified",
                            branchId = item.branch_id ?: ""
                        )
                    }

                    withContext(Dispatchers.Main) {
                        binding.rvDepositAccounts.adapter = DepositAccountAdapter(mappedList,
                            onActionClick = { selectedAccount ->
                                val intent = Intent(this@DepositAccountMaintenanceListActivity, DepositAccountMaintenanceFlowActivity::class.java)
                                intent.putExtra("ACCT_ID", selectedAccount.actNo)
                                intent.putExtra("CUST_ID", selectedAccount.custId)
                                intent.putExtra("CUST_NAME", selectedAccount.custName)
                                intent.putExtra("STATUS", selectedAccount.status)
                                startActivity(intent)
                            },
                            onCustIdClick = { account ->
                                val intent = Intent(this@DepositAccountMaintenanceListActivity, com.example.bgls.CustomerMaster.CustomerMasterViewActivity::class.java)
                                val trimmedId = account.custId.trim()
                                intent.putExtra("customerId", trimmedId)
                                intent.putExtra("CUSTOMER_ID", trimmedId)
                                intent.putExtra("branchKey", account.branchId)
                                startActivity(intent)
                            },
                            onLedgerClick = { actNo ->
                                val intent = Intent(this@DepositAccountMaintenanceListActivity, com.example.bgls.CustomerMaster.AccountLedgerActivity::class.java)
                                intent.putExtra("acct_num", actNo)
                                startActivity(intent)
                            }
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DepositAccountMaintenanceListActivity, "Failed to load deposit list", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DepositAccountMaintenanceListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
