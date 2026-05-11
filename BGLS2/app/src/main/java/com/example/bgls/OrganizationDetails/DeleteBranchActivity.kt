package com.example.bgls.OrganizationDetails

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class DeleteBranchActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_branch)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        // Bind ALL fields
        val etBranchCode = findViewById<EditText>(R.id.etBranchCode)
        val etBranchName = findViewById<EditText>(R.id.etBranchName)
        val etBranchHead = findViewById<EditText>(R.id.etBranchHead)
        val etDesignation = findViewById<EditText>(R.id.etDesignation)
        val etSwiftCode = findViewById<EditText>(R.id.etSwiftCode)
        val etRemarks = findViewById<EditText>(R.id.etRemarks)
        val etLandline = findViewById<EditText>(R.id.etLandline)
        val etFax = findViewById<EditText>(R.id.etFax)
        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etContactPerson = findViewById<EditText>(R.id.etContactPerson)
        val etWebSite = findViewById<EditText>(R.id.etWebSite)
        val etMailId = findViewById<EditText>(R.id.etMailId)
        val etAddress1 = findViewById<EditText>(R.id.etAddress1)
        val etAddress2 = findViewById<EditText>(R.id.etAddress2)
        val etCity = findViewById<EditText>(R.id.etCity)
        val etState = findViewById<EditText>(R.id.etState)
        val etCountry = findViewById<EditText>(R.id.etCountry)
        val etZipCode = findViewById<EditText>(R.id.etZipCode)

        // Receive data from intent
        etBranchCode.setText(intent.getStringExtra("code") ?: "")
        etBranchName.setText(intent.getStringExtra("name") ?: "")
        etBranchHead.setText(intent.getStringExtra("head") ?: "")
        etDesignation.setText(intent.getStringExtra("designation") ?: "")
        etSwiftCode.setText(intent.getStringExtra("swift") ?: "")
        etRemarks.setText(intent.getStringExtra("remarks") ?: "")
        etLandline.setText(intent.getStringExtra("landline") ?: "")
        etFax.setText(intent.getStringExtra("fax") ?: "")
        etMobile.setText(intent.getStringExtra("mobile") ?: "")
        etContactPerson.setText(intent.getStringExtra("contact") ?: "")
        etWebSite.setText(intent.getStringExtra("website") ?: "")
        etMailId.setText(intent.getStringExtra("email") ?: "")
        etAddress1.setText(intent.getStringExtra("address1") ?: "")
        etAddress2.setText(intent.getStringExtra("address2") ?: "")
        etCity.setText(intent.getStringExtra("city") ?: "")
        etState.setText(intent.getStringExtra("state") ?: "")
        etCountry.setText(intent.getStringExtra("country") ?: "")
        etZipCode.setText(intent.getStringExtra("zip") ?: "")

        // Disable all fields (view-only)
        val allFields = listOf(
            etBranchCode, etBranchName, etBranchHead, etDesignation,
            etSwiftCode, etRemarks, etLandline, etFax,
            etMobile, etContactPerson, etWebSite, etMailId,
            etAddress1, etAddress2, etCity, etState, etCountry, etZipCode
        )
        allFields.forEach { it.isEnabled = false }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Delete button with backend call
        btnDelete.setOnClickListener {
            val branchCode = etBranchCode.text.toString().trim()
            if (branchCode.isEmpty()) {
                Toast.makeText(this, "Branch code missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnDelete.isEnabled = false   // prevent double click

            lifecycleScope.launch {
                try {
                    val response: Response<ResponseBody> = RetrofitClient.api.deleteBranch(branchCode)
                    if (response.isSuccessful) {
                        Toast.makeText(this@DeleteBranchActivity, "Branch deleted successfully", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@DeleteBranchActivity, "Delete failed: $errorBody", Toast.LENGTH_LONG).show()
                        btnDelete.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@DeleteBranchActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnDelete.isEnabled = true
                }
            }
        }
    }
}