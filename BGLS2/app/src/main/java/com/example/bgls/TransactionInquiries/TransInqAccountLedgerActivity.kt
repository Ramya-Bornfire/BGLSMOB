package com.example.bgls.TransactionInquiries

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.AccountLedgerActivity
import com.example.bgls.DataModels.ChartAccountItem
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class TransInqAccountLedgerActivity : AppCompatActivity() {

    private val TAG = "TransInqAcctLedger"

    private lateinit var rvTransLedger: RecyclerView
    private lateinit var ledgerAdapter: AccountLedgerListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoData: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView
    private lateinit var btnFilter: Button
    private lateinit var etSearch: EditText

    private var fullList = mutableListOf<ChartAccountItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trans_account_ledger)

        initViews()
        setupNavigation()
        setupFilter()
        loadAccountLedgerList()
    }

    private fun initViews() {
        rvTransLedger = findViewById(R.id.rvTransLedger)
        rvTransLedger.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)
        tvNoData = findViewById(R.id.tvNoData)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)
        btnFilter = findViewById(R.id.btnFilter)
        etSearch = findViewById(R.id.etSearch)

        // Set login time
        try {
            val currentTime = java.text.SimpleDateFormat(
                "dd-MM-yyyy HH:mm", java.util.Locale.getDefault()
            ).format(java.util.Date())
            findViewById<TextView>(R.id.txtLoginTimeInfo).text = currentTime
        } catch (_: Exception) {}

        ledgerAdapter = AccountLedgerListAdapter(emptyList()) { item ->
            // Navigate to Account Ledger detail (view mode)
            val intent = Intent(this, AccountLedgerActivity::class.java)
            intent.putExtra("acct_num", item.acct_num ?: "")
            startActivity(intent)
        }
        rvTransLedger.adapter = ledgerAdapter
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener { finish() }
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupFilter() {
        btnFilter.setOnClickListener {
            if (etSearch.visibility == View.VISIBLE) {
                etSearch.visibility = View.GONE
                etSearch.setText("")
                ledgerAdapter.updateData(fullList)
            } else {
                etSearch.visibility = View.VISIBLE
                etSearch.requestFocus()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                if (query.isEmpty()) {
                    ledgerAdapter.updateData(fullList)
                } else {
                    val filtered = fullList.filter { item ->
                        (item.acct_num?.lowercase()?.contains(query) == true) ||
                        (item.acct_name?.lowercase()?.contains(query) == true) ||
                        (item.classification?.lowercase()?.contains(query) == true) ||
                        (item.gl_code?.lowercase()?.contains(query) == true)
                    }
                    ledgerAdapter.updateData(filtered)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadAccountLedgerList() {
        progressBar.visibility = View.VISIBLE
        tvNoData.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.filterChartOfAccountsSuspend(type = "O")
                if (response.isSuccessful && response.body() != null) {
                    val accounts = response.body()!!

                    fullList.clear()
                    fullList.addAll(accounts)

                    Log.d(TAG, "Loaded ${fullList.size} chart accounts from filter API")

                    if (fullList.isEmpty()) {
                        tvNoData.visibility = View.VISIBLE
                    } else {
                        tvNoData.visibility = View.GONE
                    }

                    ledgerAdapter.updateData(fullList)
                } else {
                    Log.e(TAG, "API error: ${response.code()}")
                    tvNoData.visibility = View.VISIBLE
                    Toast.makeText(
                        this@TransInqAccountLedgerActivity,
                        "Failed to load account ledger",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error", e)
                tvNoData.visibility = View.VISIBLE
                Toast.makeText(
                    this@TransInqAccountLedgerActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    // ── Inner Adapter ─────────────────────────────────────────────────────────

    inner class AccountLedgerListAdapter(
        private var list: List<ChartAccountItem>,
        private val onActionClick: (ChartAccountItem) -> Unit
    ) : RecyclerView.Adapter<AccountLedgerListAdapter.ViewHolder>() {

        private val decimalFormat = DecimalFormat("#,##0.00")

        fun updateData(newList: List<ChartAccountItem>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvHead: TextView = view.findViewById(R.id.tvHead)
            val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvCredits: TextView = view.findViewById(R.id.tvCredits)
            val tvDebits: TextView = view.findViewById(R.id.tvDebits)
            val tvBalance: TextView = view.findViewById(R.id.tvBalance)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val ivAction: RadioButton = view.findViewById(R.id.ivAction)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_trans_inq_ledger, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]

            holder.tvHead.text = item.classification ?: "Asset"
            holder.tvAcctId.text = item.acct_num ?: ""
            holder.tvAcctName.text = item.acct_name ?: ""
            holder.tvCurrency.text = item.acct_crncy ?: "KES"

            // Credits
            val credits = parseAmount(item.cr_amt)
            holder.tvCredits.text = decimalFormat.format(credits)

            // Debits
            val debits = parseAmount(item.dr_amt)
            holder.tvDebits.text = decimalFormat.format(debits)

            // Balance
            val balance = parseAmount(item.acct_bal)
            holder.tvBalance.text = decimalFormat.format(balance)
            if (balance < 0) {
                holder.tvBalance.setTextColor(Color.parseColor("#D32F2F"))
            } else {
                holder.tvBalance.setTextColor(Color.parseColor("#333333"))
            }

            // Status
            val status = if (item.entity_flg == "Y") "Active" else "Inactive"
            holder.tvStatus.text = status
            holder.tvStatus.setTextColor(
                if (status == "Active") Color.parseColor("#4CAF50")
                else Color.parseColor("#F44336")
            )

            // Zebra striping
            holder.itemView.setBackgroundColor(
                if (position % 2 == 0) Color.WHITE
                else Color.parseColor("#F9F9F9")
            )

            // Radio button - navigate to ledger detail
            holder.ivAction.isChecked = false
            holder.ivAction.isClickable = false
            holder.ivAction.setOnClickListener {
                onActionClick(item)
            }

            // Also clickable on ACCT ID
            holder.tvAcctId.setOnClickListener {
                onActionClick(item)
            }
        }

        override fun getItemCount(): Int = list.size

        private fun parseAmount(value: String?): Double {
            if (value.isNullOrBlank()) return 0.0
            return try {
                value.replace(",", "").toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    }
}
