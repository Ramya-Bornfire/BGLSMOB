package com.example.bgls.ChartOfAccounts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.BglsTransactionAccount
import com.example.bgls.DataModels.BglsTransactionAccountDetailResponse
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import android.widget.ImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionAccountModifyActivity : AppCompatActivity() {

    private var accountId: Long = -1L

    // All editable fields in layout order (no IDs in modify layout, so we use index)
    // Left column: Id(0), GL Code(1), Scheme Code(2), GLSH Code(3),
    //              Interest Income(4), Fees Income(5), Collection Account(6)
    // Right column: Product Key(7), GL Description(8), Scheme Description(9),
    //               GLSH Description(10), Interest Receivable(11),
    //               Penalty Income(12), Loan Parking Account(13)
    private var allEditTexts = listOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_account_modify)

        accountId = intent.getStringExtra("ID")?.toLongOrNull() ?: -1L

        // Collect all EditTexts in document order
        allEditTexts = getAllEditTexts(findViewById(android.R.id.content))

        if (accountId != -1L) {
            loadCurrentData(accountId)
        }

        findViewById<Button>(R.id.btnSubmitModify).setOnClickListener {
            submitUpdate()
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun loadCurrentData(id: Long) {
        RetrofitClient.api.getTransactionAccountDetail("modify", id)
            .enqueue(object : Callback<BglsTransactionAccountDetailResponse> {
                override fun onResponse(
                    call: Call<BglsTransactionAccountDetailResponse>,
                    response: Response<BglsTransactionAccountDetailResponse>
                ) {
                    if (response.isSuccessful) {
                        val a = response.body()?.account
                        if (a != null && allEditTexts.size >= 14) {
                            allEditTexts[0].setText(a.id?.toString() ?: "")
                            allEditTexts[1].setText(a.gl_code ?: "")
                            allEditTexts[2].setText(a.scheme_code ?: "")
                            allEditTexts[3].setText(a.glsh_code ?: "")
                            allEditTexts[4].setText(a.interest_income ?: "")
                            allEditTexts[5].setText(a.fees_income ?: "")
                            allEditTexts[6].setText(a.collection_account ?: "")
                            allEditTexts[7].setText(a.product_key ?: "")
                            allEditTexts[8].setText(a.gl_description ?: "")
                            allEditTexts[9].setText(a.scheme_description ?: "")
                            allEditTexts[10].setText(a.glsh_description ?: "")
                            allEditTexts[11].setText(a.interest_receivable ?: "")
                            allEditTexts[12].setText(a.penalty_income ?: "")
                            allEditTexts[13].setText(a.loan_parking_account ?: "")
                        }
                    } else {
                        Toast.makeText(this@TransactionAccountModifyActivity,
                            "Failed to load: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BglsTransactionAccountDetailResponse>, t: Throwable) {
                    Toast.makeText(this@TransactionAccountModifyActivity,
                        "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun submitUpdate() {
        if (allEditTexts.size < 14) {
            Toast.makeText(this, "Form not ready", Toast.LENGTH_SHORT).show()
            return
        }

        val account = BglsTransactionAccount(
            id                  = accountId,
            gl_code             = allEditTexts[1].text.toString().trim(),
            scheme_code         = allEditTexts[2].text.toString().trim(),
            glsh_code           = allEditTexts[3].text.toString().trim(),
            interest_income     = allEditTexts[4].text.toString().trim(),
            fees_income         = allEditTexts[5].text.toString().trim(),
            collection_account  = allEditTexts[6].text.toString().trim(),
            product_key         = allEditTexts[7].text.toString().trim(),
            gl_description      = allEditTexts[8].text.toString().trim(),
            scheme_description  = allEditTexts[9].text.toString().trim(),
            glsh_description    = allEditTexts[10].text.toString().trim(),
            interest_receivable = allEditTexts[11].text.toString().trim(),
            penalty_income      = allEditTexts[12].text.toString().trim(),
            loan_parking_account = allEditTexts[13].text.toString().trim()
        )

        RetrofitClient.api.updateTransactionAccount(account)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@TransactionAccountModifyActivity,
                            "Modified Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@TransactionAccountModifyActivity,
                            "Error: ${response.code()} - ${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@TransactionAccountModifyActivity,
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
