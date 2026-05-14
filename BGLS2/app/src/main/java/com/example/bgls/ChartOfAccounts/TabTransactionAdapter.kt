package com.example.bgls.ChartOfAccounts

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TabTransactionModel
import com.example.bgls.R

class TabTransactionAdapter(
    private val context: Context,
    private var list: List<TabTransactionModel>
) : RecyclerView.Adapter<TabTransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvId)
        val tvEvent: TextView = view.findViewById(R.id.tvEvent)
        val tvDebitAccNo: TextView = view.findViewById(R.id.tvDebitAccNo)
        val tvDebitAccName: TextView = view.findViewById(R.id.tvDebitAccName)
        val tvCreditAccNo: TextView = view.findViewById(R.id.tvCreditAccNo)
        val tvCreditAccName: TextView = view.findViewById(R.id.tvCreditAccName)
        val tvTranParticular: TextView = view.findViewById(R.id.tvTranParticular)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvAction: TextView = view.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_tab_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvId.text = item.id
        holder.tvEvent.text = item.event
        holder.tvDebitAccNo.text = item.debitAccNo
        holder.tvDebitAccName.text = item.debitAccName
        holder.tvCreditAccNo.text = item.creditAccNo
        holder.tvCreditAccName.text = item.creditAccName
        holder.tvTranParticular.text = item.tranParticular
        holder.tvType.text = item.type
        
        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        // Event Click -> Navigate to TransactionAccountViewActivity (VIEW MODE)
        holder.tvEvent.paintFlags = holder.tvEvent.paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
        holder.tvEvent.setTextColor(Color.parseColor("#2196F3"))
        holder.tvEvent.setOnClickListener {
            val intent = android.content.Intent(context, TransactionAccountViewActivity::class.java)
            intent.putExtra("MODE", "VIEW")
            context.startActivity(intent)
        }

        // Action Dropdown -> Edit, Delete
        holder.tvAction.text = "Action ▼"
        holder.tvAction.setTextColor(Color.parseColor("#2196F3"))
        holder.tvAction.setOnClickListener {
            val popup = android.widget.PopupMenu(context, holder.tvAction)
            popup.menu.add("Edit")
            popup.menu.add("Delete")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "Edit" -> {
                        val intent = android.content.Intent(context, TransactionAccountModifyActivity::class.java)
                        context.startActivity(intent)
                        true
                    }
                    "Delete" -> {
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Delete Account")
                            .setMessage("Are you sure you want to delete this Transaction Account?")
                            .setPositiveButton("Yes") { _, _ ->
                                val pos = holder.adapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    val mutableList = list.toMutableList()
                                    mutableList.removeAt(pos)
                                    updateList(mutableList)
                                    android.widget.Toast.makeText(context, "Deleted", android.widget.Toast.LENGTH_SHORT).show()
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

    fun updateList(newList: List<TabTransactionModel>) {
        list = newList
        notifyDataSetChanged()
    }
}


