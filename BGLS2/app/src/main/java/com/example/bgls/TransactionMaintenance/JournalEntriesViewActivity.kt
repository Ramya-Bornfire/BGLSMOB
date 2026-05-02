package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton

data class JournalEntryDetailModel(
    val tranId: String,
    val partTranId: String,
    val acctId: String,
    val acctName: String,
    val tranType: String,
    val partTranType: String,
    val currency: String,
    val amount: String,
    val particulars: String,
    val remarks: String,
    val flowCode: String,
    val flowDate: String,
    val tranDate: String,
    val valueDate: String,
    val entryUser: String,
    val tranStatus: String,
    val deleted: String
)

class JournalEntriesViewActivity : AppCompatActivity() {

    private var currentIndex = 0
    private var entriesList = mutableListOf<JournalEntryDetailModel>()

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
    
    private lateinit var tvPageInfo: TextView
    private lateinit var btnPrev: Button
    private lateinit var btnNext: Button
    
    private lateinit var rvRelatedEntries: RecyclerView
    private lateinit var relatedAdapter: RelatedEntriesAdapter
    private lateinit var layoutTableContainer: android.widget.LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal_entries_view)

        initViews()
        loadMockData()
        setupBottomTable()
        updateDisplay()

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
        
        tvPageInfo = findViewById(R.id.tvPageInfo)
        btnPrev = findViewById(R.id.btnPrev)
        btnNext = findViewById(R.id.btnNext)
        
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
    }

    private fun loadMockData() {
        // As per screenshot: TR8798 has 2 parts
        entriesList.add(JournalEntryDetailModel(
            "TR8798", "1", "LA0019", "PRAKASH", "TRANSFER", "Debit", "SCR", "500,000.00", 
            "LA0019 Loan Disbursement", "LA0019 Loan Disbursement", "DISBT", "27-04-2026", "27-04-2026", "27-04-2026", 
            "EMP04", "ENTERED", "N"
        ))
        entriesList.add(JournalEntryDetailModel(
            "TR8798", "2", "WA0019", "PRAKASH", "TRANSFER", "Credit", "SCR", "500,000.00", 
            "LA0019 Loan Disbursement", "LA0019 Loan Disbursement", "DISBT", "27-04-2026", "27-04-2026", "27-04-2026", 
            "EMP04", "ENTERED", "N"
        ))
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
        
        etTranId.setText(entry.tranId)
        etPartTranId.setText(entry.partTranId)
        etAcctId.setText(entry.acctId)
        etAcctName.setText(entry.acctName)
        etTranType.setText(entry.tranType)
        etPartTranType.setText(entry.partTranType)
        etAcctCurrency.setText(entry.currency)
        etTranAmt.setText(entry.amount)
        etTranParticulars.setText(entry.particulars)
        etTranRemarks.setText(entry.remarks)
        etFlowCode.setText(entry.flowCode)
        etFlowDate.setText(entry.flowDate)
        etTranDate.setText(entry.tranDate)
        etValueDate.setText(entry.valueDate)
        etEntryUser.setText(entry.entryUser)
        etTranStatus.setText(entry.tranStatus)
        etDeleted.setText(entry.deleted)
        
        tvPageInfo.text = "${currentIndex + 1} / ${entriesList.size}"
        
        relatedAdapter.setSelected(currentIndex)
    }

    inner class RelatedEntriesAdapter(
        private val list: List<JournalEntryDetailModel>,
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
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_journal_entry_related, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvTranDate.text = item.tranDate
            holder.tvTranId.text = "${item.tranId}/${item.partTranId}"
            holder.tvPaTranTy.text = item.partTranType
            holder.tvCurrency.text = item.currency
            holder.tvAmount.text = item.amount
            holder.tvAcctId.text = item.acctId
            holder.tvAcctName.text = item.acctName
            holder.tvStatus.text = item.tranStatus
            holder.rbSelect.isChecked = (position == selectedIndex)
            
            holder.itemView.setOnClickListener { onItemClick(position) }
        }

        override fun getItemCount() = list.size
    }
}
