package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class TransactionAccountAddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_add)

        val btnSubmitAddTransaction = findViewById<Button>(R.id.btnSubmitAddTransaction)
        btnSubmitAddTransaction.setOnClickListener {
            Toast.makeText(this, "Added Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
