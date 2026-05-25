package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.BglsTransactionAccount
import com.example.bgls.DataModels.BglsTransactionAccountDetailResponse
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import android.widget.ImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionAccountViewActivity : AppCompatActivity() {

    private var accountId: Long = -1L

    // LEFT COLUMN
    private lateinit var etId: EditText
    private lateinit var etGLCode: EditText
    private lateinit var etSchemeCode: EditText
    private lateinit var etGLSHCode: EditText
    private lateinit var etInterestIncome: EditText
    private lateinit var etFeesIncome: EditText
    private lateinit var etCollectionAccount: EditText

    // RIGHT COLUMN
    private lateinit var etProductKey: EditText
    private lateinit var etGLDescription: EditText
    private lateinit var etSchemeDescription: EditText
    private lateinit var etGLSHDescription: EditText
    private lateinit var etInterestReceivable: EditText
    private lateinit var etPenaltyIncome: EditText
    private lateinit var etLoanParkingAccount: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_view)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        accountId = intent.getLongExtra("ID", -1L)
            .takeIf { it != -1L }
            ?: intent.getStringExtra("ID")?.toLongOrNull()
            ?: -1L

        bindViews()

        if (accountId != -1L) {
            loadAccountDetail(accountId)
        } else {
            populateFromIntentExtras()
        }

        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, TransactionAccountModifyActivity::class.java)
            intent.putExtra("ID", accountId.toString())
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startActivity(Intent(this, TransactionAccountAddActivity::class.java))
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun bindViews() {
        etId                 = findViewById(R.id.etId)
        etGLCode             = findViewById(R.id.etGLCode)
        etSchemeCode         = findViewById(R.id.etSchemeCode)
        etGLSHCode           = findViewById(R.id.etGLSHCode)
        etInterestIncome     = findViewById(R.id.etInterestIncome)
        etFeesIncome         = findViewById(R.id.etFeesIncome)
        etCollectionAccount  = findViewById(R.id.etCollectionAccount)
        etProductKey         = findViewById(R.id.etProductKey)
        etGLDescription      = findViewById(R.id.etGLDescription)
        etSchemeDescription  = findViewById(R.id.etSchemeDescription)
        etGLSHDescription    = findViewById(R.id.etGLSHDescription)
        etInterestReceivable = findViewById(R.id.etInterestReceivable)
        etPenaltyIncome      = findViewById(R.id.etPenaltyIncome)
        etLoanParkingAccount = findViewById(R.id.etLoanParkingAccount)
    }

    private fun loadAccountDetail(id: Long) {
        RetrofitClient.api.getTransactionAccountDetail("view", id)
            .enqueue(object : Callback<BglsTransactionAccountDetailResponse> {
                override fun onResponse(
                    call: Call<BglsTransactionAccountDetailResponse>,
                    response: Response<BglsTransactionAccountDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        val account = response.body()?.account
                        if (account != null) {
                            populate(account)
                        } else {
                            Toast.makeText(this@TransactionAccountViewActivity,
                                "No data returned", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@TransactionAccountViewActivity,
                            "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BglsTransactionAccountDetailResponse>, t: Throwable) {
                    Toast.makeText(this@TransactionAccountViewActivity,
                        "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun populate(a: BglsTransactionAccount) {
        etId.setText(a.id?.toString() ?: "")
        etGLCode.setText(a.gl_code ?: "")
        etSchemeCode.setText(a.scheme_code ?: "")
        etGLSHCode.setText(a.glsh_code ?: "")
        etInterestIncome.setText(a.interest_income ?: "")
        etFeesIncome.setText(a.fees_income ?: "")
        etCollectionAccount.setText(a.collection_account ?: "")
        etProductKey.setText(a.product_key ?: "")
        etGLDescription.setText(a.gl_description ?: "")
        etSchemeDescription.setText(a.scheme_description ?: "")
        etGLSHDescription.setText(a.glsh_description ?: "")
        etInterestReceivable.setText(a.interest_receivable ?: "")
        etPenaltyIncome.setText(a.penalty_income ?: "")
        etLoanParkingAccount.setText(a.loan_parking_account ?: "")
    }

    /** Fallback: populate from intent extras if no live ID */
    private fun populateFromIntentExtras() {
        etId.setText(intent.getStringExtra("ID") ?: "")
        etGLCode.setText(intent.getStringExtra("GL_CODE") ?: "")
        etSchemeCode.setText(intent.getStringExtra("SCHEME_CODE") ?: "")
        etGLSHCode.setText(intent.getStringExtra("GLSH_CODE") ?: "")
        etInterestIncome.setText(intent.getStringExtra("INTEREST_INCOME") ?: "")
        etFeesIncome.setText(intent.getStringExtra("FEES_INCOME") ?: "")
        etCollectionAccount.setText(intent.getStringExtra("COLLECTION_ACCOUNT") ?: "")
        etProductKey.setText(intent.getStringExtra("PRODUCT_KEY") ?: "")
        etGLDescription.setText(intent.getStringExtra("GL_DESCRIPTION") ?: "")
        etSchemeDescription.setText(intent.getStringExtra("SCHEME_DESCRIPTION") ?: "")
        etGLSHDescription.setText(intent.getStringExtra("GLSH_DESCRIPTION") ?: "")
        etInterestReceivable.setText(intent.getStringExtra("INTEREST_RECEIVABLE") ?: "")
        etPenaltyIncome.setText(intent.getStringExtra("PENALTY_INCOME") ?: "")
        etLoanParkingAccount.setText(intent.getStringExtra("LOAN_PARKING_ACCOUNT") ?: "")
    }
}
