package com.example.bgls.WalletMaintenance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.WalletTransaction
import com.example.bgls.Adapter.WalletTransactionAdapter
import com.example.bgls.CustomerMaster.CustomerMasterViewActivity
import com.example.bgls.MainActivity
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.databinding.ActivityWalletInquiryDetailBinding
import kotlinx.coroutines.launch

class WalletInquiryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletInquiryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletInquiryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.etCustId.setOnClickListener {
            // Due to swap: etCustId shows Account No, etAccNoFrom shows Customer ID
            val custId = binding.etAccNoFrom.text.toString().trim() 
            if (custId.isNotEmpty()) {
                val intent = Intent(this, CustomerMasterViewActivity::class.java)
                intent.putExtra("customerId", custId)
                intent.putExtra("CUSTOMER_ID", custId)
                intent.putExtra("branchKey", binding.etBranchId.text.toString())
                startActivity(intent)
            }
        }

        val acctId = intent.getStringExtra("acctId") ?: ""
        
        if (acctId.isEmpty()) {
            Toast.makeText(this, "Account ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
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

        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        
        fetchInquiryDetails(acctId)
    }

    private fun fetchInquiryDetails(acctId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getWalletInquiries("view", acctId)
                if (response.isSuccessful) {
                    val body = response.body()
                    
                    // Populate wallet details
                    val walletData = body?.wallet
                    if (walletData != null) {
                        populateWalletUI(walletData)
                    }

                    // Populate additional inquiry fields
                    binding.etTranDateFrom.setText(formatDate(body?.TRMwallet1))
                    binding.etTranDateTo.setText(formatDate(body?.TRMwallet2))
                    binding.etTranType.setText(body?.TRMwallet ?: "")

                    // Populate transactions
                    val dataList = body?.dataList ?: emptyList()
                    val transactions = dataList.mapNotNull { item ->
                        try {
                            if (item is Map<*, *>) {
                                WalletTransaction(
                                    tranDate = formatDate(item["tran_date"]?.toString()),
                                    valueDate = formatDate(item["value_date"]?.toString()),
                                    custId = item["cust_id"]?.toString() ?: "",
                                    acctNum = item["acct_num"]?.toString() ?: "",
                                    acctName = item["acct_name"]?.toString() ?: "",
                                    tranType = item["tran_type"]?.toString() ?: "",
                                    particulars = item["tran_particular"]?.toString() ?: "",
                                    currency = item["acct_crncy"]?.toString() ?: "",
                                    amount = formatCurrency(item["tran_amt"]?.toString())
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    binding.rvTransactions.adapter = WalletTransactionAdapter(transactions)
                    
                } else {
                    Toast.makeText(this@WalletInquiryDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WalletInquiryDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateWalletUI(data: Any) {
        if (data is List<*>) {
            // Index-based mapping for [branch_id, branch_name, wallet_category, customer_id, wallet_acct_num, bips_acct_num, tran_date_from, tran_date_to, wallet_type, tran_type]
            binding.etBranchId.setText(data.getOrNull(0)?.toString() ?: "")
            binding.etBranchName.setText(data.getOrNull(1)?.toString() ?: "")
            binding.etWalletCategory.setText(data.getOrNull(2)?.toString() ?: "")
            binding.etCustId.setText(data.getOrNull(4)?.toString() ?: "")
            binding.etAccNoFrom.setText(data.getOrNull(3)?.toString() ?: "")
            binding.etAccNoTo.setText(data.getOrNull(5)?.toString() ?: "")
            
            // Populate dates and tran type from the list if they exist at indices 6, 7, 9
            val tDateFrom = data.getOrNull(6)?.toString()
            if (!tDateFrom.isNullOrEmpty()) binding.etTranDateFrom.setText(formatDate(tDateFrom))
            
            val tDateTo = data.getOrNull(7)?.toString()
            if (!tDateTo.isNullOrEmpty()) binding.etTranDateTo.setText(formatDate(tDateTo))
            
            binding.etWalletType.setText(data.getOrNull(8)?.toString() ?: "")
            
            val tType = data.getOrNull(9)?.toString()
            if (!tType.isNullOrEmpty()) binding.etTranType.setText(tType)

        } else if (data is Map<*, *>) {
            binding.etBranchId.setText(data["branch_id"]?.toString() ?: "")
            binding.etBranchName.setText(data["branch_name"]?.toString() ?: "")
            binding.etWalletCategory.setText(data["wallet_category"]?.toString() ?: "")
            binding.etCustId.setText(data["wallet_acct_num"]?.toString() ?: "")
            binding.etAccNoFrom.setText(data["customer_id"]?.toString() ?: "")
            binding.etAccNoTo.setText(data["bips_acct_num"]?.toString() ?: "")
            binding.etWalletType.setText(data["wallet_type"]?.toString() ?: "")
            
            // If they are inside the map as keys (might happen if Entity is returned as Map)
            data["tran_date_from"]?.let { binding.etTranDateFrom.setText(formatDate(it.toString())) }
            data["tran_date_to"]?.let { binding.etTranDateTo.setText(formatDate(it.toString())) }
            data["tran_type"]?.let { binding.etTranType.setText(it.toString()) }
        }
    }

    private fun formatCurrency(value: String?): String {
        if (value == null || value == "null" || value.isEmpty()) return "0.00"
        return try {
            val amount = value.replace(",", "").toDouble()
            val df = java.text.DecimalFormat("#,##0.00")
            df.format(amount)
        } catch (e: Exception) {
            value
        }
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null || dateStr == "null" || dateStr.isEmpty()) return ""
        
        // Handle long timestamps
        val timestamp = dateStr.toLongOrNull()
        if (timestamp != null) {
            return try {
                val sdf = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            } catch (e: Exception) {
                dateStr
            }
        }

        return if (dateStr.contains("T")) {
            val parts = dateStr.split("T")[0].split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else if (dateStr.length >= 10 && dateStr.contains("-")) {
            val parts = dateStr.substring(0, 10).split("-")
            if (parts.size == 3 && parts[0].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else {
            dateStr
        }
    }
}
