package com.example.bgls.TransactionInquiries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.content.Intent
import com.example.bgls.MainActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.example.bgls.Retrofit.ServiceApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

data class BalanceSheetModel(
    val glHead: String,
    val glDesc: String,
    val noOfAc: String,
    val currency: String,
    val amount: String
)

class BalanceSheetActivity : AppCompatActivity() {

    private lateinit var rvAsset: RecyclerView
    private lateinit var rvLiability: RecyclerView
    private lateinit var assetAdapter: BalanceSheetAdapter
    private lateinit var liabilityAdapter: BalanceSheetAdapter
    private var assetList = mutableListOf<BalanceSheetModel>()
    private var liabilityList = mutableListOf<BalanceSheetModel>()
    private var fullAssetList = mutableListOf<BalanceSheetModel>()
    private var fullLiabilityList = mutableListOf<BalanceSheetModel>()
    private lateinit var btnHome: ImageView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_sheet)

        initViews()
        setupNavigation()
        loadData()
        setupFilters()
    }

    private fun initViews() {
        rvAsset = findViewById(R.id.rvAsset)
        rvLiability = findViewById(R.id.rvLiability)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)

        assetAdapter = BalanceSheetAdapter(assetList)
        rvAsset.layoutManager = LinearLayoutManager(this)
        rvAsset.adapter = assetAdapter

        liabilityAdapter = BalanceSheetAdapter(liabilityList)
        rvLiability.layoutManager = LinearLayoutManager(this)
        rvLiability.adapter = liabilityAdapter
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

    private fun setupFilters() {
        // Asset section
        val layoutFilterAsset = findViewById<android.widget.LinearLayout>(R.id.layoutFilterAsset)
        val assetHeaderRow = findViewById<android.widget.LinearLayout>(R.id.assetHeaderRow)
        val btnFilterAsset = findViewById<View>(R.id.btnFilterAsset)

        btnFilterAsset.setOnClickListener {
            val isVisible = layoutFilterAsset.visibility == View.VISIBLE
            layoutFilterAsset.visibility = if (isVisible) View.GONE else View.VISIBLE
            assetHeaderRow.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) {
                // Reset to full list when closing filter
                findViewById<android.widget.EditText>(R.id.etFilterAssetGlHead).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterAssetGlDesc).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterAssetCurrency).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterAssetNoOfAc).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterAssetAmount).text.clear()
                assetList.clear()
                assetList.addAll(fullAssetList)
                assetAdapter.notifyDataSetChanged()
            }
        }

        val etGlHead = findViewById<android.widget.EditText>(R.id.etFilterAssetGlHead)
        val etGlDesc = findViewById<android.widget.EditText>(R.id.etFilterAssetGlDesc)
        val etCurrency = findViewById<android.widget.EditText>(R.id.etFilterAssetCurrency)
        val etNoOfAc = findViewById<android.widget.EditText>(R.id.etFilterAssetNoOfAc)
        val etAmount = findViewById<android.widget.EditText>(R.id.etFilterAssetAmount)

        val filterAsset = {
            val gl = etGlHead.text.toString().trim().lowercase()
            val desc = etGlDesc.text.toString().trim().lowercase()
            val cur = etCurrency.text.toString().trim().lowercase()
            val noAc = etNoOfAc.text.toString().trim()
            val amt = etAmount.text.toString().trim()
            assetList.clear()
            assetList.addAll(fullAssetList.filter { item ->
                (gl.isBlank() || item.glHead.lowercase().contains(gl)) &&
                (desc.isBlank() || item.glDesc.lowercase().contains(desc)) &&
                (cur.isBlank() || item.currency.lowercase().contains(cur)) &&
                (noAc.isBlank() || item.noOfAc.contains(noAc)) &&
                (amt.isBlank() || item.amount.contains(amt))
            })
            assetAdapter.notifyDataSetChanged()
        }

        val assetWatcher = com.example.bgls.TransactionMaintenance.SearchTextWatcher { filterAsset() }
        etGlHead.addTextChangedListener(assetWatcher)
        etGlDesc.addTextChangedListener(assetWatcher)
        etCurrency.addTextChangedListener(assetWatcher)
        etNoOfAc.addTextChangedListener(assetWatcher)
        etAmount.addTextChangedListener(assetWatcher)

        // Liability section
        val layoutFilterLiability = findViewById<android.widget.LinearLayout>(R.id.layoutFilterLiability)
        val liabilityHeaderRow = findViewById<android.widget.LinearLayout>(R.id.liabilityHeaderRow)
        val btnFilterLiability = findViewById<View>(R.id.btnFilterLiability)

        btnFilterLiability.setOnClickListener {
            val isVisible = layoutFilterLiability.visibility == View.VISIBLE
            layoutFilterLiability.visibility = if (isVisible) View.GONE else View.VISIBLE
            liabilityHeaderRow.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) {
                // Reset to full list when closing filter
                findViewById<android.widget.EditText>(R.id.etFilterLiabilityGlHead).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterLiabilityGlDesc).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterLiabilityCurrency).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterLiabilityNoOfAc).text.clear()
                findViewById<android.widget.EditText>(R.id.etFilterLiabilityAmount).text.clear()
                liabilityList.clear()
                liabilityList.addAll(fullLiabilityList)
                liabilityAdapter.notifyDataSetChanged()
            }
        }

        val etLiabGlHead = findViewById<android.widget.EditText>(R.id.etFilterLiabilityGlHead)
        val etLiabGlDesc = findViewById<android.widget.EditText>(R.id.etFilterLiabilityGlDesc)
        val etLiabCurrency = findViewById<android.widget.EditText>(R.id.etFilterLiabilityCurrency)
        val etLiabNoOfAc = findViewById<android.widget.EditText>(R.id.etFilterLiabilityNoOfAc)
        val etLiabAmount = findViewById<android.widget.EditText>(R.id.etFilterLiabilityAmount)

        val filterLiability = {
            val gl = etLiabGlHead.text.toString().trim().lowercase()
            val desc = etLiabGlDesc.text.toString().trim().lowercase()
            val cur = etLiabCurrency.text.toString().trim().lowercase()
            val noAc = etLiabNoOfAc.text.toString().trim()
            val amt = etLiabAmount.text.toString().trim()
            liabilityList.clear()
            liabilityList.addAll(fullLiabilityList.filter { item ->
                (gl.isBlank() || item.glHead.lowercase().contains(gl)) &&
                (desc.isBlank() || item.glDesc.lowercase().contains(desc)) &&
                (cur.isBlank() || item.currency.lowercase().contains(cur)) &&
                (noAc.isBlank() || item.noOfAc.contains(noAc)) &&
                (amt.isBlank() || item.amount.contains(amt))
            })
            liabilityAdapter.notifyDataSetChanged()
        }

        val liabilityWatcher = com.example.bgls.TransactionMaintenance.SearchTextWatcher { filterLiability() }
        etLiabGlHead.addTextChangedListener(liabilityWatcher)
        etLiabGlDesc.addTextChangedListener(liabilityWatcher)
        etLiabCurrency.addTextChangedListener(liabilityWatcher)
        etLiabNoOfAc.addTextChangedListener(liabilityWatcher)
        etLiabAmount.addTextChangedListener(liabilityWatcher)
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.api
                val response = api.getBalanceSheet("list")

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    
                    data.balancesheet1?.let { list ->
                        assetList.clear()
                        list.forEach { row ->
                            val rowList = row as? List<*>
                            if (rowList != null) {
                                assetList.add(BalanceSheetModel(
                                    glHead = rowList.getOrNull(0)?.toString() ?: "",
                                    glDesc = rowList.getOrNull(1)?.toString() ?: "",
                                    noOfAc = rowList.getOrNull(3)?.toString() ?: "0",
                                    currency = rowList.getOrNull(2)?.toString() ?: "",
                                    amount = rowList.getOrNull(4)?.toString() ?: "0.00"
                                ))
                            }
                        }
                        fullAssetList.clear()
                        fullAssetList.addAll(assetList)
                        assetAdapter.notifyDataSetChanged()
                    }

                    data.balancesheet2?.let { list ->
                        liabilityList.clear()
                        list.forEach { row ->
                            val rowList = row as? List<*>
                            if (rowList != null) {
                                liabilityList.add(BalanceSheetModel(
                                    glHead = rowList.getOrNull(0)?.toString() ?: "",
                                    glDesc = rowList.getOrNull(1)?.toString() ?: "",
                                    noOfAc = rowList.getOrNull(3)?.toString() ?: "0",
                                    currency = rowList.getOrNull(2)?.toString() ?: "",
                                    amount = rowList.getOrNull(4)?.toString() ?: "0.00"
                                ))
                            }
                        }
                        fullLiabilityList.clear()
                        fullLiabilityList.addAll(liabilityList)
                        liabilityAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun loadMockData() {
        // Asset mock data from image
        assetList.add(BalanceSheetModel("1100", "CASH AND BANK BALANCES", "2", "KES", "0.00"))
        assetList.add(BalanceSheetModel("1200", "CURRENT ASSETS", "2", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129010", "Consumer Credit New Client", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129020", "Consumer Credit Repeat Client", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129030", "Consumer Credit Cash loan", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129050", "Consumer Education Financing", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129060", "Investment in Hire Purchase", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129070", "Investment in Credit Facilities", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129080", "Investment in Boda Financing", "1", "KES", "0.00"))
        assetList.add(BalanceSheetModel("129090", "Investment in FSD financing", "1", "KES", "0.00"))

        // Liability mock data from image
        liabilityList.add(BalanceSheetModel("164400", "Debtors Adjustment Control", "1", "KES", "4,795,745.00"))
        liabilityList.add(BalanceSheetModel("170311", "Payables- Collection Agency", "1", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("2100", "BORROWINGS", "1", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("2200", "CURRENT LIABILITIES", "2", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("2500", "DEPOSITS", "2", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("2500", "DEPOSITS", "16", "SCR", "1,000,000.00"))
        liabilityList.add(BalanceSheetModel("2700", "LOAN MIGRATION BALANCE", "1", "KES", "280,136,185.10"))
        liabilityList.add(BalanceSheetModel("2700", "SUNDRY CREDITORS", "1", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("272500", "Aspira Credit Protection-New Product", "1", "KES", "0.00"))
        liabilityList.add(BalanceSheetModel("274210", "Staff Salary deductions-Mambu", "1", "KES", "0.00"))
    }

    inner class BalanceSheetAdapter(private val list: List<BalanceSheetModel>) : RecyclerView.Adapter<BalanceSheetAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlHead: TextView = view.findViewById(R.id.tvGlHead)
            val tvGlDesc: TextView = view.findViewById(R.id.tvGlDesc)
            val tvNoOfAc: TextView = view.findViewById(R.id.tvNoOfAc)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_balance_sheet, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlHead.text = item.glHead
            holder.tvGlDesc.text = item.glDesc
            holder.tvNoOfAc.text = item.noOfAc
            holder.tvCurrency.text = item.currency
            holder.tvAmount.text = item.amount
        }

        override fun getItemCount() = list.size
    }
}
