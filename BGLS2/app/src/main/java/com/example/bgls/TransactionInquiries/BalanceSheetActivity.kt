package com.example.bgls.TransactionInquiries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance_sheet)

        rvAsset = findViewById(R.id.rvAsset)
        rvLiability = findViewById(R.id.rvLiability)

        assetAdapter = BalanceSheetAdapter(assetList)
        rvAsset.layoutManager = LinearLayoutManager(this)
        rvAsset.adapter = assetAdapter

        liabilityAdapter = BalanceSheetAdapter(liabilityList)
        rvLiability.layoutManager = LinearLayoutManager(this)
        rvLiability.adapter = liabilityAdapter

        loadData()

        val layoutFilterAsset = findViewById<View>(R.id.layoutFilterAsset)
        val btnFilterAsset = findViewById<View>(R.id.btnFilterAsset)
        btnFilterAsset.setOnClickListener {
            if (layoutFilterAsset.visibility == View.GONE) {
                layoutFilterAsset.visibility = View.VISIBLE
            } else {
                layoutFilterAsset.visibility = View.GONE
            }
        }

        val layoutFilterLiability = findViewById<View>(R.id.layoutFilterLiability)
        val btnFilterLiability = findViewById<View>(R.id.btnFilterLiability)
        btnFilterLiability.setOnClickListener {
            if (layoutFilterLiability.visibility == View.GONE) {
                layoutFilterLiability.visibility = View.VISIBLE
            } else {
                layoutFilterLiability.visibility = View.GONE
            }
        }

        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
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
