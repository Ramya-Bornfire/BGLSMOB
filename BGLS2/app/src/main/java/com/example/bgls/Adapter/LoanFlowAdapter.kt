package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanFlowModel
import com.example.bgls.R

class LoanFlowAdapter(private val flowList: List<LoanFlowModel>) :
    RecyclerView.Adapter<LoanFlowAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFlowDate: TextView = view.findViewById(R.id.tvFlowDate)
        val tvFlowCode: TextView = view.findViewById(R.id.tvFlowCode)
        val tvFlowFreq: TextView = view.findViewById(R.id.tvFlowFreq)
        val tvFlowAmt: TextView = view.findViewById(R.id.tvFlowAmt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan_flow, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = flowList[position]
        holder.tvFlowDate.text = item.flowDate
        holder.tvFlowCode.text = item.flowCode
        holder.tvFlowFreq.text = item.flowFreq
        holder.tvFlowAmt.text = item.flowAmt
    }

    override fun getItemCount() = flowList.size
}
