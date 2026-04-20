package com.example.bgls

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bgls.DataModels.Transaction
import com.example.bgls.databinding.ActivityMainBinding
import android.widget.TextView
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val txtLoginTime = findViewById<TextView>(R.id.txtLoginTime)

        val currentTime = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())

        txtLoginTime.text = "$currentTime"

        val list = listOf(
            Transaction("Admin"),
            Transaction("Migration"),
            Transaction("Customer Maintenance"),
            Transaction("Loan Maintenance"),
            Transaction("Loan Operation"),
            Transaction("Transaction Maintanance"),
            Transaction("Reversal Transactions"),
            Transaction("Day End Operation"),
            Transaction("Collection Process"),
            Transaction("Batch Job Execution"),
            Transaction("Transaction Reports"),
            Transaction("Transaction Inquiries")

        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = TransactionAdapter(list)
    }
}
