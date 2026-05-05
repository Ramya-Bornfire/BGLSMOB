package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.DepositAccountAdapter
import com.example.bgls.DataModels.DepositAccountModel
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityDepositAccountMaintenanceListBinding

class DepositAccountMaintenanceListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountMaintenanceListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountMaintenanceListBinding.inflate(layoutInflater)
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
        val dummyAccounts = listOf(
            DepositAccountModel("CUST0000050701", "LALITH KUMAR", "TD0041", "29-04-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000051301", "SHASHA", "TD0046", "30-04-2026", "500,000.00", "UnVerified"),
            DepositAccountModel("CUST0000055401", "VIJI", "TD0059", "04-05-2026", "500,000.00", "Verified"),
            DepositAccountModel("CUST0000055101", "KUMARAN RAJENDERAN", "TD0058", "02-05-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000053401", "JACKIE JHAN", "TD0054", "01-05-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000052001", "MOHAN", "TD0049", "30-04-2026", "400,000.00", "Verified"),
            DepositAccountModel("CUST0000053901", "KUMAR RAVI", "TD0056", "01-05-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000054501", "JEYARAJ JEYA", "TD0057", "02-05-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000052601", "HARISH KALYAN", "TD0051", "01-05-2026", "100,000.00", "Verified"),
            DepositAccountModel("CUST0000052901", "KUMARESAN KUMAR", "TD0052", "01-05-2026", "100,000.00", "Verified")
        )

        binding.rvDepositAccounts.layoutManager = LinearLayoutManager(this)
        binding.rvDepositAccounts.adapter = DepositAccountAdapter(dummyAccounts) { selectedAccount ->
            val intent = Intent(this, DepositAccountMaintenanceFlowActivity::class.java)
            intent.putExtra("CUST_ID", selectedAccount.custId)
            intent.putExtra("CUST_NAME", selectedAccount.custName)
            startActivity(intent)
        }
    }
}
