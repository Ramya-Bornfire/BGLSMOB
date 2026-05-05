package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ExcelTransactionModel
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.R

class FailedReversalEditActivity : AppCompatActivity() {

    private lateinit var rvOriginalTransaction: RecyclerView
    private lateinit var rvExcelTransaction: RecyclerView

    private lateinit var originalAdapter: RelatedReversalAdapter
    private lateinit var excelAdapter: ExcelTransactionAdapter

    private var originalList = mutableListOf<ReversalDetailModel>()
    private var excelList = mutableListOf<ExcelTransactionModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_reversal_edit)

        rvOriginalTransaction = findViewById(R.id.rvOriginalTransaction)
        rvExcelTransaction = findViewById(R.id.rvExcelTransaction)

        loadMockData()
        setupRecyclerViews()

        findViewById<Button>(R.id.btnEditHome).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnEditBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnEditSubmit).setOnClickListener {
            submitDataToApi()
        }
    }

    // ==========================================
    // API INTEGRATION POINTS
    // ==========================================

    private fun fetchDataFromApi(tranId: String) {
        // TODO: Call API to fetch original and excel details
        loadMockData()
    }

    private fun submitDataToApi() {
        // TODO: Call API to submit the reversals
        Toast.makeText(this, "Failed Transaction Reversal Submitted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadMockData() {
        // Original Transaction Table
        originalList.clear()
        originalList.add(ReversalDetailModel(
            "TR09910/1", "1", "1704120001", "Paybill Mambu clearing Account", "TRANSFER", "Debit", "KES", "14,000.00",
            "Receivable Failed Transaction", "0.00", "RECOVERY", "01-10-2025", "01-10-2025", "01-10-2025",
            "", "TJ15O680BP", "", "", "", "01-10-2025", "0.00", "", "0.00",
            "EMP04", "09-01-2026", "POST USER", "01-10-2025", "POSTED", "N"
        ))
        originalList.add(ReversalDetailModel(
            "TR09910/2", "2", "1644000001", "Debtors Adjustment Control", "TRANSFER", "Credit", "KES", "14,000.00",
            "Receivable Failed Transaction", "0.00", "RECOVERY", "01-10-2025", "01-10-2025", "01-10-2025",
            "", "TJ15O680BP", "", "", "", "01-10-2025", "0.00", "", "0.00",
            "EMP04", "09-01-2026", "POST USER", "01-10-2025", "POSTED", "N"
        ))

        // Excel Transaction Table
        excelList.clear()
        excelList.add(ExcelTransactionModel(
            "TJ15O680BP", "", "22998710", "", "14,000.00", "0.00", "01-10-2025", "UNALLOCATED"
        ))
    }

    private fun setupRecyclerViews() {
        // Original Table
        rvOriginalTransaction.layoutManager = LinearLayoutManager(this)
        originalAdapter = RelatedReversalAdapter(originalList) { position ->
            originalAdapter.setSelectedIndex(position)
            showTransactionDetailsDialog(originalList[position])
        }
        rvOriginalTransaction.adapter = originalAdapter

        // Excel Table
        rvExcelTransaction.layoutManager = LinearLayoutManager(this)
        excelAdapter = ExcelTransactionAdapter(
            list = excelList,
            onAccountsClick = { position -> showExcelAccountsDialog() },
            onValuesClick = { position -> showExcelValuesDialog() }
        )
        rvExcelTransaction.adapter = excelAdapter
    }

    private fun showExcelValuesDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_excel_values)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val rvValues = dialog.findViewById<RecyclerView>(R.id.rvExcelValues)
        rvValues.layoutManager = LinearLayoutManager(this)

        // Mock data
        val values = listOf(
            ExcelValueModel("01-10-2025", "F1001", "RECOVERY", "14,000.00", "27917600", "MERCY NYANGOGE"),
            ExcelValueModel("01-10-2025", "F1002", "PLCREC", "5,000.00", "22187093", "BEATRICE KEMUNTO")
        )

        val valueAdapter = ExcelValueAdapter(values) { selected ->
            Toast.makeText(this, "Selected Flow: ${selected.flowId}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvValues.adapter = valueAdapter

        dialog.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnFilter).setOnClickListener {
            // TODO: API INTEGRATION
            Toast.makeText(this, "Filtering values...", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showExcelAccountsDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_excel_accounts)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        val rvAccounts = dialog.findViewById<RecyclerView>(R.id.rvExcelAccounts)
        rvAccounts.layoutManager = LinearLayoutManager(this)
        
        // Mock data from screenshot
        val accounts = listOf(
            AccountSearchModel("27917600", "MERCY NYANGOGE"),
            AccountSearchModel("27917600", "MERCY NYANGOGE"),
            AccountSearchModel("27917600", "MERCY NYANGOGE"),
            AccountSearchModel("27917600", "MERCY NYANGOGE"),
            AccountSearchModel("22187093", "BEATRICE KEMUNTO"),
            AccountSearchModel("22187093", "BEATRICE KEMUNTO"),
            AccountSearchModel("22187093", "BEATRICE KEMUNTO"),
            AccountSearchModel("13667114", "JACKLYNE"),
            AccountSearchModel("CUST0000045101", "PRAKASH"),
            AccountSearchModel("22619397", "NJOGU YUNA")
        )

        val accountAdapter = ExcelAccountAdapter(accounts) { selected ->
            Toast.makeText(this, "Selected: ${selected.name}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvAccounts.adapter = accountAdapter

        dialog.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnFilter).setOnClickListener {
            // TODO: API INTEGRATION - Filter accounts
            Toast.makeText(this, "Filtering accounts...", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showTransactionDetailsDialog(data: ReversalDetailModel) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_transaction_details)
        dialog.window?.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)

        // Setup Spinner for Flow Code (Editable)
        val spinner = dialog.findViewById<android.widget.Spinner>(R.id.spinnerDiagFlowCode)
        val flowCodes = arrayOf("select", "RECOVERY", "PLCREC", "FEREC", "INREC", "PRREC")
        val spinnerAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, flowCodes)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        
        // Find current flow code position
        val initialPos = flowCodes.indexOf(data.flowCode).coerceAtLeast(0)
        spinner.setSelection(initialPos)

        // Populate fields
        dialog.findViewById<TextView>(R.id.diagTranId).text = data.tranId
        dialog.findViewById<TextView>(R.id.diagPartTranId).text = data.partTranId
        
        val etAcctId = dialog.findViewById<android.widget.EditText>(R.id.etDiagAcctId)
        etAcctId.setText(data.acctId)
        etAcctId.isEnabled = false // Read-only
        etAcctId.setBackgroundResource(R.drawable.table_cell_bg)

        dialog.findViewById<TextView>(R.id.diagAcctName).text = data.acctName
        dialog.findViewById<TextView>(R.id.diagTranType).text = data.tranType
        dialog.findViewById<TextView>(R.id.diagPartTranType).text = data.partTranType
        dialog.findViewById<TextView>(R.id.diagCurrency).text = data.currency

        // Tran Amt (Editable)
        val etAmount = dialog.findViewById<android.widget.EditText>(R.id.etDiagAmount)
        etAmount.setText(data.amount.replace(",", ""))
        etAmount.isEnabled = true
        etAmount.setBackgroundResource(R.drawable.edittext_bg)

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
            // Update the model with edited values
            // data.amount = etAmount.text.toString()
            // data.flowCode = spinner.selectedItem.toString()
            Toast.makeText(this, "Row Updated locally", Toast.LENGTH_SHORT).show()
            originalAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialog.show()
    }
}
