package com.example.bgls.TransactionInquiries

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.MainActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.ChartOfAccounts.ChartOfAccountsDetailActivity
import com.example.bgls.R

data class BalancingReportModel(
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

class BalancingReportActivity :  AppCompatActivity() {

    private lateinit var rvBalancingReport: RecyclerView
    private lateinit var balancingAdapter: BalancingReportAdapter
    private var reportList = mutableListOf<BalancingReportModel>()
    private lateinit var btnHome: ImageView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balancing_report)

        initViews()
        setupNavigation()
        setupSpinners()
        setupListeners()
    }

    private fun initViews() {
        rvBalancingReport = findViewById(R.id.rvBalancingReport)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)

        balancingAdapter = BalancingReportAdapter(reportList)
        rvBalancingReport.layoutManager = LinearLayoutManager(this)
        rvBalancingReport.adapter = balancingAdapter
        
        loadOfficeData()
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun setupSpinners() {
        val spinnerOffice = findViewById<Spinner>(R.id.spinnerOffice)
        val officeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("OFFICE", "CUSTOMER"))
        officeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerOffice.adapter = officeAdapter

        spinnerOffice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    loadOfficeData()
                } else {
                    loadCustomerData()
                }
                balancingAdapter.notifyDataSetChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        val layoutFilter = findViewById<View>(R.id.layoutFilter)
        findViewById<View>(R.id.btnFilter).setOnClickListener {
            layoutFilter.visibility = if (layoutFilter.visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    private fun loadOfficeData() {
        reportList.clear()
        reportList.add(BalancingReportModel("LIABILITY", "1000", "OAGEN", "1100001110", "BANK ACCOUNT", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "1000", "OAGEN", "1100001120", "CASH ON HAND", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "1000", "OAGEN", "1100001130", "PETTY CASH", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "1000", "OAGEN", "1200001210", "CURRENT ASSETS", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "1000", "OAGEN", "1200001220", "INTEREST RECEIVABLE", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "129", "", "1291100001", "Interest Receivable-Consumer Credit New Client", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "129", "", "1291200001", "Interest Receivable-Consumer Credit Repeat Client", "KES", "0.00", "0.00", "0.00", "Active"))
        reportList.add(BalancingReportModel("Asset", "129", "", "1291300001", "Interest Receivable- Consumer Credit Cash Loan", "KES", "0.00", "0.00", "0.00", "Active"))
    }

    private fun loadCustomerData() {
        reportList.clear()
        reportList.add(BalancingReportModel("Asset", "12", "LA", "8878c9751e394855a1ef174520a5142e", "MOSES WACHIRA MWANGI", "KES", "22,158.00", "28,426.57", "-6,268.57", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190701417", "SPEARS HUMBEG INVESTMENTS LIMITED LTD", "KES", "54,114.00", "266,495.32", "-212,381.32", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190701451", "IZONE AFRICA LIMITED", "KES", "173,045.81", "727,015.79", "-553,969.98", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190701931", "STELLA NYAKERU KARIMI", "KES", "227,502.01", "2,519,207.76", "-2,291,705.75", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190702838", "THE IGNATION GROUP LIMITED", "KES", "2,268,421.38", "3,080,000.00", "-811,578.62", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190706075", "LEVITICUS VENTURES LIMITED", "KES", "1,532,650.10", "2,236,273.56", "-703,623.46", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190709009", "IRON BRIDGE LTD", "KES", "46,685.00", "126,855.20", "-80,170.20", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190709177", "CORNERSTONE ACADEMY LIMITED", "KES", "159,500.00", "231,164.24", "-71,664.24", "Active"))
        reportList.add(BalancingReportModel("Asset", "12", "LA", "BFM190714580", "AUTO SPARKLE", "KES", "794,983.10", "1,555,927.26", "-760,944.16", "Active"))
    }

    inner class BalancingReportAdapter(private val list: List<BalancingReportModel>) : RecyclerView.Adapter<BalancingReportAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvHead: TextView = view.findViewById(R.id.tvHead)
            val tvGl: TextView = view.findViewById(R.id.tvGl)
            val tvSchemeCode: TextView = view.findViewById(R.id.tvSchemeCode)
            val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvCredits: TextView = view.findViewById(R.id.tvCredits)
            val tvDebits: TextView = view.findViewById(R.id.tvDebits)
            val tvBalance: TextView = view.findViewById(R.id.tvBalance)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val spinnerAction: Spinner = view.findViewById(R.id.spinnerAction)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_balancing_report, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvHead.text = item.head
            holder.tvGl.text = item.gl
            holder.tvSchemeCode.text = item.schemeCode
            holder.tvAcctId.text = item.acctId
            holder.tvAcctName.text = item.acctName
            holder.tvCurrency.text = item.currency
            holder.tvCredits.text = item.credits
            holder.tvDebits.text = item.debits
            holder.tvBalance.text = item.balance
            holder.tvStatus.text = item.status

            // Zebra striping
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

            // Setup Action Spinner
            val actions = listOf("Action", "Modify", "Verify", "Delete", "View")
            val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, actions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            holder.spinnerAction.adapter = adapter

            holder.spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val selectedAction = actions[pos]
                    if (selectedAction != "Action") {
                        navigateToDetail(item.acctId, selectedAction.uppercase())
                        holder.spinnerAction.setSelection(0) // Reset to "Action"
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            holder.tvAcctId.setOnClickListener {
                navigateToDetail(item.acctId, "View")
            }
        }

        override fun getItemCount() = list.size

        private fun navigateToDetail(acctId: String, mode: String) {
            val intent = Intent(this@BalancingReportActivity, ChartOfAccountsDetailActivity::class.java)
            intent.putExtra("ACCT_NUM", acctId)
            intent.putExtra("MODE", mode)
            startActivity(intent)
        }
    }
}


