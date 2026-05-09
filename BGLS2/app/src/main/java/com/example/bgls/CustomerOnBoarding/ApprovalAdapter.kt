package com.example.bgls.CustomerOnBoarding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ApprovalModel
import com.example.bgls.databinding.ItemApprovalBinding

class ApprovalAdapter(
    private var list: List<ApprovalModel>,
    private val onItemSelected: (ApprovalModel) -> Unit
) : RecyclerView.Adapter<ApprovalAdapter.ViewHolder>() {

    private var selectedPosition = -1

    class ViewHolder(val binding: ItemApprovalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApprovalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.apply {
            tvSlNo.text = item.slNo.toString()
            tvCustGroup.text = item.custGroup
            tvAppRefNo.text = item.appRefNo
            tvAccountType.text = item.accountType
            tvCustomerName.text = item.customerName
            tvNationalId.text = item.nationalId
            tvStatus.text = item.status
            
            // Status color
            if (item.status == "NOT APPROVED") {
                tvStatus.setTextColor(Color.parseColor("#FF0000"))
            } else {
                tvStatus.setTextColor(Color.parseColor("#008000"))
            }

            rbSelect.isChecked = position == selectedPosition

            val clickListener = android.view.View.OnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = holder.adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onItemSelected(item)
            }

            root.setOnClickListener(clickListener)
            rbSelect.setOnClickListener(clickListener)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ApprovalModel>) {
        list = newList
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
