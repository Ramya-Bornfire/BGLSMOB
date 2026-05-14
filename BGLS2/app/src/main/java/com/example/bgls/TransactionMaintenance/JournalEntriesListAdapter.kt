package com.example.bgls.TransactionMaintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class JournalEntryListModel(
    val tranDate: String,
    val tranId: String,
    val paTranTy: String,
    val currency: String,
    val amount: String,
    val acctId: String,
    val acctName: String,
    val tranParticular: String,
    val status: String
)

class JournalEntriesListAdapter(
    private var dataList: List<JournalEntryListModel>,
    private val onActionSelected: (String, JournalEntryListModel) -> Unit
) : RecyclerView.Adapter<JournalEntriesListAdapter.ViewHolder>() {

    fun updateData(newList: List<JournalEntryListModel>) {
        dataList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val tvPaTranTy: TextView = view.findViewById(R.id.tvPaTranTy)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvTranParticular: TextView = view.findViewById(R.id.tvTranParticular)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvAction: TextView = view.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journal_entry_list_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        holder.tvTranDate.text = item.tranDate
        holder.tvTranId.text = item.tranId
        holder.tvPaTranTy.text = item.paTranTy
        holder.tvCurrency.text = item.currency
        holder.tvAmount.text = item.amount
        holder.tvAcctId.text = item.acctId
        holder.tvAcctName.text = item.acctName
        holder.tvTranParticular.text = item.tranParticular
        holder.tvStatus.text = item.status

        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        holder.tvAction.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(view.context, view)
            popup.menu.add("View")
            popup.menu.add("Delete")
            
            popup.setOnMenuItemClickListener { menuItem ->
                onActionSelected(menuItem.title.toString(), item)
                true
            }
            popup.show()
        }
        holder.tvTranId.setOnClickListener {
            onActionSelected("View", item)
        }


    }


    override fun getItemCount(): Int {
        return dataList.size
    }
}


