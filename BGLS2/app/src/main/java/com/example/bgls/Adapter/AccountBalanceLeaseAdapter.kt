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
    private var initialList: List<AccountBalanceLeaseModel>,
    private val onCustomerClick: (AccountBalanceLeaseModel) -> Unit,
    private val onAccountClick: (AccountBalanceLeaseModel) -> Unit
) : RecyclerView.Adapter<AccountBalanceLeaseAdapter.LeaseViewHolder>() {

    private var fullList: List<AccountBalanceLeaseModel> = initialList
    private var filteredList: MutableList<AccountBalanceLeaseModel> = initialList.toMutableList()

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

    fun updateData(newList: List<AccountBalanceLeaseModel>) {
        fullList = newList
        filteredList.clear()
        filteredList.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(srl: String, custId: String, acctId: String, name: String, date: String, loanAmt: String, disbAmt: String) {
        val f1 = srl.lowercase()
        val f2 = custId.lowercase()
        val f3 = acctId.lowercase()
        val f4 = name.lowercase()
        val f5 = date.lowercase()
        val f6 = loanAmt.lowercase()
        val f7 = disbAmt.lowercase()

        val filtered = fullList.filter {
            (f1.isEmpty() || it.srlNo.toString().lowercase().contains(f1)) &&
            (f2.isEmpty() || it.customerId.lowercase().contains(f2)) &&
            (f3.isEmpty() || it.accountId.lowercase().contains(f3)) &&
            (f4.isEmpty() || it.accountName.lowercase().contains(f4)) &&
            (f5.isEmpty() || it.dateOfLoan.lowercase().contains(f5)) &&
            (f6.isEmpty() || it.loanAmount.lowercase().contains(f6)) &&
            (f7.isEmpty() || it.disbursedAmount.lowercase().contains(f7))
        }

        filteredList.clear()
        filteredList.addAll(filtered)
        notifyDataSetChanged()
    }
}


