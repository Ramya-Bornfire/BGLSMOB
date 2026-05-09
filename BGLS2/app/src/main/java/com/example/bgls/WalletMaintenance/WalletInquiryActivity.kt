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
import com.example.bgls.databinding.ActivityWalletInquiryBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast

class WalletInquiryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletInquiryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletInquiryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

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
        binding.rvWalletInquiries.layoutManager = LinearLayoutManager(this)
        binding.rvWalletInquiries.adapter = WalletAccountAdapter(
            emptyList(), false, { }, { }, { }
        )

        // Fetch data from API
        fetchWalletInquiriesList()


    }

    private fun fetchWalletInquiriesList() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getWalletInquiries("list")
                if (response.isSuccessful) {
                    val body = response.body()
                    val inquiryList = body?.walletMaintenanceList ?: emptyList()

                    val mappedList = inquiryList.mapNotNull { item ->
                        try {
                            when (item) {
                                is Map<*, *> -> {
                                    val map = item as Map<String, Any?>
                                    WalletAccountModel(
                                        category = map["wallet_category"]?.toString() ?: "Wallet",
                                        custId = map["customer_id"]?.toString() ?: "",
                                        accNo = map["bips_acct_num"]?.toString() ?: (map["wallet_acct_num"]?.toString() ?: ""),
                                        walletAcctNum = map["wallet_acct_num"]?.toString() ?: "",
                                        name = map["wallet_acct_name"]?.toString() ?: "",
                                        openDate = formatDateForUI(map["acct_open_date"]?.toString()),
                                        closeDate = formatDateForUI(map["act_cls_date"]?.toString()),
                                        currency = map["wallet_crncy"]?.toString() ?: "",
                                        balance = map["acct_bal"]?.toString() ?: "0.00",
                                        status = if (map["entity_flg"]?.toString() == "Y") "Verified" else "Unverified",
                                        isSelected = false
                                    )
                                }
                                is List<*> -> {
                                    val list = item as List<Any?>
                                    // Order based on getWalletWithLoanDetails repository call:
                                    // [category, customer_id, bips_acct_num, name, open_date, cls_date, currency, balance, wallet_acct_num]
                                    WalletAccountModel(
                                        category = list.getOrNull(0)?.toString() ?: "Wallet",
                                        custId = list.getOrNull(1)?.toString() ?: "",
                                        accNo = list.getOrNull(2)?.toString() ?: "",
                                        walletAcctNum = list.getOrNull(8)?.toString() ?: "",
                                        name = list.getOrNull(3)?.toString() ?: "",
                                        openDate = formatDateForUI(list.getOrNull(4)?.toString()),
                                        closeDate = formatDateForUI(list.getOrNull(5)?.toString()),
                                        currency = list.getOrNull(6)?.toString() ?: "",
                                        balance = list.getOrNull(7)?.toString() ?: "0.00",
                                        status = "Verified",
                                        isSelected = false
                                    )
                                }
                                else -> null
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WalletInquiry", "Error parsing item: ${e.message}")
                            null
                        }
                    }

                    withContext(Dispatchers.Main) {
                        (binding.rvWalletInquiries.adapter as? WalletAccountAdapter)?.let { adapter ->
                            binding.rvWalletInquiries.adapter = WalletAccountAdapter(
                                mappedList, false, { clickedCustId ->
                                    val intent = Intent(this@WalletInquiryActivity, CustomerMasterViewActivity::class.java)
                                    intent.putExtra("CUSTOMER_ID", clickedCustId)
                                    startActivity(intent)
                                }, { clickedAccNo ->
                                    if (clickedAccNo.startsWith("TD")) {
                                        val intent = Intent(this@WalletInquiryActivity, DepositAccountMaintenanceFlowActivity::class.java)
                                        intent.putExtra("ACCT_ID", clickedAccNo)
                                        startActivity(intent)
                                    } else if (clickedAccNo.startsWith("LA")) {
                                        val intent = Intent(this@WalletInquiryActivity, LoanMasterViewActivity::class.java)
                                        intent.putExtra("LOAN_ID", clickedAccNo)
                                        startActivity(intent)
                                    }
                                }, { selectedAccount ->
                                    val intent = Intent(this@WalletInquiryActivity, WalletAccountFlowActivity::class.java)
                                    intent.putExtra("CUST_ID", selectedAccount.custId)
                                    intent.putExtra("CUST_NAME", selectedAccount.name)
                                    intent.putExtra("STATUS", selectedAccount.status)
                                    intent.putExtra("ACC_NO", selectedAccount.accNo)
                                    intent.putExtra("INTERNAL_ACC_NO", selectedAccount.walletAcctNum)
                                    startActivity(intent)
                                }
                            )
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@WalletInquiryActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@WalletInquiryActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDateForUI(dateStr: String?): String {
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