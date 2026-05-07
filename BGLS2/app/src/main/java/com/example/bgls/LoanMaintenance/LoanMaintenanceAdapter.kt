package com.example.bgls.LoanMaintenance

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.LoanMaintenanceModel
import com.example.bgls.R

class LoanMaintenanceAdapter(
    private val context: Context,
    private val list: List<LoanMaintenanceModel>
) : RecyclerView.Adapter<LoanMaintenanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSNo: TextView = view.findViewById(R.id.tvSNo)
        val tvLoanId: TextView = view.findViewById(R.id.tvLoanId)
        val tvLoanType: TextView = view.findViewById(R.id.tvLoanType)
        val tvLoanName: TextView = view.findViewById(R.id.tvLoanName)
        val tvMobileNo: TextView = view.findViewById(R.id.tvMobileNo)
        val tvRetailerBranchId: TextView = view.findViewById(R.id.tvRetailerBranchId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_loan_maintenance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSNo.text = item.sNo
        holder.tvLoanId.text = item.loanId
        holder.tvLoanType.text = item.loanType
        holder.tvLoanName.text = item.loanName
        holder.tvMobileNo.text = item.mobileNo
        holder.tvRetailerBranchId.text = item.retailerBranchId
        holder.tvStatus.text = item.status

        // Make Loan ID look like a clickable link
        holder.tvLoanId.paintFlags = holder.tvLoanId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvLoanId.setTextColor(Color.parseColor("#2196F3"))

        holder.tvLoanId.setOnClickListener {
            val intent = Intent(context, LoanMaintananceViewActivity::class.java)
            intent.putExtra("loanId", item.loanId)
            intent.putExtra("holderKey", "8a81878d91c781030191c95350901e14") // Dummy holderKey for testing
            intent.putExtra("branchKey", "8a81878d91c781030191c95350901e14") // Dummy branchKey for testing
            intent.putExtra("source", "LoanMaintenance")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}