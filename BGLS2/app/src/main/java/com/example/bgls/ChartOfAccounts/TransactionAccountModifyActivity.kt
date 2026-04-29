package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class TransactionAccountModifyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_modify)
        
        val btnSubmitModify = findViewById<Button>(R.id.btnSubmitModify)
        btnSubmitModify.setOnClickListener {
            Toast.makeText(this, "Modified Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
