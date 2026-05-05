package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class JournalEntriesViewActivity : AppCompatActivity() {

    private lateinit var etTranId: EditText
    private lateinit var etPartTranId: EditText
    private lateinit var etAcctId: EditText
    private lateinit var etAcctName: EditText
    private lateinit var etTranType: EditText
    private lateinit var etPartTranType: EditText
    private lateinit var etAcctCurrency: EditText
    private lateinit var etTranAmt: EditText
    private lateinit var etTranParticulars: EditText
    private lateinit var etTranRemarks: EditText
    private lateinit var etFlowCode: EditText
    private lateinit var etFlowDate: EditText
    private lateinit var etTranDate: EditText
    private lateinit var etValueDate: EditText
    private lateinit var etEntryUser: EditText
    private lateinit var etTranStatus: EditText
    private lateinit var etDeleted: EditText
    private lateinit var etTranCode: EditText
    private lateinit var etTranRefNo: EditText
    private lateinit var etPartitionType: EditText
    private lateinit var etInstrumentNo: EditText
    private lateinit var etAccountCurrency2: EditText
    private lateinit var etRateCode: EditText
    private lateinit var etTranReportCode: EditText
    private lateinit var etAdditionalDetails: EditText
    private lateinit var etPartitionDetails: EditText
    private lateinit var etInstrumentDate: EditText
    private lateinit var etHomeCurrencyAmount: EditText
    private lateinit var etRate: EditText
    private lateinit var etPostUser: EditText
    private lateinit var etPostTime: EditText
    private lateinit var etEntryTime: EditText

    private lateinit var tvPageInfo: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar

    private var currentTranId = ""
    private var currentAcctNum = ""
    private var currentPartTranId = ""
    private var currentPartIndex = 1
    private var maxPartIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entries_view)

        initViews()
        setupButtons()

        currentTranId = intent.getStringExtra("tran_id") ?: ""
        currentPartTranId = intent.getStringExtra("part_tran_id") ?: "1"
        currentAcctNum = intent.getStringExtra("acct_num") ?: ""

        if (currentTranId.isNotEmpty() && currentAcctNum.isNotEmpty()) {
            loadJournalEntry(currentPartTranId)
        } else {
            Toast.makeText(this, "Missing transaction parameters", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        etTranId = findViewById(R.id.etTranId)
        etPartTranId = findViewById(R.id.etPartTranId)
        etAcctId = findViewById(R.id.etAcctId)
        etAcctName = findViewById(R.id.etAcctName)
        etTranType = findViewById(R.id.etTranType)
        etPartTranType = findViewById(R.id.etPartTranType)
        etAcctCurrency = findViewById(R.id.etAcctCurrency)
        etTranAmt = findViewById(R.id.etTranAmt)
        etTranParticulars = findViewById(R.id.etTranParticulars)
        etTranRemarks = findViewById(R.id.etTranRemarks)
        etFlowCode = findViewById(R.id.etFlowCode)
        etFlowDate = findViewById(R.id.etFlowDate)
        etTranDate = findViewById(R.id.etTranDate)
        etValueDate = findViewById(R.id.etValueDate)
        etEntryUser = findViewById(R.id.etEntryUser)
        etTranStatus = findViewById(R.id.etTranStatus)
        etDeleted = findViewById(R.id.etDeleted)
        etTranCode = findViewById(R.id.etTranCode)
        etTranRefNo = findViewById(R.id.etTranRefNo)
        etPartitionType = findViewById(R.id.etPartitionType)
        etInstrumentNo = findViewById(R.id.etInstrumentNo)
        etAccountCurrency2 = findViewById(R.id.etAccountCurrency2)
        etRateCode = findViewById(R.id.etRateCode)
        etTranReportCode = findViewById(R.id.etTranReportCode)
        etAdditionalDetails = findViewById(R.id.etAdditionalDetails)
        etPartitionDetails = findViewById(R.id.etPartitionDetails)
        etInstrumentDate = findViewById(R.id.etInstrumentDate)
        etHomeCurrencyAmount = findViewById(R.id.etHomeCurrencyAmount)
        etRate = findViewById(R.id.etRate)
        etPostUser = findViewById(R.id.etPostUser)
        etPostTime = findViewById(R.id.etPostTime)
        etEntryTime = findViewById(R.id.etEntryTime)

        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupButtons() {
        btnPrev.setOnClickListener {
            if (currentPartIndex > 1) {
                loadJournalEntry((currentPartIndex - 1).toString())
            } else {
                Toast.makeText(this, "This is the first entry", Toast.LENGTH_SHORT).show()
            }
        }

        btnNext.setOnClickListener {
            if (currentPartIndex < maxPartIndex) {
                loadJournalEntry((currentPartIndex + 1).toString())
            } else {
                Toast.makeText(this, "This is the last entry", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadJournalEntry(partTranId: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntryView(
                    formmode = "view",
                    tranId = currentTranId,
                    partTranId = partTranId,
                    acctNum = currentAcctNum
                )
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    result.ledgervalues?.let { populateUI(it) }
                    currentPartIndex = result.currentPartTran ?: 1
                    maxPartIndex = result.maxPartTran ?: 1
                    updatePaginationInfo()
                } else {
                    Toast.makeText(this@JournalEntriesViewActivity,
                        "Failed to load entry: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JournalEntries", "API error", e)
                Toast.makeText(this@JournalEntriesViewActivity,
                    "Network error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun populateUI(entry: JournalEntryItem) {
        // Column 1
        etTranId.setText(entry.tran_id ?: "")
        etAcctId.setText(entry.acct_num ?: "")
        etTranType.setText(entry.tran_type ?: "")
        etAcctCurrency.setText(entry.acct_crncy ?: "")
        etTranParticulars.setText(entry.tran_particular ?: "")
        etFlowCode.setText(entry.flow_code ?: "")
        etTranDate.setText(formatDate(entry.tran_date))
        etTranCode.setText(entry.tran_code ?: "")
        etTranRefNo.setText(entry.tran_ref_no ?: "")
        etPartitionType.setText(entry.partition_type ?: "")
        etInstrumentNo.setText(entry.instr_num ?: "")
        etAccountCurrency2.setText(entry.ref_crncy ?: "")
        etRateCode.setText(entry.rate_code ?: "")
        etEntryUser.setText(entry.entry_user ?: "")
        etEntryTime.setText(formatDate(entry.entry_time))
        etTranStatus.setText(entry.tran_status ?: "")

        // Column 2
        etPartTranId.setText(entry.part_tran_id ?: "")
        etAcctName.setText(entry.acct_name ?: "")
        etPartTranType.setText(entry.part_tran_type ?: "")
        etTranAmt.setText(formatAmount(entry.tran_amt))
        etTranRemarks.setText(entry.tran_remarks ?: "")
        etFlowDate.setText(formatDate(entry.flow_date))
        etValueDate.setText(formatDate(entry.value_date))
        etTranReportCode.setText(entry.tran_rpt_code ?: "")
        etAdditionalDetails.setText(entry.add_details ?: "")
        etPartitionDetails.setText(entry.partition_det ?: "")
        etInstrumentDate.setText(formatDate(entry.instr_date))
        etHomeCurrencyAmount.setText(formatAmount(entry.ref_crncy_amt))
        etRate.setText(formatAmount(entry.rate))
        etPostUser.setText(entry.post_user ?: "")
        etPostTime.setText(formatDate(entry.post_time))
        etDeleted.setText(entry.del_flg ?: "")
    }

    private fun updatePaginationInfo() {
        tvPageInfo.text = "$currentPartIndex / $maxPartIndex"
        btnPrev.isEnabled = currentPartIndex > 1
        btnNext.isEnabled = currentPartIndex < maxPartIndex
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val epoch = dateString.toLongOrNull()
            if (epoch != null) {
                val date = Date(epoch)
                SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
            } else {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val date = inputFormat.parse(dateString)
                date?.let { outputFormat.format(it) } ?: dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    private fun formatAmount(amount: Double?): String {
        if (amount == null) return ""
        return DecimalFormat("#,##0.00").format(amount)
    }
}