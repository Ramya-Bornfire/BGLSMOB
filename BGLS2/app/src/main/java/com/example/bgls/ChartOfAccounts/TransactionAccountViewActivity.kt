package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

import com.example.bgls.MainActivity
import android.widget.ImageView

class TransactionAccountViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_view)

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, TransactionAccountModifyActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val intent = Intent(this, TransactionAccountAddActivity::class.java)
            startActivity(intent)
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
