package com.example.bgls.ChartOfAccounts

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.ChartOfAccountsDetailResponse
import com.example.bgls.DataModels.RefCodeItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChartOfAccountsDetailActivity : AppCompatActivity() {

    // Views
    private lateinit var tvDetailTitle: TextView
    private lateinit var btnSubmit: Button
    private lateinit var btnVerify: Button
    private lateinit var btnDelete: Button

    // Left column
    private lateinit var spinClassification: Spinner
    private lateinit var etGeneralLedger: EditText
    private lateinit var etGLSubHead: EditText
    private lateinit var spinSchemeType: Spinner
    private lateinit var etAccountID: EditText
    private lateinit var spinAdditionalDetails: Spinner
    private lateinit var etAccountCurrency: EditText
    private lateinit var etRefCode: EditText
    private lateinit var etReportCode: EditText
    private lateinit var spinOwnership: Spinner
    private lateinit var etTotalCreditBalance: EditText
    private lateinit var etAccountBalance: EditText

    // Right column
    private lateinit var spinAccountType: Spinner
    private lateinit var etGLDescription: EditText
    private lateinit var etGLSubHeadDescription: EditText
    private lateinit var etSchemeCode: EditText
    private lateinit var etAccountName: EditText
    private lateinit var spinAccountPartitioning: Spinner
    private lateinit var etHomeCurrency: EditText
    private lateinit var etRefDescription: EditText
    private lateinit var spinAccountStatus: Spinner
    private lateinit var etRemarks: EditText
    private lateinit var etTotalDebitBalance: EditText
    private lateinit var etRefCurrencyBalance: EditText

    private lateinit var progressBar: ProgressBar
    private lateinit var scrollView: ScrollView

    private var currentMode = "VIEW"   // VIEW, MODIFY, VERIFY, DELETE
    private var currentAcctNum: String? = null

    // Store reference lists for spinners
    private var chart1List = listOf<RefCodeItem>()
    private var chart2List = listOf<RefCodeItem>()
    private var chart3List = listOf<RefCodeItem>()
    private var chart4List = listOf<RefCodeItem>()
    private var chart5List = listOf<RefCodeItem>()
    private var chart6List = listOf<RefCodeItem>()
    private var chart7List = listOf<RefCodeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_of_accounts_detail)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        currentMode = intent.getStringExtra("MODE") ?: "VIEW"
        currentAcctNum = intent.getStringExtra("ACCT_NUM")
        initViews()
        setupModeUI()
        loadData()
    }

    private fun initViews() {
        tvDetailTitle = findViewById(R.id.tvDetailTitle)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnVerify = findViewById(R.id.btnVerify)
        btnDelete = findViewById(R.id.btnDelete)
       // progressBar = ProgressBar(this).apply { visibility = View.GONE }
        progressBar = findViewById(R.id.progressBar)
        scrollView = findViewById(R.id.scrollView) // you need to add this id to your ScrollView

        // Left column
        spinClassification = findViewById(R.id.spinClassification)
        etGeneralLedger = findViewById(R.id.etGeneralLedger)
        etGLSubHead = findViewById(R.id.etGLSubHead)
        spinSchemeType = findViewById(R.id.spinSchemeType)
        etAccountID = findViewById(R.id.etAccountID)
        spinAdditionalDetails = findViewById(R.id.spinAdditionalDetails)
        etAccountCurrency = findViewById(R.id.etAccountCurrency)
        etRefCode = findViewById(R.id.etRefCode)
        etReportCode = findViewById(R.id.etReportCode)
        spinOwnership = findViewById(R.id.spinOwnership)
        etTotalCreditBalance = findViewById(R.id.etTotalCreditBalance)
        etAccountBalance = findViewById(R.id.etAccountBalance)

        // Right column
        spinAccountType = findViewById(R.id.spinAccountType)
        etGLDescription = findViewById(R.id.etGLDescription)
        etGLSubHeadDescription = findViewById(R.id.etGLSubHeadDescription)
        etSchemeCode = findViewById(R.id.etSchemeCode)
        etAccountName = findViewById(R.id.etAccountName)
        spinAccountPartitioning = findViewById(R.id.spinAccountPartitioning)
        etHomeCurrency = findViewById(R.id.etHomeCurrency)
        etRefDescription = findViewById(R.id.etRefDescription)
        spinAccountStatus = findViewById(R.id.spinAccountStatus)
        etRemarks = findViewById(R.id.etRemarks)
        etTotalDebitBalance = findViewById(R.id.etTotalDebitBalance)
        etRefCurrencyBalance = findViewById(R.id.etRefCurrencyBalance)

        btnSubmit.setOnClickListener { submitModify() }
        btnVerify.setOnClickListener { verifyAccount() }
        btnDelete.setOnClickListener { confirmDelete() }
        findViewById<ImageView>(R.id.btnBack)?.setOnClickListener { finish() }
        findViewById<ImageView>(R.id.btnHome)?.setOnClickListener {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun setupModeUI() {
        when (currentMode) {
            "VIEW" -> {
                tvDetailTitle.text = "CHART OF ACCOUNTS - VIEW"
                setFieldsEnabled(false)
                btnSubmit.visibility = View.GONE
                btnVerify.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
            "MODIFY" -> {
                tvDetailTitle.text = "CHART OF ACCOUNTS - MODIFY"
                setFieldsEnabled(true)
                btnSubmit.visibility = View.VISIBLE
                btnVerify.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
            "VERIFY" -> {
                tvDetailTitle.text = "CHART OF ACCOUNTS - VERIFY"
                setFieldsEnabled(false)   // fields are read‑only, user just clicks Verify
                btnSubmit.visibility = View.GONE
                btnVerify.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
            }
            "DELETE" -> {
                tvDetailTitle.text = "CHART OF ACCOUNTS - DELETE"
                setFieldsEnabled(false)
                btnSubmit.visibility = View.GONE
                btnVerify.visibility = View.GONE
                btnDelete.visibility = View.VISIBLE
            }
        }
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        val editTexts = listOf(
            etGeneralLedger, etGLSubHead, etAccountID, etAccountCurrency,
            etRefCode, etReportCode, etTotalCreditBalance, etAccountBalance,
            etGLDescription, etGLSubHeadDescription, etSchemeCode, etAccountName,
            etHomeCurrency, etRefDescription, etRemarks, etTotalDebitBalance,
            etRefCurrencyBalance
        )
        editTexts.forEach { it.isEnabled = enabled }

        val spinners = listOf(
            spinClassification, spinSchemeType, spinAdditionalDetails, spinOwnership,
            spinAccountType, spinAccountPartitioning, spinAccountStatus
        )
        spinners.forEach { it.isEnabled = enabled }
    }

    private fun loadData() {
        showProgress(true)
        val call = RetrofitClient.api.getChartOfAccountsDetail(currentMode.lowercase(), currentAcctNum!!)
        call.enqueue(object : Callback<ChartOfAccountsDetailResponse> {
            override fun onResponse(
                call: Call<ChartOfAccountsDetailResponse>,
                response: Response<ChartOfAccountsDetailResponse>
            ) {
                showProgress(false)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    chart1List = data.Chart1 ?: emptyList()
                    chart2List = data.Chart2 ?: emptyList()
                    chart3List = data.Chart3 ?: emptyList()
                    chart4List = data.Chart4 ?: emptyList()
                    chart5List = data.Chart5 ?: emptyList()
                    chart6List = data.Chart6 ?: emptyList()
                    chart7List = data.Chart7 ?: emptyList()

                    data.chartaccount?.let { populateFields(it) }
                    setupSpinners()
                    // 🔥 CRITICAL: set the selected values in the spinners
                    data.chartaccount?.let { setSpinnerSelections(it) }
                } else {
                    Toast.makeText(this@ChartOfAccountsDetailActivity,
                        "Failed to load data: ${response.code()}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<ChartOfAccountsDetailResponse>, t: Throwable) {
                showProgress(false)
                Toast.makeText(this@ChartOfAccountsDetailActivity,
                    "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun populateFields(account: com.example.bgls.DataModels.ChartAccountApiItem) {
        etGeneralLedger.setText(account.gl_code ?: "")
        etGLDescription.setText(account.gl_desc ?: "")
        etGLSubHead.setText(account.glsh_code ?: "")
        etGLSubHeadDescription.setText(account.glsh_desc ?: "")
        etSchemeCode.setText(account.schm_code ?: "")
        etAccountID.setText(account.acct_num ?: "")
        etAccountName.setText(account.acct_name ?: "")
        etAccountCurrency.setText(account.ref_crncy ?: "")
        etHomeCurrency.setText(account.acct_crncy ?: "")
        etRefCode.setText(account.ref_code ?: "")
        etRefDescription.setText(account.ref_desc ?: "")
        etReportCode.setText(account.rpt_code ?: "")
        etRemarks.setText(account.own_remarks ?: "")
        etTotalCreditBalance.setText(account.cr_amt ?: "0.00")
        etTotalDebitBalance.setText(account.dr_amt ?: "0.00")
        etAccountBalance.setText(account.acct_bal ?: "0.00")
        etRefCurrencyBalance.setText(account.ref_crncy_bal ?: "0.00")
    }

    private fun setupSpinners() {
        // Helper to set spinner selection by value (ref_type_desc)
        fun setSpinnerSelection(spinner: Spinner, list: List<RefCodeItem>, value: String?) {
            val index = list.indexOfFirst { it.ref_type_desc == value }
            if (index >= 0) spinner.setSelection(index + 1) // +1 because first item is "Select"
            else spinner.setSelection(0)
        }

        // Setup each spinner with its reference list
        setupSpinnerAdapter(spinClassification, chart1List, "Select Classification")
        setupSpinnerAdapter(spinAccountType, chart6List, "Select Account Type")
        setupSpinnerAdapter(spinSchemeType, chart7List, "Select Scheme Type")
        setupSpinnerAdapter(spinAdditionalDetails, chart2List, "Select Additional Details")
        setupSpinnerAdapter(spinAccountPartitioning, chart3List, "Select Partitioning")
        setupSpinnerAdapter(spinAccountStatus, chart4List, "Select Status")
        setupSpinnerAdapter(spinOwnership, chart5List, "Select Ownership")

        // Set selections (if the API already returned a chartaccount object, use its values)
        // For MODIFY/VERIFY we already have data loaded, but we need to set spinners after adapter is ready.
        // The populate method is called before spinners are populated, so we delay selection until after adapter.
    }

    private fun setupSpinnerAdapter(spinner: Spinner, items: List<RefCodeItem>, hint: String) {
        val displayList = mutableListOf(hint)
        displayList.addAll(items.map { it.ref_id_desc })
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Store mapping for retrieving actual values later
        spinner.tag = items.map { it.ref_type_desc }
    }

    private fun getSpinnerSelectedValue(spinner: Spinner): String {
        @Suppress("UNCHECKED_CAST")
        val valueList = spinner.tag as? List<String> ?: emptyList()
        val pos = spinner.selectedItemPosition
        return if (pos > 0 && pos - 1 < valueList.size) valueList[pos - 1] else ""
    }

    private fun collectFields(): Map<String, String> {
        // Use the selected values from spinners
        return mapOf(
            "classification" to getSpinnerSelectedValue(spinClassification),
            "acct_type" to getSpinnerSelectedValue(spinAccountType),
            "gl_code" to etGeneralLedger.text.toString(),
            "gl_desc" to etGLDescription.text.toString(),
            "glsh_code" to etGLSubHead.text.toString(),
            "glsh_desc" to etGLSubHeadDescription.text.toString(),
            "schm_type" to getSpinnerSelectedValue(spinSchemeType),
            "schm_code" to etSchemeCode.text.toString(),
            "acct_num" to etAccountID.text.toString(),
            "acct_name" to etAccountName.text.toString(),
            "add_det_flg" to getSpinnerSelectedValue(spinAdditionalDetails),
            "acct_partition" to getSpinnerSelectedValue(spinAccountPartitioning),
            "ref_crncy" to etAccountCurrency.text.toString(),
            "acct_crncy" to etHomeCurrency.text.toString(),
            "ref_code" to etRefCode.text.toString(),
            "ref_desc" to etRefDescription.text.toString(),
            "rpt_code" to etReportCode.text.toString(),
            "acct_status" to getSpinnerSelectedValue(spinAccountStatus),
            "own_type" to getSpinnerSelectedValue(spinOwnership),
            "own_remarks" to etRemarks.text.toString(),
            "cr_amt" to etTotalCreditBalance.text.toString(),
            "dr_amt" to etTotalDebitBalance.text.toString(),
            "acct_bal" to etAccountBalance.text.toString(),
            "ref_crncy_bal" to etRefCurrencyBalance.text.toString()
        )
    }

    private fun submitModify() {
        val acctNum = etAccountID.text.toString()
        if (acctNum.isEmpty()) {
            Toast.makeText(this, "Account ID is required", Toast.LENGTH_SHORT).show()
            return
        }
        showProgress(true)
        RetrofitClient.api.modifyChartOfAccount(acctNum, collectFields())
            .enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                    showProgress(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChartOfAccountsDetailActivity, "Modified Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ChartOfAccountsDetailActivity, "Modify failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    showProgress(false)
                    Toast.makeText(this@ChartOfAccountsDetailActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun verifyAccount() {
        showProgress(true)
        RetrofitClient.api.verifyChartOfAccount(collectFields())
            .enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                    showProgress(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChartOfAccountsDetailActivity, "Verified Successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@ChartOfAccountsDetailActivity, "Verify failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                    showProgress(false)
                    Toast.makeText(this@ChartOfAccountsDetailActivity, t.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete this account?")
            .setPositiveButton("Yes") { _, _ ->
                showProgress(true)
                RetrofitClient.api.deleteChartOfAccount(currentAcctNum!!)
                    .enqueue(object : Callback<okhttp3.ResponseBody> {
                        override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                            showProgress(false)
                            if (response.isSuccessful) {
                                Toast.makeText(this@ChartOfAccountsDetailActivity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@ChartOfAccountsDetailActivity, "Delete failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {
                            showProgress(false)
                            Toast.makeText(this@ChartOfAccountsDetailActivity, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showProgress(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            scrollView.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
        }
    }
    private fun setSpinnerSelections(account: com.example.bgls.DataModels.ChartAccountApiItem?) {
        if (account == null) return
        setSpinnerValue(spinClassification, account.classification)
        setSpinnerValue(spinAccountType, account.acct_type)
        setSpinnerValue(spinSchemeType, account.schm_type)
        setSpinnerValue(spinAdditionalDetails, account.add_det_flg)
        setSpinnerValue(spinAccountPartitioning, account.acct_partition)
        setSpinnerValue(spinAccountStatus, account.acct_status)
        setSpinnerValue(spinOwnership, account.own_type)
    }

    private fun setSpinnerValue(spinner: Spinner, value: String?) {
        if (value.isNullOrEmpty()) {
            spinner.setSelection(0)
            return
        }
        @Suppress("UNCHECKED_CAST")
        val valueList = spinner.tag as? List<String> ?: emptyList()
        val index = valueList.indexOfFirst { it == value }
        if (index >= 0) {
            spinner.setSelection(index + 1) // +1 because first item is the hint ("Select ...")
        } else {
            spinner.setSelection(0)
        }
    }
}