package com.example.bgls.CustomerMaster

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class LoanScheduleItem(
    val dueDate: String,
    val principalExpenses: String,
    val interestExpenses: String,
    val feeExpenses: String,
    val penaltyExpenses: String,
    val repaidDate: String,
    val principalPaid: String,
    val interestPaid: String,
    val feePaid: String,
    val penaltyPaid: String,
    val totalDues: String
)

class LoanScheduleAdapter(
    private val context: Context,
    private val scheduleList: List<LoanScheduleItem>
) : RecyclerView.Adapter<LoanScheduleAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDueDate: TextView          = itemView.findViewById(R.id.tvDueDate)
        val tvPrincipalExpenses: TextView= itemView.findViewById(R.id.tvPrincipalExpenses)
        val tvInterestExpenses: TextView = itemView.findViewById(R.id.tvInterestExpenses)
        val tvFeeExpenses: TextView      = itemView.findViewById(R.id.tvFeeExpenses)
        val tvPenaltyExpenses: TextView  = itemView.findViewById(R.id.tvPenaltyExpenses)
        val tvRepaidDate: TextView       = itemView.findViewById(R.id.tvRepaidDate)
        val tvPrincipalPaid: TextView    = itemView.findViewById(R.id.tvPrincipalPaid)
        val tvInterestPaid: TextView     = itemView.findViewById(R.id.tvInterestPaid)
        val tvFeePaid: TextView          = itemView.findViewById(R.id.tvFeePaid)
        val tvPenaltyPaid: TextView      = itemView.findViewById(R.id.tvPenaltyPaid)
        val tvTotalDues: TextView        = itemView.findViewById(R.id.tvTotalDues)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_loan_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleList[position]

        holder.tvDueDate.text           = item.dueDate
        holder.tvPrincipalExpenses.text = item.principalExpenses
        holder.tvInterestExpenses.text  = item.interestExpenses
        holder.tvFeeExpenses.text       = item.feeExpenses
        holder.tvPenaltyExpenses.text   = item.penaltyExpenses
        holder.tvRepaidDate.text        = item.repaidDate
        holder.tvPrincipalPaid.text     = item.principalPaid
        holder.tvInterestPaid.text      = item.interestPaid
        holder.tvFeePaid.text           = item.feePaid
        holder.tvPenaltyPaid.text       = item.penaltyPaid
        holder.tvTotalDues.text         = item.totalDues
    }

    override fun getItemCount(): Int = scheduleList.size
}
