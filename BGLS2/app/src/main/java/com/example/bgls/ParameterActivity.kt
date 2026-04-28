package com.example.bgls

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.bgls.DataModels.RefResponse
import com.example.bgls.Retrofit.RetrofitClient

import com.example.bgls.databinding.ActivityParameterBinding
import retrofit2.Call
import retrofit2.Response

class ParameterActivity : AppCompatActivity() {

    private lateinit var binding:  ActivityParameterBinding

    private lateinit var tableLayout: TableLayout
    private lateinit var tvTitle: TextView

    data class ReferenceItem(
        val refType: String,
        val typeDesc: String,
        val refId: String,
        val refDesc: String,
        val moduleId: String
    )
    data class GLItem(
        val sNo: String,
        val glCode: String,
        val glDesc: String,
        val branchId: String,
        val branchDesc: String,
        val glshCode: String,
        val glshDesc: String,
        val crncyCode: String,
        val creditBal: String,
        val debitBal: String
    )
    data class SchemeItem(
        val product: String,
        val id: String,
        val category: String,
        val type: String,
        val description: String,
        val status: String
    )

    data class ChartAccountItem(
        val head: String,
        val gl: String,
        val schemeCode: String,
        val acctId: String,
        val acctName: String,
        val currency: String,
        val credits: String,
        val debits: String,
        val balance: String,
        val status: String
    )
    data class AccountLedgerItem(
        val head: String,
        val acctId: String,
        val acctName: String,
        val currency: String,
        val credits: String,
        val debits: String,
        val balance: String,
        val status: String
    )
    data class TransactionAccountItem(
        val id: String,
        val event: String,
        val debitAccNo: String,
        val debitAccName: String,
        val creditAccNo: String,
        val creditAccName: String,
        val tranParticular: String,
        val type: String
    )

    private val moduleDataMap = mapOf(
        "Reference Code Maintenance" to getReferenceCodeMaintenanceData(),
        "GL Structure" to getGLTableData(),
        "Scheme Codes" to getSchemeTableData(),
        "Chart of Accounts" to getChartAccountsTableData(),
        "Account Ledger" to getAccountLedgerTableData(),
        "Transaction Accounts" to getTransactionAccountsTableData()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityParameterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvTitle = binding.tvTitle

        tableLayout = binding.tableLayout

        createModuleButtons()
        loadModuleData("Reference Code Maintenance")
    }

    private fun createModuleButtons() {
        val modules = listOf(
            "Reference Code Maintenance",
            "GL Structure",
            "Scheme Codes",
            "Chart of Accounts",
            "Account Ledger",
            "Transaction Accounts"
        )

        for (module in modules) {
            val button = Button(this).apply {
                text = module
                background = ContextCompat.getDrawable(context, R.drawable.tab_unselected)

                setTextColor(ContextCompat.getColor(context, R.color.cyanblue))
                setPadding(40, 16, 40, 16)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
                setOnClickListener {
                    loadModuleData(module)
                    updateButtonSelection(this)
                }
            }
            binding.moduleButtonsContainer.addView(button)
        }
    }
    private fun updateButtonSelection(selectedButton: Button) {
        val container = binding.moduleButtonsContainer

        for (i in 0 until container.childCount) {
            val btn = container.getChildAt(i) as Button

            if (btn == selectedButton) {
                btn.background = ContextCompat.getDrawable(this, R.drawable.tab_selected)
                btn.setTextColor(Color.WHITE)
            } else {
                btn.background = ContextCompat.getDrawable(this, R.drawable.tab_unselected)
                btn.setTextColor(ContextCompat.getColor(this, R.color.cyanblue))
            }
        }
    }

    private fun loadModuleData(moduleName: String) {
        tvTitle.text = moduleName

        when (moduleName) {
            "Reference Code Maintenance" -> {
                loadReferenceCodesFromAPI()   // ✅ CALL API HERE
            }
            "GL Structure" -> {
                populateGLTable(getGLTableData())
            }
            "Scheme Codes" -> {
                populateSchemeTable(getSchemeTableData())
            }
            "Chart of Accounts" -> {
                populateChartAccountsTable(getChartAccountsTableData())
            }
            "Account Ledger" -> {
                populateAccountLedgerTable(getAccountLedgerTableData())
            }
            "Transaction Accounts" -> {
                populateTransactionAccountsTable(getTransactionAccountsTableData())
            }
            else -> {
                val data = moduleDataMap[moduleName] ?: emptyList()
                populateTable(data as List<ReferenceItem>)
            }
        }
    }
    private fun populateTransactionAccountsTable(data: List<TransactionAccountItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "ID", "EVENT",
            "DEBIT ACCOUNT NUMBER", "DEBIT ACCOUNT NAME",
            "CREDIT ACCOUNT NUMBER", "CREDIT ACCOUNT NAME",
            "TRAN PARTICULAR", "TYPE", "ACTIONS"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            tv.setTextColor(Color.WHITE)
            headerRow.addView(tv)
        }

