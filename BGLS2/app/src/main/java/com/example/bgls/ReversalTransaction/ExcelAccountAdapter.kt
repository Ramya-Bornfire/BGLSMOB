package com.example.bgls.ReversalTransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class AccountSearchModel(val number: String, val name: String)

class ExcelAccountAdapter(
    private val list: List<AccountSearchModel>,
    private val onAccountSelected: (AccountSearchModel) -> Unit
) : RecyclerView.Adapter<ExcelAccountAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAccNumber: TextView = view.findViewById(R.id.tvAccNumber)
        val tvAccName: TextView = view.findViewById(R.id.tvAccName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_excel_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvAccNumber.text = item.number
        holder.tvAccName.text = item.name
        holder.itemView.setOnClickListener { onAccountSelected(item) }
    }

    override fun getItemCount() = list.size
}
