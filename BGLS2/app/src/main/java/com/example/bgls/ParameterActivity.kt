package com.example.bgls

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
import androidx.appcompat.widget.PopupMenu

import com.example.bgls.databinding.ActivityParameterBinding
import retrofit2.Call
import retrofit2.Response
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.bgls.ChartOfAccounts.ChartOfAccountsDetailActivity
import com.example.bgls.DataModels.ChartOfAccountsListResponse
import com.google.android.material.navigation.NavigationView
import retrofit2.Callback
import android.app.AlertDialog
import android.view.WindowManager
import com.example.bgls.CustomerMaster.AccountLedgerActivity
import com.example.bgls.DataModels.ChartAccountApiItem
import com.example.bgls.DataModels.TransactionAccountsResponse
import okhttp3.ResponseBody
class ParameterActivity : AppCompatActivity() {

    private var moduleFromIntent: String = "Reference Code Maintenance"
    private var isFilterVisible = false
    private var currentHeaderRow: TableRow? = null
    private var currentFilterRow: TableRow? = null

    private lateinit var binding:  ActivityParameterBinding

    private lateinit var tableLayout: TableLayout

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleFileUpload(uri)
        }
    }
    private lateinit var tvTitle: TextView

    data class ReferenceItem(
        val refType: String,
        val typeDesc: String,
        val refId: String,
        val refDesc: String,
        val moduleId: String,
        val remarks: String? = null
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
        val debitBal: String,
        val balSheetGroup: String,
        val seqOrder: String,
        val noAcctOpened: String,
        val noAcctClosed: String,
        val totalBalance: String
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

    private var currentModule: String = "Reference Code Maintenance"

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding =  ActivityParameterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        tvTitle = binding.tvTitle
        tableLayout = binding.tableLayout

        createModuleButtons()
        moduleFromIntent = intent.getStringExtra("MODULE_NAME")
            ?: "Reference Code Maintenance"

        loadModuleData(moduleFromIntent)

        setupHeaderActions()

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun setupHeaderActions() {
        binding.btnAdd.setOnClickListener {
            when (currentModule) {
                "Reference Code Maintenance" -> {
                    val intent = Intent(this, com.example.bgls.ReferenceDetailAddActivity::class.java)
                    startActivity(intent)
                }
                "GL Structure" -> {
                    val intent = Intent(this, com.example.bgls.GLStructureAddActivity::class.java)
                    startActivity(intent)
                }
                "Scheme Codes"-> {
                    val intent = Intent(this, com.example.bgls.SchemeCodeAddActivity::class.java)
                    startActivity(intent)
                }
                "Chart of Accounts" -> {
                    val intent = Intent(this, com.example.bgls.ChartOfAccounts.ChartOfAccountsAddActivity::class.java)
                    startActivity(intent)
                }
                "Transaction Accounts" -> {
                    val intent = Intent(this, com.example.bgls.ChartOfAccounts.TransactionAccountAddActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Add functionality for $currentModule", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnFilter.setOnClickListener {
            isFilterVisible = !isFilterVisible
            currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
            currentFilterRow?.visibility = if (isFilterVisible) View.VISIBLE else View.GONE
            
            if (isFilterVisible) {
                applyFilters()
            } else {
                clearFilters()
                applyFilters()
            }
        }

        binding.btnUpload.setOnClickListener {
            if (currentModule == "GL Structure") {
                // Launch file picker for GL Structure
                filePickerLauncher.launch("*/*") 
            } else {
                Toast.makeText(this, "Upload functionality for $currentModule", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleFileUpload(uri: Uri) {
        // In a real implementation, you would use a library like Retrofit or Volley 
        // to send the file at 'uri' to your server.
        
        Toast.makeText(this, "Processing file: ${uri.lastPathSegment}", Toast.LENGTH_SHORT).show()

        // Simulate a network upload delay
        binding.root.postDelayed({
            android.app.AlertDialog.Builder(this)
                .setTitle("Success")
                .setMessage("The GL Structure has been successfully uploaded and processed.")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("OK") { dialog, _ -> 
                    dialog.dismiss()
                    // Refresh data if needed
                    loadModuleData(currentModule)
                }
                .show()
        }, 2000)
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

        binding.moduleButtonsContainer.removeAllViews()

        for (module in modules) {
            val button = Button(this).apply {
                text = module
                background = ContextCompat.getDrawable(context, R.drawable.tab_unselected)
                setTextColor(Color.BLACK)
                isAllCaps = false

                setPadding(60, 20, 60, 20)

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 24
                }

                setOnClickListener {
                    loadModuleData(module)
                    updateButtonSelection(this)
                }
            }

            binding.moduleButtonsContainer.addView(button)
        }

        // ✅ SELECT BUTTON AFTER ALL BUTTONS ADDED
        binding.moduleButtonsContainer.post {
            for (i in 0 until binding.moduleButtonsContainer.childCount) {
                val btn = binding.moduleButtonsContainer.getChildAt(i) as Button
                if (btn.text.toString() == moduleFromIntent) {
                    updateButtonSelection(btn)
                    break
                }
            }
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
                btn.setTextColor(Color.BLACK)
            }
        }
    }

    private fun loadModuleData(moduleName: String) {
        currentModule = moduleName
        tvTitle.text = moduleName

        // Show Upload button only for GL Structure
        binding.btnUpload.visibility = if (moduleName == "GL Structure") View.VISIBLE else View.GONE

        // Hide Add button for Account Ledger
        binding.btnAdd.visibility = if (moduleName == "Account Ledger") View.GONE else View.VISIBLE

        when (moduleName) {
            "Reference Code Maintenance" -> {
                loadReferenceCodesFromAPI()   // ✅ CALL API HERE
            }
            "GL Structure" -> {
                loadGLStructureFromAPI()
            }
            "Scheme Codes" -> {
                loadSchemeCodesFromAPI()
            }
            "Chart of Accounts" -> {
                loadChartOfAccountsFromAPI()
            }
            "Account Ledger" -> {
                loadAccountLedgerFromAPI()
            }
            "Transaction Accounts" -> {
                loadTransactionAccountsFromAPI()
            }
            else -> {
                Toast.makeText(
                    this,
                    "No handler for $moduleName",
                    Toast.LENGTH_SHORT
                ).show()
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
            tv.setBackgroundColor(Color.WHITE)
            tv.setTextColor(Color.BLACK)
            headerRow.addView(tv)
        }

        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEach {
            filterRow.addView(createFilterEditText(it, 1f))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

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
                "Action ▼"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                if (index == 1) { // EVENT
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        val intent = Intent(this@ParameterActivity, com.example.bgls.ChartOfAccounts.TransactionAccountViewActivity::class.java)
                        intent.putExtra("MODE", "VIEW")
                        startActivity(intent)
                    }
                }

                // ACTION column
                if (index == values.lastIndex) {
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true

                    tv.setOnClickListener { view ->
                        val popup = PopupMenu(this@ParameterActivity, view)
                        popup.menu.add("View")
                        popup.menu.add("Edit")
                        popup.menu.add("Delete")

                        popup.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.title) {
                                "View" -> {
                                    val intent = Intent(this@ParameterActivity, com.example.bgls.ChartOfAccounts.TransactionAccountViewActivity::class.java)
                                    intent.putExtra("ID", item.id)
                                    startActivity(intent)
                                }
                                "Edit" -> {
                                    val intent = Intent(this@ParameterActivity, com.example.bgls.ChartOfAccounts.TransactionAccountModifyActivity::class.java)
                                    intent.putExtra("ID", item.id)
                                    startActivity(intent)
                                }
                                "Delete" -> {
                                    android.app.AlertDialog.Builder(this@ParameterActivity)
                                        .setTitle("Delete Account")
                                        .setMessage("Are you sure you want to delete this Transaction Account?")
                                        .setPositiveButton("Yes") { _, _ ->
                                            RetrofitClient.api.getTransactionsAccounts("delete", item.id.toLongOrNull())
                                                .enqueue(object : Callback<Map<String, Any>> {
                                                    override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                                                        if (response.isSuccessful) {
                                                            Toast.makeText(this@ParameterActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                                            loadTransactionAccountsFromAPI()
                                                        } else {
                                                            Toast.makeText(this@ParameterActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                                        Toast.makeText(this@ParameterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                        }
                                        .setNegativeButton("No", null)
                                        .show()
                                }
                            }
                            true
                        }

                        popup.show()
                    }
                }

                // alternate row colors
                tv.setBackgroundColor(android.graphics.Color.WHITE)

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
            tv.setBackgroundColor(Color.WHITE)
            tv.setTextColor(Color.BLACK)
            headerRow.addView(tv)
        }

        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEach {
            filterRow.addView(createFilterEditText(it, 1f))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

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

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                if (index == 1) { // ACCT ID column
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        val intent = Intent(this@ParameterActivity, AccountLedgerActivity::class.java)
                        intent.putExtra("acct_num", item.acctId)
                        startActivity(intent)
                    }
                }

                if (index == 7) { // STATUS
                    if (value.equals("Active", ignoreCase = true)) {
                        tv.setTextColor(Color.parseColor("#4CAF50"))
                    } else {
                        tv.setTextColor(Color.RED)
                    }
                }

                // alternate row color
                tv.setBackgroundColor(android.graphics.Color.WHITE)

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateChartAccountsTable(data: List<ChartAccountItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "HEAD", "GL", "SCHEME CODE", "ACCT ID", "ACCT NAME",
            "CURRENCY", "CREDITS", "DEBITS", "BALANCE", "STATUS", "ACTION"
        )

        val headerRow = TableRow(this)
        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(Color.WHITE)
            tv.setTextColor(Color.BLACK)
            headerRow.addView(tv)
        }
        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEach {
            filterRow.addView(createFilterEditText(it, 1f))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)
            val values = listOf(
                item.head, item.gl, item.schemeCode, item.acctId, item.acctName,
                item.currency, item.credits, item.debits, item.balance, item.status, "Action ▼"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                // ACCT ID column (index 3) – click to view
                if (index == 3) {
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        val intent = Intent(this@ParameterActivity, ChartOfAccountsDetailActivity::class.java)
                        intent.putExtra("MODE", "VIEW")
                        intent.putExtra("ACCT_NUM", item.acctId)
                        startActivity(intent)
                    }
                }

                // STATUS column (index 9) – color
                if (index == 9) {
                    if (value.equals("Active", ignoreCase = true)) {
                        tv.setTextColor(Color.parseColor("#4CAF50"))
                    } else {
                        tv.setTextColor(Color.RED)
                    }
                }

                // ACTION column (last index)
                if (index == values.lastIndex) {
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener { view ->
                        val popup = PopupMenu(this@ParameterActivity, view)
                        popup.menu.add("View")
                        popup.menu.add("Modify")
                        popup.menu.add("Verify")
                        popup.menu.add("Delete")
                        popup.setOnMenuItemClickListener { menuItem ->
                            val intent = Intent(this@ParameterActivity, ChartOfAccountsDetailActivity::class.java)
                            when (menuItem.title.toString()) {
                                "View" -> {
                                    intent.putExtra("MODE", "VIEW")
                                    intent.putExtra("ACCT_NUM", item.acctId)
                                    startActivity(intent)
                                }
                                "Modify" -> {
                                    intent.putExtra("MODE", "MODIFY")
                                    intent.putExtra("ACCT_NUM", item.acctId)
                                    startActivity(intent)
                                }
                                "Verify" -> {
                                    intent.putExtra("MODE", "VERIFY")
                                    intent.putExtra("ACCT_NUM", item.acctId)
                                    startActivity(intent)
                                }
                                "Delete" -> {
                                    AlertDialog.Builder(this@ParameterActivity)
                                        .setTitle("Delete Account")
                                        .setMessage("Are you sure you want to delete ${item.acctId}?")
                                        .setPositiveButton("Yes") { _, _ ->
                                            RetrofitClient.api.deleteChartOfAccount(item.acctId)
                                                .enqueue(object : Callback<ResponseBody> {
                                                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                                        if (response.isSuccessful) {
                                                            Toast.makeText(this@ParameterActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                                            loadChartOfAccountsFromAPI() // refresh list
                                                        } else {
                                                            Toast.makeText(this@ParameterActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                        Toast.makeText(this@ParameterActivity, t.message, Toast.LENGTH_SHORT).show()
                                                    }
                                                })
                                        }
                                        .setNegativeButton("No", null)
                                        .show()
                                }
                            }
                            true
                        }
                        popup.show()
                    }
                }

                // Alternate row background
                tv.setBackgroundColor(android.graphics.Color.WHITE)
                row.addView(tv)
            }
            tableLayout.addView(row)
        }
    }

    private fun populateSchemeTable(data: List<SchemeItem>) {
        tableLayout.removeAllViews()

        val headers = listOf(
            "PRODUCT", "ID", "CATEGORY", "TYPE", "DESCRIPTION", "STATUS", "ACTION"
        )

        val headerRow = TableRow(this)

        headers.forEach {
            val tv = createTextView(it, true, 1f)
            tv.setBackgroundColor(Color.WHITE)
            tv.setTextColor(Color.BLACK)
            headerRow.addView(tv)
        }

        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEach {
            filterRow.addView(createFilterEditText(it, 1f))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

        data.forEachIndexed { rowIndex, item ->
            val row = TableRow(this)

            val values = listOf(
                item.product,
                item.id,
                item.category,
                item.type,
                item.description,
                item.status,
                "Action ▼"
            )

            values.forEachIndexed { index, value ->
                val tv = createTextView(value, false, 1f)

                if (index == 1) { // "ID" column
                    tv.setTextColor(Color.BLUE)
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener {
                        val intent = Intent(this@ParameterActivity, SchemeCodeViewActivity::class.java).apply {
                            putExtra("MODE", "VIEW")
                            putExtra("PRODUCT", item.product)
                            putExtra("ID", item.id)
                            putExtra("CATEGORY", item.category)
                            putExtra("TYPE", item.type)
                            putExtra("DESCRIPTION", item.description)
                            putExtra("STATUS", item.status)
                        }
                        startActivity(intent)
                    }
                }

                if (index == values.lastIndex) {
                    tv.setTextColor(Color.parseColor("#2196F3"))
                    tv.paint.isUnderlineText = true
                    tv.setOnClickListener { view ->
                        val popup = PopupMenu(this@ParameterActivity, view)
                        popup.menu.add("View")
                        popup.menu.add("Modify")
                        popup.menu.add("Delete")
                        popup.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.title.toString()) {
                                "View" -> {
                                    val intent = Intent(this@ParameterActivity, SchemeCodeViewActivity::class.java).apply {
                                        putExtra("MODE", "VIEW")
                                        putExtra("PRODUCT", item.product)
                                        putExtra("ID", item.id)
                                        putExtra("CATEGORY", item.category)
                                        putExtra("TYPE", item.type)
                                        putExtra("DESCRIPTION", item.description)
                                        putExtra("STATUS", item.status)
                                    }
                                    startActivity(intent)
                                }
                                "Modify" -> {
                                    val intent = Intent(this@ParameterActivity, SchemeCodeViewActivity::class.java).apply {
                                        putExtra("MODE", "MODIFY")
                                        putExtra("PRODUCT", item.product)
                                        putExtra("ID", item.id)
                                        putExtra("CATEGORY", item.category)
                                        putExtra("TYPE", item.type)
                                        putExtra("DESCRIPTION", item.description)
                                        putExtra("STATUS", item.status)
                                    }
                                    startActivity(intent)
                                }
                                "Delete" -> {
                                    AlertDialog.Builder(this@ParameterActivity)
                                        .setTitle("Delete Scheme")
                                        .setMessage("Are you sure you want to delete ${item.id}?")
                                        .setPositiveButton("Yes") { _, _ ->
                                            RetrofitClient.api.deleteParameter(
                                                com.example.bgls.DataModels.SchemeCode(id = item.id), "delete"
                                            ).enqueue(object : Callback<ResponseBody> {
                                                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                                    if (response.isSuccessful) {
                                                        Toast.makeText(this@ParameterActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                                        loadSchemeCodesFromAPI()
                                                    } else {
                                                        Toast.makeText(this@ParameterActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                                    Toast.makeText(this@ParameterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                        }
                                        .setNegativeButton("No", null)
                                        .show()
                                }
                            }
                            true
                        }
                        popup.show()
                    }
                }

                // alternate row color
                tv.setBackgroundColor(android.graphics.Color.WHITE)

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
            tv.setBackgroundColor(Color.WHITE)
            tv.setTextColor(Color.BLACK)
            headerRow.addView(tv)
        }

        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEach {
            filterRow.addView(createFilterEditText(it, 1f))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

        // Use forEachIndexed instead of forEach to get rowIndex
        data.forEachIndexed { rowIndex, item ->
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

                    // Add popup menu on click
                    tv.setOnClickListener { view ->
                        val popupMenu = PopupMenu(this@ParameterActivity, view)
                        popupMenu.menu.add("View")
                        popupMenu.menu.add("Modify")
                        popupMenu.menu.add("Delete")

                        popupMenu.setOnMenuItemClickListener { menuItem ->
                            when (menuItem.title.toString()) {
                                "View" -> {
                                    Toast.makeText(
                                        this@ParameterActivity,
                                        "View clicked for GL: ${item.glCode}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@ParameterActivity,
                                        GLStructureViewActivity::class.java
                                    )

                                    intent.putExtra("branchId", item.branchId)
                                    intent.putExtra("branchDesc", item.branchDesc)
                                    intent.putExtra("glCode", item.glCode)
                                    intent.putExtra("glDesc", item.glDesc)
                                    intent.putExtra("glshCode", item.glshCode)
                                    intent.putExtra("glshDesc", item.glshDesc)
                                    intent.putExtra("currencyCode", item.crncyCode)
                                    intent.putExtra("creditBal", item.creditBal)
                                    intent.putExtra("debitBal", item.debitBal)
                                    intent.putExtra("balanceGroup", item.balSheetGroup)
                                    intent.putExtra("sequence", item.seqOrder)
                                    intent.putExtra("totalBalance", item.totalBalance)
                                    intent.putExtra("accountOpen", item.noAcctOpened)
                                    intent.putExtra("accountClose", item.noAcctClosed)
                                    startActivity(intent)
                                }
                                "Modify" ->  {
                                    Toast.makeText(
                                        this@ParameterActivity,
                                        "View clicked for GL: ${item.glCode}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@ParameterActivity,
                                        GLStructureModifyActivity::class.java
                                    )

                                    intent.putExtra("branchId", item.branchId)
                                    intent.putExtra("branchDesc", item.branchDesc)
                                    intent.putExtra("glCode", item.glCode)
                                    intent.putExtra("glDesc", item.glDesc)
                                    intent.putExtra("glshCode", item.glshCode)
                                    intent.putExtra("glshDesc", item.glshDesc)
                                    intent.putExtra("currencyCode", item.crncyCode)
                                    intent.putExtra("creditBal", item.creditBal)
                                    intent.putExtra("debitBal", item.debitBal)
                                    intent.putExtra("balanceGroup", item.balSheetGroup)
                                    intent.putExtra("sequence", item.seqOrder)
                                    intent.putExtra("totalBalance", item.totalBalance)
                                    intent.putExtra("accountOpen", item.noAcctOpened)
                                    intent.putExtra("accountClose", item.noAcctClosed)

                                    startActivity(intent)
                                }
                                "Delete" -> {
                                    Toast.makeText(
                                        this@ParameterActivity,
                                        "View clicked for GL: ${item.glCode}",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@ParameterActivity,
                                        GLStructureDeleteActivity::class.java
                                    )

                                    intent.putExtra("branchId", item.branchId)
                                    intent.putExtra("branchDesc", item.branchDesc)
                                    intent.putExtra("glCode", item.glCode)
                                    intent.putExtra("glDesc", item.glDesc)
                                    intent.putExtra("glshCode", item.glshCode)
                                    intent.putExtra("glshDesc", item.glshDesc)
                                    intent.putExtra("currencyCode", item.crncyCode)
                                    intent.putExtra("creditBal", item.creditBal)
                                    intent.putExtra("debitBal", item.debitBal)
                                    intent.putExtra("balanceGroup", item.balSheetGroup)
                                    intent.putExtra("sequence", item.seqOrder)
                                    intent.putExtra("totalBalance", item.totalBalance)
                                    intent.putExtra("accountOpen", item.noAcctOpened)
                                    intent.putExtra("accountClose", item.noAcctClosed)

                                    startActivity(intent)
                                }
                            }
                            true
                        }

                        popupMenu.show()
                    }
                }

                // Alternate row colors - now rowIndex is available
                tv.setBackgroundColor(android.graphics.Color.WHITE)

                row.addView(tv)
            }

            tableLayout.addView(row)
        }
    }

    private fun populateTable(data: List<ReferenceItem>) {
        tableLayout.removeAllViews()

        // Header row
        val headerRow = TableRow(this).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
        }

        val headers = listOf(
            "REF TYPE",
            "TYPE DESC",
            "REF ID",
            "REF DESC",
            "MODULE ID",
            "ACTION"
        )

        val weights = floatArrayOf(
            1.0f,
            1.5f,
            1.5f,
            2.0f,
            1.5f,
            1.0f
        )

        // Header create
        headers.forEachIndexed { index, header ->
            val textView = createTextView(
                header,
                true,
                weights[index]
            )

            textView.setBackgroundColor(Color.WHITE)
            textView.setTextColor(Color.BLACK)


            headerRow.addView(textView)
        }

        currentHeaderRow = headerRow
        currentHeaderRow?.visibility = if (isFilterVisible) View.GONE else View.VISIBLE
        tableLayout.addView(headerRow)

        val filterRow = TableRow(this).apply {
            setBackgroundColor(Color.parseColor("#E0E5E9"))
            visibility = if (isFilterVisible) View.VISIBLE else View.GONE
        }
        headers.forEachIndexed { index, header ->
            filterRow.addView(createFilterEditText(header, weights[index]))
        }
        currentFilterRow = filterRow
        tableLayout.addView(filterRow)

        // Data rows
        data.forEachIndexed { rowIndex, item ->

            val row = TableRow(this)

            val rowData = listOf(
                item.refType,
                item.typeDesc,
                item.refId,
                item.refDesc,
                item.moduleId
            )

            // First 5 columns
            for (colIndex in 0 until 5) {

                val value = rowData[colIndex]

                val textView = createTextView(
                    value,
                    false,
                    weights[colIndex]
                )

                // REF ID clickable (column index = 2)
                if (colIndex == 2) {

                    textView.setTextColor(Color.BLUE)
                    textView.paint.isUnderlineText = true

                    textView.setOnClickListener  {
                        val intent = Intent(
                            this@ParameterActivity,
                            ReferenceDetailViewActivity::class.java
                        )

                        intent.putExtra("refId", item.refId)
                        intent.putExtra("refType", item.refType)
                        intent.putExtra("typeDesc", item.typeDesc)
                        intent.putExtra("refDes", item.refDesc)
                        intent.putExtra("moduleId", item.moduleId)
                        intent.putExtra("remarks", item.remarks)

                        startActivity(intent)
                    }
                }

                // Alternate row colors
                textView.setBackgroundColor(android.graphics.Color.WHITE)

                row.addView(textView)
            }

            // ACTION column
            val actionText = createTextView(
                "Action",
                false,
                weights[5]
            )

            actionText.setTextColor(Color.BLUE)
            actionText.paint.isUnderlineText = true

            actionText.setOnClickListener { view ->
                // Create popup menu
                val popupMenu = PopupMenu(this@ParameterActivity, view)
                popupMenu.menu.add("View")
                popupMenu.menu.add("Modify")
                popupMenu.menu.add("Delete")

                popupMenu.setOnMenuItemClickListener { menuItem ->

                    when (menuItem.title.toString()) {

                        "View" -> {
                            val intent = Intent(
                                this@ParameterActivity,
                                ReferenceDetailViewActivity::class.java
                            )

                            intent.putExtra("refId", item.refId)
                            intent.putExtra("refType", item.refType)
                            intent.putExtra("typeDesc", item.typeDesc)
                            intent.putExtra("refDes", item.refDesc)
                            intent.putExtra("moduleId", item.moduleId)
                            intent.putExtra("remarks", item.remarks)

                            startActivity(intent)
                        }

                        "Modify" -> {
                            val intent = Intent(
                                this@ParameterActivity,
                                ReferenceDetailModifyActivity::class.java
                            )

                            intent.putExtra("refId", item.refId)
                            intent.putExtra("refType", item.refType)
                            intent.putExtra("typeDesc", item.typeDesc)
                            intent.putExtra("refDes", item.refDesc)
                            intent.putExtra("moduleId", item.moduleId)
                            intent.putExtra("remarks", item.remarks)

                            startActivity(intent)
                        }

                        "Delete" -> {
                            val intent = Intent(
                                this@ParameterActivity,
                                ReferenceDetailDeleteActivity::class.java
                            )

                            intent.putExtra("refId", item.refId)
                            intent.putExtra("refType", item.refType)
                            intent.putExtra("typeDesc", item.typeDesc)
                            intent.putExtra("refDes", item.refDesc)
                            intent.putExtra("moduleId", item.moduleId)
                            intent.putExtra("remarks", item.remarks)

                            startActivity(intent)
                        }
                    }

                    true
                }

                popupMenu.show()
            }

            actionText.setBackgroundColor(android.graphics.Color.WHITE)

            row.addView(actionText)

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
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight).apply {
                setMargins(2, 2, 2, 2)
            }
            maxLines = 2
        }
    }

    private fun createFilterEditText(hintText: String, weight: Float): View {
        val actionHints = listOf("Action ▼", "ACTIONS", "ACTION", "Select")
        if (actionHints.any { it.equals(hintText, ignoreCase = true) }) {
            return TextView(this).apply {
                text = hintText
                setPadding(8, 8, 8, 8)
                gravity = Gravity.CENTER
                setTypeface(typeface, Typeface.BOLD)
                textSize = 10f
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight).apply {
                    setMargins(2, 2, 2, 2)
                }
            }
        }
        return android.widget.EditText(this).apply {
            hint = hintText
            setPadding(8, 8, 8, 8)
            gravity = Gravity.CENTER_VERTICAL
            textSize = 10f
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight).apply {
                setMargins(2, 2, 2, 2)
            }
            setBackgroundResource(R.drawable.edittext_bg)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            setSingleLine(true)
            
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (isFilterVisible) applyFilters()
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    applyFilters()
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(windowToken, 0)
                    true
                } else false
            }
        }
    }

    private fun applyFilters() {
        if (currentFilterRow == null) return
        
        val filterTexts = mutableListOf<String>()
        for (i in 0 until currentFilterRow!!.childCount) {
            val child = currentFilterRow!!.getChildAt(i)
            if (child is android.widget.EditText) {
                filterTexts.add(child.text.toString().trim().lowercase())
            } else {
                filterTexts.add("")
            }
        }
        
        for (i in 2 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            var match = true
            for (j in 0 until row.childCount) {
                val filterText = filterTexts.getOrNull(j) ?: ""
                if (filterText.isNotEmpty()) {
                    val cellText = (row.getChildAt(j) as? TextView)?.text?.toString()?.lowercase() ?: ""
                    if (!cellText.contains(filterText)) {
                        match = false
                        break
                    }
                }
            }
            row.visibility = if (match) View.VISIBLE else View.GONE
        }
    }

    private fun clearFilters() {
        if (currentFilterRow == null) return
        for (i in 0 until currentFilterRow!!.childCount) {
            val child = currentFilterRow!!.getChildAt(i)
            if (child is android.widget.EditText) {
                child.text.clear()
            }
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
            GLItem("1", "GL001", "Cash Account", "001", "Chennai", "SH01", "Main", "INR", "10000", "5000", "Group1", "1", "10", "0", "15000"),
            GLItem("2", "GL002", "Bank Account", "002", "Mumbai", "SH02", "Branch", "INR", "20000", "8000", "Group2", "2", "20", "5", "25000")
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

                        if (body != null) {

                            val list = body.refList?.map {
                                ReferenceItem(
                                    refType = it.ref_type,
                                    typeDesc = it.ref_type_desc,
                                    refId = it.ref_id,
                                    refDesc = it.ref_id_desc,
                                    moduleId = it.module_id,
                                    remarks = it.remarks
                                )
                            } ?: emptyList()

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

    private fun loadGLStructureFromAPI() {
        RetrofitClient.api.getGLCode("list", null, null)
            .enqueue(object : retrofit2.Callback<com.example.bgls.DataModels.GLResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.bgls.DataModels.GLResponse>, response: retrofit2.Response<com.example.bgls.DataModels.GLResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val list = body.getvaluelist?.mapIndexed { index, it ->
                                GLItem(
                                    sNo = (index + 1).toString(),
                                    glCode = it.glCode ?: "",
                                    glDesc = it.gl_type_description ?: it.glDescription ?: "",
                                    branchId = it.branch_id ?: "",
                                    branchDesc = it.branch_desc ?: "",
                                    glshCode = it.glsh_code ?: "",
                                    glshDesc = it.glsh_desc ?: "",
                                    crncyCode = it.crncy_code ?: "",
                                    creditBal = it.total_balance ?: "0.00",
                                    debitBal = it.total_balance ?: "0.00",
                                    balSheetGroup = it.bal_sheet_group ?: "",
                                    seqOrder = it.seq_order ?: "",
                                    noAcctOpened = it.no_acct_opened ?: "",
                                    noAcctClosed = it.no_acct_closed ?: "",
                                    totalBalance = it.total_balance ?: ""
                                )
                            } ?: emptyList()
                            populateGLTable(list)
                        }
                    } else {
                        Toast.makeText(this@ParameterActivity, "GL Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.example.bgls.DataModels.GLResponse>, t: Throwable) {
                    Toast.makeText(this@ParameterActivity, "GL Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadSchemeCodesFromAPI() {
        RetrofitClient.api.getParameters("list")
            .enqueue(object : retrofit2.Callback<com.example.bgls.DataModels.SchemeResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.bgls.DataModels.SchemeResponse>, response: retrofit2.Response<com.example.bgls.DataModels.SchemeResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            val list = body.lms_schemes?.map {
                                val rawStatus = it.state ?: it.entityFlg ?: ""
                                val displayStatus = when (rawStatus.uppercase()) {
                                    "Y" -> "Active"
                                    "N" -> "Inactive"
                                    "ACTIVE" -> "Active"
                                    "INACTIVE" -> "Inactive"
                                    else -> rawStatus
                                }

                                SchemeItem(
                                    product = it.product ?: "",
                                    id = it.id ?: "",
                                    category = it.productCategory ?: it.category ?: "",
                                    type = it.productType ?: it.type ?: "",
                                    description = it.productDescription ?: it.description ?: "",
                                    status = displayStatus
                                )
                            } ?: emptyList()
                            populateSchemeTable(list)
                        }
                    } else {
                        Toast.makeText(this@ParameterActivity, "Scheme Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.example.bgls.DataModels.SchemeResponse>, t: Throwable) {
                    Toast.makeText(this@ParameterActivity, "Scheme Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun loadChartOfAccountsFromAPI() {
        RetrofitClient.api.getChartOfAccountsList()
            .enqueue(object : Callback<ChartOfAccountsListResponse> {
                override fun onResponse(
                    call: Call<ChartOfAccountsListResponse>,
                    response: Response<ChartOfAccountsListResponse>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.chartaccount?.map { item ->
                            ChartAccountItem(
                                head = item.classification ?: "",
                                gl = item.gl_code ?: "",
                                schemeCode = item.schm_code ?: "",
                                acctId = item.acct_num ?: "",
                                acctName = item.acct_name ?: "",
                                currency = item.acct_crncy ?: "",
                                credits = item.cr_amt ?: "0",
                                debits = item.dr_amt ?: "0",
                                balance = item.acct_bal ?: "0",
                                status = if (item.entity_flg == "Y") "Active" else "Inactive"
                            )
                        } ?: emptyList()
                        populateChartAccountsTable(list)
                    } else {
                        Toast.makeText(this@ParameterActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ChartOfAccountsListResponse>, t: Throwable) {
                    Toast.makeText(this@ParameterActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun loadAccountLedgerFromAPI() {
        val type = "O" // "O" for Office, "C" for Customer
        RetrofitClient.api.filterChartOfAccounts(type)
            .enqueue(object : Callback<List<com.example.bgls.DataModels.ChartAccountItem>> {
                override fun onResponse(
                    call: Call<List<com.example.bgls.DataModels.ChartAccountItem>>,
                    response: Response<List<com.example.bgls.DataModels.ChartAccountItem>>
                ) {
                    if (response.isSuccessful) {
                        val list = response.body()?.map { item ->
                            AccountLedgerItem(
                                head = item.classification ?: "",
                                acctId = item.acct_num ?: "",
                                acctName = item.acct_name ?: "",
                                currency = item.acct_crncy ?: "",
                                credits = item.cr_amt ?: "0",
                                debits = item.dr_amt ?: "0",
                                balance = item.acct_bal ?: "0",
                                status = if (item.entity_flg == "Y") "Active" else "Inactive"
                            )
                        } ?: emptyList()
                        populateAccountLedgerTable(list)
                    } else {
                        Toast.makeText(this@ParameterActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<com.example.bgls.DataModels.ChartAccountItem>>, t: Throwable) {
                    Toast.makeText(this@ParameterActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun loadTransactionAccountsFromAPI() {
        RetrofitClient.api.getTransactionAccountsList("list")
            .enqueue(object : Callback<TransactionAccountsResponse> {
                override fun onResponse(
                    call: Call<TransactionAccountsResponse>,
                    response: Response<TransactionAccountsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val apiList = response.body()!!.list ?: emptyList()
                        val localList = apiList.map { item ->
                            TransactionAccountItem(
                                id = item.id ?: "",
                                event = item.event ?: "",
                                debitAccNo = item.debitAccountNumber ?: "",
                                debitAccName = item.debitAccountName ?: "",
                                creditAccNo = item.creditAccountNumber ?: "",
                                creditAccName = item.creditAccountName ?: "",
                                tranParticular = item.tranParticular ?: "",
                                type = item.accountType ?: ""
                            )
                        }
                        populateTransactionAccountsTable(localList)
                    } else {
                        Toast.makeText(this@ParameterActivity,
                            "Failed to load transaction accounts: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TransactionAccountsResponse>, t: Throwable) {
                    Toast.makeText(this@ParameterActivity,
                        "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    override fun onResume() {
        super.onResume()
        loadModuleData(currentModule)
    }
}

