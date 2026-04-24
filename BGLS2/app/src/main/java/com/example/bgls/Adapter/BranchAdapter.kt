package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.Branch
import com.example.bgls.R

class BranchAdapter(private val list: List<Branch>) :
    RecyclerView.Adapter<BranchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrl: TextView = view.findViewById(R.id.tvSrl)
        val tvCode: TextView = view.findViewById(R.id.tvCode)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvSwift: TextView = view.findViewById(R.id.tvSwift)
        val tvHead: TextView = view.findViewById(R.id.tvHead)
        val spinner: Spinner = view.findViewById(R.id.spinnerAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_branch, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvSrl.text = item.srlNo.toString()
        holder.tvCode.text = item.code
        holder.tvName.text = item.name
        holder.tvSwift.text = item.swift
        holder.tvHead.text = item.head

        // Spinner actions
        val actions = listOf("Action", "Edit", "Delete")
        val adapter = ArrayAdapter(
            holder.itemView.context,
            android.R.layout.simple_spinner_dropdown_item,
            actions
        )
        holder.spinner.adapter = adapter
    }
}
