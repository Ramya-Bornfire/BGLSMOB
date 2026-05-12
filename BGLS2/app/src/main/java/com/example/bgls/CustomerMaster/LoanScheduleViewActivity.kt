package com.example.bgls.CustomerMaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanSchedule
import com.example.bgls.DataModels.LoanScheduleViewResponse
import com.example.bgls.LoanSchedule.LoanScheduleActivityAdapter
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import com.example.bgls.MainActivity
import android.widget.ImageView

class LoanScheduleViewActivity : AppCompatActivity() {

    private lateinit var recyclerViewLoanSchedule: RecyclerView
    private lateinit var adapter: LoanScheduleActivityAdapter
    private lateinit var btnAccount: Button
    private lateinit var btnLedger: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var etCustomerId: EditText
    private lateinit var etCustomerName: EditText
    private lateinit var etDateOfLoan: EditText
    private lateinit var etAccountId: EditText
    private lateinit var etAccountName: EditText
    private lateinit var etLoanAmount: EditText
    private lateinit var etCurrency: EditText
    private lateinit var etOutstandingBalance: EditText
    private lateinit var etArrearsAge: EditText
    private lateinit var etDemand: EditText
    private lateinit var etCollection: EditText
    private lateinit var etArrears: EditText

    private val decimalFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private var loanId: String = ""
    private var holderKey: String = ""
    private var encodedKey: String = ""
    private var branchKey: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_schedule_view)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val hIntent = Intent(this, MainActivity::class.java)
            hIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(hIntent)
        }

        loanId = intent.getStringExtra("loanId") ?: ""
        holderKey = intent.getStringExtra("holder_key") ?: ""
        encodedKey = intent.getStringExtra("encoded_key") ?: ""
        branchKey = intent.getStringExtra("branchKey") ?: ""
        initViews()
        setupButtons()
        fetchLoanScheduleDetails()
    }

    private fun initViews() {
        btnAccount = findViewById(R.id.btnAccount)
        btnLedger = findViewById(R.id.btnLedger)
        progressBar = findViewById(R.id.progressBar)
        recyclerViewLoanSchedule = findViewById(R.id.recyclerViewLoanSchedule)
        recyclerViewLoanSchedule.layoutManager = LinearLayoutManager(this)

        etCustomerId = findViewById(R.id.etCustomerId)
        etCustomerName = findViewById(R.id.etCustomerName)
        etDateOfLoan = findViewById(R.id.etDateOfLoan)
        etAccountId = findViewById(R.id.etAccountId)
        etAccountName = findViewById(R.id.etAccountName)
        etLoanAmount = findViewById(R.id.etLoanAmount)
        etCurrency = findViewById(R.id.etCurrency)
        etOutstandingBalance = findViewById(R.id.etOutstandingBalance)
        etArrearsAge = findViewById(R.id.etArrearsAge)
        etDemand = findViewById(R.id.etDemand)
        etCollection = findViewById(R.id.etCollection)
        etArrears = findViewById(R.id.etArrears)
    }

    private fun setupButtons() {
        val loanId = intent.getStringExtra("loanId") ?: ""
        val holderKey = intent.getStringExtra("holder_key") ?: ""

        btnAccount.setOnClickListener {
            val intent = Intent(this, LoanMasterViewActivity::class.java).apply {
                putExtra("loanId", loanId)           // correct key for LoanMasterViewActivity
                putExtra("holderKey", holderKey)     // correct key
                putExtra("branchKey", branchKey)     // correct key
            }
            startActivity(intent)
        }

        btnLedger.setOnClickListener {
            val intent = Intent(this, AccountLedgerActivity::class.java)
            intent.putExtra("acct_num", etAccountId.text.toString())
            startActivity(intent)
        }
    }

    private fun fetchLoanScheduleDetails() {
        val loanId = intent.getStringExtra("loanId") ?: run {
            Toast.makeText(this, "Loan ID missing", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("LoanSchedule", "Calling API with loanId=$loanId, holderKey=$holderKey, encodedKey=$encodedKey")
        val holderKey = intent.getStringExtra("holder_key") ?: ""
        val encodedKey = intent.getStringExtra("encoded_key") ?: ""

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getLoanScheduleView(
                    id = loanId,
                    holderKey = holderKey,
                    encodedKey = encodedKey
                )
                if (response.isSuccessful) {
                    response.body()?.let { populateData(it) }
                } else {
                    Toast.makeText(this@LoanScheduleViewActivity,
                        "Error: ${response.code()} ${response.message()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoanScheduleViewActivity,
                    "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun populateData(data: LoanScheduleViewResponse) {
        // Handle loanDetails: could be List<List<Any?>> or List<Any?>
        val loanDetailsRow: List<Any?> = when (val details = data.loanDetails) {
            null -> emptyList()
            is List<*> -> {
                val first = details.firstOrNull()
                if (first is List<*>) {
                    first as List<Any?>
                } else {
                    details as List<Any?>
                }
            }
            else -> emptyList()
        }

        if (loanDetailsRow.isNotEmpty()) {
            etCustomerId.setText(getSafeString(loanDetailsRow, 0))
            etCustomerName.setText(getSafeString(loanDetailsRow, 20))
            etDateOfLoan.setText(formatDate(getSafeAny(loanDetailsRow, 3)))
            etAccountId.setText(getSafeString(loanDetailsRow, 4))
            etAccountName.setText(getSafeString(loanDetailsRow, 9))
            etLoanAmount.setText(formatAmount(getSafeAny(loanDetailsRow, 8)))
            etCurrency.setText(getSafeString(loanDetailsRow, 10))
            etOutstandingBalance.setText(formatAmount(getSafeAny(loanDetailsRow, 11)))
            etArrearsAge.setText(getSafeString(loanDetailsRow, 16))
            etDemand.setText(formatAmount(getSafeAny(loanDetailsRow, 17)))
            etCollection.setText(formatAmount(getSafeAny(loanDetailsRow, 18)))
            etArrears.setText(formatAmount(getSafeAny(loanDetailsRow, 19)))
        } else {
            clearLoanDetailsFields()
        }

        val dues = data.dues ?: emptyList()
        val scheduleItems = dues.mapNotNull { row ->
            try {
                LoanSchedule(
                    dueDate = formatDate(getSafeAny(row, 1)),
                    principalExpenses = formatAmount(getSafeAny(row, 3)),
                    interestExpenses = formatAmount(getSafeAny(row, 6)),
                    feeExpenses = formatAmount(getSafeAny(row, 9)),
                    penaltyExpenses = formatAmount(getSafeAny(row, 12)),
                    repaidDate = formatDate(getSafeAny(row, 2)),
                    principalPaid = formatAmount(getSafeAny(row, 4)),
                    interestPaid = formatAmount(getSafeAny(row, 7)),
                    feePaid = formatAmount(getSafeAny(row, 10)),
                    penaltyPaid = formatAmount(getSafeAny(row, 13)),
                    totalDues = decimalFormat.format(calculateTotalDues(row))
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        adapter = LoanScheduleActivityAdapter(this, scheduleItems)
        recyclerViewLoanSchedule.adapter = adapter
    }

    private fun calculateTotalDues(row: List<Any?>): Double {
        val pExp = getSafeDouble(row, 3)
        val pPaid = getSafeDouble(row, 4)
        val iExp = getSafeDouble(row, 6)
        val iPaid = getSafeDouble(row, 7)
        val fExp = getSafeDouble(row, 9)
        val fPaid = getSafeDouble(row, 10)
        val penExp = getSafeDouble(row, 12)
        val penPaid = getSafeDouble(row, 13)

        return max(0.0, pExp - pPaid) +
                max(0.0, iExp - iPaid) +
                max(0.0, fExp - fPaid) +
                max(0.0, penExp - penPaid)
    }

    private fun getSafeAny(row: List<Any?>, index: Int): Any? {
        return if (index < row.size) row[index] else null
    }

    private fun getSafeString(row: List<Any?>, index: Int): String {
        return getSafeAny(row, index)?.toString() ?: ""
    }

    private fun getSafeDouble(row: List<Any?>, index: Int): Double {
        val value = getSafeAny(row, index)
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.replace(",", "").toDoubleOrNull() ?: 0.0
            else -> 0.0
        }
    }

    private fun formatAmount(amount: Any?): String {
        return try {
            val value = when (amount) {
                is Number -> amount.toDouble()
                is String -> amount.replace(",", "").toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            decimalFormat.format(value)
        } catch (e: Exception) {
            "0.00"
        }
    }

    private fun formatDate(dateObj: Any?): String {
        if (dateObj == null) return ""
        return try {
            when (dateObj) {
                is Long -> dateFormat.format(Date(dateObj))
                is String -> dateObj.toLongOrNull()?.let { dateFormat.format(Date(it)) } ?: dateObj
                is Date -> dateFormat.format(dateObj)
                else -> dateObj.toString()
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun clearLoanDetailsFields() {
        etCustomerId.setText("")
        etCustomerName.setText("")
        etDateOfLoan.setText("")
        etAccountId.setText("")
        etAccountName.setText("")
        etLoanAmount.setText("")
        etCurrency.setText("")
        etOutstandingBalance.setText("")
        etArrearsAge.setText("")
        etDemand.setText("")
        etCollection.setText("")
        etArrears.setText("")
    }

    fun onHomeClick(view: View) {
        val intent = Intent(this, com.example.bgls.MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    fun onBackClick(view: View) {
        onBackPressed()
    }
}