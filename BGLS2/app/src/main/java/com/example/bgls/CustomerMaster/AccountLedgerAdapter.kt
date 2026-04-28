package com.example.bgls.CustomerMaster

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class LedgerItem(
    val tranDate: String,
    val tranId: String,
    val tranParticulars: String,
    val currency: String,
    val credits: String,
    val debits: String,
    val balance: String
)

class AccountLedgerAdapter(
    private val context: Context,
    private val ledgerList: List<LedgerItem>
) : RecyclerView.Adapter<AccountLedgerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTranDate: TextView        = itemView.findViewById(R.id.tvTranDate)
        val tvTranId: TextView          = itemView.findViewById(R.id.tvTranId)
        val tvTranParticulars: TextView = itemView.findViewById(R.id.tvTranParticulars)
        val tvCurrency: TextView        = itemView.findViewById(R.id.tvCurrency)
        val tvCredits: TextView         = itemView.findViewById(R.id.tvCredits)
        val tvDebits: TextView          = itemView.findViewById(R.id.tvDebits)
        val tvBalance: TextView         = itemView.findViewById(R.id.tvBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_account_ledger, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = ledgerList[position]

        holder.tvTranDate.text        = item.tranDate
        holder.tvTranId.text          = item.tranId
        holder.tvTranParticulars.text = item.tranParticulars
        holder.tvCurrency.text        = item.currency
        holder.tvCredits.text         = item.credits
        holder.tvDebits.text          = item.debits
        holder.tvBalance.text         = item.balance
    }

    override fun getItemCount(): Int = ledgerList.size
}
