package com.example.bgls.TransactionMaintenance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import java.util.Calendar

data class TrialBalanceModel(
    val glCode: String,
    val accountName: String,
    val openingBal: String,
    val credit: String,
    val debit: String,
    val netChange: String,
    val closingBal: String
)

class TrialBalanceActivity : AppCompatActivity() {

    private lateinit var tvSelectedDate: TextView
    private lateinit var layoutDatePicker: LinearLayout
    private lateinit var rvTrialBalance: RecyclerView
    private lateinit var adapter: TrialBalanceAdapter
    private var dataList = mutableListOf<TrialBalanceModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trial_balance)

        initViews()
        setupDatePicker()
        loadMockData()
        
        adapter = TrialBalanceAdapter(dataList)
        rvTrialBalance.layoutManager = LinearLayoutManager(this)
        rvTrialBalance.adapter = adapter
    }

    private fun initViews() {
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        layoutDatePicker = findViewById(R.id.layoutDatePicker)
        rvTrialBalance = findViewById(R.id.rvTrialBalance)
    }

    private fun setupDatePicker() {
        layoutDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format("%02d-%s-%d", selectedDay, getMonthName(selectedMonth), selectedYear)
                tvSelectedDate.text = dateStr
            }, year, month, day)
            datePickerDialog.show()
        }
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months[month]
    }

    private fun loadMockData() {
        dataList.add(TrialBalanceModel("164", "Debtors Adjustment Control", "462.00", "921.00", "0.00", "921.00", "459.00"))
        dataList.add(TrialBalanceModel("506", "Interest Income Credit Facilities", "2,416.28", "4,355.57", "0.00", "4,355.57", "1,939.29"))
        dataList.add(TrialBalanceModel("2000", "LOAN MIGRATION BALANCE ACCOUNT", "53,765.00", "100,186.17", "46,400.02", "53,786.10", "21.00"))
        dataList.add(TrialBalanceModel("170", "NCBA Mambu KES clearing Account", "-538.60", "0.00", "64.00", "64.00", "474.80"))
        dataList.add(TrialBalanceModel("170", "Paybill Mambu clearing Account", "-4,153.00", "0.00", "8,295.60", "8,295.60", "4,143.00"))
        dataList.add(TrialBalanceModel("274", "Payment received from clients", "19.41", "49.31", "0.00", "49.31", "29.89"))
    }

    inner class TrialBalanceAdapter(private val list: List<TrialBalanceModel>) : RecyclerView.Adapter<TrialBalanceAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvGlCode: TextView = view.findViewById(R.id.tvGlCode)
            val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
            val tvOpeningBal: TextView = view.findViewById(R.id.tvOpeningBal)
            val tvCredit: TextView = view.findViewById(R.id.tvCredit)
            val tvDebit: TextView = view.findViewById(R.id.tvDebit)
            val tvNetChange: TextView = view.findViewById(R.id.tvNetChange)
            val tvClosingBal: TextView = view.findViewById(R.id.tvClosingBal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trial_balance, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            holder.tvGlCode.text = item.glCode
            holder.tvAccountName.text = item.accountName
            holder.tvOpeningBal.text = item.openingBal
            holder.tvCredit.text = item.credit
            holder.tvDebit.text = item.debit
            holder.tvNetChange.text = item.netChange
            holder.tvClosingBal.text = item.closingBal
        }

        override fun getItemCount() = list.size
    }
}