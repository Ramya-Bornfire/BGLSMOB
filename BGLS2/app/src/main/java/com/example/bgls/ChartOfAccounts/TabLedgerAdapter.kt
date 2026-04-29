package com.example.bgls.ChartOfAccounts

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TabLedgerModel
import com.example.bgls.R

class TabLedgerAdapter(
    private val context: Context,
    private var list: List<TabLedgerModel>
) : RecyclerView.Adapter<TabLedgerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHead: TextView = view.findViewById(R.id.tvHead)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvCredits: TextView = view.findViewById(R.id.tvCredits)
        val tvDebits: TextView = view.findViewById(R.id.tvDebits)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_ledger, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvHead.text = item.head
        holder.tvAcctId.text = item.acctId
        holder.tvAcctName.text = item.acctName
        holder.tvCurrency.text = item.currency
        holder.tvCredits.text = item.credits
        holder.tvDebits.text = item.debits
        holder.tvBalance.text = item.balance
        holder.tvStatus.text = item.status

        if (item.status.equals("Active", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.setTextColor(Color.RED)
        }

        // Account ID Click -> Navigate to AccountLedgerActivity
        holder.tvAcctId.paintFlags = holder.tvAcctId.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        holder.tvAcctId.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAcctId.setOnClickListener {
            val intent = android.content.Intent(context, com.example.bgls.CustomerMaster.AccountLedgerActivity::class.java)
            // intent.putExtra("ACCT_ID", item.acctId) // Pass ID if needed
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<TabLedgerModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
