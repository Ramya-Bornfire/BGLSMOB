package com.example.bgls

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.Transaction
import android.view.View

import com.example.bgls.databinding.ItemTransactionBinding

class TransactionAdapter(private val list: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    private var expandedPosition = -1   // track opened item

    inner class ViewHolder(val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvName.text = item.name

        val isExpanded = position == expandedPosition

        // ✅ Show dropdown ONLY for Admin
        if (item.name == "Admin") {
            holder.binding.layoutDropdown.visibility =
                if (isExpanded) View.VISIBLE else View.GONE
        } else {
            holder.binding.layoutDropdown.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            // Only allow click for Admin
            if (item.name == "Admin") {
                val previousExpanded = expandedPosition
                expandedPosition = if (isExpanded) -1 else position

                if (previousExpanded != -1) {
                    notifyItemChanged(previousExpanded)
                }
                notifyItemChanged(position)
            }
        }
    }


    override fun getItemCount() = list.size
}

