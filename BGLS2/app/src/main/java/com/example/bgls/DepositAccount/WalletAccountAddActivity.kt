package com.example.bgls.DepositAccount

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import com.example.bgls.databinding.ActivityWalletAccountAddBinding

class WalletAccountAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletAccountAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletAccountAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSubmit.setOnClickListener {
            Toast.makeText(this, "Wallet account created successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

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
}
