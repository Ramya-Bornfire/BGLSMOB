package com.example.bgls.AuditTrial

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.bgls.AuditTrial.BusinessActivityDetailsActivity
import com.example.bgls.R
import com.example.bgls.AuditTrial.UserActivityDetailsActivity
import com.example.bgls.MainActivity
import com.google.android.material.navigation.NavigationView

class AuditTrailDetailsActivity : AppCompatActivity() {

    private lateinit var tvUserActivity: TextView
    private lateinit var tvBusinessActivity: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audit_trail_details)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        tvUserActivity = findViewById(R.id.tvUserActivity)
        tvBusinessActivity = findViewById(R.id.tvBusinessActivity)
       // btnHome = findViewById(R.id.btnHome)
      //  btnBack = findViewById(R.id.btnBack)
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
//        btnBack.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
    }
}