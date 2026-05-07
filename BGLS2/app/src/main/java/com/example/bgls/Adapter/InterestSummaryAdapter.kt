package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.InterestSummaryModel
import com.example.bgls.R

class InterestSummaryAdapter(
    private var dataList: List<InterestSummaryModel>,
    private val onLoanNoClick: (InterestSummaryModel) -> Unit
) : RecyclerView.Adapter<InterestSummaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLoanNo: TextView = view.findViewById(R.id.tvLoanNo)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvDateOfLoan: TextView = view.findViewById(R.id.tvDateOfLoan)
        val tvLoanAmt: TextView = view.findViewById(R.id.tvLoanAmt)
        val tvInterestRate: TextView = view.findViewById(R.id.tvInterestRate)
        val tvLiability: TextView = view.findViewById(R.id.tvLiability)
        val tvAccruedInterest: TextView = view.findViewById(R.id.tvAccruedInterest)
        val tvBookedInterest: TextView = view.findViewById(R.id.tvBookedInterest)
        val tvAppliedInterest: TextView = view.findViewById(R.id.tvAppliedInterest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_interest_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvLoanNo.text = item.loanNo
        holder.tvName.text = item.name
        holder.tvDateOfLoan.text = item.dateOfLoan
        holder.tvLoanAmt.text = item.loanAmt
        holder.tvInterestRate.text = item.interestRate
        holder.tvLiability.text = item.liability
        holder.tvAccruedInterest.text = item.accruedInterest
        holder.tvBookedInterest.text = item.bookedInterest
        holder.tvAppliedInterest.text = item.appliedInterest

        holder.tvLoanNo.setOnClickListener { onLoanNoClick(item) }
    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(newData: List<InterestSummaryModel>) {
        dataList = newData
        notifyDataSetChanged()
    }
}
