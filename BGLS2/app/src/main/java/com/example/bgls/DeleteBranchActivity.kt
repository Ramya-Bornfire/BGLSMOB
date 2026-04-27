package com.example.bgls

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeleteBranchActivity : AppCompatActivity() {

    private lateinit var etBranchCode: EditText
    private lateinit var etBranchName: EditText
    private lateinit var etSwiftCode: EditText
    private lateinit var etBranchHead: EditText
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_branch)

        // XML la Toolbar illa, btnBack use pannrom
        btnBack = findViewById(R.id.btnBack)

        etBranchCode = findViewById(R.id.etBranchCode)
        etBranchName = findViewById(R.id.etBranchName)
        etSwiftCode = findViewById(R.id.etSwiftCode)
        etBranchHead = findViewById(R.id.etBranchHead)
        btnDelete = findViewById(R.id.btnDelete)

        val code = intent.getStringExtra("code")
        val name = intent.getStringExtra("name")
        val swift = intent.getStringExtra("swift")
        val head = intent.getStringExtra("head")

        etBranchCode.setText(code)
        etBranchName.setText(name)
        etSwiftCode.setText(swift)
        etBranchHead.setText(head)

        // Delete screen la edit panna koodathu
        etBranchCode.isEnabled = false
        etBranchName.isEnabled = false
        etSwiftCode.isEnabled = false
        etBranchHead.isEnabled = false

        // Back button click
        btnBack.setOnClickListener {
            finish()
        }

        // Delete button click
        btnDelete.setOnClickListener {
            Toast.makeText(
                this,
                "Branch Deleted Successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }
}