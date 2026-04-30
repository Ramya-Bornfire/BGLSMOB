package com.example.bgls

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReferenceDetailAddActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnUpdate: Button
    private lateinit var toolbarTitle: TextView

    private lateinit var spRefType: Spinner
    private lateinit var etType: EditText
    private lateinit var etRefId: EditText
    private lateinit var etRefDes: EditText
    private lateinit var etMod: EditText
    private lateinit var etRemark: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference_detail_add) // 👈 உங்கள் XML file name

        // Toolbar setup
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize views
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
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

        // Spinner data
        val refTypes = listOf("Select Type", "COA", "EMPLOYEE PROFILE", "SCHEME")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            refTypes
        )
        spRefType.adapter = adapter

        // Spinner selection
        spRefType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val selected = refTypes[position]

                // Example logic
                if (selected != "Select Type") {
                    Toast.makeText(this@ReferenceDetailAddActivity, "Selected: $selected", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Submit button
        btnUpdate.setOnClickListener {

            val refType = spRefType.selectedItem.toString()
            val typeDesc = etType.text.toString()
            val refId = etRefId.text.toString()
            val refDesc = etRefDes.text.toString()
            val module = etMod.text.toString()
            val remark = etRemark.text.toString()

            // Validation
            if (refType == "Select Type" ||
                typeDesc.isEmpty() ||
                refId.isEmpty() ||
                refDesc.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: API call here
            Toast.makeText(
                this,
                "Submitted:\n$refId - $refDesc",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}