package com.example.bgls

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReferenceDetailModifyActivity: AppCompatActivity() {

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
        setContentView(R.layout.activity_reference_detail_modify) // உங்கள் XML file name

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

        // Back button click
        btnBack.setOnClickListener {
            finish()
        }

        // Spinner sample data
        val refTypes = listOf("Select Type", "Type A", "Type B", "Type C")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            refTypes
        )
        spRefType.adapter = adapter

        // Optional: Spinner selection
        spRefType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selected = refTypes[position]
                // use selected value if needed
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}