package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class ChartOfAccountsDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_of_accounts_detail)

        val tvDetailTitle = findViewById<TextView>(R.id.tvDetailTitle)

        val mode = intent.getStringExtra("MODE") ?: "VIEW"
        
        // The user said: "if i click verify means i have attached the view above do same as it is"
        // "when i click the modify means i have attached above it should open"
        tvDetailTitle.text = "CHART OF ACCOUNTS - $mode"

        // Here we could dynamically disable fields if mode == "VIEW" or "VERIFY"
        // or load data based on an passed "ACCT_ID" extra.
    }
}
