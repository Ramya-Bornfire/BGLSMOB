package com.example.bgls

import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class ReferenceDetailDeleteActivity: AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var btnUpdate: Button

    private lateinit var spRefType: Spinner
    private lateinit var etType: EditText
    private lateinit var etRefId: EditText
    private lateinit var etRefDes: EditText
    private lateinit var etMod: EditText
    private lateinit var etRemark: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference_detail_delete) // à®‰à®™à¯à®•à®³à¯ XML file name
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        // Views
        btnBack = findViewById(R.id.btnBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        btnUpdate = findViewById(R.id.btnUpdate)

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

        // Home button
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Populate fields from Intent
        etType.setText(intent.getStringExtra("typeDesc"))
        etRefId.setText(intent.getStringExtra("refId"))
        etRefDes.setText(intent.getStringExtra("refDes"))
        etMod.setText(intent.getStringExtra("moduleId"))
        etRemark.setText(intent.getStringExtra("remarks"))

        // Make fields non-editable since it's a delete view
        etType.isEnabled = false
        etRefId.isEnabled = false
        etRefDes.isEnabled = false
        etMod.isEnabled = false
        etRemark.isEnabled = false
        spRefType.isEnabled = false

        // Spinner sample data
        val refType = intent.getStringExtra("refType") ?: ""
        val refTypes = listOf("Select Type", refType)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            refTypes
        )
        spRefType.adapter = adapter
        spRefType.setSelection(1)

        btnUpdate.setOnClickListener {
            val referenceCode = com.example.bgls.DataModels.ReferenceCode(
                ref_type = spRefType.selectedItem.toString(),
                ref_type_desc = etType.text.toString(),
                ref_id = etRefId.text.toString(),
                ref_id_desc = etRefDes.text.toString(),
                module_id = etMod.text.toString(),
                remarks = etRemark.text.toString()
            )

            com.example.bgls.Retrofit.RetrofitClient.api.deleteReferenceCode(
                ref_id = etRefId.text.toString()
            ).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReferenceDetailDeleteActivity, "Reference Code Deleted Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ReferenceDetailDeleteActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ReferenceDetailDeleteActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
