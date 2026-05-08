package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityAccountLedgerDetailsBinding

class AccountLedgerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountLedgerDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountLedgerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val acctId = intent.getStringExtra("ACCT_ID") ?: "TD0088"
        binding.etAcctId.setText(acctId)

        setupDownloadSpinner()

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

    private fun setupDownloadSpinner() {
        val options = arrayOf("Download", "Excel")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spnDownload.adapter = adapter
    }
}
