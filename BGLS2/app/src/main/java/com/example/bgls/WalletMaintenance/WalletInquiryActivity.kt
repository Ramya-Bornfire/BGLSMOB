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
        val dummyWallets = listOf(
            WalletAccountModel(
                "Wallet",
                "CUST0000052001",
                "TD0049",
                "MOHAN",
                "01-04-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000047601",
                "LA0050",
                "MONISHA",
                "01-04-2026",
                "",
                "SCR",
                "400,000.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000052901",
                "TD0052",
                "KUMARESAN KUMAR",
                "01-05-2026",
                "",
                "SCR",
                "10.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000053201",
                "TD0053",
                "JACKIE JHAN",
                "01-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000053401",
                "TD0054",
                "JACKIE JHAN",
                "01-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000053901",
                "TD0056",
                "KUMAR RAVI",
                "01-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000054501",
                "TD0057",
                "JEYARAJ JEYA",
                "02-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000055101",
                "TD0058",
                "KUMARAN RAJENDERAN",
                "02-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000055401",
                "TD0059",
                "VIJI",
                "02-04-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000051001",
                "LA0060",
                "DISHAA",
                "01-04-2026",
                "",
                "SCR",
                "50,000.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000054801",
                "LA0077",
                "IRWIN KUMAR",
                "02-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000058301",
                "TD0087",
                "PON PRASANTH",
                "05-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000058901",
                "TD0089",
                "GOPIKA PRAKASH",
                "03-04-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000059201",
                "TD0092",
                "NILA",
                "03-04-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000060201",
                "TD0098",
                "PON PRASANTH",
                "06-05-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            ),
            WalletAccountModel(
                "Wallet",
                "CUST0000060501",
                "TD0099",
                "JAI",
                "06-03-2026",
                "",
                "SCR",
                "0.00",
                "Verified"
            )
        )

        binding.rvWalletInquiries.layoutManager = LinearLayoutManager(this)
        binding.rvWalletInquiries.adapter =
            WalletAccountAdapter(dummyWallets, false, { clickedCustId ->
                val intent = Intent(this, CustomerMasterViewActivity::class.java)
                intent.putExtra("CUSTOMER_ID", clickedCustId)
                startActivity(intent)
            }, { clickedAccNo ->
                if (clickedAccNo.startsWith("TD")) {
                    val intent = Intent(this, DepositAccountMaintenanceFlowActivity::class.java)
                    intent.putExtra("ACCT_ID", clickedAccNo)
                    startActivity(intent)
                } else if (clickedAccNo.startsWith("LA")) {
                    val intent = Intent(this, LoanMasterViewActivity::class.java)
                    intent.putExtra("LOAN_ID", clickedAccNo)
                    startActivity(intent)
                }
            }, { selectedAccount ->
                // Navigate to Flow/Detail screen
                val intent = Intent(this, WalletAccountFlowActivity::class.java)
                intent.putExtra("CUST_ID", selectedAccount.custId)
                intent.putExtra("CUST_NAME", selectedAccount.name)
                intent.putExtra("STATUS", selectedAccount.status)
                intent.putExtra("ACC_NO", selectedAccount.accNo)
                startActivity(intent)
            })
    }
}