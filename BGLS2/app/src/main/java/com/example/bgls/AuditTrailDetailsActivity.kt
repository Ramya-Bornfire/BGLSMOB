package com.example.bgls

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AuditTrailDetailsActivity : AppCompatActivity() {

    private lateinit var tvUserActivity: TextView
    private lateinit var tvBusinessActivity: TextView
    //private lateinit var btnHome: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audit_trail_details)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvUserActivity = findViewById(R.id.tvUserActivity)
        tvBusinessActivity = findViewById(R.id.tvBusinessActivity)
       // btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupClickListeners() {

        // ─── User Activity click ───
        tvUserActivity.setOnClickListener {
            // TODO: Navigate to User Activity screen
            startActivity(Intent(this, UserActivityDetailsActivity::class.java))
           // Toast.makeText(this, "Opening User Activity...", Toast.LENGTH_SHORT).show()
        }

        // ─── Business Activity click ───
        tvBusinessActivity.setOnClickListener {
            // TODO: Navigate to Business Activity screen
             startActivity(Intent(this, BusinessActivityDetailsActivity::class.java))
           // Toast.makeText(this, "Opening Business Activity...", Toast.LENGTH_SHORT).show()
        }

        // ─── Home button ───
//        btnHome.setOnClickListener {
//            // TODO: startActivity(Intent(this, HomeActivity::class.java))
//            Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show()
//        }

        // ─── Back button ───
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}