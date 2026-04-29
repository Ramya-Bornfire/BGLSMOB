package com.example.bgls

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class GLStructureModifyActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var btnUpdate: Button

    private lateinit var etBranchId: EditText
    private lateinit var etBranchDes: EditText
    private lateinit var etGlCode: EditText
    private lateinit var etGlDes: EditText
    private lateinit var etGlSub: EditText
    private lateinit var etGlsh: EditText
    private lateinit var etCurrencyCode: EditText
    private lateinit var etBal: EditText
    private lateinit var etSeq: EditText
    private lateinit var etTol: EditText
    private lateinit var etActOpen: EditText
    private lateinit var etActClose: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glstructure_modify)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Views
        btnBack = findViewById(R.id.btnBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        btnUpdate = findViewById(R.id.btnUpdate)

        etBranchId = findViewById(R.id.etbranchid)
        etBranchDes = findViewById(R.id.etbranchdes)
        etGlCode = findViewById(R.id.etglcode)
        etGlDes = findViewById(R.id.etgldes)
        etGlSub = findViewById(R.id.etglsub)
        etGlsh = findViewById(R.id.etglsh)
        etCurrencyCode = findViewById(R.id.etcurrentcode)
        etBal = findViewById(R.id.etbal)
        etSeq = findViewById(R.id.etseq)
        etTol = findViewById(R.id.ettol)
        etActOpen = findViewById(R.id.etactopen)
        etActClose = findViewById(R.id.etactclose)

        // Back Button
        btnBack.setOnClickListener {
            finish()
        }

        // Get data from Intent
        etBranchId.setText(intent.getStringExtra("branchId"))
        etBranchDes.setText(intent.getStringExtra("branchDesc"))
        etGlCode.setText(intent.getStringExtra("glCode"))
        etGlDes.setText(intent.getStringExtra("glDesc"))
        etGlSub.setText(intent.getStringExtra("glshCode"))
        etGlsh.setText(intent.getStringExtra("glshDesc"))
        etCurrencyCode.setText(intent.getStringExtra("currencyCode"))
        etBal.setText(intent.getStringExtra("creditBal"))
        etTol.setText(intent.getStringExtra("debitBal"))

        // Submit Button
        btnUpdate.setOnClickListener {
            Toast.makeText(
                this,
                "GL Structure Updated Successfully",
                Toast.LENGTH_SHORT
            ).show()

            // TODO:
            // API call for update
            // or save updated data here
        }
    }
}