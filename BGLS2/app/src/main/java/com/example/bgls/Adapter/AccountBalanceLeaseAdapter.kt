package com.example.bgls.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountBalanceLeaseModel
import com.example.bgls.R

class AccountBalanceLeaseAdapter(
    private var fullList: List<AccountBalanceLeaseModel>,
    private val onCustomerClick: (AccountBalanceLeaseModel) -> Unit,
    private val onAccountClick: (AccountBalanceLeaseModel) -> Unit
) : RecyclerView.Adapter<AccountBalanceLeaseAdapter.LeaseViewHolder>() {

    private var filteredList: MutableList<AccountBalanceLeaseModel> = fullList.toMutableList()

    class LeaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrlNo: TextView       = view.findViewById(R.id.tvSrlNo)
        val tvCustomerId: TextView  = view.findViewById(R.id.tvCustomerId)
        val tvAccountId: TextView   = view.findViewById(R.id.tvAccountId)
        val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
        val tvDateOfLoan: TextView  = view.findViewById(R.id.tvDateOfLoan)
        val tvLoanAmount: TextView  = view.findViewById(R.id.tvLoanAmount)
        val tvDisbAmount: TextView  = view.findViewById(R.id.tvDisbAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_account_balance_lease, parent, false)
        return LeaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaseViewHolder, position: Int) {
        val item = filteredList[position]
        holder.tvSrlNo.text       = item.srlNo.toString()
        holder.tvCustomerId.text  = item.customerId
        holder.tvAccountId.text   = item.accountId
        holder.tvAccountName.text = item.accountName
        holder.tvDateOfLoan.text  = item.dateOfLoan
        holder.tvLoanAmount.text  = item.loanAmount
        holder.tvDisbAmount.text  = item.disbursedAmount

        // Style clickable columns blue like the web
        holder.tvCustomerId.setTextColor(Color.parseColor("#1a6bb5"))
        holder.tvAccountId.setTextColor(Color.parseColor("#1a6bb5"))

        // Alternate row background
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        holder.tvCustomerId.setOnClickListener { onCustomerClick(item) }
        holder.tvAccountId.setOnClickListener  { onAccountClick(item)  }
    }

    override fun getItemCount(): Int = filteredList.size

    /** Filter by any column text */
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            fullList.toMutableList()
        } else {
            fullList.filter { item ->
                item.customerId.contains(query, ignoreCase = true) ||
                item.accountId.contains(query, ignoreCase = true) ||
                item.accountName.contains(query, ignoreCase = true) ||
                item.dateOfLoan.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun updateData(newList: List<AccountBalanceLeaseModel>) {
        fullList = newList
        filteredList = newList.toMutableList()
        notifyDataSetChanged()
    }
}


