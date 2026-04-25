package com.example.bgls

import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar


class DeleteBranchActivity : AppCompatActivity() {

    private lateinit var etBranchCode: EditText
    private lateinit var etBranchName: EditText
    private lateinit var etSwiftCode: EditText
    private lateinit var etBranchHead: EditText
    private lateinit var btnDelete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_branch)

        // Back Arrow show panna
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Delete Branch"

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

        btnDelete.setOnClickListener {
            Toast.makeText(
                this,
                "Branch Deleted Successfully",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    // Back Arrow click handle
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}