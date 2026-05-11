package com.example.bgls.TransactionMaintenance

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import android.view.LayoutInflater
import android.view.ViewGroup

import com.example.bgls.MainActivity
import android.widget.ImageView

class JournalEntriesViewActivity : AppCompatActivity() {

    // All EditText fields as defined in your layout (you already have them)
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
    private lateinit var progressBar: ProgressBar
    private lateinit var btnView: Button

    private lateinit var relatedEntriesAdapter: RelatedEntriesAdapter

    private lateinit var tvFooterEntryUser: TextView
    private lateinit var tvFooterEntryTime: TextView
    private lateinit var tvFooterModifyUser: TextView
    private lateinit var tvFooterModifyTime: TextView
    private lateinit var tvFooterVerifyUser: TextView
    private lateinit var tvFooterVerifyTime: TextView

    private lateinit var layoutTableContainer: LinearLayout
    private lateinit var rvRelatedEntries: RecyclerView

    private val relatedEntriesList = mutableListOf<JournalEntryItem>()

    private var currentTranId = ""
    private var currentAcctNum = ""
    private var currentPartTranId = ""
    private var currentPartIndex = 1
    private var maxPartIndex = 1
    private var isTableVisible = false



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
            Toast.makeText(this, "Missing parameters", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        // Bind all EditTexts (same as your existing code - keep it)
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
        progressBar = findViewById(R.id.progressBar)
        btnView = findViewById(R.id.btnView)
        
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)

        rvRelatedEntries.layoutManager = LinearLayoutManager(this)
        relatedEntriesAdapter = RelatedEntriesAdapter(relatedEntriesList)
        rvRelatedEntries.adapter = relatedEntriesAdapter

        tvFooterEntryUser = findViewById(R.id.tvEntryUser)
        tvFooterEntryTime = findViewById(R.id.tvEntryTime)
        tvFooterModifyUser = findViewById(R.id.tvModifyUser)
        tvFooterModifyTime = findViewById(R.id.tvModifyTime)
        tvFooterVerifyUser = findViewById(R.id.tvVerifyUser)
        tvFooterVerifyTime = findViewById(R.id.tvVerifyTime)

    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnPrev).setOnClickListener {
            if (currentPartIndex > 1) loadJournalEntry((currentPartIndex - 1).toString())
            else Toast.makeText(this, "First entry", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnNext).setOnClickListener {
            if (currentPartIndex < maxPartIndex) loadJournalEntry((currentPartIndex + 1).toString())
            else Toast.makeText(this, "Last entry", Toast.LENGTH_SHORT).show()
        }
        btnView.setOnClickListener {
            isTableVisible = !isTableVisible
            layoutTableContainer.visibility = if (isTableVisible) View.VISIBLE else View.GONE
            if (isTableVisible && relatedEntriesList.isEmpty()) loadAllLegs()
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
                    Toast.makeText(this@JournalEntriesViewActivity, "Failed to load entry", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("JournalEntries", "API error", e)
                Toast.makeText(this@JournalEntriesViewActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun loadAllLegs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getJournalEntriesListForTran(tranId = currentTranId)
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val legs = result.tableparttran as? List<JournalEntryItem> ?: emptyList()
                    relatedEntriesAdapter.updateData(legs)
                }
            } catch (e: Exception) {
                Log.e("JournalEntries", "Error loading legs", e)
            }
        }
    }

    private fun populateUI(entry: JournalEntryItem) {
        etTranId.setText(entry.tran_id ?: "")
        etPartTranId.setText(entry.part_tran_id?.toString() ?: "")
        etAcctId.setText(entry.acct_num ?: "")
        etAcctName.setText(entry.acct_name ?: "")
        etTranType.setText(entry.tran_type ?: "")
        etPartTranType.setText(entry.part_tran_type ?: "")
        etAcctCurrency.setText(entry.acct_crncy ?: "")
        etTranAmt.setText(formatAmount(entry.tran_amt))
        etTranParticulars.setText(entry.tran_particular ?: "")
        etTranRemarks.setText(entry.tran_remarks ?: "")
        etFlowCode.setText(entry.flow_code ?: "")
        etFlowDate.setText(formatDate(entry.flow_date))
        etTranDate.setText(formatDate(entry.tran_date))
        etValueDate.setText(formatDate(entry.value_date))
        etEntryUser.setText(entry.entry_user ?: "")
        etEntryTime.setText(formatDate(entry.entry_time))
        etTranStatus.setText(entry.tran_status ?: "")
        etDeleted.setText(entry.del_flg ?: "")
        etTranCode.setText(entry.tran_code ?: "")
        etTranRefNo.setText(entry.tran_ref_no ?: "")
        etPartitionType.setText(entry.partition_type ?: "")
        etInstrumentNo.setText(entry.instr_num ?: "")
        etAccountCurrency2.setText(entry.ref_crncy ?: "")
        etRateCode.setText(entry.rate_code ?: "")
        etTranReportCode.setText(entry.tran_rpt_code ?: "")
        etAdditionalDetails.setText(entry.add_details ?: "")
        etPartitionDetails.setText(entry.partition_det ?: "")
        etInstrumentDate.setText(formatDate(entry.instr_date))
        etHomeCurrencyAmount.setText(formatAmount(entry.ref_crncy_amt))
        etRate.setText(formatAmount(entry.rate))
        etPostUser.setText(entry.post_user ?: "")
        etPostTime.setText(formatDate(entry.post_time))
    }

    private fun updatePaginationInfo() {
        tvPageInfo.text = "$currentPartIndex / $maxPartIndex"
        findViewById<Button>(R.id.btnPrev).isEnabled = currentPartIndex > 1
        findViewById<Button>(R.id.btnNext).isEnabled = currentPartIndex < maxPartIndex
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

// Adapter for the related entries table
class RelatedEntriesAdapter(private var entries: List<JournalEntryItem>) :
    RecyclerView.Adapter<RelatedEntriesAdapter.ViewHolder>() {

    fun updateData(newList: List<JournalEntryItem>) {
        entries = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val tvPaTranTy: TextView = view.findViewById(R.id.tvPaTranTy)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: android.widget.RadioButton = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_journal_entry_related, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = entries[position]
        holder.tvTranDate.text = formatDate(item.tran_date)
        holder.tvTranId.text = "${item.tran_id}/${item.part_tran_id}"
        holder.tvPaTranTy.text = item.part_tran_type
        holder.tvCurrency.text = item.acct_crncy
        holder.tvAmount.text = DecimalFormat("#,##0.00").format(item.tran_amt ?: 0.0)
        holder.tvAcctId.text = item.acct_num
        holder.tvAcctName.text = item.acct_name
        holder.tvStatus.text = item.tran_status
        holder.rbSelect.isChecked = false
    }

    override fun getItemCount(): Int = entries.size

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
}