        tableLayout.addView(headerRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)

            val values = listOf(
                item.id,
                item.event,
                item.debitAccNo,
                item.debitAccName,
                item.creditAccNo,
                item.creditAccName,
                item.tranParticular,
                item.type,
                "Action"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                // ACTION column
                if (index == values.lastIndex) {
                    tv.setTextColor(Color.BLUE)
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        Toast.makeText(
                            this,
                            "Action clicked for ${item.id}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // alternate row colors
                if (rowIndex % 2 == 0) {
                    tv.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    tv.setBackgroundColor(Color.WHITE)
                }

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateAccountLedgerTable(data: List<AccountLedgerItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "HEAD", "ACCT ID", "ACCT NAME",
            "CURRENCY", "CREDITS", "DEBITS", "BALANCE","STATUS"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            tv.setTextColor(Color.WHITE)
            headerRow.addView(tv)
        }

        tableLayout.addView(headerRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)

            val values = listOf(
                item.head,
                item.acctId,
                item.acctName,
                item.currency,
                item.credits,
                item.debits,
                item.balance,
                item.status
            )

            values.forEach { value ->
                val tv = createTextView(value, false, 1f)

                // alternate row color
                if (rowIndex % 2 == 0) {
                    tv.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    tv.setBackgroundColor(Color.WHITE)
                }

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateChartAccountsTable(data: List<ChartAccountItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "HEAD", "GL", "SCHEME CODE", "ACCT ID", "ACCT NAME",
            "CURRENCY", "CREDITS", "DEBITS", "BALANCE","STATUS", "ACTION"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            tv.setTextColor(Color.WHITE)
            headerRow.addView(tv)
        }

        tableLayout.addView(headerRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)

            val values = listOf(
                item.head,
                item.gl,
                item.schemeCode,
                item.acctId,
                item.acctName,
                item.currency,
                item.credits,
                item.debits,
                item.balance,
                item.status,
                "Action"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                // ACTION column
                if (index == values.lastIndex) {
                    tv.setTextColor(Color.BLUE)
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        Toast.makeText(
                            this,
                            "Action clicked for ${item.acctName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                // alternate row colors
                if (rowIndex % 2 == 0) {
                    tv.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    tv.setBackgroundColor(Color.WHITE)
                }

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateSchemeTable(data: List<SchemeItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "PRODUCT", "ID", "CATEGORY", "TYPE", "DESCRIPTION", "STATUS"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            tv.setTextColor(Color.WHITE)
            headerRow.addView(tv)
        }

        tableLayout.addView(headerRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)

            val values = listOf(
                item.product,
                item.id,
                item.category,
                item.type,
                item.description,
                item.status
            )

            values.forEach { value ->
                val tv = createTextView(value, false, 1f)

                // alternate row color
                if (rowIndex % 2 == 0) {
                    tv.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    tv.setBackgroundColor(Color.WHITE)
                }

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateGLTable(data: List<GLItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "S.NO", "GL CODE", "GL DESC", "BRANCH ID", "BRANCH DESC",
            "GLSH CODE", "GLSH DESC", "CRNCY CODE", "CREDIT BAL", "DEBIT BAL", "SELECT"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            tv.setTextColor(Color.WHITE)
            headerRow.addView(tv)
        }

        tableLayout.addView(headerRow)

        data.forEach { item ->
            val row = TableRow(this)

            val values = listOf(
                item.sNo,
                item.glCode,
                item.glDesc,
                item.branchId,
                item.branchDesc,
                item.glshCode,
                item.glshDesc,
                item.crncyCode,
                item.creditBal,
                item.debitBal,
                "Select"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                if (index == values.lastIndex) {
                    tv.setTextColor(Color.BLUE)
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        Toast.makeText(this, "Selected ${item.glCode}", Toast.LENGTH_SHORT).show()
                    }
                }

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateTable(data: List<ReferenceItem>) {
        tableLayout.removeAllViews()

        // Header row
        val headerRow = TableRow(this).apply {
            // FIXED: use a color that exists, e.g., gray_light or colorPrimary
            setBackgroundColor(ContextCompat.getColor(this@ParameterActivity, R.color.gray_light))
        }

        val headers = listOf("REF TYPE", "TYPE DESC", "REF ID", "REF DESC", "MODULE ID", "ACTION")
        val weights = floatArrayOf(1.0f, 1.5f, 1.5f, 2.0f, 1.5f, 0.8f)

        headers.forEachIndexed { index, header ->
            val textView = createTextView(header, true, weights[index])
            textView.setBackgroundColor(ContextCompat.getColor(this, R.color.cyanblue))
            textView.setTextColor(Color.WHITE)
            headerRow.addView(textView)
        }
        tableLayout.addView(headerRow)

        // Data rows
        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)
            val rowData = listOf(
                item.refType,
                item.typeDesc,
                item.refId,
                item.refDesc,
                item.moduleId,
                "Action"
            )

//            rowData.forEachIndexed { colIndex, value ->
//                val isActionColumn = colIndex == 5
//                val textView = createTextView(value, false, weights[colIndex])
//
//                if (isActionColumn) {
//                    textView.setTextColor(Color.BLUE)
//                    textView.paint.isUnderlineText = true
//                    textView.setOnClickListener {
//                        Toast.makeText(
//                            this,
//                            "Action clicked for ${item.refType} - ${item.typeDesc}",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//
//                // Alternate row colors
//                if (rowIndex % 2 == 0) {
//                    textView.setBackgroundColor(Color.parseColor("#F5F5F5"))
//                } else {
//                    textView.setBackgroundColor(Color.WHITE)
//                }
//
//                row.addView(textView)
//            }
            rowData.forEachIndexed { colIndex, value ->

                val textView = createTextView(value, false, weights[colIndex])

                // ✅ REF ID column (index 2)
                if (colIndex == 2) {

                    textView.setTextColor(Color.BLUE)
                    textView.paint.isUnderlineText = true

                    textView.setOnClickListener {

                       // val intent = Intent(this, ReferenceDetailActivity::class.java)
                        intent.putExtra("REF_ID", item.refId)
                        intent.putExtra("REF_DESC", item.refDesc)
                        intent.putExtra("TYPE_DESC", item.typeDesc)
                    // ✅ Toast message
                        Toast.makeText(this, "Clicked Ref ID: ${item.refId}",Toast.LENGTH_SHORT).show()                      //  startActivity(intent)
                    }
                }

                // alternate row color (keep existing logic)
                if (rowIndex % 2 == 0) {
                    textView.setBackgroundColor(Color.parseColor("#F5F5F5"))
                } else {
                    textView.setBackgroundColor(Color.WHITE)
                }

                row.addView(textView)
            }
            tableLayout.addView(row)
        }
    }

    private fun createTextView(text: String, isHeader: Boolean, weight: Float): TextView {
        return TextView(this).apply {
            this.text = text
            setPadding(16, 12, 16, 12)
            gravity = Gravity.CENTER_VERTICAL
            if (isHeader) {
                setTypeface(typeface, Typeface.BOLD)
                textSize = 14f
            } else {
                textSize = 13f
            }
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
            maxLines = 2
        }
    }

    // ---------- Data providers ----------
    private fun getReferenceCodeMaintenanceData(): List<ReferenceItem> {
        return listOf(
            ReferenceItem("COA_01", "ASSET", "CLASSIFICATION1", "ASSET", "COA"),
            ReferenceItem("COA_01", "LIABILITY", "CLASSIFICATION2", "LIABILITY", "COA"),
            ReferenceItem("COA_01", "INCOME", "CLASSIFICATION3", "INCOME", "COA"),
            ReferenceItem("COA_01", "EXPENSES", "CLASSIFICATION4", "EXPENSES", "COA"),
            ReferenceItem("COA_01", "PROFIT", "CLASSIFICATION5", "PROFIT", "COA"),
            ReferenceItem("COA_02", "D", "ADDITIONAL DETAILS REQUIRED3", "DEBIT", "COA"),
            ReferenceItem("COA_02", "N", "ADDITIONAL DETAILS REQUIRED2", "NO", "COA"),
            ReferenceItem("COA_02", "C", "ADDITIONAL DETAILS REQUIRED1", "CREDIT", "COA"),
            ReferenceItem("COA_03", "N", "ACCOUNT PARTITIONING2", "N", "COA"),
            ReferenceItem("COA_03", "Y", "ACCOUNT PARTITIONING1", "Y", "COA"),
            ReferenceItem("COA_04", "ACTIVE", "ACCOUNT_STATUS1", "ACTIVE", "COA"),
            ReferenceItem("COA_04", "INACTIVE", "ACCOUNT_STATUS2", "INACTIVE", "COA"),
            ReferenceItem("COA_05", "O", "OWNERSHIP1", "OFFICE", "COA"),
            ReferenceItem("COA_05", "C", "OWNERSHIP2", "CUSTOMER", "COA"),
            ReferenceItem("COA_06", "CURRENT ACCOUNT", "ACCOUNT_TYPE2", "CURRENT ACCOUNT", "COA"),
            ReferenceItem("COA_06", "SAVINGS ACCOUNT", "ACCOUNT_TYPE1", "SAVINGS ACCOUNT", "COA"),
            ReferenceItem("COA_07", "RECURRING DEPOSIT SCHEME", "SCHEME_TYPE2", "RECURRING DEPOSIT (RD) SCHEME", "COA"),
            ReferenceItem("COA_07", "FIXED DEPOSIT SCHEME", "SCHEME_TYPE1", "FIXED DEPOSIT (FD) SCHEME", "COA"),
            ReferenceItem("EMP_PRO_03", "HUMAN RESOURCES", "DEPARTMENT1", "HUMAN RESOURCES", "EMPLOYEE PROFILE"),
            ReferenceItem("EMP_PRO_03", "FINANCE", "DEPARTMENT2", "FINANCE", "EMPLOYEE PROFILE")
        )
    }

    private fun getGLTableData(): List<GLItem> {
        return listOf(
            GLItem("1", "GL001", "Cash Account", "001", "Chennai", "SH01", "Main", "INR", "10000", "5000"),
            GLItem("2", "GL002", "Bank Account", "002", "Mumbai", "SH02", "Branch", "INR", "20000", "8000")
        )
    }


    private fun getSchemeTableData(): List<SchemeItem> {
        return listOf(
            SchemeItem("FD", "001", "Deposit", "Fixed", "Fixed Deposit 1 Year", "Active"),
            SchemeItem("FD", "002", "Deposit", "Fixed", "Fixed Deposit 2 Year", "Active"),
            SchemeItem("RD", "003", "Deposit", "Recurring", "Recurring Deposit 12M", "Active"),
            SchemeItem("SA", "004", "Savings", "Premium", "Premium Savings Account", "Inactive")
        )
    }


    private fun getChartAccountsTableData(): List<ChartAccountItem> {
        return listOf(
            ChartAccountItem("ASSET", "GL001", "SCH01", "ACC001", "Cash Account", "INR", "10000", "5000", "CR","Active"),
            ChartAccountItem("LIABILITY", "GL002", "SCH02", "ACC002", "Loan Account", "INR", "2000", "8000", "DR","Active"),
            ChartAccountItem("INCOME", "GL003", "SCH03", "ACC003", "Interest Income", "INR", "15000", "2000", "CR","Active")
        )
    }


    private fun getAccountLedgerTableData(): List<AccountLedgerItem> {
        return listOf(
            AccountLedgerItem("ASSET", "ACC001", "Cash Account", "INR", "10000", "5000", "CR","Active"),
            AccountLedgerItem("LIABILITY", "ACC002", "Loan Account", "INR", "2000", "8000", "DR","Active"),
            AccountLedgerItem("INCOME", "ACC003", "Interest Income", "INR", "15000", "2000", "CR","Active")
        )
    }


    private fun getTransactionAccountsTableData(): List<TransactionAccountItem> {
        return listOf(
            TransactionAccountItem(
                "1", "Deposit", "ACC001", "Cash Account",
                "ACC002", "Bank Account", "Cash Deposit", "CR"
            ),
            TransactionAccountItem(
                "2", "Withdrawal", "ACC002", "Bank Account",
                "ACC001", "Cash Account", "ATM Withdrawal", "DR"
            ),
            TransactionAccountItem(
                "3", "Transfer", "ACC003", "Customer Account",
                "ACC004", "Vendor Account", "Fund Transfer", "TR"
            )
        )
    }
    private fun loadReferenceCodesFromAPI() {

        RetrofitClient.api.getRefList("list")
            .enqueue(object : retrofit2.Callback<RefResponse> {

                override fun onResponse(
                    call: Call<RefResponse>,
                    response: Response<RefResponse>
                ) {
                    if (response.isSuccessful) {

                        val body = response.body()

                        if (body != null && body.refList != null) {

                            val list = body.refList.map {
                                ReferenceItem(
                                    refType = it.ref_type,
                                    typeDesc = it.ref_type_desc,
                                    refId = it.ref_id,
                                    refDesc = it.ref_id_desc,
                                    moduleId = it.module_id
                                )
                            }

                            populateTable(list)

                        } else {
                            Toast.makeText(
                                this@ParameterActivity,
                                "Response empty or null",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {
                        // 🔥 THIS IS THE REAL ERROR
                        Toast.makeText(
                            this@ParameterActivity,
                            "Error Code: ${response.code()} \n${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                override fun onFailure(
                    call: retrofit2.Call<RefResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@ParameterActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }



}