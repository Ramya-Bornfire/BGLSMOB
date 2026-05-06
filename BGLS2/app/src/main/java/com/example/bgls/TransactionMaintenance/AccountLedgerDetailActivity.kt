package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ChartAccountItem
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.util.Locale


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
    private lateinit var progressBar: ProgressBar
    private var entriesList = mutableListOf<JournalEntryItem>()
    private var currentIndex = 0

    // Stored current transaction identifiers
    private var currentTranId: String = ""
    private var currentPartTranId: String = ""
    private var currentAcctNum: String = ""
    private var currentEntryUser: String = ""

    private fun initViews() {
        tvPage = findViewById(R.id.tvDetPage)
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)
        progressBar = findViewById(R.id.progressBar)
        
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
            layoutTableContainer.visibility = if (layoutTableContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        findViewById<Button>(R.id.btnDetValidate).setOnClickListener {
            validateTransaction()
        }
        findViewById<Button>(R.id.btnDetPost).setOnClickListener {
            postTransaction()
        }
        findViewById<Button>(R.id.btnDetPrevious).setOnClickListener {
            if (currentIndex > 0) {
                navigateToEntry(currentIndex - 1)
            } else {
                Toast.makeText(this, "First record", Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<Button>(R.id.btnDetNext).setOnClickListener {
            if (currentIndex < entriesList.size - 1) {
                navigateToEntry(currentIndex + 1)
            } else {
                Toast.makeText(this, "Last record", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDataFromAPI(tranId: String, partTranId: String, acctNum: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAccountLedgerPostingDetail(
                    formmode = "verify",
                    tranId = tranId,
                    partTranId = partTranId,
                    acctNum = acctNum
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    // Update main details
                    body.ledgervalues?.let { leg ->
                        updateDisplayFromItem(leg)
                    }

                    // Update related entries list
                    body.jour?.let { legs ->
                        entriesList.clear()
                        entriesList.addAll(legs)
                        relatedAdapter.notifyDataSetChanged()
                        
                        currentIndex = entriesList.indexOfFirst { it.part_tran_id?.toString() == partTranId }
                        if (currentIndex != -1) {
                            relatedAdapter.setSelected(currentIndex)
                            tvPage.text = "${currentIndex + 1} / ${entriesList.size}"
                        }
                    }
                    
                    // Fetch GL details
                    fetchGLDetails(acctNum)
                    
                } else {
                    Toast.makeText(this@AccountLedgerDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountLedgerDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun fetchGLDetails(acctNum: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getGLAccountDetails(acctNum)
                if (response.isSuccessful && response.body() != null) {
                    val gl = response.body()!!
                    findViewById<TextView>(R.id.tvGlCode).text = gl.gl_code
                    findViewById<TextView>(R.id.tvGlshCode).text = gl.glsh_code
                    findViewById<TextView>(R.id.tvGlCurrency).text = gl.acct_crncy
                    findViewById<TextView>(R.id.tvGlAccountBal).text = gl.acct_bal
                    findViewById<TextView>(R.id.tvGlBalanceInd).text = if (gl.add_det_flg == "C" || gl.add_det_flg == "N") "Credit" else "Debit"
                }
            } catch (e: Exception) {
                // handle silently
            }
        }
    }

    private fun validateTransaction() {
        if (currentTranId.isEmpty()) return
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.validateAccountStatus(currentTranId)
                val msg = response.body()?.string() ?: "Validation request failed"
                showResultDialog("Validation Result", msg)
            } catch (e: Exception) {
                Toast.makeText(this@AccountLedgerDetailActivity, "Validation Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun postTransaction() {
        if (currentTranId.isEmpty()) return
        
        AlertDialog.Builder(this)
            .setTitle("Confirm Posting")
            .setMessage("Are you sure you want to post this transaction?")
            .setPositiveButton("Yes") { _, _ ->
                performPosting()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performPosting() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.postLedgerRecords(
                    tranId = currentTranId,
                    partTranId = currentPartTranId,
                    acctNum = currentAcctNum,
                    entryUser = currentEntryUser
                )
                val msg = response.body()?.string() ?: "Posting failed"
                showResultDialog("Posting Result", msg)
            } catch (e: Exception) {
                Toast.makeText(this@AccountLedgerDetailActivity, "Posting Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showResultDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun navigateToEntry(index: Int) {
        val item = entriesList[index]
        currentTranId = item.tran_id ?: ""
        currentPartTranId = item.part_tran_id?.toString() ?: ""
        currentAcctNum = item.acct_num ?: ""
        
        updateDisplayFromItem(item)
        relatedAdapter.setSelected(index)
        currentIndex = index
        tvPage.text = "${currentIndex + 1} / ${entriesList.size}"
        
        fetchGLDetails(currentAcctNum)
    }

    private fun updateDisplayFromItem(item: JournalEntryItem) {
        currentTranId = item.tran_id ?: ""
        currentPartTranId = item.part_tran_id?.toString() ?: ""
        currentAcctNum = item.acct_num ?: ""
        currentEntryUser = item.entry_user ?: "SYSTEM"

        findViewById<TextView>(R.id.tvDetTranId).text = item.tran_id
        findViewById<TextView>(R.id.tvDetPartTranId).text = item.part_tran_id?.toString()
        findViewById<TextView>(R.id.tvDetAcctId).text = item.acct_num
        findViewById<TextView>(R.id.tvDetAcctName).text = item.acct_name
        findViewById<TextView>(R.id.tvDetTranType).text = item.tran_type
        findViewById<TextView>(R.id.tvDetPartTranType).text = item.part_tran_type
        findViewById<TextView>(R.id.tvDetAcctCcy).text = item.acct_crncy
        findViewById<TextView>(R.id.tvDetTranAmt).text = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0)
        findViewById<TextView>(R.id.tvDetTranParticulars).text = item.tran_particular
        findViewById<TextView>(R.id.tvDetTranRemarks).text = item.tran_remarks
        findViewById<TextView>(R.id.tvDetFlowCode).text = item.flow_code
        findViewById<TextView>(R.id.tvDetFlowDate).text = item.flow_date
        findViewById<TextView>(R.id.tvDetTranDate).text = item.tran_date
        findViewById<TextView>(R.id.tvDetValueDate).text = item.value_date
        findViewById<TextView>(R.id.tvDetTranCode).text = item.tran_code
        findViewById<TextView>(R.id.tvDetTranReportCode).text = item.tran_rpt_code
        findViewById<TextView>(R.id.tvDetTranRefNo).text = item.tran_ref_no
        findViewById<TextView>(R.id.tvDetAdditionalDetails).text = item.add_details
        findViewById<TextView>(R.id.tvDetPartitionType).text = item.partition_type
        findViewById<TextView>(R.id.tvDetPartitionDetails).text = item.partition_det
        findViewById<TextView>(R.id.tvDetInstrumentNo).text = item.instr_num
        findViewById<TextView>(R.id.tvDetInstrumentDate).text = item.instr_date
        findViewById<TextView>(R.id.tvDetRefCcy).text = item.ref_crncy
        findViewById<TextView>(R.id.tvDetRefCcyAmt).text = String.format(Locale.US, "%,.2f", item.ref_crncy_amt ?: 0.0)
        findViewById<TextView>(R.id.tvDetRateCode).text = item.rate_code
        findViewById<TextView>(R.id.tvDetRate).text = item.rate?.toString()
        findViewById<TextView>(R.id.tvDetEntryUser).text = item.entry_user
        findViewById<TextView>(R.id.tvDetEntryTime).text = item.entry_time
        findViewById<TextView>(R.id.tvDetPostUser).text = item.post_user
        findViewById<TextView>(R.id.tvDetPostTime).text = item.post_time
        findViewById<TextView>(R.id.tvDetTranStatus).text = item.tran_status
        findViewById<TextView>(R.id.tvDetDeleted).text = item.del_flg
        findViewById<TextView>(R.id.tvDetailHeaderDate).text = item.tran_date
    }

    private fun loadDataFromIntent() {
        currentTranId = intent.getStringExtra("tranId") ?: ""
        currentPartTranId = intent.getStringExtra("partTranId") ?: ""
        currentAcctNum = intent.getStringExtra("acctId") ?: ""

        if (currentTranId.isNotEmpty()) {
            loadDataFromAPI(currentTranId, currentPartTranId, currentAcctNum)
        }
    }

    inner class RelatedEntriesAdapter(
        private val list: List<JournalEntryItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<RelatedEntriesAdapter.ViewHolder>() {
        
        private var selectedIndex = -1
        private var validatedIndex = -1
        private var acctBalIndex = -1

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
            val rbValidate: RadioButton = view.findViewById(R.id.rbValidate)
            val rbAccountBal: RadioButton = view.findViewById(R.id.rbAccountBal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_account_ledger_detail_related, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvTranDate.text = item.tran_date
            holder.tvTranId.text = "${item.tran_id}/${item.part_tran_id}"
            holder.tvPaTranTy.text = item.part_tran_type
            holder.tvCurrency.text = item.acct_crncy
            holder.tvAmount.text = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0)
            holder.tvAcctId.text = item.acct_num
            holder.tvAcctName.text = item.acct_name
            holder.tvStatus.text = item.tran_status
            
            holder.rbSelect.isChecked = (position == selectedIndex)
            holder.rbValidate.isChecked = (position == validatedIndex)
            holder.rbAccountBal.isChecked = (position == acctBalIndex)
            
            holder.rbSelect.setOnClickListener { 
                setSelected(position)
                onItemClick(position)
            }
            holder.rbValidate.setOnClickListener { 
                validatedIndex = position
                notifyDataSetChanged()
                validateTransaction() 
            }
            holder.rbAccountBal.setOnClickListener { 
                acctBalIndex = position
                notifyDataSetChanged()
                fetchGLDetails(item.acct_num ?: "") 
            }
            
            holder.itemView.setOnClickListener { 
                setSelected(position)
                onItemClick(position)
            }
        }

        override fun getItemCount() = list.size
    }
}
