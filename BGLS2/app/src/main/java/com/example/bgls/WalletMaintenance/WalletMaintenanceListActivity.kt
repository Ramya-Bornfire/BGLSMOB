package com.example.bgls.WalletMaintenance

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.WalletAccountAdapter
import com.example.bgls.CustomerMaster.CustomerMasterViewActivity
import com.example.bgls.CustomerMaster.LoanMasterViewActivity
import com.example.bgls.DataModels.WalletAccountModel
import com.example.bgls.DepositAccount.DepositAccountMaintenanceFlowActivity
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityWalletMaintenanceListBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

class WalletMaintenanceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletMaintenanceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletMaintenanceListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, WalletAccountAddActivity::class.java)
            startActivity(intent)
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

    private fun setupRecyclerView() {
        binding.rvWalletAccounts.layoutManager = LinearLayoutManager(this)
        fetchWalletMaintenanceList()
    }

    private fun fetchWalletMaintenanceList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getWalletMaintenance("list")
                if (response.isSuccessful) {
                    val body = response.body()
                    val entityList = body?.walletMaintenanceList ?: emptyList()

                    val mappedList = entityList.map { entity ->
                        WalletAccountModel(
                            category = entity.wallet_category ?: "Wallet",
                            custId = entity.customer_id ?: "",
                            accNo = entity.bips_acct_num ?: (entity.wallet_acct_num ?: ""),
                            walletAcctNum = entity.wallet_acct_num ?: "",
                            name = entity.wallet_acct_name ?: "",
                            openDate = entity.acct_open_date?.split("T")?.get(0) ?: "",
                            closeDate = entity.act_cls_date?.split("T")?.get(0) ?: "",
                            currency = entity.wallet_crncy ?: "",
                            balance = entity.acct_bal ?: "0.00",
                            status = if (entity.entity_flg == "Y") "Verified" else "Unverified",
                            isSelected = false
                        )
                    }

                    withContext(Dispatchers.Main) {
                        binding.rvWalletAccounts.adapter = WalletAccountAdapter(
                            mappedList,
                            true,
                            { clickedCustId ->
                                val intent = Intent(this@WalletMaintenanceListActivity, CustomerMasterViewActivity::class.java)
                                val trimmedId = clickedCustId.trim()
                                intent.putExtra("customerId", trimmedId)
                                intent.putExtra("CUSTOMER_ID", trimmedId)
                                
                                // Find corresponding entity to get branchKey
                                val entity = entityList.find { it.customer_id == clickedCustId }
                                intent.putExtra("branchKey", entity?.branch_id ?: "")
                                
                                startActivity(intent)
                            },
                            { clickedAccNo ->
                                if (clickedAccNo.startsWith("TD")) {
                                    val intent = Intent(this@WalletMaintenanceListActivity, DepositAccountMaintenanceFlowActivity::class.java)
                                    intent.putExtra("ACCT_ID", clickedAccNo)
                                    startActivity(intent)
                                } else if (clickedAccNo.startsWith("LA")) {
                                    val intent = Intent(this@WalletMaintenanceListActivity, LoanMasterViewActivity::class.java)
                                    intent.putExtra("loanId", clickedAccNo)
                                    intent.putExtra("holderKey", "HOLDER001") // Default holder key as fallback
                                    startActivity(intent)
                                }
                            },
                            { selectedAccount ->
                                val intent = Intent(this@WalletMaintenanceListActivity, WalletAccountFlowActivity::class.java)
                                intent.putExtra("MODE", "VIEW")
                                intent.putExtra("CUST_ID", selectedAccount.custId)
                                intent.putExtra("CUST_NAME", selectedAccount.name)
                                intent.putExtra("STATUS", selectedAccount.status)
                                intent.putExtra("ACC_NO", selectedAccount.accNo)
                                intent.putExtra("INTERNAL_ACC_NO", selectedAccount.walletAcctNum)
                                startActivity(intent)
                            }
                        )
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletMaintenanceListActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletMaintenanceListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
