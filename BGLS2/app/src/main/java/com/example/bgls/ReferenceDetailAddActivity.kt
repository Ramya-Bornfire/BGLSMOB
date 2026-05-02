package com.example.bgls

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.DataModels.ReferenceCode
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Response


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

        // Fetch Reference Types from API
        fetchReferenceTypes()

        // Back button
        btnBack.setOnClickListener {
            finish()
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
            if (refType == "Select" || typeDesc.isEmpty() || refId.isEmpty() || refDesc.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // API call here
            val referenceCode = com.example.bgls.DataModels.ReferenceCode(
                ref_type = refType,
                ref_type_desc = typeDesc,
                ref_id = refId,
                ref_id_desc = refDesc,
                module_id = module,
                remarks = remark
            )

            RetrofitClient.api.addReferenceCode(
                ref_type = refType,
                ref_type_desc = typeDesc,
                ref_id = refId,
                ref_id_desc = refDesc,
                module_id = module,
                remarks = remark
            ).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReferenceDetailAddActivity, "Reference Code Added Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ReferenceDetailAddActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ReferenceDetailAddActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchReferenceTypes() {
        // Use "list" as in web and ParameterActivity
        RetrofitClient.api.getRefList("list").enqueue(object : retrofit2.Callback<RefResponse> {
            override fun onResponse(call: Call<RefResponse>, response: Response<RefResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val refTypeList = body?.refType ?: body?.refList ?: emptyList()
                    if (refTypeList.isNotEmpty()) {
                        setupRefTypeSpinner(refTypeList)
                    } else {
                        // Fallback to static list from web
                        setupStaticRefTypeSpinner()
                    }
                } else {
                    // Fallback on error
                    setupStaticRefTypeSpinner()
                    Toast.makeText(this@ReferenceDetailAddActivity, "Using static reference types", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<com.example.bgls.DataModels.RefResponse>, t: Throwable) {
                Toast.makeText(this@ReferenceDetailAddActivity, "Failed to fetch types: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupStaticRefTypeSpinner() {
        // Static list from web JavaScript (refTypes array)
        val staticRefTypes = listOf(
            "A01", "A02", "A03", "A04", "A05", "A06", "A07", "A08", "A09", "A10",
            "A11", "A12", "A13", "A14", "A15", "A16", "A17", "A18", "A19", "A20",
            "A21", "A22", "A23", "A24", "A25", "A26", "A27", "A28", "A29", "A30",
            "A31", "A32", "EMP_PRO_01", "EMP_PRO_02", "EMP_PRO_03", "EMP_PRO_04",
            "EMP_PRO_05", "EMP_PRO_06", "EMP_PRO_07", "EMP_PRO_08", "EMP_PRO_09", "EMP_PRO_10"
        )
        // Map type to description & module (simplified – you can expand)
        val refCodeList = staticRefTypes.map { refType ->
            com.example.bgls.DataModels.ReferenceCode(
                ref_type = refType,
                ref_type_desc = "$refType description",
                ref_id = "",
                ref_id_desc = "",
                module_id = "COA",
                remarks = ""
            )
        }
        setupRefTypeSpinner(refCodeList)
    }

    private fun setupRefTypeSpinner(refTypeList: List<ReferenceCode>) {
        val typeNames = mutableListOf("Select")
        typeNames.addAll(refTypeList.map { it.ref_type })

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeNames)
        spRefType.adapter = adapter

        spRefType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selectedRef = refTypeList[position - 1]
                    etType.setText(selectedRef.ref_type_desc)
                    etMod.setText(selectedRef.module_id)
                } else {
                    etType.setText("")
                    etMod.setText("")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}