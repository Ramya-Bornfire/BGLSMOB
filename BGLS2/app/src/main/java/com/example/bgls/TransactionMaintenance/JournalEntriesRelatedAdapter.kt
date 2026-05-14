package com.example.bgls.TransactionMaintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

class JournalEntriesRelatedAdapter(
    private val dataList: List<JournalEntryListModel>,
) : RecyclerView.Adapter<JournalEntriesRelatedAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val tvPaTranTy: TextView = view.findViewById(R.id.tvPaTranTy)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: TextView = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journal_entry_related, parent, false)
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
        holder.tvStatus.text = item.status

        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
    }

    override fun getItemCount(): Int = dataList.size
}


