package com.example.bgls

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class GLStructureDeleteActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_glstructure_delete)

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

        // Home button
        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Get data from Intent
        etBranchId.setText(intent.getStringExtra("branchId"))
        etBranchDes.setText(intent.getStringExtra("branchDesc"))
        etGlCode.setText(intent.getStringExtra("glCode"))
        etGlDes.setText(intent.getStringExtra("glDesc"))
        etGlSub.setText(intent.getStringExtra("glshCode"))
        etGlsh.setText(intent.getStringExtra("glshDesc"))
        etCurrencyCode.setText(intent.getStringExtra("currencyCode"))
        etBal.setText(intent.getStringExtra("balanceGroup") ?: intent.getStringExtra("creditBal"))
        etSeq.setText(intent.getStringExtra("sequence"))
        etTol.setText(intent.getStringExtra("totalBalance") ?: intent.getStringExtra("debitBal"))
        etActOpen.setText(intent.getStringExtra("accountOpen"))
        etActClose.setText(intent.getStringExtra("accountClose"))

        // Submit Button
        btnUpdate.setOnClickListener {
            // API call here
            val fields = mapOf(
                "glCode" to etGlCode.text.toString(),
                "glsh_code" to etGlSub.text.toString()
            )

            com.example.bgls.Retrofit.RetrofitClient.api.manageGLStructure("delete", fields["glCode"], fields["glsh_code"], fields).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@GLStructureDeleteActivity, "GL Structure Deleted Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@GLStructureDeleteActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                    Toast.makeText(this@GLStructureDeleteActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
