package com.example.bgls.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CustomerMaster
import com.example.bgls.R

class CustomerMasterAdapter(
    private val context: Context,
    private var customerList: List<CustomerMaster>,
    private val onCustomerClick: (CustomerMaster) -> Unit
) : RecyclerView.Adapter<CustomerMasterAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSno: TextView          = itemView.findViewById(R.id.tvSno)
        val tvCustomerId: TextView   = itemView.findViewById(R.id.tvCustomerId)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvDob: TextView          = itemView.findViewById(R.id.tvDob)
        val tvBranchName: TextView   = itemView.findViewById(R.id.tvBranchName)
        val tvMobileNo: TextView     = itemView.findViewById(R.id.tvMobileNo)
        val tvEmail: TextView        = itemView.findViewById(R.id.tvEmail)
        val tvStatus: TextView       = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = customerList[position]

        holder.tvSno.text          = item.sno
        holder.tvCustomerId.text   = item.customerId
        holder.tvCustomerName.text = item.customerName
        holder.tvDob.text          = item.dob
        holder.tvBranchName.text   = item.branchName
        holder.tvMobileNo.text     = item.mobileNo
        holder.tvEmail.text        = item.email
        holder.tvStatus.text       = item.status

        // Alternate row background for readability
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#F9F9F9"))
        }

        // Highlight selected row (like row 10 in screenshot)
        holder.itemView.setOnClickListener {
            onCustomerClick(item)
        }

        // Customer ID clickable (blue link)
        holder.tvCustomerId.setOnClickListener {
            onCustomerClick(item)
        }
    }


    override fun getItemCount(): Int = customerList.size

    // ─── Update list when page changes or filter applied ───
    fun updateList(newList: List<CustomerMaster>) {
        customerList = newList
        notifyDataSetChanged()
    }
}