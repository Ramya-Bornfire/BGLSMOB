package com.example.bgls.ChartOfAccounts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TabChartModel
import com.example.bgls.R

class TabChartAdapter(
    private val context: Context,
    private val initialList: List<TabChartModel>
) : RecyclerView.Adapter<TabChartAdapter.ViewHolder>() {

    private var fullList: List<TabChartModel> = initialList
    private val list = initialList.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHead: TextView = view.findViewById(R.id.tvHead)
        val tvGl: TextView = view.findViewById(R.id.tvGl)
        val tvSchemeCode: TextView = view.findViewById(R.id.tvSchemeCode)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvCredits: TextView = view.findViewById(R.id.tvCredits)
        val tvDebits: TextView = view.findViewById(R.id.tvDebits)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvAction: TextView = view.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_chart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvHead.text = item.head
        holder.tvGl.text = item.gl
        holder.tvSchemeCode.text = item.schemeCode
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

        // Account ID Click -> Navigate to View
        holder.tvAcctId.paintFlags = holder.tvAcctId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvAcctId.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAcctId.setOnClickListener {
            val intent = Intent(context, ChartOfAccountsDetailActivity::class.java)
            intent.putExtra("MODE", "VIEW")
            intent.putExtra("ACCT_NUM", item.acctId)   // 🔥 ADD THIS LINE
            context.startActivity(intent)
        }

        // Action Dropdown -> Modify, Verify, Delete, View
        holder.tvAction.text = "Action ▼"
        holder.tvAction.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAction.setOnClickListener {
            val popup = PopupMenu(context, holder.tvAction)
            popup.menu.add("Modify")
            popup.menu.add("Verify")
            popup.menu.add("Delete")
            popup.menu.add("View")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Modify", "Verify", "View" -> {
                        val intent = Intent(context, ChartOfAccountsDetailActivity::class.java)
                        intent.putExtra("MODE", menuItem.title.toString().uppercase())
                        intent.putExtra("ACCT_NUM", item.acctId)   // ✅ Add this line
                        context.startActivity(intent)
                        true
                    }
                    "Delete" -> {
                        val intent = Intent(context, ChartOfAccountsDetailActivity::class.java)
                        intent.putExtra("MODE", "DELETE")
                        intent.putExtra("ACCT_NUM", item.acctId)
                        context.startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<TabChartModel>) {
        fullList = newList
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(head: String, gl: String, scheme: String, acctId: String, name: String, currency: String, credits: String, debits: String, balance: String, status: String) {
        val f1 = head.lowercase()
        val f2 = gl.lowercase()
        val f3 = scheme.lowercase()
        val f4 = acctId.lowercase()
        val f5 = name.lowercase()
        val f6 = currency.lowercase()
        val f7 = credits.lowercase()
        val f8 = debits.lowercase()
        val f9 = balance.lowercase()
        val f10 = status.lowercase()

        val filtered = fullList.filter {
            (f1.isEmpty() || it.head.lowercase().contains(f1)) &&
            (f2.isEmpty() || it.gl.lowercase().contains(f2)) &&
            (f3.isEmpty() || it.schemeCode.lowercase().contains(f3)) &&
            (f4.isEmpty() || it.acctId.lowercase().contains(f4)) &&
            (f5.isEmpty() || it.acctName.lowercase().contains(f5)) &&
            (f6.isEmpty() || it.currency.lowercase().contains(f6)) &&
            (f7.isEmpty() || it.credits.lowercase().contains(f7)) &&
            (f8.isEmpty() || it.debits.lowercase().contains(f8)) &&
            (f9.isEmpty() || it.balance.lowercase().contains(f9)) &&
            (f10.isEmpty() || it.status.lowercase().contains(f10))
        }

        list.clear()
        list.addAll(filtered)
        notifyDataSetChanged()
    }
}


