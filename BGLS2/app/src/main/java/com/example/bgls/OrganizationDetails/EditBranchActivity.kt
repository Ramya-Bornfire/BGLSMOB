package com.example.bgls.OrganizationDetails

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bgls.R

class EditBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_branch)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Default title remove
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnBack = findViewById<ImageView>(R.id.btnBack)

        val etBranchCode = findViewById<EditText>(R.id.etBranchCode)
        val etBranchName = findViewById<EditText>(R.id.etBranchName)
        val etSwiftCode = findViewById<EditText>(R.id.etSwiftCode)
        val etBranchHead = findViewById<EditText>(R.id.etBranchHead)
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)

        // old values set
        etBranchCode.setText(intent.getStringExtra("code"))
        etBranchName.setText(intent.getStringExtra("name"))
        etSwiftCode.setText(intent.getStringExtra("swift"))
        etBranchHead.setText(intent.getStringExtra("head"))


        btnBack.setOnClickListener {
            finish()
        }
        btnUpdate.setOnClickListener {
            finish()
        }
    }
}