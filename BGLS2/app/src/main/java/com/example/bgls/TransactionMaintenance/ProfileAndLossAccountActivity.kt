package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class ProfitLossModel(
    val glHead: String,
    val acctId: String,
    val acctName: String,
    val currency: String,
    val amount: String
)

class ProfileAndLossAccountActivity : AppCompatActivity() {

    private lateinit var rvIncome: RecyclerView
    private lateinit var rvExpenditure: RecyclerView
    private lateinit var incomeAdapter: ProfitLossAdapter
    private lateinit var expenditureAdapter: ProfitLossAdapter
    private var incomeList = mutableListOf<ProfitLossModel>()
    private var expenditureList = mutableListOf<ProfitLossModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_and_loss_account)

        rvIncome = findViewById(R.id.rvIncome)
        rvExpenditure = findViewById(R.id.rvExpenditure)

        loadMockData()

        incomeAdapter = ProfitLossAdapter(incomeList)
        rvIncome.layoutManager = LinearLayoutManager(this)
        rvIncome.adapter = incomeAdapter

        expenditureAdapter = ProfitLossAdapter(expenditureList)
        rvExpenditure.layoutManager = LinearLayoutManager(this)
        rvExpenditure.adapter = expenditureAdapter
        
        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun loadMockData() {
        // Income mock data
        incomeList.add(ProfitLossModel("170133 - Penalty Income- Boda Financing", "1701330001", "Penalty Income- Boda Financing", "KES", "0.00"))
        incomeList.add(ProfitLossModel("506193 - Interest Income- Consumer checkoff T3", "5061930001", "Interest Income- Consumer checkoff T3", "KES", "0.00"))
        incomeList.add(ProfitLossModel("506110 - Interest Income Credit Facilities", "5061100001", "Interest Income Credit Facilities", "KES", "22,554,392.73"))
        incomeList.add(ProfitLossModel("571250 - Other Income-Credit Facilities", "5712500001", "Other Income-Credit Facilities", "KES", "3,200.00"))

        // Expenditure mock data
        expenditureList.add(ProfitLossModel("6100 - INTEREST PAID", "6100006110", "INTEREST PAID DEPOSITS", "KES", "0.00"))
        expenditureList.add(ProfitLossModel("6200 - MISC EXPENSES", "6200006210", "MISC EXPENSES", "KES", "0.00"))
    }

    inner class ProfitLossAdapter(private val list: List<ProfitLossModel>) : RecyclerView.Adapter<ProfitLossAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlHead: TextView = view.findViewById(R.id.tvGlHead)
            val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
            val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
            val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
            val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_profit_loss, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlHead.text = item.glHead
            holder.tvAcctId.text = item.acctId
            holder.tvAcctName.text = item.acctName
            holder.tvCurrency.text = item.currency
            holder.tvAmount.text = item.amount
        }

        override fun getItemCount() = list.size
    }
}