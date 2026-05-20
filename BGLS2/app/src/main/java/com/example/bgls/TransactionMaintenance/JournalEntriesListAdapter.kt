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

    private var allItems = dataList.toList()

    fun setFullData(newList: List<JournalEntryListModel>) {
        allItems = newList.toList()
        dataList = newList
        notifyDataSetChanged()
    }

    fun applyFilters(
        tranDate: String,
        tranId: String,
        paTranTy: String,
        currency: String,
        amount: String,
        acctId: String,
        acctName: String,
        tranParticular: String,
        status: String
    ) {
        dataList = allItems.filter { item ->
            val matchDate = tranDate.isBlank() || (item.tranDate).contains(tranDate.trim(), true)
            val matchId = tranId.isBlank() || (item.tranId).contains(tranId.trim(), true)
            val matchTy = paTranTy.isBlank() || (item.paTranTy).contains(paTranTy.trim(), true)
            val matchCurr = currency.isBlank() || (item.currency).contains(currency.trim(), true)
            val matchAmt = amount.isBlank() || (item.amount).contains(amount.trim(), true)
            val matchAcct = acctId.isBlank() || (item.acctId).contains(acctId.trim(), true)
            val matchName = acctName.isBlank() || (item.acctName).contains(acctName.trim(), true)
            val matchPart = tranParticular.isBlank() || (item.tranParticular).contains(tranParticular.trim(), true)
            val matchStat = status.isBlank() || (item.status).contains(status.trim(), true)
            
            matchDate && matchId && matchTy && matchCurr && matchAmt && matchAcct && matchName && matchPart && matchStat
        }
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


