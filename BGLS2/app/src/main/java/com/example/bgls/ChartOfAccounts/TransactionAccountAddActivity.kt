package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.BglsTransactionAccount
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import android.widget.ImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionAccountAddActivity : AppCompatActivity() {

    // Add layout EditTexts (no IDs) in document order:
    // Left column: Product Key(0), GL Description(1), Scheme Description(2),
    //              GLSH Description(3), Interest Receivable(4), Penalty Income(5), Loan Parking Account(6)
    // Right column: GL Code(7), Scheme Code(8), GLSH Code(9),
    //               Interest Income(10), Fees Income(11), Collection Account(12)
    private var allEditTexts = listOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_add)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        allEditTexts = getAllEditTexts(findViewById(android.R.id.content))

        val btnSubmitAddTransaction = findViewById<Button>(R.id.btnSubmitAddTransaction)
        btnSubmitAddTransaction.setOnClickListener {
            submitAdd()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun submitAdd() {
        if (allEditTexts.size < 13) {
            Toast.makeText(this, "Form not ready", Toast.LENGTH_SHORT).show()
            return
        }

        // Map fields in order they appear in the add layout
        // Left column: Product Key, GL Description, Scheme Description, GLSH Description,
        //              Interest Receivable, Penalty Income, Loan Parking Account
        // Right column: GL Code, Scheme Code, GLSH Code, Interest Income, Fees Income, Collection Account
        val product_key         = allEditTexts[0].text.toString().trim()
        val gl_description      = allEditTexts[1].text.toString().trim()
        val scheme_description  = allEditTexts[2].text.toString().trim()
        val glsh_description    = allEditTexts[3].text.toString().trim()
        val interest_receivable = allEditTexts[4].text.toString().trim()
        val penalty_income      = allEditTexts[5].text.toString().trim()
        val loan_parking_account = allEditTexts[6].text.toString().trim()
        val gl_code             = allEditTexts[7].text.toString().trim()
        val scheme_code         = allEditTexts[8].text.toString().trim()
        val glsh_code           = allEditTexts[9].text.toString().trim()
        val interest_income     = allEditTexts[10].text.toString().trim()
        val fees_income         = allEditTexts[11].text.toString().trim()
        val collection_account  = allEditTexts[12].text.toString().trim()

        val account = BglsTransactionAccount(
            gl_code             = gl_code,
            scheme_code         = scheme_code,
            glsh_code           = glsh_code,
            interest_income     = interest_income,
            fees_income         = fees_income,
            collection_account  = collection_account,
            product_key         = product_key,
            gl_description      = gl_description,
            scheme_description  = scheme_description,
            glsh_description    = glsh_description,
            interest_receivable = interest_receivable,
            penalty_income      = penalty_income,
            loan_parking_account = loan_parking_account
        )

        RetrofitClient.api.addTransactionAccount(account)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TransactionAccountAddActivity,
                            "Added Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TransactionAccountAddActivity,
                            "Error: ${response.code()} - ${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@TransactionAccountAddActivity,
                        "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getAllEditTexts(view: View): List<EditText> {
        val list = mutableListOf<EditText>()
        if (view is EditText) {
            list.add(view)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) list.addAll(getAllEditTexts(view.getChildAt(i)))
        }
        return list
    }
}
