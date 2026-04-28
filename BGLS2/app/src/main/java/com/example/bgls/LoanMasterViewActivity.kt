package com.example.bgls

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoanMasterViewActivity : AppCompatActivity() {
    lateinit var btnLedger: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_master_view)
        
        val btnSchedule: Button = findViewById(R.id.btnSchedule)
        btnSchedule.setOnClickListener {
            val intent = Intent(this, LoanScheduleViewActivity::class.java)
            startActivity(intent)
        }

        btnLedger=findViewById<Button>(R.id.btnLedger)
        btnLedger.setOnClickListener {
            val intent = Intent(this, AccountLedgerActivity::class.java)
            startActivity(intent)
        }

        val btnWallet: Button = findViewById(R.id.btnWallet)
        btnWallet.setOnClickListener {
            val intent = Intent(this, WalletActivity::class.java)
            startActivity(intent)
        }
    }
}
