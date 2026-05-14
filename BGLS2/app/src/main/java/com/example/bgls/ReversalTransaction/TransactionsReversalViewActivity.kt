package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.R
import android.content.Intent
import android.widget.ImageView
import com.example.bgls.MainActivity

class TransactionsReversalViewActivity : AppCompatActivity() {

    private var currentIndex = 0
    private var detailList = mutableListOf<ReversalDetailModel>()
    private lateinit var rvRelatedEntries: RecyclerView
    private lateinit var relatedAdapter: RelatedReversalAdapter
    private lateinit var layoutTableContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions_reversal_view)

        setupNavigation()
        loadMockData()
        setupFields()
        setupBottomTable()

        findViewById<Button>(R.id.btnDetailPrev).setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnDetailNext).setOnClickListener {
            if (currentIndex < detailList.size - 1) {
                currentIndex++
                updateDisplay()
            }
        }

        findViewById<Button>(R.id.btnDetailView).setOnClickListener {
            layoutTableContainer.visibility = if (layoutTableContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

//        findViewById<Button>(R.id.btnDetailBack).setOnClickListener { finish() }
//        findViewById<Button>(R.id.btnDetailHome).setOnClickListener { finish() }

        updateDisplay()
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

    private fun setupFields() {
        rvRelatedEntries = findViewById(R.id.rvRelatedEntries)
        layoutTableContainer = findViewById(R.id.layoutTableContainer)
    }

    private fun loadMockData() {
        // Mock data based on screenshot
        detailList.add(ReversalDetailModel(
            "TR00001", "1523", "MGJJ129", "HAROLD OPICHO", "TRANSFER", "Debit", "KES", "33,600.00",
            "Loan Disbursement Amount", "Loan Disbursement Amount", "DISBT", "10-04-2019", "10-04-2019", "10-04-2019",
            "", "", "", "", "P", "Partition Detail", "", "10-04-2019", "0.00", "RC01", "0.00",
            "SYSTEM", "30-03-2019", "POST USER", "02-10-2025", "POSTED", "N"
        ))
        // Add one more for pagination demo
        detailList.add(ReversalDetailModel(
            "TR00001", "1524", "WA00001", "HAROLD OPICHO", "TRANSFER", "Credit", "KES", "33,600.00",
            "Loan Disbursement Amount", "Loan Disbursement Amount", "DISBT", "10-04-2019", "10-04-2019", "10-04-2019",
            "", "", "", "", "P", "Partition Detail", "", "10-04-2019", "0.00", "RC01", "0.00",
            "SYSTEM", "30-03-2019", "POST USER", "02-10-2025", "POSTED", "N"
        ))
    }

    private fun setupBottomTable() {
        rvRelatedEntries.layoutManager = LinearLayoutManager(this)
        relatedAdapter = RelatedReversalAdapter(detailList) { index ->
            currentIndex = index
            updateDisplay()
            showTransactionDetailsDialog(detailList[index])
        }
        rvRelatedEntries.adapter = relatedAdapter
    }

    private fun showTransactionDetailsDialog(data: ReversalDetailModel) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_transaction_details)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        // Setup Spinner
        val spinner = dialog.findViewById<android.widget.Spinner>(R.id.spinnerDiagFlowCode)
        val flowCodes = arrayOf("select", "recoveryPLCREC", "FEREC", "INREC", "PRREC")
        val spinnerAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, flowCodes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        // Populate fields
        dialog.findViewById<TextView>(R.id.diagTranId).text = data.tranId
        dialog.findViewById<TextView>(R.id.diagPartTranId).text = data.partTranId
        
        val etAcctId = dialog.findViewById<android.widget.EditText>(R.id.etDiagAcctId)
        etAcctId.setText(data.acctId)
        etAcctId.isEnabled = false // Read-only in View Activity
        etAcctId.setBackgroundResource(R.drawable.table_cell_bg)
        dialog.findViewById<TextView>(R.id.diagAcctName).text = data.acctName
        dialog.findViewById<TextView>(R.id.diagTranType).text = data.tranType
        dialog.findViewById<TextView>(R.id.diagPartTranType).text = data.partTranType
        dialog.findViewById<TextView>(R.id.diagCurrency).text = data.currency
        dialog.findViewById<android.widget.EditText>(R.id.etDiagAmount).setText(data.amount.replace(",", ""))
        dialog.findViewById<TextView>(R.id.diagFlowDate).text = data.flowDate
        dialog.findViewById<TextView>(R.id.diagTranDate).text = data.tranDate
        dialog.findViewById<TextView>(R.id.diagValueDate).text = data.valueDate
        dialog.findViewById<TextView>(R.id.diagEntryUser).text = data.entryUser
        dialog.findViewById<TextView>(R.id.diagPostUser).text = data.postUser
        dialog.findViewById<TextView>(R.id.diagStatus).text = data.tranStatus
        dialog.findViewById<TextView>(R.id.diagDeleted).text = data.deleted

        dialog.findViewById<android.widget.ImageView>(R.id.ivCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnDiagClose).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnDiagSubmit).setOnClickListener {
            // Logic for submit
            android.widget.Toast.makeText(this, "Transaction Updated", android.widget.Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateDisplay() {
        val data = detailList[currentIndex]

        // Map fields to views
        setFieldValue(R.id.fieldTranId, "Tran Id", data.tranId)
        setFieldValue(R.id.fieldAcctId, "Acct ID", data.acctId)
        setFieldValue(R.id.fieldTranType, "Tran Type", data.tranType)
        setFieldValue(R.id.fieldAcctCurrency, "Account Currency", data.currency)
        setFieldValue(R.id.fieldTranParticulars, "Tran Particulars", data.particulars)
        setFieldValue(R.id.fieldFlowCode, "Flow Code", data.flowCode)
        setFieldValue(R.id.fieldTranDate, "Tran Date", data.tranDate)
        setFieldValue(R.id.fieldTranCode, "Tran Code", "")
        setFieldValue(R.id.fieldTranRefNo, "Tran Ref NO", "Customer Loan Product")
        setFieldValue(R.id.fieldPartitionType, "Partition Type", data.partitionType)
        setFieldValue(R.id.fieldInstrumentNo, "Instrument NO", data.instrumentNo)
        setFieldValue(R.id.fieldAcctCurrency2, "Account Currency", data.currency)
        setFieldValue(R.id.fieldRateCode, "Rate Code", data.rateCode)
        setFieldValue(R.id.fieldEntryUser, "Entry User", data.entryUser)
        setFieldValue(R.id.fieldEntryTime, "Entry Time", data.entryTime)
        setFieldValue(R.id.fieldTranStatus, "Tran Status", data.tranStatus)

        // Right Column
        setFieldValue(R.id.fieldPartTranId, "Part Tran Id", data.partTranId)
        setFieldValue(R.id.fieldAcctName, "Acct Name", data.acctName)
        setFieldValue(R.id.fieldPartTranType, "Part Tran Type", data.partTranType)
        setFieldValue(R.id.fieldTranAmt, "Tran Amt", data.amount)
        setFieldValue(R.id.fieldTranRemarks, "Tran Remarks", data.remarks)
        setFieldValue(R.id.fieldFlowDate, "Flow Date", data.flowDate)
        setFieldValue(R.id.fieldValueDate, "Value Date", data.valueDate)
        setFieldValue(R.id.fieldTranReportCode, "Tran Report Code", data.tranReportCode)
        setFieldValue(R.id.fieldAdditionalDetails, "Additional Details", data.additionalDetails)
        setFieldValue(R.id.fieldPartitionDetails, "Partition Details", data.partitionDetails)
        setFieldValue(R.id.fieldInstrumentDate, "Instrument Date", data.instrumentDate)
        setFieldValue(R.id.fieldHomeCurrencyAmount, "Home Currency Amount", data.homeCurrencyAmount)
        setFieldValue(R.id.fieldRate, "Rate", data.rate)
        setFieldValue(R.id.fieldPostUser, "Post User", data.postUser)
        setFieldValue(R.id.fieldPostTime, "Post Time", data.postTime)
        setFieldValue(R.id.fieldDeleted, "Deleted", data.deleted)

        findViewById<TextView>(R.id.tvDetailInfo).text = "${data.partTranId} / 2607"
        findViewById<TextView>(R.id.tvHeaderDate).text = data.tranDate

        relatedAdapter.setSelectedIndex(currentIndex)
    }

    private fun setFieldValue(layoutId: Int, label: String, value: String) {
        val layout = findViewById<View>(layoutId)
        layout.findViewById<TextView>(R.id.tvLabel).text = label
        layout.findViewById<TextView>(R.id.tvValue).text = value
    }
}
