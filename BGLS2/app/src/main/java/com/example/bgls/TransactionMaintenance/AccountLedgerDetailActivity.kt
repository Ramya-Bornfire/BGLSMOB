package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.JournalEntryDetailModel
import com.example.bgls.R


class AccountLedgerDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_ledger_detail)

        initViews()
        setupListeners()
        loadDataFromIntent()
    }

    private lateinit var tvPage: TextView
    private lateinit var layoutTableContainer: android.widget.LinearLayout
    private lateinit var rvRelatedEntries: RecyclerView
    private lateinit var relatedAdapter: RelatedEntriesAdapter
    private var entriesList = mutableListOf<JournalEntryDetailModel>()
    private var currentIndex = 0

    private fun initViews() {
        tvPage = findViewById(R.id.tvDetPage)
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)
        
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        rvRelatedEntries.layoutManager = LinearLayoutManager(this)
        relatedAdapter = RelatedEntriesAdapter(entriesList) { index ->
            currentIndex = index
            updateDisplayFromItem(entriesList[index])
            relatedAdapter.setSelected(index)
            tvPage.text = "${index + 1} / ${entriesList.size}"
        }
        rvRelatedEntries.adapter = relatedAdapter
    }

    private fun setupListeners() {
        findViewById<Button>(R.id.btnDetView).setOnClickListener {
            loadTableData()
            layoutTableContainer.visibility = View.VISIBLE
        }
        findViewById<Button>(R.id.btnDetValidate).setOnClickListener {
            // Placeholder for validate logic
        }
        findViewById<Button>(R.id.btnDetPost).setOnClickListener {
            // Placeholder for post logic
        }
        findViewById<Button>(R.id.btnDetPrevious).setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateDisplayFromItem(entriesList[currentIndex])
                relatedAdapter.setSelected(currentIndex)
                tvPage.text = "${currentIndex + 1} / ${entriesList.size}"
            }
        }
        findViewById<Button>(R.id.btnDetNext).setOnClickListener {
            if (currentIndex < entriesList.size - 1) {
                currentIndex++
                updateDisplayFromItem(entriesList[currentIndex])
                relatedAdapter.setSelected(currentIndex)
                tvPage.text = "${currentIndex + 1} / ${entriesList.size}"
            }
        }
    }

    private fun loadTableData() {
        entriesList.clear()
        // Mocking data based on screenshot
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
        relatedAdapter.notifyDataSetChanged()
        tvPage.text = "1 / ${entriesList.size}"
    }

    private fun updateDisplayFromItem(item: JournalEntryDetailModel) {
        findViewById<TextView>(R.id.tvDetTranId).text = item.tranId
        findViewById<TextView>(R.id.tvDetPartTranId).text = item.partTranId
        findViewById<TextView>(R.id.tvDetAcctId).text = item.acctId
        findViewById<TextView>(R.id.tvDetAcctName).text = item.acctName
        findViewById<TextView>(R.id.tvDetTranAmt).text = item.amount
        findViewById<TextView>(R.id.tvDetTranDate).text = item.tranDate
        findViewById<TextView>(R.id.tvDetPartTranType).text = item.partTranType
        findViewById<TextView>(R.id.tvDetAcctCcy).text = item.currency
        findViewById<TextView>(R.id.tvDetTranParticulars).text = item.particulars
        findViewById<TextView>(R.id.tvDetTranRemarks).text = item.remarks
        findViewById<TextView>(R.id.tvDetFlowCode).text = item.flowCode
        findViewById<TextView>(R.id.tvDetFlowDate).text = item.flowDate
        findViewById<TextView>(R.id.tvDetValueDate).text = item.valueDate
        findViewById<TextView>(R.id.tvDetEntryUser).text = item.entryUser
        findViewById<TextView>(R.id.tvDetEntryTime).text = item.tranDate
        findViewById<TextView>(R.id.tvDetTranStatus).text = item.tranStatus
        findViewById<TextView>(R.id.tvDetDeleted).text = item.deleted
    }

    private fun loadDataFromIntent() {
        val tranId = intent.getStringExtra("tranId") ?: ""
        val partTranId = intent.getStringExtra("partTranId") ?: ""
        val acctId = intent.getStringExtra("acctId") ?: ""
        val acctName = intent.getStringExtra("acctName") ?: ""
        val amount = intent.getStringExtra("amount") ?: ""
        val tranDate = intent.getStringExtra("tranDate") ?: ""
        val partTranType = intent.getStringExtra("partTranType") ?: ""
        val currency = intent.getStringExtra("currency") ?: ""
        val tranParticular = intent.getStringExtra("tranParticular") ?: ""
        val status = intent.getStringExtra("status") ?: ""

        findViewById<TextView>(R.id.tvDetTranId).text = tranId
        findViewById<TextView>(R.id.tvDetPartTranId).text = partTranId
        findViewById<TextView>(R.id.tvDetAcctId).text = acctId
        findViewById<TextView>(R.id.tvDetAcctName).text = acctName
        findViewById<TextView>(R.id.tvDetTranAmt).text = amount
        findViewById<TextView>(R.id.tvDetTranDate).text = tranDate
        findViewById<TextView>(R.id.tvDetPartTranType).text = partTranType
        findViewById<TextView>(R.id.tvDetAcctCcy).text = currency
        findViewById<TextView>(R.id.tvDetTranParticulars).text = tranParticular
        findViewById<TextView>(R.id.tvDetTranStatus).text = status
        findViewById<TextView>(R.id.tvDetailHeaderDate).text = tranDate
        
        // Mocking some other fields that were in the image but might not be in the model yet
        findViewById<TextView>(R.id.tvDetFlowCode).text = "DISBT"
        findViewById<TextView>(R.id.tvDetFlowDate).text = tranDate
        findViewById<TextView>(R.id.tvDetValueDate).text = tranDate
        findViewById<TextView>(R.id.tvDetEntryUser).text = "EMP04"
        findViewById<TextView>(R.id.tvDetEntryTime).text = tranDate
        findViewById<TextView>(R.id.tvDetDeleted).text = "N"
        
        // Additional fields from JournalEntriesView style
        findViewById<TextView>(R.id.tvDetTranRemarks).text = tranParticular
        findViewById<TextView>(R.id.tvDetValueDate).text = tranDate
        
        tvPage.text = "1 / 1"
    }

    inner class RelatedEntriesAdapter(
        private val list: List<JournalEntryDetailModel>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<RelatedEntriesAdapter.ViewHolder>() {
        
        private var selectedIndex = -1
        private var validatedIndex = -1
        private var acctBalIndex = -1

        fun setSelected(index: Int) {
            selectedIndex = index
            notifyDataSetChanged()
        }

        fun setValidated(index: Int) {
            validatedIndex = index
            notifyDataSetChanged()
        }

        fun setAcctBal(index: Int) {
            acctBalIndex = index
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
            val rbValidate: RadioButton = view.findViewById(R.id.rbValidate)
            val rbAccountBal: RadioButton = view.findViewById(R.id.rbAccountBal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_account_ledger_detail_related, parent, false)
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
            holder.rbValidate.isChecked = (position == validatedIndex)
            holder.rbAccountBal.isChecked = (position == acctBalIndex)
            
            holder.rbSelect.setOnClickListener { 
                setSelected(position)
                onItemClick(position)
            }
            holder.rbValidate.setOnClickListener { setValidated(position) }
            holder.rbAccountBal.setOnClickListener { setAcctBal(position) }
            
            holder.itemView.setOnClickListener { 
                setSelected(position)
                onItemClick(position)
            }
        }

        override fun getItemCount() = list.size
    }
}
