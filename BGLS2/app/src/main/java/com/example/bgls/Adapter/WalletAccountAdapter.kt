package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.WalletAccountModel
import com.example.bgls.R

class WalletAccountAdapter(
    private val walletList: List<WalletAccountModel>,
    private val showStatus: Boolean = true,
    private val onCustIdClick: (String) -> Unit,
    private val onAccNoClick: (String) -> Unit,
    private val onItemClick: (WalletAccountModel) -> Unit
) : RecyclerView.Adapter<WalletAccountAdapter.WalletViewHolder>() {

    private var selectedPosition = -1

    class WalletViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvCustId: TextView = view.findViewById(R.id.tvCustId)
        val tvAccNo: TextView = view.findViewById(R.id.tvAccNo)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvOpenDate: TextView = view.findViewById(R.id.tvOpenDate)
        val tvCloseDate: TextView = view.findViewById(R.id.tvCloseDate)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: TextView = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet_account, parent, false)
        return WalletViewHolder(view)
    }

    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val account = walletList[position]

        holder.tvCategory.text = account.category
        holder.tvCustId.text = account.custId
        holder.tvAccNo.text = account.accNo
        holder.tvName.text = account.name
        holder.tvOpenDate.text = account.openDate
        holder.tvCloseDate.text = account.closeDate
        holder.tvCurrency.text = account.currency
        holder.tvBalance.text = account.balance
        holder.tvStatus.text = account.status
        
        if (showStatus) {
            holder.tvStatus.visibility = View.VISIBLE
            // Color coding for status
            if (account.status.equals("UnVerified", ignoreCase = true)) {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#DC3545")) // Red
            } else {
                holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#28A745")) // Green
            }
        } else {
            holder.tvStatus.visibility = View.GONE
        }

        // Zebra striping
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA"))
        }


        holder.rbSelect.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onItemClick(account)
        }

        holder.tvCustId.setOnClickListener {
            onCustIdClick(account.custId)
        }

        holder.tvAccNo.setOnClickListener {
            onAccNoClick(account.accNo)
        }
    }

    override fun getItemCount(): Int = walletList.size
}
