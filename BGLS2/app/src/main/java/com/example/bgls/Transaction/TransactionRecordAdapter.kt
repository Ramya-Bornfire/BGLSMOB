package com.example.bgls.Transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.TransactionRecord
import com.example.bgls.R

class TransactionRecordAdapter(
    private var transactionList: List<TransactionRecord>
) : RecyclerView.Adapter<TransactionRecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSNo: TextView = view.findViewById(R.id.tvSNo)
        val tvFlowId: TextView = view.findViewById(R.id.tvFlowId)
        val tvFlowDate: TextView = view.findViewById(R.id.tvFlowDate)
        val tvFlowCode: TextView = view.findViewById(R.id.tvFlowCode)
        val tvFlowAmount: TextView = view.findViewById(R.id.tvFlowAmount)
        val tvAccountNumber: TextView = view.findViewById(R.id.tvAccountNumber)
        val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = transactionList[position]
        holder.tvSNo.text = record.sNo
        holder.tvFlowId.text = record.flowId
        holder.tvFlowDate.text = record.flowDate
        holder.tvFlowCode.text = record.flowCode
        holder.tvFlowAmount.text = record.flowAmount
        holder.tvAccountNumber.text = record.accountNumber
        holder.tvAccountName.text = record.accountName

        // Zebra striping
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F9F9F9"))
        }
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateData(newList: List<TransactionRecord>) {
        transactionList = newList
        notifyDataSetChanged()
    }
}