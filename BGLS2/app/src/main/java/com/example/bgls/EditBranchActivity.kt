package com.example.bgls

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class EditBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_branch)

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

        btnUpdate.setOnClickListener {
            finish()
        }
    }
}