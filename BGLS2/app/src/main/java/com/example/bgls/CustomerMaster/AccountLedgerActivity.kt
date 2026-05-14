package com.example.bgls.CustomerMaster

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.TransactionMaintenance.JournalEntriesViewActivity
import kotlinx.coroutines.launch
import android.widget.ImageView
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.bgls.MainActivity

class AccountLedgerActivity : AppCompatActivity() {

    private val TAG = "AccountLedgerActivity"
    private lateinit var recyclerViewAccountLedger: RecyclerView
    private lateinit var adapter: AccountLedgerAdapter
    
    // Header Views
    private lateinit var etAcctId: EditText
    private lateinit var etAcctName: EditText
    private lateinit var etAcctCcy: EditText
    private lateinit var etAcctBal: EditText
    private lateinit var etGenLed: EditText
    private lateinit var etGlDes: EditText
    private lateinit var etGlSubHead: EditText
    private lateinit var etGlshDes: EditText
    private lateinit var etAccountCurrency: EditText
    private lateinit var etHomeCurrencyBal: EditText
    private lateinit var etAcctOpenDate: EditText
    private lateinit var etAcctCloseDate: EditText
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText
    private lateinit var etAcctStatus: EditText

    private var acctNum: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_ledger)

        acctNum = intent.getStringExtra("acct_num") ?: ""
        
        initViews()

        if (acctNum.isNotEmpty()) {
            fetchAccountLedger(acctNum)
        } else {
            Toast.makeText(this, "No account number provided", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        recyclerViewAccountLedger = findViewById(R.id.recyclerViewAccountLedger)
        recyclerViewAccountLedger.layoutManager = LinearLayoutManager(this)

        etAcctId = findViewById(R.id.etAcctId)
        etAcctName = findViewById(R.id.etAcctName)
        etAcctCcy = findViewById(R.id.etAcctCcy)
        etAcctBal = findViewById(R.id.etAcctBal)
        etGenLed = findViewById(R.id.etGenLed)
        etGlDes = findViewById(R.id.etGlDes)
        etGlSubHead = findViewById(R.id.etGlSubHead)
        etGlshDes = findViewById(R.id.etGlshDes)
        etAccountCurrency = findViewById(R.id.etAccountCurrency)
        etHomeCurrencyBal = findViewById(R.id.etHomeCurrencyBal)
        etAcctOpenDate = findViewById(R.id.etAcctOpenDate)
        etAcctCloseDate = findViewById(R.id.etAcctCloseDate)
        etFromDate = findViewById(R.id.etFromDate)
        etToDate = findViewById(R.id.etToDate)
        etAcctStatus = findViewById(R.id.etAcctStatus)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun fetchAccountLedger(num: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAccountLedger2(acctNum = num)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    populateHeader(data)
                    setupTransactions(data.dataList)
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    Toast.makeText(this@AccountLedgerActivity, "Failed to load ledger", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error", e)
                Toast.makeText(this@AccountLedgerActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateHeader(data: com.example.bgls.DataModels.AccountLedgerViewResponse) {
        val ca = data.chartAccount ?: return
        etAcctId.setText(ca.acctNum ?: "")
        etAcctName.setText(ca.acctName ?: "")
        etAcctCcy.setText(ca.acctCrncy ?: "")
        etAcctBal.setText(formatDecimal(ca.acctBal))
        etGenLed.setText(ca.glCode ?: "")
        etGlDes.setText(ca.glDesc ?: "")
        etGlSubHead.setText(ca.glshCode ?: "")
        etGlshDes.setText(ca.glshDesc ?: "")
        etAccountCurrency.setText(ca.refCrncy ?: "")
        etHomeCurrencyBal.setText(formatDecimal(ca.acctBal))
        
        // Status
        val status = if (ca.acctStatus == "Y" || ca.entityFlg == "Y") "ACTIVE" else "INACTIVE"
        etAcctStatus.setText(status)

        // Open/Close Dates
        etAcctOpenDate.setText(formatBackendDate(ca.acctOpnDate))
        etAcctCloseDate.setText(formatBackendDate(ca.acctClsDate))
        
        // From/To Dates
        etFromDate.setText(formatBackendDate(data.tranDate))
        etToDate.setText(formatBackendDate(data.tranDate))
    }

    private fun setupTransactions(dataList: List<List<Any?>>?) {
        if (dataList == null) return

        val ledgerItems = mutableListOf<LedgerItem>()
        var runningBalance = 0.0

        dataList.forEach { row ->
            if (row.size >= 7) {
                val dateStr = formatBackendDate(row[0])
                val tranId = row[1]?.toString() ?: ""
                val particulars = row[2]?.toString() ?: ""
                val currency = row[3]?.toString() ?: ""
                
                // Index 5 is Credit, Index 6 is Debit as per web template
                val credit = parseDouble(row[5])
                val debit = parseDouble(row[6])
                
                runningBalance = runningBalance + credit - debit
                
                ledgerItems.add(LedgerItem(
                    tranDate = dateStr,
                    tranId = tranId,
                    tranParticulars = particulars,
                    currency = currency,
                    credits = formatDecimal(credit),
                    debits = formatDecimal(debit),
                    balance = formatDecimal(runningBalance)
                ))
            }
        }
        adapter = AccountLedgerAdapter(this, ledgerItems) { ledgerItem ->
            // When transaction ID is clicked
            val combinedId = ledgerItem.tranId  // format: "TRAN_ID/PART_TRAN_ID"
            val parts = combinedId.split("/")
            val tranId = parts.getOrNull(0) ?: ""
            val partTranId = parts.getOrNull(1) ?: ""

            val intent = Intent(this, JournalEntriesViewActivity::class.java).apply {
                putExtra("tran_id", tranId)
                putExtra("part_tran_id", partTranId)
                putExtra("acct_num", acctNum)   // from the intent that opened this activity
            }
            startActivity(intent)
        }
        recyclerViewAccountLedger.adapter = adapter
    }

    private fun parseDouble(value: Any?): Double {
        if (value == null) return 0.0
        return try {
            when (value) {
                is Double -> value
                is Number -> value.toDouble()
                is String -> value.toDouble()
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun formatDecimal(value: Double?): String {
        if (value == null) return "0.00"
        return DecimalFormat("#,##0.00").format(value)
    }

    private fun formatBackendDate(dateObj: Any?): String {
        if (dateObj == null) return ""
        val dateStr = dateObj.toString().trim()
        if (dateStr.isEmpty()) return ""

        // If already in dd-MM-yyyy format, return it
        if (dateStr.matches(Regex("^\\d{2}-\\d{2}-\\d{4}$"))) return dateStr

        return try {
            // Check if it's a timestamp (Long)
            val timestamp = dateStr.toLongOrNull()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                return sdf.format(java.util.Date(timestamp))
            }
            
            // Standard backend formats
            val inputFormats = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "dd-MM-yyyy",
                "EEE MMM dd HH:mm:ss zzz yyyy" // Java Date.toString()
            )
            
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            
            for (format in inputFormats) {
                try {
                    val parser = SimpleDateFormat(format, Locale.US)
                    val date = parser.parse(dateStr)
                    if (date != null) {
                        return outputFormat.format(date)
                    }
                } catch (e: Exception) {}
            }

            // Fallback: If it contains 'T', just take the date part
            if (dateStr.contains("T")) {
                val datePart = dateStr.substringBefore("T")
                if (datePart.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val date = sdf.parse(datePart)
                        if (date != null) return outputFormat.format(date)
                    } catch (e: Exception) {}
                }
            }

            dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}