package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.Adapter.DepositFlowAdapter
import com.example.bgls.DataModels.DepositFlowModel
import com.example.bgls.MainActivity
import com.example.bgls.TransactionMaintenance.JournalEntriesActivity
import com.example.bgls.databinding.ActivityDepositAccountMaintenanceFlowBinding

class DepositAccountMaintenanceFlowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDepositAccountMaintenanceFlowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDepositAccountMaintenanceFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val custId = intent.getStringExtra("CUST_ID") ?: "CUST0000050701"
        val custName = intent.getStringExtra("CUST_NAME") ?: "LALITH"

        binding.etCustId.setText(custId)
        binding.etCustName.setText(custName)

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
    }

    private fun setupRecyclerView() {

        val dummyFlows = listOf(
            DepositFlowModel("1", "PI", "29-04-2026", "100,000.00", "100,000.00"),
            DepositFlowModel("2", "IO", "29-05-2026", "416.66", "100,000.00"),
            DepositFlowModel("3", "IO", "29-06-2026", "416.66", "100,000.00"),
            DepositFlowModel("4", "IO", "29-07-2026", "416.66", "100,000.00"),
            DepositFlowModel("5", "IO", "29-08-2026", "416.66", "100,000.00"),
            DepositFlowModel("6", "IO", "29-09-2026", "416.66", "100,000.00"),
            DepositFlowModel("7", "IO", "29-10-2026", "416.66", "100,000.00"),
            DepositFlowModel("8", "PO", "29-10-2026", "100,416.66", ".00")
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
        val adapter = binding.rvFlows.adapter as? DepositFlowAdapter

        if (isFlows) {
            binding.tabFlows.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
            binding.tabFlows.setTextColor(android.graphics.Color.WHITE)

            binding.tabOperations.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tabOperations.setTextColor(android.graphics.Color.parseColor("#333333"))

            binding.tvSelectHeader.visibility = View.GONE
            binding.layoutOperations.visibility = View.GONE

            adapter?.setOperationsMode(false)

        } else {
            binding.tabOperations.setBackgroundColor(android.graphics.Color.parseColor("#17A2B8"))
            binding.tabOperations.setTextColor(android.graphics.Color.WHITE)

            binding.tabFlows.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            binding.tabFlows.setTextColor(android.graphics.Color.parseColor("#333333"))

            binding.tvSelectHeader.visibility = View.VISIBLE
            binding.layoutOperations.visibility = View.VISIBLE

            adapter?.setOperationsMode(true)
        }
    }
}