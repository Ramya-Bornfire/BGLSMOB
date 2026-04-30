package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class ChartOfAccountsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_of_accounts_detail)

        val tvDetailTitle = findViewById<TextView>(R.id.tvDetailTitle)

        val mode = intent.getStringExtra("MODE") ?: "VIEW"
        tvDetailTitle.text = "CHART OF ACCOUNTS - $mode"

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            // Re-open this same activity with MODIFY mode
            val intent = Intent(this, ChartOfAccountsDetailActivity::class.java)
            intent.putExtra("MODE", "MODIFY")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            val intent = Intent(this, ChartOfAccountsAddActivity::class.java)
            startActivity(intent)
        }
    }
}
