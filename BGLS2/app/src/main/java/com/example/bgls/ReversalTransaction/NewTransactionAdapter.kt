package com.example.bgls.ReversalTransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ReversalDetailModel
import com.example.bgls.R

class NewTransactionAdapter(
    private val list: List<ReversalDetailModel>,
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<NewTransactionAdapter.ViewHolder>() {

    private var selectedIndex = 0

    fun setSelectedIndex(index: Int) {
        selectedIndex = index
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranIdPart: TextView = view.findViewById(R.id.tvTranIdPart)
        val tvPaTranTy: TextView = view.findViewById(R.id.tvPaTranTy)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAcctId: TextView = view.findViewById(R.id.tvAcctId)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_transactions_reversal_edit_new, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTranDate.text = item.tranDate
        holder.tvTranIdPart.text = "${item.tranId}/${item.partTranId}"
        holder.tvPaTranTy.text = item.partTranType
        holder.tvCurrency.text = item.currency
        holder.tvAmount.text = item.amount
        holder.tvAcctId.text = item.acctId
        holder.tvAcctName.text = item.acctName
        holder.tvStatus.text = item.tranStatus
        holder.rbSelect.isChecked = (position == selectedIndex)

        holder.itemView.setOnClickListener { onItemClick(position) }
        holder.ivDelete.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = list.size
}
