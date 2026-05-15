package com.example.bgls.ChartOfAccounts

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.CustomerMaster.AccountLedgerActivity
import com.example.bgls.DataModels.TabLedgerModel
import com.example.bgls.R

class TabLedgerAdapter(
    private val context: Context,
    private val initialList: List<TabLedgerModel>
) : RecyclerView.Adapter<TabLedgerAdapter.ViewHolder>() {

    private var fullList: List<TabLedgerModel> = initialList
    private val list = initialList.toMutableList()
    
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
        
        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        if (item.status.equals("Active", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatus.setTextColor(Color.RED)
        }

        // Account ID Click -> Navigate to AccountLedgerActivity
        holder.tvAcctId.paintFlags = holder.tvAcctId.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        holder.tvAcctId.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAcctId.setOnClickListener {
            val intent = Intent(context, AccountLedgerActivity::class.java)
            intent.putExtra("acct_num", item.acctId)   // <-- add this line
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<TabLedgerModel>) {
        fullList = newList
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(head: String, acctId: String, name: String, currency: String, credits: String, debits: String, balance: String, status: String) {
        val f1 = head.lowercase()
        val f2 = acctId.lowercase()
        val f3 = name.lowercase()
        val f4 = currency.lowercase()
        val f5 = credits.lowercase()
        val f6 = debits.lowercase()
        val f7 = balance.lowercase()
        val f8 = status.lowercase()

        val filtered = fullList.filter {
            (f1.isEmpty() || it.head.lowercase().contains(f1)) &&
            (f2.isEmpty() || it.acctId.lowercase().contains(f2)) &&
            (f3.isEmpty() || it.acctName.lowercase().contains(f3)) &&
            (f4.isEmpty() || it.currency.lowercase().contains(f4)) &&
            (f5.isEmpty() || it.credits.lowercase().contains(f5)) &&
            (f6.isEmpty() || it.debits.lowercase().contains(f6)) &&
            (f7.isEmpty() || it.balance.lowercase().contains(f7)) &&
            (f8.isEmpty() || it.status.lowercase().contains(f8))
        }

        list.clear()
        list.addAll(filtered)
        notifyDataSetChanged()
    }
}


