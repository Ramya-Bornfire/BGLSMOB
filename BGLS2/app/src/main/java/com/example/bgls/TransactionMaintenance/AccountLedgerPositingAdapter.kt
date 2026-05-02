package com.example.bgls.TransactionMaintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountLedgerPostingModel
import com.example.bgls.R

class AccountLedgerPositingAdapter(
    private val list: MutableList<AccountLedgerPostingModel>,
    private val onItemSelected: (Int) -> Unit
) : RecyclerView.Adapter<AccountLedgerPositingAdapter.ViewHolder>() {

    private var selectedPosition = -1

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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account_ledger_positing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTranDate.text = item.tranDate
        holder.tvTranId.text = "${item.tranId}/${item.partTranId}"
        holder.tvPaTranTy.text = item.partTranType
        holder.tvCurrency.text = item.currency
        holder.tvAmount.text = item.amount
        holder.tvAcctId.text = item.acctId
        holder.tvAcctName.text = item.acctName
        holder.tvTranParticular.text = item.tranParticular
        holder.tvStatus.text = item.status
        
        holder.rbSelect.isChecked = position == selectedPosition

        holder.itemView.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedPosition)
            onItemSelected(position)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<AccountLedgerPostingModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
