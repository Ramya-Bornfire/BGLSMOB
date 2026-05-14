package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.DataModels.ReversalSubmissionPayload
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import android.content.Intent
import android.widget.ImageView
import com.example.bgls.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.Locale

class TransactionsReversalEditActivity : AppCompatActivity() {

    private lateinit var rvOriginalTransaction: RecyclerView
    private lateinit var rvReversalTransaction: RecyclerView
    private lateinit var rvNewTransaction: RecyclerView

    private lateinit var originalAdapter: RelatedReversalAdapter
    private lateinit var reversalAdapter: RelatedReversalAdapter
    private lateinit var newTransactionAdapter: NewTransactionAdapter

    private var originalList = mutableListOf<ReversalDetailModel>()
    private var reversalList = mutableListOf<ReversalDetailModel>()
    private var newList = mutableListOf<ReversalDetailModel>()

    // Store original JournalEntryItems for submission
    private var originalEntries = mutableListOf<JournalEntryItem>()
    private var reversalEntries = mutableListOf<JournalEntryItem>()
    private var newEntries = mutableListOf<JournalEntryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_reversal_edit)

        setupNavigation()
        val tranId = intent.getStringExtra("tran_id") ?: ""
        val partTranId = intent.getStringExtra("part_tran_id") ?: ""
        val acctNum = intent.getStringExtra("acct_num") ?: ""

        initViews()
        setupTables()
        fetchDataFromApi(tranId, partTranId, acctNum)

        findViewById<Button>(R.id.btnEditSubmit).setOnClickListener {
            submitDataToApi()
        }
        
        findViewById<Button>(R.id.btnAddTransaction).setOnClickListener {
            val emptyModel = ReversalDetailModel("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "")
            showTransactionDetailsDialog(emptyModel, isAddMode = true)
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun initViews() {
        rvOriginalTransaction = findViewById(R.id.rvOriginalTransaction)
        rvReversalTransaction = findViewById(R.id.rvReversalTransaction)
        rvNewTransaction = findViewById(R.id.rvNewTransaction)
    }

    private fun fetchDataFromApi(tranId: String, partTranId: String, acctNum: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getReversalTransactions("view1", tranId, partTranId, acctNum)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val gson = Gson()
                    
                    // Parse 'jour' as list of JournalEntryItem
                    val jourJson = gson.toJson(body["jour"])
                    val type = object : TypeToken<List<JournalEntryItem>>() {}.type
                    val items: List<JournalEntryItem>? = gson.fromJson(jourJson, type)
                    
                    originalEntries.clear()
                    originalList.clear()
                    reversalList.clear()
                    reversalEntries.clear()
                    newList.clear()
                    newEntries.clear()

                    if (items != null) {
                        originalEntries.addAll(items)
                        items.forEach { originalList.add(mapToDetailModel(it)) }
                        
                        items.forEach { reversalList.add(mapToDetailModel(it, isReversal = true)) }
                        items.forEach { reversalEntries.add(it.copy(part_tran_type = if (it.part_tran_type == "Debit") "Credit" else "Debit")) }

                        newList.addAll(originalList) // Or map as needed
                        newEntries.addAll(items)
                    } else {
                        Toast.makeText(this@TransactionsReversalEditActivity, "No details found", Toast.LENGTH_SHORT).show()
                    }

                    originalAdapter.notifyDataSetChanged()
                    reversalAdapter.notifyDataSetChanged()
                    newTransactionAdapter.notifyDataSetChanged()
                    
                } else {
                    Toast.makeText(this@TransactionsReversalEditActivity, "Failed to fetch details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TransactionsReversalEditActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapToDetailModel(item: JournalEntryItem, isReversal: Boolean = false): ReversalDetailModel {
        val type = if (isReversal) {
            if (item.part_tran_type == "Debit") "Credit" else "Debit"
        } else {
            item.part_tran_type ?: ""
        }
        
        return ReversalDetailModel(
            tranId = item.tran_id ?: "",
            partTranId = item.part_tran_id?.toString() ?: "",
            acctId = item.acct_num ?: "",
            acctName = item.acct_name ?: "",
            tranType = item.tran_type ?: "",
            partTranType = type,
            currency = item.acct_crncy ?: "",
            amount = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0),
            particulars = item.tran_particular ?: "",
            remarks = item.tran_remarks ?: "",
            flowCode = item.flow_code ?: "",
            flowDate = formatDate(item.flow_date),
            tranDate = formatDate(item.tran_date),
            valueDate = formatDate(item.value_date),
            tranCode = item.tran_code ?: "",
            tranReportCode = item.tran_rpt_code ?: "",
            tranRefNo = item.tran_ref_no ?: "",
            additionalDetails = item.add_details ?: "",
            partitionType = item.partition_type ?: "",
            partitionDetails = item.partition_det ?: "",
            instrumentNo = item.instr_num ?: "",
            instrumentDate = formatDate(item.instr_date),
            homeCurrencyAmount = String.format(Locale.US, "%.2f", item.ref_crncy_amt ?: 0.0),
            rateCode = item.rate_code ?: "",
            rate = item.rate?.toString() ?: "",
            entryUser = item.entry_user ?: "",
            entryTime = formatDate(item.entry_time),
            postUser = item.post_user ?: "",
            postTime = formatDate(item.post_time),
            tranStatus = item.tran_status ?: "",
            deleted = item.del_flg ?: ""
        )
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            if (dateStr.contains(" ")) {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val date = sdfInput.parse(dateStr)
                val sdfOutput = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.US)
                sdfOutput.format(date!!)
            } else if (dateStr.contains("-") && dateStr.split("-")[0].length == 4) {
                val sdfInput = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdfInput.parse(dateStr)
                val sdfOutput = java.text.SimpleDateFormat("dd-MM-yyyy", Locale.US)
                sdfOutput.format(date!!)
            } else {
                dateStr
            }
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun submitDataToApi() {
        lifecycleScope.launch {
            try {
                val payload = ReversalSubmissionPayload(
                    originalTransactions = originalEntries,
                    reversalTransactions = reversalEntries,
                    newTransactions = newEntries
                )
                
                val response = RetrofitClient.api.submitReversalData(payload)
                if (response.isSuccessful) {
                    Toast.makeText(this@TransactionsReversalEditActivity, "Transactions Reversal Submitted Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TransactionsReversalEditActivity, "Submission Failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@TransactionsReversalEditActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTables() {
        rvOriginalTransaction.layoutManager = LinearLayoutManager(this)
        originalAdapter = RelatedReversalAdapter(originalList) { index ->
            originalAdapter.setSelectedIndex(index)
            showTransactionDetailsDialog(originalList[index], isAddMode = false)
        }
        rvOriginalTransaction.adapter = originalAdapter

        rvReversalTransaction.layoutManager = LinearLayoutManager(this)
        reversalAdapter = RelatedReversalAdapter(reversalList) { index ->
            reversalAdapter.setSelectedIndex(index)
            showTransactionDetailsDialog(reversalList[index], isAddMode = false)
        }
        rvReversalTransaction.adapter = reversalAdapter

        rvNewTransaction.layoutManager = LinearLayoutManager(this)
        newTransactionAdapter = NewTransactionAdapter(newList, 
            onItemClick = { index ->
                newTransactionAdapter.setSelectedIndex(index)
                showTransactionDetailsDialog(newList[index], isAddMode = false)
            },
            onDeleteClick = { index ->
                newList.removeAt(index)
                newEntries.removeAt(index)
                newTransactionAdapter.notifyItemRemoved(index)
                newTransactionAdapter.notifyItemRangeChanged(index, newList.size)
            }
        )
        rvNewTransaction.adapter = newTransactionAdapter
    }

    private fun showTransactionDetailsDialog(data: ReversalDetailModel, isAddMode: Boolean) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_transaction_details)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        // Setup Spinner
        val spinner = dialog.findViewById<android.widget.Spinner>(R.id.spinnerDiagFlowCode)
        val flowCodes = arrayOf("-- Select --", "Recovery", "PLREC", "FEREC", "INREC", "PRREC")
        val spinnerAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, flowCodes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        
        // Select current flow code
        val flowPos = flowCodes.indexOf(data.flowCode).coerceAtLeast(0)
        spinner.setSelection(flowPos)

        // Populate fields
        dialog.findViewById<android.widget.TextView>(R.id.diagTranId).text = data.tranId
        dialog.findViewById<android.widget.TextView>(R.id.diagPartTranId).text = data.partTranId
        
        val etAcctId = dialog.findViewById<android.widget.EditText>(R.id.etDiagAcctId)
        etAcctId.setText(data.acctId)
        
        dialog.findViewById<android.widget.TextView>(R.id.diagAcctName).text = data.acctName
        dialog.findViewById<android.widget.TextView>(R.id.diagTranType).text = data.tranType
        dialog.findViewById<android.widget.TextView>(R.id.diagPartTranType).text = data.partTranType
        dialog.findViewById<android.widget.TextView>(R.id.diagCurrency).text = data.currency
        
        val etAmount = dialog.findViewById<android.widget.EditText>(R.id.etDiagAmount)
        etAmount.setText(data.amount.replace(",", ""))
        
        dialog.findViewById<android.widget.TextView>(R.id.diagParticulars).text = data.particulars
        dialog.findViewById<android.widget.TextView>(R.id.diagRemarks).text = data.remarks
        dialog.findViewById<android.widget.TextView>(R.id.diagFlowDate).text = data.flowDate
        dialog.findViewById<android.widget.TextView>(R.id.diagTranDate).text = data.tranDate
        dialog.findViewById<android.widget.TextView>(R.id.diagValueDate).text = data.valueDate
        dialog.findViewById<android.widget.TextView>(R.id.diagTranCode).text = data.tranCode
        dialog.findViewById<android.widget.TextView>(R.id.diagTranReportCode).text = data.tranReportCode
        dialog.findViewById<android.widget.TextView>(R.id.diagTranRefNo).text = data.tranRefNo
        dialog.findViewById<android.widget.TextView>(R.id.diagAdditionalDetails).text = data.additionalDetails
        dialog.findViewById<android.widget.TextView>(R.id.diagPartitionType).text = data.partitionType
        dialog.findViewById<android.widget.TextView>(R.id.diagPartitionDetails).text = data.partitionDetails
        dialog.findViewById<android.widget.TextView>(R.id.diagInstrumentNo).text = data.instrumentNo
        dialog.findViewById<android.widget.TextView>(R.id.diagInstrumentDate).text = data.instrumentDate
        dialog.findViewById<android.widget.TextView>(R.id.diagRefCurrency).text = data.currency
        dialog.findViewById<android.widget.TextView>(R.id.diagHomeCurrencyAmount).text = data.homeCurrencyAmount
        dialog.findViewById<android.widget.TextView>(R.id.diagRateCode).text = data.rateCode
        dialog.findViewById<android.widget.TextView>(R.id.diagRate).text = data.rate
        dialog.findViewById<android.widget.TextView>(R.id.diagEntryUser).text = data.entryUser
        dialog.findViewById<android.widget.TextView>(R.id.diagPostUser).text = data.postUser
        dialog.findViewById<android.widget.TextView>(R.id.diagEntryTime).text = data.entryTime
        dialog.findViewById<android.widget.TextView>(R.id.diagPostTime).text = data.postTime
        dialog.findViewById<android.widget.TextView>(R.id.diagStatus).text = data.tranStatus
        dialog.findViewById<android.widget.TextView>(R.id.diagDeleted).text = data.deleted

        // Handle Add Mode vs Edit Mode editability
        if (isAddMode) {
            etAcctId.isEnabled = true
            etAcctId.setBackgroundResource(R.drawable.edittext_bg)
            
            etAmount.isEnabled = false
            etAmount.setBackgroundResource(R.drawable.table_cell_bg)
            
            spinner.isEnabled = true
        } else {
            etAcctId.isEnabled = false
            etAcctId.setBackgroundResource(R.drawable.table_cell_bg)
            
            etAmount.isEnabled = true
            etAmount.setBackgroundResource(R.drawable.edittext_bg)
            
            spinner.isEnabled = true
        }

        dialog.findViewById<android.widget.ImageView>(R.id.ivCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnDiagClose).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnDiagSubmit).setOnClickListener {
            // Logic for submit - update the list item
            Toast.makeText(this, "Transaction Updated Locally", Toast.LENGTH_SHORT).show()
            // Here you would normally update the corresponding entry in newEntries or originalEntries
            dialog.dismiss()
        }

        dialog.show()
    }
}
