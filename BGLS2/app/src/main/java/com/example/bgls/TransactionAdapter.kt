package com.example.bgls

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.Transaction
import android.view.View
import android.widget.Toast
import android.content.Intent
import android.widget.TextView


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

        val context = holder.itemView.context

        // 👉 Recycler reuse issue avoid
        holder.binding.layoutDropdown.removeAllViews()

        if (item.subItems.isNotEmpty() && isExpanded) {

            item.subItems.forEach { subItem ->

                val textView = TextView(context)
                textView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                textView.text = subItem
                textView.setPadding(20, 20, 20, 20)

                // 👉 Click action
                textView.setOnClickListener {
                    Toast.makeText(context, "$subItem Clicked", Toast.LENGTH_SHORT).show()
                }

                holder.binding.layoutDropdown.addView(textView)
            }

            holder.binding.layoutDropdown.visibility = View.VISIBLE

        } else {
            holder.binding.layoutDropdown.visibility = View.GONE
        }

        // 👉 Expand / Collapse
        holder.itemView.setOnClickListener {
            val previousExpanded = expandedPosition
            expandedPosition = if (isExpanded) -1 else position

            if (previousExpanded != -1) notifyItemChanged(previousExpanded)
            notifyItemChanged(position)
        }
    }


    override fun getItemCount() = list.size
}

