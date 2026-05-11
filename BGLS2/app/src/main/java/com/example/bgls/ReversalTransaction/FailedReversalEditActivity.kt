package com.example.bgls.ReversalTransaction

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ExcelTransactionModel
import com.example.bgls.DataModels.FailedReversalSubmissionPayload
import com.example.bgls.DataModels.JournalEntryItem
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.util.Locale

class FailedReversalEditActivity : AppCompatActivity() {

    private lateinit var rvOriginalTransaction: RecyclerView
    private lateinit var rvExcelTransaction: RecyclerView

    private lateinit var originalAdapter: RelatedReversalAdapter
    private lateinit var excelAdapter: ExcelTransactionAdapter

    private var originalList = mutableListOf<ReversalDetailModel>()
    private var excelList = mutableListOf<ExcelTransactionModel>()

    private var originalEntries = mutableListOf<JournalEntryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_reversal_edit)

        val tranId = intent.getStringExtra("tran_id") ?: ""
        val partTranId = intent.getStringExtra("part_tran_id") ?: ""
        val acctNum = intent.getStringExtra("acct_num") ?: ""

        rvOriginalTransaction = findViewById(R.id.rvOriginalTransaction)
        rvExcelTransaction = findViewById(R.id.rvExcelTransaction)

        setupRecyclerViews()
        fetchDataFromApi(tranId, partTranId, acctNum)

        findViewById<Button>(R.id.btnHeaderSubmit).setOnClickListener {
            submitDataToApi()
        }
    }

    private fun fetchDataFromApi(tranId: String, partTranId: String, acctNum: String) {
        lifecycleScope.launch {
            try {
                // view1 formmode returns: jour, jour1, accountdetails, customervalues
                // This matches the web frontend's reversaluser() which uses formmode=view1
                val response = RetrofitClient.api.getFailedTransactionsDetails("view1", tranId, partTranId, acctNum)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val gson = Gson()

                    // ── 1. Parse Original Transactions from "jour" ──────────────────
                    val jourJson = gson.toJson(body["jour"])
                    val jourType = object : TypeToken<List<JournalEntryItem>>() {}.type
                    val jourItems: List<JournalEntryItem>? = gson.fromJson(jourJson, jourType)

                    originalEntries.clear()
                    originalList.clear()
                    if (jourItems != null) {
                        originalEntries.addAll(jourItems)
                        jourItems.forEach { originalList.add(mapToDetailModel(it)) }
                    }

                    // ── 2. Parse Excel / Multiple-Transaction-History from "accountdetails" ──
                    // The backend populates this via MULTIPLE_TRANSACTION_HISTORY_REPO
                    // .getAccountDetailsByRefTranId(tran_id) in the view1 formmode
                    val accountDetailsJson = gson.toJson(body["accountdetails"])
                    val accountDetailsType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val accountDetailsList: List<Map<String, Any>>? =
                        gson.fromJson(accountDetailsJson, accountDetailsType)

                    excelList.clear()
                    accountDetailsList?.forEach { item ->
                        excelList.add(
                            ExcelTransactionModel(
                                tranId          = item["ref_tran_id"]?.toString()
                                                  ?: item["tran_id"]?.toString() ?: "",
                                names           = item["customer_name"]?.toString()
                                                  ?: item["names"]?.toString() ?: "",
                                reference       = item["reference_no"]?.toString()
                                                  ?: item["reference"]?.toString() ?: "",
                                mobileNumber    = item["mobile_number"]?.toString()
                                                  ?: item["mobileNumber"]?.toString() ?: "",
                                amount          = item["amount"]?.toString() ?: "",
                                allocatedAmount = item["allocated_amount"]?.toString()
                                                  ?: item["alloc_amount"]?.toString() ?: "",
                                transTime       = item["trans_time"]?.toString()
                                                  ?: item["tran_date"]?.toString() ?: "",
                                status          = item["status"]?.toString() ?: ""
                            )
                        )
                    }

                    if ((jourItems == null || jourItems.isEmpty()) &&
                        (accountDetailsList == null || accountDetailsList.isEmpty())
                    ) {
                        Toast.makeText(
                            this@FailedReversalEditActivity,
                            "No details found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    originalAdapter.notifyDataSetChanged()
                    excelAdapter.notifyDataSetChanged()

                } else {
                    Toast.makeText(
                        this@FailedReversalEditActivity,
                        "Failed to fetch details (HTTP ${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@FailedReversalEditActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mapToDetailModel(item: JournalEntryItem): ReversalDetailModel {
        return ReversalDetailModel(
            tranId = item.tran_id ?: "",
            partTranId = item.part_tran_id?.toString() ?: "",
            acctId = item.acct_num ?: "",
            acctName = item.acct_name ?: "",
            tranType = item.tran_type ?: "",
            partTranType = item.part_tran_type ?: "",
            currency = item.acct_crncy ?: "",
            amount = String.format(Locale.US, "%,.2f", item.tran_amt ?: 0.0),
            particulars = item.tran_particular ?: "",
            remarks = item.tran_remarks ?: "",
            flowCode = item.flow_code ?: "",
            flowDate = formatDate(item.flow_date),
            tranDate = formatDate(item.tran_date),
            valueDate = formatDate(item.value_date),
            tranReportCode = item.tran_rpt_code ?: "",
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
                val payload = FailedReversalSubmissionPayload(
                    originalTransactions = originalEntries,
                    excelTransactions = excelList
                )
                
                val response = RetrofitClient.api.submitFailedReversal(payload)
                if (response.isSuccessful) {
                    Toast.makeText(this@FailedReversalEditActivity, "Failed Transaction Reversal Submitted Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@FailedReversalEditActivity, "Submission Failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@FailedReversalEditActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

        // Mock data or API call for values
        val values = emptyList<ExcelValueModel>() // Should be fetched via API if needed

        val valueAdapter = ExcelValueAdapter(values) { selected ->
            Toast.makeText(this, "Selected Flow: ${selected.flowId}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvValues.adapter = valueAdapter

        dialog.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnFilter).setOnClickListener {
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
        
        val accounts = emptyList<AccountSearchModel>() // Should be fetched via API

        val accountAdapter = ExcelAccountAdapter(accounts) { selected ->
            Toast.makeText(this, "Selected: ${selected.name}", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        rvAccounts.adapter = accountAdapter

        dialog.findViewById<Button>(R.id.btnCloseDialog).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnFilter).setOnClickListener {
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
            Toast.makeText(this, "Row Updated locally", Toast.LENGTH_SHORT).show()
            originalAdapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        dialog.show()
    }
}
