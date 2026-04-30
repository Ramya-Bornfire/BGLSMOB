package com.example.bgls

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class GLStructureAddActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnUpdate: Button
    private lateinit var toolbarTitle: TextView

    private lateinit var etBranchId: EditText
    private lateinit var etBranchDes: EditText
    private lateinit var etGlCode: EditText
    private lateinit var etGlDes: EditText
    private lateinit var etGlSub: EditText
    private lateinit var etGlSh: EditText
    private lateinit var etCurrency: EditText
    private lateinit var etBal: EditText
    private lateinit var etSeq: EditText
    private lateinit var etTol: EditText
    private lateinit var etActOpen: EditText
    private lateinit var etActClose: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glstructure_add) // 👈 உங்கள் XML name

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Init Views
        btnBack = findViewById(R.id.btnBack)
        btnUpdate = findViewById(R.id.btnUpdate)
        toolbarTitle = findViewById(R.id.toolbarTitle)

        etBranchId = findViewById(R.id.etbranchid)
        etBranchDes = findViewById(R.id.etbranchdes)
        etGlCode = findViewById(R.id.etglcode)
        etGlDes = findViewById(R.id.etgldes)
        etGlSub = findViewById(R.id.etglsub)
        etGlSh = findViewById(R.id.etglsh)
        etCurrency = findViewById(R.id.etcurrentcode)
        etBal = findViewById(R.id.etbal)
        etSeq = findViewById(R.id.etseq)
        etTol = findViewById(R.id.ettol)
        etActOpen = findViewById(R.id.etactopen)
        etActClose = findViewById(R.id.etactclose)

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        // Submit button
        btnUpdate.setOnClickListener {

            val branchId = etBranchId.text.toString()
            val branchDes = etBranchDes.text.toString()
            val glCode = etGlCode.text.toString()
            val glDes = etGlDes.text.toString()
            val glSub = etGlSub.text.toString()
            val glSh = etGlSh.text.toString()
            val currency = etCurrency.text.toString()
            val bal = etBal.text.toString()
            val seq = etSeq.text.toString()
            val tol = etTol.text.toString()
            val actOpen = etActOpen.text.toString()
            val actClose = etActClose.text.toString()

            // Validation
            if (branchId.isEmpty() || glCode.isEmpty() || glDes.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔥 Debug / Success
            Toast.makeText(
                this,
                "GL Added: $glCode",
                Toast.LENGTH_LONG
            ).show()

            // TODO 👉 API Call here (Retrofit POST)
        }
    }
}