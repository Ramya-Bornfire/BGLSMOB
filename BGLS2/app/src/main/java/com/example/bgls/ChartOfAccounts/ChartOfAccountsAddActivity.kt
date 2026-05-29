package com.example.bgls.ChartOfAccounts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.ChartOfAccountsAddResponse
import com.example.bgls.DataModels.RefCodeItem
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import android.view.WindowManager
import androidx.constraintlayout.helper.widget.MotionEffect.TAG

class ChartOfAccountsAddActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var spinClassification: Spinner
    private lateinit var spinAccountType: Spinner
    private lateinit var spinSchemeType: Spinner
    private lateinit var spinAdditionalDetails: Spinner
    private lateinit var spinAccountPartitioning: Spinner
    private lateinit var spinAccountStatus: Spinner
    private lateinit var spinOwnership: Spinner
    private lateinit var btnSubmitAdd: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView

    // Data holders
    private var chart1List = listOf<RefCodeItem>()
    private var chart2List = listOf<RefCodeItem>()
    private var chart3List = listOf<RefCodeItem>()
    private var chart4List = listOf<RefCodeItem>()
    private var chart5List = listOf<RefCodeItem>()
    private var chart6List = listOf<RefCodeItem>()
    private var chart7List = listOf<RefCodeItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart_of_accounts_add)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        loadDropdowns()
        setupSubmit()
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar) // add a ProgressBar in your add layout
        spinClassification = findViewById(R.id.spinClassification)
        spinAccountType = findViewById(R.id.spinAccountType)
        spinSchemeType = findViewById(R.id.spinSchemeType)
        spinAdditionalDetails = findViewById(R.id.spinAdditionalDetails)
        spinAccountPartitioning = findViewById(R.id.spinAccountPartitioning)
        spinAccountStatus = findViewById(R.id.spinAccountStatus)
        spinOwnership = findViewById(R.id.spinOwnership)
        btnSubmitAdd = findViewById(R.id.btnSubmitAdd)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)
    }

    private fun loadDropdowns() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.api.getChartOfAccountsReferences("add")
            .enqueue(object : Callback<ChartOfAccountsAddResponse> {
                override fun onResponse(call: Call<ChartOfAccountsAddResponse>, response: Response<ChartOfAccountsAddResponse>) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!
                        chart1List = data.Chart1 ?: emptyList()
                        chart2List = data.Chart2 ?: emptyList()
                        chart3List = data.Chart3 ?: emptyList()
                        chart4List = data.Chart4 ?: emptyList()
                        chart5List = data.Chart5 ?: emptyList()
                        chart6List = data.Chart6 ?: emptyList()
                        chart7List = data.Chart7 ?: emptyList()
                        setupSpinners()
                    } else {
                        Toast.makeText(this@ChartOfAccountsAddActivity, "Failed to load dropdowns", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                override fun onFailure(call: Call<ChartOfAccountsAddResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@ChartOfAccountsAddActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun setupSpinners() {
        fun setupSpinner(spinner: Spinner, items: List<RefCodeItem>, hint: String) {
            val displayList = mutableListOf(hint)
            displayList.addAll(items.map { it.ref_id_desc })
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, displayList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            spinner.tag = items.map { it.ref_type_desc }
        }
        setupSpinner(spinClassification, chart1List, "Select Classification")
        setupSpinner(spinAccountType, chart6List, "Select Account Type")
        setupSpinner(spinSchemeType, chart7List, "Select Scheme Type")
        setupSpinner(spinAdditionalDetails, chart2List, "Select Additional Details")
        setupSpinner(spinAccountPartitioning, chart3List, "Select Partitioning")
        setupSpinner(spinAccountStatus, chart4List, "Select Status")
        setupSpinner(spinOwnership, chart5List, "Select Ownership")
    }

    private fun getSpinnerValue(spinner: Spinner): String {
        @Suppress("UNCHECKED_CAST")
        val valueList = spinner.tag as? List<String> ?: emptyList()
        val pos = spinner.selectedItemPosition
        return if (pos > 0 && pos - 1 < valueList.size) valueList[pos - 1] else ""
    }

    private fun setupSubmit() {
        btnBack.setOnClickListener { finish() }
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        btnSubmitAdd.setOnClickListener {
            val acctNum = findViewById<EditText>(R.id.etAccountID).text.toString()
            if (acctNum.isEmpty()) {
                Toast.makeText(this, "Account ID is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fields = mutableMapOf<String, String>().apply {
                put("classification", getSpinnerValue(spinClassification))
                put("acct_type", getSpinnerValue(spinAccountType))
                put("gl_code", findViewById<EditText>(R.id.etGeneralLedger).text.toString())
                put("gl_desc", findViewById<EditText>(R.id.etGLDescription).text.toString())
                put("glsh_code", findViewById<EditText>(R.id.etGLSubHead).text.toString())
                put("glsh_desc", findViewById<EditText>(R.id.etGLSubHeadDescription).text.toString())
                put("schm_type", getSpinnerValue(spinSchemeType))
                put("schm_code", findViewById<EditText>(R.id.etSchemeCode).text.toString())
                put("acct_num", acctNum)
                put("acct_name", findViewById<EditText>(R.id.etAccountName).text.toString())
                put("add_det_flg", getSpinnerValue(spinAdditionalDetails))
                put("acct_partition", getSpinnerValue(spinAccountPartitioning))
                put("ref_crncy", findViewById<EditText>(R.id.etAccountCurrency).text.toString())
                put("acct_crncy", findViewById<EditText>(R.id.etHomeCurrency).text.toString())
                put("ref_code", findViewById<EditText>(R.id.etRefCode).text.toString())
                put("ref_desc", findViewById<EditText>(R.id.etRefDescription).text.toString())
                put("rpt_code", findViewById<EditText>(R.id.etReportCode).text.toString())
                put("acct_status", getSpinnerValue(spinAccountStatus))
                put("own_type", getSpinnerValue(spinOwnership))
                put("own_remarks", findViewById<EditText>(R.id.etRemarks).text.toString())
                put("cr_amt", "0.00")
                put("dr_amt", "0.00")
                put("acct_bal", "0.00")
                put("ref_crncy_bal", "0.00")
            }

            progressBar.visibility = View.VISIBLE
            RetrofitClient.api.addChartOfAccount(fields)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        progressBar.visibility = View.GONE
                        val bodyString = response.body()?.string() ?: ""
                        if (response.isSuccessful && bodyString.contains("Successfully", ignoreCase = true)) {
                            Log.i(TAG, "Add success: $bodyString")
                            finish()
                        } else {
                            Log.e(TAG, "Add failed: $bodyString")
                            // Optionally show a Snackbar – but you said you only want logs.
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        Log.e(TAG, "Network error", t)
                    }
                })
        }
    }
}