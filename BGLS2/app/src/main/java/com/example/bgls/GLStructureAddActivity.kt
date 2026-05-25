package com.example.bgls

import android.os.Bundle
import android.view.WindowManager
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
        setContentView(R.layout.activity_glstructure_add) // ðŸ‘ˆ à®‰à®™à¯à®•à®³à¯ XML name
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
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

        // Home button
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
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

            // ðŸ”¥ Debug / Success
            // API call here
            val fields = mapOf(
                "branch_id" to branchId,
                "branch_desc" to branchDes,
                "glCode" to glCode,
                "glDescription" to glDes,
                "glsh_code" to glSh,
                "crncy_code" to currency,
                "total_balance" to bal,
                "seq_order" to seq,
                "no_acct_opened" to actOpen,
                "no_acct_closed" to actClose
            )

            com.example.bgls.Retrofit.RetrofitClient.api.manageGLStructure("add", glCode, glSh, fields).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@GLStructureAddActivity, "GL Structure Added Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@GLStructureAddActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Toast.makeText(this@GLStructureAddActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
