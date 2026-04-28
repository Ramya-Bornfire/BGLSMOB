package com.example.bgls.LoanMaster

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanMaster
import com.example.bgls.R

class LoanMasterAdapter(
    private val context: Context,
    private var loanList: List<LoanMaster>,
    private val onLoanClick: (LoanMaster) -> Unit
) : RecyclerView.Adapter<LoanMasterAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSno: TextView = itemView.findViewById(R.id.tvSno)
        val tvLoanId: TextView = itemView.findViewById(R.id.tvLoanId)
        val tvLoanType: TextView = itemView.findViewById(R.id.tvLoanType)
        val tvLoanName: TextView = itemView.findViewById(R.id.tvLoanName)
        val tvMobileNo: TextView = itemView.findViewById(R.id.tvMobileNo)
        val tvRetailerBranchId: TextView = itemView.findViewById(R.id.tvRetailerBranchId)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_loan_master, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = loanList[position]

        holder.tvSno.text              = item.sno
        holder.tvLoanId.text           = item.loanId
        holder.tvLoanType.text         = item.loanType
        holder.tvLoanName.text         = item.loanName
        holder.tvMobileNo.text         = item.mobileNo
        holder.tvRetailerBranchId.text = item.retailerBranchId
        holder.tvStatus.text           = item.status

        // Alternate row background for readability
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F9F9F9"))
        }

        // Click listeners
        holder.itemView.setOnClickListener {
            onLoanClick(item)
        }

        holder.tvLoanId.setOnClickListener {
            onLoanClick(item)
        }
    }

    override fun getItemCount(): Int = loanList.size

    fun updateList(newList: List<LoanMaster>) {
        loanList = newList
        notifyDataSetChanged()
    }
}