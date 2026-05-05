package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.R

class RecoveryReversalEditActivity : AppCompatActivity() {

    private lateinit var rvOriginalTransaction: RecyclerView
    private lateinit var rvReversalTransaction: RecyclerView

    private lateinit var originalAdapter: RelatedReversalAdapter
    private lateinit var reversalAdapter: RelatedReversalAdapter

    private var originalList = mutableListOf<ReversalDetailModel>()
    private var reversalList = mutableListOf<ReversalDetailModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recovery_reversal_edit)

        initViews()
        fetchDataFromApi("TR00001") // TODO: Pass actual transaction ID from Intent
        setupTables()

        findViewById<Button>(R.id.btnEditHome).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnEditSubmit).setOnClickListener {
            submitDataToApi()
        }
        findViewById<Button>(R.id.btnEditBack).setOnClickListener { finish() }
    }

    private fun initViews() {
        rvOriginalTransaction = findViewById(R.id.rvOriginalTransaction)
        rvReversalTransaction = findViewById(R.id.rvReversalTransaction)
    }

    // ==========================================
    // API INTEGRATION POINTS
    // ==========================================

    /**
     * TODO: Replace with actual API call (e.g., Retrofit or Volley)
     * Once you receive the response, parse it into `ReversalDetailModel` lists 
     * and call setupTables() or notifyDataSetChanged().
     */
    private fun fetchDataFromApi(tranId: String) {
        // Example logic for when you get the API:
        // ApiService.getTransactionDetails(tranId).enqueue(...)
        
        // Temporarily loading mock data so the UI continues to work for now
        loadMockData()
    }

    /**
     * TODO: Replace with actual API call to submit the edited/new transactions.
     * Gather data from originalList and reversalList to send to the backend.
     */
    private fun submitDataToApi() {
        // Example logic for when you get the API:
        // val payload = mapOf("original" to originalList)
        // ApiService.submitTransaction(payload).enqueue(...)
        
        Toast.makeText(this, "Transactions Submitted to API", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadMockData() {
        // Mock data based on screenshot
        originalList.add(ReversalDetailModel("TR00001", "1523", "MGJJ129", "HAROLD OPICHO", "TRANSFER", "Debit", "KES", "33,600.00", "", "", "", "", "10-04-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        originalList.add(ReversalDetailModel("TR00001", "1991", "e01fd50109fb...", "GEOFFREY NGUI", "TRANSFER", "Debit", "KES", "82,500.00", "", "", "", "", "17-04-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        originalList.add(ReversalDetailModel("TR00001", "1017", "8878c9751e39...", "MOSES WACHIRA MWANGI", "TRANSFER", "Debit", "KES", "15,000.00", "", "", "", "", "10-08-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        originalList.add(ReversalDetailModel("TR00001", "677", "NLP1908SiDLY...", "GORDON ODHIAMBO OMOM", "TRANSFER", "Debit", "KES", "45,891.00", "", "", "", "", "21-03-2020", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))

        reversalList.add(ReversalDetailModel("TR00001", "1523", "MGJJ129", "HAROLD OPICHO", "TRANSFER", "Credit", "KES", "33,600.00", "", "", "", "", "10-04-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        reversalList.add(ReversalDetailModel("TR00001", "1991", "e01fd50109fb...", "GEOFFREY NGUI", "TRANSFER", "Credit", "KES", "82,500.00", "", "", "", "", "17-04-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        reversalList.add(ReversalDetailModel("TR00001", "1017", "8878c9751e39...", "MOSES WACHIRA MWANGI", "TRANSFER", "Credit", "KES", "15,000.00", "", "", "", "", "10-08-2019", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
        reversalList.add(ReversalDetailModel("TR00001", "677", "NLP1908SiDLY...", "GORDON ODHIAMBO OMOM", "TRANSFER", "Credit", "KES", "45,891.00", "", "", "", "", "21-03-2020", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "POSTED", ""))
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
        
        dialog.findViewById<android.widget.TextView>(R.id.diagFlowDate).text = data.flowDate
        dialog.findViewById<android.widget.TextView>(R.id.diagTranDate).text = data.tranDate
        dialog.findViewById<android.widget.TextView>(R.id.diagValueDate).text = data.valueDate
        dialog.findViewById<android.widget.TextView>(R.id.diagEntryUser).text = data.entryUser
        dialog.findViewById<android.widget.TextView>(R.id.diagPostUser).text = data.postUser
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
            // Logic for submit
            Toast.makeText(this, "Transaction Updated", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }
}
