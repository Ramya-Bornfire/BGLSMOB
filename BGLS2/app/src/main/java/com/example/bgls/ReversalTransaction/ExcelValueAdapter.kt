package com.example.bgls.ReversalTransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class ExcelValueModel(
    val flowDate: String,
    val flowId: String,
    val flowCode: String,
    val flowAmt: String,
    val acctNo: String,
    val acctName: String,
    var isSelected: Boolean = false
)

class ExcelValueAdapter(
    private val list: List<ExcelValueModel>,
    private val onSelected: (ExcelValueModel) -> Unit
) : RecyclerView.Adapter<ExcelValueAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFlowDate: TextView = view.findViewById(R.id.tvFlowDate)
        val tvFlowId: TextView = view.findViewById(R.id.tvFlowId)
        val tvFlowCode: TextView = view.findViewById(R.id.tvFlowCode)
        val tvFlowAmt: TextView = view.findViewById(R.id.tvFlowAmt)
        val tvAcctNo: TextView = view.findViewById(R.id.tvAcctNo)
        val tvAcctName: TextView = view.findViewById(R.id.tvAcctName)
        val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_excel_value, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvFlowDate.text = item.flowDate
        holder.tvFlowId.text = item.flowId
        holder.tvFlowCode.text = item.flowCode
        holder.tvFlowAmt.text = item.flowAmt
        holder.tvAcctNo.text = item.acctNo
        holder.tvAcctName.text = item.acctName
        holder.rbSelect.isChecked = item.isSelected

        holder.rbSelect.setOnClickListener {
            onSelected(item)
        }
    }

    override fun getItemCount() = list.size
}
