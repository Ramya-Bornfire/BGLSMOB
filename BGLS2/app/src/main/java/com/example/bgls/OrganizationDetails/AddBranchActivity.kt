package com.example.bgls.OrganizationDetails

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class AddBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_branch)

        // Toolbar setup
        //val toolbar = findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar)

        //toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //supportActionBar?.title = "Add Branch"

        // Views
        val etBranchCode = findViewById<EditText>(R.id.etBranchCode)
        val etBranchName = findViewById<EditText>(R.id.etBranchName)
        val etSwiftCode = findViewById<EditText>(R.id.etSwiftCode)
        val etBranchHead = findViewById<EditText>(R.id.etBranchHead)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        // Save click
        btnSave.setOnClickListener {

            val code = etBranchCode.text.toString()
            val name = etBranchName.text.toString()
            val swift = etSwiftCode.text.toString()
            val head = etBranchHead.text.toString()

            if (code.isEmpty() || name.isEmpty() || swift.isEmpty() || head.isEmpty()) {
                Toast.makeText(this, "Enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent()
            intent.putExtra("code", code)
            intent.putExtra("name", name)
            intent.putExtra("swift", swift)
            intent.putExtra("head", head)

            setResult(RESULT_OK, intent)
            finish()
        }
        btnBack.setOnClickListener {
            finish()
        }
    }

    // Back button in toolbar
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == android.R.id.home) {
//            finish()
//            return true
//        }
//        return super.onOptionsItemSelected(item)
//    }
}