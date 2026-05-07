package com.example.bgls.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountBalanceDepositModel
import com.example.bgls.R

class AccountBalanceDepositAdapter(
    private var fullList: List<AccountBalanceDepositModel>,
    private val onAccountClick: (AccountBalanceDepositModel) -> Unit
) : RecyclerView.Adapter<AccountBalanceDepositAdapter.DepositViewHolder>() {

    private var filteredList: MutableList<AccountBalanceDepositModel> = fullList.toMutableList()

    class DepositViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrlNo: TextView          = view.findViewById(R.id.tvSrlNo)
        val tvCustomerId: TextView     = view.findViewById(R.id.tvCustomerId)
        val tvAccountId: TextView      = view.findViewById(R.id.tvAccountId)
        val tvAccountName: TextView    = view.findViewById(R.id.tvAccountName)
        val tvDateOfPeriod: TextView   = view.findViewById(R.id.tvDateOfPeriod)
        val tvDepositAmount: TextView  = view.findViewById(R.id.tvDepositAmount)
        val tvPeriod: TextView         = view.findViewById(R.id.tvPeriod)
        val tvRateOfInterest: TextView = view.findViewById(R.id.tvRateOfInterest)
        val tvMaturityDate: TextView   = view.findViewById(R.id.tvMaturityDate)
        val tvAccountBalance: TextView = view.findViewById(R.id.tvAccountBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepositViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_balance_deposit, parent, false)
        return DepositViewHolder(view)
    }

    override fun onBindViewHolder(holder: DepositViewHolder, position: Int) {
        val item = filteredList[position]
        holder.tvSrlNo.text          = item.srlNo.toString()
        holder.tvCustomerId.text     = item.customerId
        holder.tvAccountId.text      = item.accountId
        holder.tvAccountName.text    = item.accountName
        holder.tvDateOfPeriod.text   = item.dateOfPeriod
        holder.tvDepositAmount.text  = item.depositAmount
        holder.tvPeriod.text         = item.period
        holder.tvRateOfInterest.text = item.rateOfInterest
        holder.tvMaturityDate.text   = item.maturityDate
        holder.tvAccountBalance.text = item.accountBalance

        holder.tvAccountId.setTextColor(Color.parseColor("#1a6bb5"))

        // Alternate row background
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F8F9FA"))
        }

        holder.tvAccountId.setOnClickListener { onAccountClick(item) }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullList.toMutableList()
        } else {
            fullList.filter { item ->
                item.customerId.contains(query, ignoreCase = true) ||
                item.accountId.contains(query, ignoreCase = true) ||
                item.accountName.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<AccountBalanceDepositModel>) {
        fullList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}
