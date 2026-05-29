package com.example.bgls

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bgls.DataModels.ReferenceCode
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.Retrofit.RetrofitClient

class ReferenceDetailModifyActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var toolbarTitle: TextView
    private lateinit var btnUpdate: Button

    private lateinit var spRefType: Spinner
    private lateinit var etType: EditText
    private lateinit var etRefId: EditText
    private lateinit var etRefDes: EditText
    private lateinit var etMod: EditText
    private lateinit var etRemark: EditText

    private var isFirstSelection = true
    private var typeToDescriptionMap = mutableMapOf<String, Pair<String, String>>() // type -> (desc, module)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reference_detail_modify)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        // Init views
        btnBack = findViewById(R.id.btnBack)
        toolbarTitle = findViewById(R.id.toolbarTitle)
        btnUpdate = findViewById(R.id.btnUpdate)

        spRefType = findViewById(R.id.spRefType)
        etType = findViewById(R.id.ettype)
        etRefId = findViewById(R.id.etrefid)
        etRefDes = findViewById(R.id.etrefdes)
        etMod = findViewById(R.id.etmod)
        etRemark = findViewById(R.id.etremark)

        btnBack.setOnClickListener { finish() }

        // Home button
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Populate fields from Intent (original data)
        etType.setText(intent.getStringExtra("typeDesc"))
        etRefId.setText(intent.getStringExtra("refId"))
        etRefDes.setText(intent.getStringExtra("refDes"))
        etMod.setText(intent.getStringExtra("moduleId"))
        etRemark.setText(intent.getStringExtra("remarks"))

        fetchReferenceTypes()

        btnUpdate.setOnClickListener {
            val refType = spRefType.selectedItem.toString()
            val typeDesc = etType.text.toString()
            val refId = etRefId.text.toString()
            val refDesc = etRefDes.text.toString()
            val module = etMod.text.toString()
            val remark = etRemark.text.toString()

            if (refType == "Select" || typeDesc.isEmpty() || refId.isEmpty() || refDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.api.updateReferenceCode(
                ref_type = refType,
                ref_type_desc = typeDesc,
                ref_id = refId,
                ref_id_desc = refDesc,
                module_id = module,
                remarks = remark
            ).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (response.isSuccessful) {
                        val msg = response.body()?.string() ?: ""
                        if (msg.contains("Success", ignoreCase = true)) {
                            Toast.makeText(this@ReferenceDetailModifyActivity, "Reference Code Updated Successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ReferenceDetailModifyActivity, "Failed: $msg", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this@ReferenceDetailModifyActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ReferenceDetailModifyActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchReferenceTypes() {
        // Use "list" to get full ReferenceCode objects (same as Add activity)
        RetrofitClient.api.getRefList("list").enqueue(object : retrofit2.Callback<RefResponse> {
            override fun onResponse(call: retrofit2.Call<RefResponse>, response: retrofit2.Response<RefResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val refList = body?.refList ?: emptyList()
                    if (refList.isNotEmpty()) {
                        // Build map of type -> (description, module)
                        refList.forEach { code ->
                            if (!typeToDescriptionMap.containsKey(code.ref_type)) {
                                typeToDescriptionMap[code.ref_type] = Pair(code.ref_type_desc, code.module_id)
                            }
                        }
                        setupRefTypeSpinner(refList.map { it.ref_type }.distinct())
                    } else {
                        useStaticFallback()
                    }
                } else {
                    useStaticFallback()
                }
            }

            override fun onFailure(call: retrofit2.Call<RefResponse>, t: Throwable) {
                useStaticFallback()
                Toast.makeText(this@ReferenceDetailModifyActivity, "Failed to fetch types: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRefTypeSpinner(typeStrings: List<String>) {
        val typeNames = mutableListOf("Select")
        typeNames.addAll(typeStrings)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeNames)
        spRefType.adapter = adapter

        // Preselect the current refType from intent
        val currentRefType = intent.getStringExtra("refType")
        if (currentRefType != null) {
            val index = typeNames.indexOf(currentRefType)
            if (index >= 0) spRefType.setSelection(index)
        }

        spRefType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) return

                if (isFirstSelection) {
                    // First load â€“ do not overwrite original fields
                    isFirstSelection = false
                    return
                }

                // User changed the type â€“ update description and module from map
                val selectedType = typeStrings[position - 1]
                val pair = typeToDescriptionMap[selectedType]
                if (pair != null) {
                    etType.setText(pair.first)   // description
                    etMod.setText(pair.second)   // module id
                } else {
                    etType.setText("")
                    etMod.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun useStaticFallback() {
        val staticTypes = listOf(
            "A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08", "A09", "A10",
            "A11", "A12", "A13", "A14", "A15", "A16", "A17", "A18", "A19", "A20",
            "A21", "A22", "A23", "A24", "A25", "A26", "A27", "A28", "A29", "A30",
            "A31", "A32", "EMP_PRO_01", "EMP_PRO_02", "EMP_PRO_03", "EMP_PRO_04",
            "EMP_PRO_05", "EMP_PRO_06", "EMP_PRO_07", "EMP_PRO_08", "EMP_PRO_09", "EMP_PRO_10"
        )
        // For static fallback, use generic mapping
        staticTypes.forEach { type ->
            typeToDescriptionMap[type] = Pair("$type description", "COA")
        }
        setupRefTypeSpinner(staticTypes)
        Toast.makeText(this, "Using static reference types", Toast.LENGTH_SHORT).show()
    }
}
