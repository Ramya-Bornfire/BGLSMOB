package com.example.bgls.ReversalTransaction

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalTransactionModel
import com.example.bgls.R

class TransactionsReversalAdapter(
    private val context: Context,
    private val list: List<ReversalTransactionModel>,
    private val onAcctIdClick: (Int) -> Unit = {},
    private val onSelectClick: (Int) -> Unit = {}
) : RecyclerView.Adapter<TransactionsReversalAdapter.ViewHolder>() {

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
        val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
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
        
        holder.rbSelect.isChecked = (position == selectedIndex)

        // Make ACCT ID clickable as a link
        holder.tvAcctId.paintFlags = holder.tvAcctId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvAcctId.setOnClickListener {
            onAcctIdClick(position)
        }

        // Handle Row Selection (updates radio button state)
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
        }

        // Handle Radio Button Click to navigate to Edit Screen
        holder.rbSelect.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onSelectClick(position)
        }
    }

    override fun getItemCount() = list.size
}
