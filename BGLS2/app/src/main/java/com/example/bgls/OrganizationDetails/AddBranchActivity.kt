package com.example.bgls.OrganizationDetails

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
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

class AddBranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_branch)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
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

        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnSave.setOnClickListener {
            val code = etBranchCode.text.toString().trim()
            val name = etBranchName.text.toString().trim()

            if (code.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Branch Code & Name are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Build parameter map matching backend field names
            val params = mapOf(
                "branch_code" to code,
                "branch_name" to name,
                "branch_head" to etBranchHead.text.toString().trim(),
                "designation" to etDesignation.text.toString().trim(),
                "swift_code" to etSwiftCode.text.toString().trim(),
                "remarks" to etRemarks.text.toString().trim(),
                "land_line" to etLandline.text.toString().trim(),
                "fax" to etFax.text.toString().trim(),
                "mobile" to etMobile.text.toString().trim(),
                "cont_person" to etContactPerson.text.toString().trim(),
                "website" to etWebSite.text.toString().trim(),
                "mail_id" to etMailId.text.toString().trim(),
                "add_1" to etAddress1.text.toString().trim(),
                "add_2" to etAddress2.text.toString().trim(),
                "city" to etCity.text.toString().trim(),
                "state" to etState.text.toString().trim(),
                "country" to etCountry.text.toString().trim(),
                "zip_code" to etZipCode.text.toString().trim()
            )

            // Disable button to prevent double submission
            btnSave.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response: Response<ResponseBody> = RetrofitClient.api.addBranch(params)
                    if (response.isSuccessful) {
                        Toast.makeText(this@AddBranchActivity, "Branch added successfully", Toast.LENGTH_SHORT).show()
                        // Return OK result so that previous screen can refresh
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(this@AddBranchActivity, "Failed: $errorBody", Toast.LENGTH_LONG).show()
                        btnSave.isEnabled = true
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AddBranchActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSave.isEnabled = true
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
