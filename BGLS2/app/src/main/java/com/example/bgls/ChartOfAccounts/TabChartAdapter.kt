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

    private var list = initialList.toMutableList()

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
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F9F9F9"))
        }

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
                        AlertDialog.Builder(context)
                            .setTitle("Delete Account")
                            .setMessage("Are you sure you want to delete this Account?")
                            .setPositiveButton("Yes") { _, _ ->
                                val pos = holder.adapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    list.removeAt(pos)
                                    notifyItemRemoved(pos)
                                    Toast.makeText(context, "Account Deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .setNegativeButton("No", null)
                            .show()
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
        list = newList.toMutableList()
        notifyDataSetChanged()
    }
}
