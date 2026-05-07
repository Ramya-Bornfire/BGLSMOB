package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanScheduleModel
import com.example.bgls.R

class LoanScheduleAdapter(private val scheduleList: List<LoanScheduleModel>) :
    RecyclerView.Adapter<LoanScheduleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrlNo: TextView = view.findViewById(R.id.tvSrlNo)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvInstallmentAmt: TextView = view.findViewById(R.id.tvInstallmentAmt)
        val tvPrincipalAmt: TextView = view.findViewById(R.id.tvPrincipalAmt)
        val tvInterestAmt: TextView = view.findViewById(R.id.tvInterestAmt)
        val tvChargesAmt: TextView = view.findViewById(R.id.tvChargesAmt)
        val tvPrincipalOutstanding: TextView = view.findViewById(R.id.tvPrincipalOutstanding)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lease_loan_schedule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = scheduleList[position]
        holder.tvSrlNo.text = item.srlNo
        holder.tvDate.text = item.date
        holder.tvDescription.text = item.description
        holder.tvInstallmentAmt.text = item.installmentAmt
        holder.tvPrincipalAmt.text = item.principalAmt
        holder.tvInterestAmt.text = item.interestAmt
        holder.tvChargesAmt.text = item.chargesAmt
        holder.tvPrincipalOutstanding.text = item.principalOutstanding
    }

    override fun getItemCount() = scheduleList.size
}
