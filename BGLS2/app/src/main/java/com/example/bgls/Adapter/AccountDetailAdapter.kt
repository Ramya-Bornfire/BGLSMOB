package com.example.bgls.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class AccountDetail(
    val accountId: String,
    val accountName: String,
    val dateOfLoan: String,
    val loanAmount: String,
    val loanBalance: String
)

class AccountDetailAdapter(
    private val context: Context,
    private val accountList: List<AccountDetail>,
    private val onAccountClick: (AccountDetail) -> Unit
) : RecyclerView.Adapter<AccountDetailAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAccountId: TextView   = itemView.findViewById(R.id.tvAccountId)
        val tvAccountName: TextView = itemView.findViewById(R.id.tvAccountName)
        val tvDateOfLoan: TextView  = itemView.findViewById(R.id.tvDateOfLoan)
        val tvLoanAmount: TextView  = itemView.findViewById(R.id.tvLoanAmount)
        val tvLoanBalance: TextView = itemView.findViewById(R.id.tvLoanBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_account_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = accountList[position]

        holder.tvAccountId.text   = item.accountId
        holder.tvAccountName.text = item.accountName
        holder.tvDateOfLoan.text  = item.dateOfLoan
        holder.tvLoanAmount.text  = item.loanAmount
        holder.tvLoanBalance.text = item.loanBalance

        holder.tvAccountId.setOnClickListener {
            onAccountClick(item)
        }
    }

    override fun getItemCount(): Int = accountList.size
}
