package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountLedgerViewResponse
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
class JournalEntriesViewActivity : AppCompatActivity() {

    private var currentIndex = 0
    private var entriesList = mutableListOf<JournalEntryItem>()
    private var currentTranId = ""
    private var currentAcctNum = ""

    // UI Views
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
    // Additional fields from layout (already defined in XML)
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

    private lateinit var tvPageInfo: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    private lateinit var rvRelatedEntries: RecyclerView
    private lateinit var relatedAdapter: RelatedEntriesAdapter
    private lateinit var layoutTableContainer: android.widget.LinearLayout
    private lateinit var etEntryTime: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entries_view)

        initViews()
        setupControls()

        currentTranId = intent.getStringExtra("tran_id") ?: ""
        val partTranId = intent.getStringExtra("part_tran_id") ?: ""
        currentAcctNum = intent.getStringExtra("acct_num") ?: ""

        android.util.Log.d("JournalEntries", "tran_id=$currentTranId, part_tran_id=$partTranId, acct_num=$currentAcctNum")

        if (currentTranId.isNotEmpty() && currentAcctNum.isNotEmpty()) {
            android.util.Log.d("JournalEntries", "Calling fetchJournalsFromLedger")
            fetchJournalsFromLedger(currentAcctNum, currentTranId, partTranId)
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

        // Additional fields
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

        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
        etEntryTime = findViewById(R.id.etEntryTime)
        layoutTableContainer.visibility = View.GONE
    }

    private fun setupControls() {
        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateDisplay()
            }
        }

        btnNext.setOnClickListener {
            if (currentIndex < entriesList.size - 1) {
                currentIndex++
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnView).setOnClickListener {
            layoutTableContainer.visibility = View.VISIBLE
        }
    }

    private fun fetchJournalsFromLedger(acctNum: String, tranId: String, requestedPartTranId: String) {
        lifecycleScope.launch {
            try {
                val ledgerResponse = RetrofitClient.api.getAccountLedger2("view", acctNum)
                if (!ledgerResponse.isSuccessful || ledgerResponse.body() == null) {
                    Toast.makeText(this@JournalEntriesViewActivity, "Failed to load ledger data", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ledgerData = ledgerResponse.body()!!
                val allRows = ledgerData.dataList ?: emptyList()
                val chartAccount = ledgerData.chartAccount

                // Collect basic entries (part_tran_id, dateStr)
                val basicEntries = mutableListOf<Pair<String, String>>()
                for (row in allRows) {
                    if (row.size >= 7) {
                        val tranIdFull = row[1]?.toString() ?: ""
                        val parts = tranIdFull.split("/")
                        val rowTranId = parts.getOrNull(0) ?: ""
                        if (rowTranId == tranId) {
                            val partTranId = parts.getOrNull(1) ?: ""
                            val dateObj = row[0]
                            val dateStr = if (dateObj is Date) formatDate(dateObj) else dateObj?.toString() ?: ""
                            basicEntries.add(partTranId to dateStr)
                        }
                    }
                }

                if (basicEntries.isEmpty()) {
                    Toast.makeText(this@JournalEntriesViewActivity, "No entries found for transaction $tranId", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Fetch full details in parallel using coroutineScope
                val fullEntries = coroutineScope {
                    val deferredList = basicEntries.map { (partTranId, _) ->
                        async {
                            try {
                                RetrofitClient.api.getTransactionDetails(tranId, partTranId)
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }
                    val responses = deferredList.awaitAll()

                    val result = mutableListOf<JournalEntryItem>()
                    for ((index, response) in responses.withIndex()) {
                        if (response != null && response.isSuccessful && response.body() != null) {
                            val body = response.body()
                            if (body?.ledgervalues != null) {
                                result.add(body.ledgervalues)
                            }

                        } else {
                            // Create fallback entry with all required fields
                            val (partTranId, dateStr) = basicEntries[index]
                            val fallbackItem = createFallbackEntry(
                                tranId = tranId,
                                partTranId = partTranId,
                                acctNum = acctNum,
                                acctName = chartAccount?.acctName ?: "",
                                tranDate = dateStr,
                                acctCrncy = chartAccount?.acctCrncy ?: "",
                                entryUser = chartAccount?.entryUser ?: "",
                                postUser = chartAccount?.authUser ?: "",
                                tranStatus = if (chartAccount?.acctStatus == "Active") "ACTIVE" else ""
                            )
                            result.add(fallbackItem)
                        }
                    }
                    result
                }

                // Sort by part_tran_id numerically
                fullEntries.sortBy { it.part_tran_id?.toIntOrNull() ?: 0 }

                if (fullEntries.isNotEmpty()) {
                    entriesList.clear()
                    entriesList.addAll(fullEntries)

                    val requestedIndex = entriesList.indexOfFirst { it.part_tran_id == requestedPartTranId }
                    currentIndex = if (requestedIndex != -1) requestedIndex else 0

                    setupBottomTable()
                    updateDisplay()
                } else {
                    Toast.makeText(this@JournalEntriesViewActivity, "No detailed entries found", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("JournalEntries", "Error", e)
                Toast.makeText(this@JournalEntriesViewActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Helper to create a fallback JournalEntryItem with all required fields
    private fun createFallbackEntry(
        tranId: String,
        partTranId: String,
        acctNum: String,
        acctName: String,
        tranDate: String,
        acctCrncy: String,
        entryUser: String,
        postUser: String,
        tranStatus: String
    ): JournalEntryItem {
        return JournalEntryItem(
            tran_id = tranId,
            part_tran_id = partTranId,
            acct_num = acctNum,
            acct_name = acctName,
            tran_type = "",
            part_tran_type = "",
            acct_crncy = acctCrncy,
            tran_amt = 0.0,
            tran_particular = "",
            tran_remarks = "",
            flow_code = "",
            flow_date = tranDate,
            tran_date = tranDate,
            value_date = tranDate,
            entry_user = entryUser,
            post_user = postUser,
            tran_status = tranStatus,
            del_flg = "",
            tran_code = "",
            tran_rpt_code = "",
            tran_ref_no = "",
            add_details = "",
            partition_type = "",
            partition_det = "",
            instr_num = "",
            instr_date = "",
            ref_crncy = acctCrncy,
            ref_crncy_amt = 0.0,
            rate_code = "",
            rate = 0.0,
            modify_user = "",
            modify_time = "",
            srl_no = ""
        )
    }

    private fun parseDouble(value: Any?): Double {
        if (value == null) return 0.0
        return try {
            when (value) {
                is Double -> value
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        } catch (e: Exception) {
            0.0
        }
    }

    private fun setupBottomTable() {
        rvRelatedEntries.layoutManager = LinearLayoutManager(this)
        relatedAdapter = RelatedEntriesAdapter(entriesList) { index ->
            currentIndex = index
            updateDisplay()
        }
        rvRelatedEntries.adapter = relatedAdapter
    }

    private fun updateDisplay() {
        val entry = entriesList[currentIndex]

        // Column 1 fields
        etTranId.setText(entry.tran_id ?: "")
        etAcctId.setText(entry.acct_num ?: "")
        etTranType.setText(entry.tran_type ?: "")
        etAcctCurrency.setText(entry.acct_crncy ?: "")
        etTranParticulars.setText(entry.tran_particular ?: "")
        etFlowCode.setText(entry.flow_code ?: "")
        etTranDate.setText(entry.tran_date ?: "")
        etTranCode.setText(entry.tran_code ?: "")
        etTranRefNo.setText(entry.tran_ref_no ?: "")
        etPartitionType.setText(entry.partition_type ?: "")
        etInstrumentNo.setText(entry.instr_num ?: "")
        etAccountCurrency2.setText(entry.ref_crncy ?: "")
        etRateCode.setText(entry.rate_code ?: "")
        etEntryUser.setText(entry.entry_user ?: "")
        etEntryTime.setText(entry.tran_date ?: "")   // Use transaction date as entry time fallback
        etTranStatus.setText(entry.tran_status ?: "")

        // Column 2 fields
        etPartTranId.setText(entry.part_tran_id ?: "")
        etAcctName.setText(entry.acct_name ?: "")
        etPartTranType.setText(entry.part_tran_type ?: "")
        etTranAmt.setText(formatAmount(entry.tran_amt))
        etTranRemarks.setText(entry.tran_remarks ?: "")
        etFlowDate.setText(entry.flow_date ?: "")
        etValueDate.setText(entry.value_date ?: "")
        etTranReportCode.setText(entry.tran_rpt_code ?: "")
        etAdditionalDetails.setText(entry.add_details ?: "")
        etPartitionDetails.setText(entry.partition_det ?: "")
        etInstrumentDate.setText(entry.instr_date ?: "")
        etHomeCurrencyAmount.setText(formatAmount(entry.ref_crncy_amt))
        etRate.setText(formatAmount(entry.rate))
        etPostUser.setText(entry.post_user ?: "")
        etPostTime.setText(entry.modify_time ?: "")
        etDeleted.setText(entry.del_flg ?: "")

        tvPageInfo.text = "${currentIndex + 1} / ${entriesList.size}"
        relatedAdapter.setSelected(currentIndex)
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val formats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
            )
            for (format in formats) {
                try {
                    val date = format.parse(dateStr)
                    if (date != null) {
                        return SimpleDateFormat("dd-MM-yyyy", Locale.US).format(date)
                    }
                } catch (_: Exception) { }
            }
            dateStr
        } catch (_: Exception) {
            dateStr
        }
    }

    private fun formatAmount(amount: Double?): String {
        if (amount == null) return ""
        return java.text.DecimalFormat("#,##0.00").format(amount)
    }

    // Inner adapter (unchanged)
    inner class RelatedEntriesAdapter(
        private val list: List<JournalEntryItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<RelatedEntriesAdapter.ViewHolder>() {

        private var selectedIndex = 0

        fun setSelected(index: Int) {
            selectedIndex = index
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
            val tvTranId: TextView = view.findViewById(R.id.tvTranId)
            val tvPaTranTy: TextView = view.findViewById(R.id.tvPaTranTy)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
            val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_journal_entry_related, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvTranDate.text = formatDate(item.tran_date)
            holder.tvTranId.text = "${item.tran_id}/${item.part_tran_id}"
            holder.tvPaTranTy.text = item.part_tran_type ?: ""
            holder.tvCurrency.text = item.acct_crncy ?: ""
            holder.tvAmount.text = formatAmount(item.tran_amt)
            holder.tvAcctId.text = item.acct_num ?: ""
            holder.tvAcctName.text = item.acct_name ?: ""
            holder.tvStatus.text = item.tran_status ?: ""
            holder.rbSelect.isChecked = (position == selectedIndex)

            holder.itemView.setOnClickListener { onItemClick(position) }
        }

        override fun getItemCount() = list.size
    }
    private fun formatDate(date: Date?): String {
        if (date == null) return ""
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return sdf.format(date)
    }
}