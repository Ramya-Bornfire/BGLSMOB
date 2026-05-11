package com.example.bgls

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class GLStructureViewActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView
   // private lateinit var btnUpdate: Button

    private lateinit var etBranchId: EditText
    private lateinit var etBranchDes: EditText
    private lateinit var etGlCode: EditText
    private lateinit var etGlDes: EditText
    private lateinit var etGlSub: EditText
    private lateinit var etGlSh: EditText
    private lateinit var etCurrentCode: EditText
    private lateinit var etBal: EditText
    private lateinit var etSeq: EditText
    private lateinit var etTol: EditText
    private lateinit var etActOpen: EditText
    private lateinit var etActClose: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glstructure_view)

        // Initialize views
        toolbarTitle = findViewById(R.id.toolbarTitle)
        
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        etBranchId = findViewById(R.id.etbranchid)
        etBranchDes = findViewById(R.id.etbranchdes)
        etGlCode = findViewById(R.id.etglcode)
        etGlDes = findViewById(R.id.etgldes)
        etGlSub = findViewById(R.id.etglsub)
        etGlSh = findViewById(R.id.etglsh)
        etCurrentCode = findViewById(R.id.etcurrentcode)
        etBal = findViewById(R.id.etbal)
        etSeq = findViewById(R.id.etseq)
        etTol = findViewById(R.id.ettol)
        etActOpen = findViewById(R.id.etactopen)
        etActClose = findViewById(R.id.etactclose)


        // Get data from intent (optional)
        val branchId = intent.getStringExtra("branchId") ?: ""
        val branchDesc = intent.getStringExtra("branchDesc") ?: ""
        val glCode = intent.getStringExtra("glCode") ?: ""
        val glDesc = intent.getStringExtra("glDesc") ?: ""
        val glSub = intent.getStringExtra("glshCode") ?: ""
        val glSh = intent.getStringExtra("glshDesc") ?: ""
        val currentCode = intent.getStringExtra("currencyCode") ?: ""
        val balance = intent.getStringExtra("balanceGroup") ?: ""
        val seq = intent.getStringExtra("sequence") ?: ""
        val total = intent.getStringExtra("totalBalance") ?: ""
        val actOpen = intent.getStringExtra("accountOpen") ?: ""
        val actClose = intent.getStringExtra("accountClose") ?: ""

        // Set values
        etBranchId.setText(branchId)
        etBranchDes.setText(branchDesc)
        etGlCode.setText(glCode)
        etGlDes.setText(glDesc)
        etGlSub.setText(glSub)
        etGlSh.setText(glSh)
        etCurrentCode.setText(currentCode)
        etBal.setText(balance)
        etSeq.setText(seq)
        etTol.setText(total)
        etActOpen.setText(actOpen)
        etActClose.setText(actClose)

        // Disable editing for View screen
        etBranchId.isEnabled = false
        etBranchDes.isEnabled = false
        etGlCode.isEnabled = false
        etGlDes.isEnabled = false
        etGlSub.isEnabled = false
        etGlSh.isEnabled = false
        etCurrentCode.isEnabled = false
        etBal.isEnabled = false
        etSeq.isEnabled = false
        etTol.isEnabled = false
        etActOpen.isEnabled = false
        etActClose.isEnabled = false

        // Submit button click
//        btnUpdate.setOnClickListener {
//            Toast.makeText(
//                this,
//                "View Mode - Submit Clicked",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }
}