package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

import com.example.bgls.MainActivity
import android.content.Intent
import android.widget.ImageView

class TransactionAccountModifyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_modify)
        
        val btnSubmitModify = findViewById<Button>(R.id.btnSubmitModify)
        btnSubmitModify.setOnClickListener {
            Toast.makeText(this, "Modified Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
