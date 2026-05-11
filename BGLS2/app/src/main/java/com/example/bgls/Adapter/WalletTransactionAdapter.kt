package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class WalletTransaction(
    val tranDate: String,
    val valueDate: String,
    val custId: String,
    val acctNum: String,
    val acctName: String,
    val tranType: String,
    val particulars: String,
    val currency: String,
    val amount: String
)

class WalletTransactionAdapter(private val transactions: List<WalletTransaction>) :
    RecyclerView.Adapter<WalletTransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvValueDate: TextView = view.findViewById(R.id.tvValueDate)
        val tvCustId: TextView = view.findViewById(R.id.tvCustId)
        val tvAccNo: TextView = view.findViewById(R.id.tvAccNo)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvParticulars: TextView = view.findViewById(R.id.tvParticulars)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_wallet_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val tran = transactions[position]
        holder.tvTranDate.text = tran.tranDate
        holder.tvValueDate.text = tran.valueDate
        holder.tvCustId.text = tran.custId
        holder.tvAccNo.text = tran.acctNum
        holder.tvName.text = tran.acctName
        holder.tvType.text = tran.tranType
        holder.tvParticulars.text = tran.particulars
        holder.tvCurrency.text = tran.currency
        holder.tvAmount.text = tran.amount
        
        // Alternate colors
        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA"))
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
        }
    }

    override fun getItemCount(): Int = transactions.size
}
