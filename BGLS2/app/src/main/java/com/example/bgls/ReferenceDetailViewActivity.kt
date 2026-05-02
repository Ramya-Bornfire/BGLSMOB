package com.example.bgls

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReferenceDetailViewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var toolbarTitle: TextView

    private lateinit var spRefType: Spinner
    private lateinit var etType: EditText
    private lateinit var etRefId: EditText
    private lateinit var etRefDes: EditText
    private lateinit var etMod: EditText
    private lateinit var etRemark: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference_detail_view)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Views
        btnBack = findViewById(R.id.btnBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        spRefType = findViewById(R.id.spRefType)
        etType = findViewById(R.id.ettype)
        etRefId = findViewById(R.id.etrefid)
        etRefDes = findViewById(R.id.etrefdes)
        etMod = findViewById(R.id.etmod)
        etRemark = findViewById(R.id.etremark)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Get data from Intent
        val refType = intent.getStringExtra("refType") ?: ""
        val typeDesc = intent.getStringExtra("typeDesc") ?: ""
        val refId = intent.getStringExtra("refId") ?: ""
        val refDes = intent.getStringExtra("refDes") ?: ""
        val moduleId = intent.getStringExtra("moduleId") ?: ""
        val remarks = intent.getStringExtra("remarks") ?: ""

        // Spinner data with selected value
        val refTypes = listOf(
            "Select Type",
            refType
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            refTypes
        )

        spRefType.adapter = adapter
        spRefType.setSelection(1)

        // Set values
        etType.setText(typeDesc)
        etRefId.setText(refId)
        etRefDes.setText(refDes)
        etMod.setText(moduleId)
        etRemark.setText(remarks)

        // Disable all fields (READ ONLY)
        spRefType.isEnabled = false

        etType.isEnabled = false
        etRefId.isEnabled = false
        etRefDes.isEnabled = false
        etMod.isEnabled = false
        etRemark.isEnabled = false

        // Remove focus and cursor
        etType.isFocusable = false
        etRefId.isFocusable = false
        etRefDes.isFocusable = false
        etMod.isFocusable = false
        etRemark.isFocusable = false
    }
}