package com.example.bgls.LoanSchedule

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanScheduleListModel
import com.example.bgls.R

class LoanScheduleListActivityAdapter(
    private val context: Context,
    private var list: List<LoanScheduleListModel>,
    private val onItemClick: (LoanScheduleListModel) -> Unit
) : RecyclerView.Adapter<LoanScheduleListActivityAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSno: TextView = itemView.findViewById(R.id.tvSno)
        val tvLoanName: TextView = itemView.findViewById(R.id.tvLoanName)
        val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        val tvRetailerName: TextView = itemView.findViewById(R.id.tvRetailerName)
        val tvRetailerBranchId: TextView = itemView.findViewById(R.id.tvRetailerBranchId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_loan_schedule_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.tvSno.text              = item.sno
        holder.tvLoanName.text         = item.loanName
        holder.tvLoanId.text           = item.loanId
        holder.tvRetailerName.text     = item.retailerName
        holder.tvRetailerBranchId.text = item.retailerBranchId
        holder.tvStatus.text           = item.status

        // Alternate row background
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F9F9F9"))
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.tvLoanId.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<LoanScheduleListModel>) {
        list = newList
        notifyDataSetChanged()
    }
}