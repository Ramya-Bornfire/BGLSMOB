package com.example.bgls.OrganizationDetails

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bgls.R

class ViewBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Correct XML file
        setContentView(R.layout.activity_view_branch)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Remove default toolbar title
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val etBranchCode = findViewById<EditText>(R.id.etBranchCode)
        val etBranchName = findViewById<EditText>(R.id.etBranchName)
        val etBranchHead = findViewById<EditText>(R.id.etBranchHead)
        val etSwiftCode = findViewById<EditText>(R.id.etSwiftCode)
        val etLandline = findViewById<EditText>(R.id.etLandline)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etWebSite = findViewById<EditText>(R.id.etWebSite)
        val etAddress1 = findViewById<EditText>(R.id.etAddress1)

        // Receive values from Intent
        etBranchCode.setText(intent.getStringExtra("code") ?: "")
        etBranchName.setText(intent.getStringExtra("name") ?: "")
        etSwiftCode.setText(intent.getStringExtra("swift") ?: "")
        etBranchHead.setText(intent.getStringExtra("head") ?: "")
        etLandline.setText(intent.getStringExtra("landline") ?: "")
        etMobile.setText(intent.getStringExtra("mobile") ?: "")
        etWebSite.setText(intent.getStringExtra("website") ?: "")
        etAddress1.setText(intent.getStringExtra("address") ?: "")

        // Disable editing (View only mode)
        etBranchCode.isFocusable = false
        etBranchCode.isClickable = false

        etBranchName.isFocusable = false
        etBranchName.isClickable = false

        etBranchHead.isFocusable = false
        etBranchHead.isClickable = false

        etSwiftCode.isFocusable = false
        etSwiftCode.isClickable = false

        etLandline.isFocusable = false
        etLandline.isClickable = false

        etMobile.isFocusable = false
        etMobile.isClickable = false

        etWebSite.isFocusable = false
        etWebSite.isClickable = false

        etAddress1.isFocusable = false
        etAddress1.isClickable = false

        // Back button click
        btnBack.setOnClickListener {
            finish()
        }
    }
}