package com.example.bgls.BatchJobExecution

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.FlowDetail
import com.example.bgls.R
import java.text.DecimalFormat

class FlowDetailAdapter(
    private var items: List<FlowDetail>,
    private val onItemSelected: (FlowDetail) -> Unit
) : RecyclerView.Adapter<FlowDetailAdapter.ViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flow_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvFlowDate.text = item.flowDate
        holder.tvFlowId.text = item.flowId
        holder.tvFlowCode.text = item.flowCode
        holder.tvFlowAmt.text = DecimalFormat("#,###.00").format(item.flowAmt)
        holder.tvAcctNo.text = item.acctNo
        holder.tvAcctName.text = item.acctName
        holder.rbSelect.isChecked = (position == selectedPosition)

        holder.rbSelect.setOnClickListener {
            selectedPosition = position
            onItemSelected(item)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<FlowDetail>) {
        items = newItems
        selectedPosition = -1
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvFlowDate: TextView = itemView.findViewById(R.id.tvFlowDate)
        val tvFlowId: TextView = itemView.findViewById(R.id.tvFlowId)
        val tvFlowCode: TextView = itemView.findViewById(R.id.tvFlowCode)
        val tvFlowAmt: TextView = itemView.findViewById(R.id.tvFlowAmt)
        val tvAcctNo: TextView = itemView.findViewById(R.id.tvAcctNo)
        val tvAcctName: TextView = itemView.findViewById(R.id.tvAcctName)
        val rbSelect: RadioButton = itemView.findViewById(R.id.rbSelectFlow)
    }
}