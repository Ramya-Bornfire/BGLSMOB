package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.DepositFlowModel
import com.example.bgls.R

class DepositFlowAdapter(
    private val flowList: List<DepositFlowModel>,
    private val onItemClick: ((DepositFlowModel) -> Unit)? = null
) : RecyclerView.Adapter<DepositFlowAdapter.FlowViewHolder>() {

    private var isOperationsMode = false
    private var selectedPosition = -1

    fun setOperationsMode(enabled: Boolean) {
        isOperationsMode = enabled
        selectedPosition = -1
        notifyDataSetChanged()
    }

    class FlowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFlowId: TextView = view.findViewById(R.id.tvFlowId)
        val tvFlowCode: TextView = view.findViewById(R.id.tvFlowCode)
        val tvFlowDate: TextView = view.findViewById(R.id.tvFlowDate)
        val tvFlowAmount: TextView = view.findViewById(R.id.tvFlowAmount)
        val tvOutstandingBalance: TextView = view.findViewById(R.id.tvOutstandingBalance)
        val rbSelect: TextView = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlowViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deposit_flow, parent, false)
        return FlowViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlowViewHolder, position: Int) {
        val flow = flowList[position]

        holder.tvFlowId.text = flow.flowId
        holder.tvFlowCode.text = flow.flowCode
        holder.tvFlowDate.text = flow.flowDate
        holder.tvFlowAmount.text = flow.flowAmount
        holder.tvOutstandingBalance.text = flow.outstandingBalance

        // Show radio button only in operations mode
        holder.rbSelect.visibility = if (isOperationsMode) View.VISIBLE else View.GONE

        // Zebra striping
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#F8F9FA"))
        }
        holder.rbSelect.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onItemClick?.invoke(flow)
        }

        holder.itemView.setOnClickListener {
            if (isOperationsMode) {
                selectedPosition = position
                notifyDataSetChanged()
                onItemClick?.invoke(flow)
            }
        }
    }

    override fun getItemCount(): Int = flowList.size
}