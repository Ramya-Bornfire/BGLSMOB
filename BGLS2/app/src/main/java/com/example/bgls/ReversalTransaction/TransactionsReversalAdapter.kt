package com.example.bgls.ReversalTransaction

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R

class TransactionsReversalAdapter(
    private val context: Context,
    private val initialList: List<ReversalTransactionModel>,
    private val onAcctIdClick: (Int) -> Unit = {},
    private val onSelectClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<TransactionsReversalAdapter.ViewHolder>() {

    private var fullList: List<ReversalTransactionModel> = initialList
    private val list = initialList.toMutableList()
    private var selectedIndex = -1

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
        val rbSelect: android.widget.RadioButton = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_transactions_reversal, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTranDate.text = item.tranDate
        holder.tvTranId.text = item.tranId
        holder.tvPaTranTy.text = item.paTranTy
        holder.tvCurrency.text = item.currency
        holder.tvAmount.text = item.amount
        holder.tvAcctId.text = item.acctId
        holder.tvAcctName.text = item.acctName
        holder.tvTranParticular.text = item.tranParticular
        holder.tvStatus.text = item.status
        
        holder.rbSelect.isChecked = (selectedIndex == position)
        
        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        // Make ACCT ID clickable as a link
        holder.tvAcctId.paintFlags = holder.tvAcctId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvAcctId.setOnClickListener {
            onAcctIdClick(position)
        }

        // Handle Row Selection (updates radio button state)
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onSelectClick(position)
        }

        // Handle Radio Button Click directly
        holder.rbSelect.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onSelectClick(position)
        }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<ReversalTransactionModel>) {
        fullList = newList
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    fun filter(date: String, id: String, type: String, curr: String, amt: String, acctId: String, name: String, particular: String, status: String) {
        val f1 = date.lowercase()
        val f2 = id.lowercase()
        val f3 = type.lowercase()
        val f4 = curr.lowercase()
        val f5 = amt.lowercase()
        val f6 = acctId.lowercase()
        val f7 = name.lowercase()
        val f8 = particular.lowercase()
        val f9 = status.lowercase()

        val filtered = fullList.filter {
            (f1.isEmpty() || it.tranDate.lowercase().contains(f1)) &&
            (f2.isEmpty() || it.tranId.lowercase().contains(f2)) &&
            (f3.isEmpty() || it.paTranTy.lowercase().contains(f3)) &&
            (f4.isEmpty() || it.currency.lowercase().contains(f4)) &&
            (f5.isEmpty() || it.amount.lowercase().contains(f5)) &&
            (f6.isEmpty() || it.acctId.lowercase().contains(f6)) &&
            (f7.isEmpty() || it.acctName.lowercase().contains(f7)) &&
            (f8.isEmpty() || it.tranParticular.lowercase().contains(f8)) &&
            (f9.isEmpty() || it.status.lowercase().contains(f9))
        }

        list.clear()
        list.addAll(filtered)
        notifyDataSetChanged()
    }
}


