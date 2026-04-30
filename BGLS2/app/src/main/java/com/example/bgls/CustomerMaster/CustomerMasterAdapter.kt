package com.example.bgls.CustomerMaster

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
        val tvSno: TextView        = itemView.findViewById(R.id.tvSno)
        val tvCustomerId: TextView = itemView.findViewById(R.id.tvCustomerId)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val tvDob: TextView        = itemView.findViewById(R.id.tvDob)
        val tvBranchName: TextView = itemView.findViewById(R.id.tvBranchName)
        val tvMobileNo: TextView   = itemView.findViewById(R.id.tvMobileNo)
        val tvEmail: TextView      = itemView.findViewById(R.id.tvEmail)
        val tvStatus: TextView     = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = customerList[position]

        // All fields are now nullable – fall back to empty string safely
        holder.tvSno.text          = (position + 1).toString()
        holder.tvCustomerId.text   = item.customerId   ?: ""
        holder.tvCustomerName.text = item.customerName ?: ""
        holder.tvDob.text          = formatDate(item.dob)
        holder.tvBranchName.text   = item.branchName   ?: ""
        holder.tvMobileNo.text     = item.mobileNo     ?: ""
        holder.tvEmail.text        = item.email        ?: ""
        holder.tvStatus.text       = item.status       ?: ""

        // Alternate row background for readability
        holder.itemView.setBackgroundColor(
            if (position % 2 == 0) Color.WHITE else Color.parseColor("#F9F9F9")
        )

        // Status badge colour hint
        when (item.status?.uppercase()) {
            "1", "Y", "ACTIVE"   -> holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
            "0", "N", "INACTIVE" -> holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
            else                 -> holder.tvStatus.setTextColor(Color.parseColor("#555555"))
        }

        holder.itemView.setOnClickListener { onCustomerClick(item) }
        holder.tvCustomerId.setOnClickListener { onCustomerClick(item) }
    }

    override fun getItemCount(): Int = customerList.size

    fun updateList(newList: List<CustomerMaster>) {
        customerList = newList
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        try {
            // Check if it's already an epoch timestamp in milliseconds
            val epoch = dateString.toLongOrNull()
            if (epoch != null) {
                val date = java.util.Date(epoch)
                val format = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
                return format.format(date)
            }
            
            // Try ISO format
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val outputFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.US)
            val date = inputFormat.parse(dateString)
            return if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            return dateString
        }
    }
}