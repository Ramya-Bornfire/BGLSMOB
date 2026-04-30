package com.example.bgls.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CustomerMaintenanceModel
import com.example.bgls.R

class CustomerMaintenanceAdapter(
    private val context: Context,
    private val list: List<CustomerMaintenanceModel>
) : RecyclerView.Adapter<CustomerMaintenanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSNo: TextView = view.findViewById(R.id.tvSNo)
        val tvCustomerId: TextView = view.findViewById(R.id.tvCustomerId)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvDob: TextView = view.findViewById(R.id.tvDob)
        val tvBranchName: TextView = view.findViewById(R.id.tvBranchName)
        val tvMobileNo: TextView = view.findViewById(R.id.tvMobileNo)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_customer_maintenance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvSNo.text = item.sNo
        holder.tvCustomerId.text = item.customerId
        holder.tvCustomerName.text = item.customerName
        holder.tvDob.text = item.dob
        holder.tvBranchName.text = item.branchName
        holder.tvMobileNo.text = item.mobileNo
        holder.tvEmail.text = item.email
        holder.tvStatus.text = item.status

        // Make Customer ID look like a clickable link
        holder.tvCustomerId.paintFlags = holder.tvCustomerId.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        holder.tvCustomerId.setTextColor(Color.parseColor("#2196F3"))
        
        holder.tvCustomerId.setOnClickListener {
            val intent = android.content.Intent(context, com.example.bgls.CustomerMaintenance.CustomerMaintenanceViewActivity::class.java)
            intent.putExtra("CUSTOMER_ID", item.customerId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size
}
