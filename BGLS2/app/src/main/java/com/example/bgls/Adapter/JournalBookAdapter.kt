package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.JournalBookModel
import com.example.bgls.R

class JournalBookAdapter(
    private var list: List<JournalBookModel>,
    private val onViewClick: (JournalBookModel) -> Unit,
    private val onDeleteClick: (JournalBookModel) -> Unit
) : RecyclerView.Adapter<JournalBookAdapter.ViewHolder>() {

    private var filteredList = list

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAcctNum: TextView = view.findViewById(R.id.tvAcctNum)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvParticular: TextView = view.findViewById(R.id.tvParticular)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvAction: TextView = view.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_journal_book, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredList[position]
        holder.tvTranDate.text = item.tranDate
        holder.tvTranId.text = "${item.tranId}/${item.partTranId}"
        holder.tvType.text = item.partTranType
        holder.tvCurrency.text = item.currency
        holder.tvAmount.text = item.amount
        holder.tvAcctNum.text = item.acctNum
        holder.tvAcctName.text = item.acctName
        holder.tvParticular.text = item.particular
        holder.tvStatus.text = item.status

        holder.tvAction.setOnClickListener {
            // Show popup menu for View/Delete
            val popup = android.widget.PopupMenu(holder.itemView.context, it)
            popup.menu.add("View")
            if (item.status == "ENTERED") {
                popup.menu.add("Delete")
            }
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "View" -> onViewClick(item)
                    "Delete" -> onDeleteClick(item)
                }
                true
            }
            popup.show()
        }
        
        holder.tvTranId.setOnClickListener { onViewClick(item) }
    }

    override fun getItemCount() = filteredList.size

    fun updateData(newList: List<JournalBookModel>) {
        list = newList
        filteredList = newList
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            list
        } else {
            list.filter {
                it.tranId.contains(query, true) || 
                it.acctNum.contains(query, true) || 
                it.acctName.contains(query, true)
            }
        }
        notifyDataSetChanged()
    }
}
