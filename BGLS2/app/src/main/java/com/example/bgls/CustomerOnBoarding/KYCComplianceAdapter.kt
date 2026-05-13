package com.example.bgls.CustomerOnBoarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R

data class KYCItem(
    val srNo: String,
    val customerGroup: String,
    val applRefNo: String,
    val accountType: String,
    val customerName: String,
    val nationalId: String,
    val status: String? = null,
    var isSelected: Boolean = false
)

class KYCComplianceAdapter(
    private var items: List<KYCItem>,
    private val onItemSelected: (KYCItem) -> Unit
) : RecyclerView.Adapter<KYCComplianceAdapter.ViewHolder>() {

    private var selectedAppRefNo: String? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSrNo: TextView = view.findViewById(R.id.tvSrNo)
        val tvCustomerGroup: TextView = view.findViewById(R.id.tvCustomerGroup)
        val tvApplRefNo: TextView = view.findViewById(R.id.tvApplRefNo)
        val tvAccountType: TextView = view.findViewById(R.id.tvAccountType)
        val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
        val tvNationalId: TextView = view.findViewById(R.id.tvNationalId)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: RadioButton = view.findViewById(R.id.rbSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kyccompliance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvSrNo.text = item.srNo
        holder.tvCustomerGroup.text = item.customerGroup
        holder.tvApplRefNo.text = item.applRefNo
        holder.tvAccountType.text = item.accountType
        holder.tvCustomerName.text = item.customerName
        holder.tvNationalId.text = item.nationalId

        if (item.status != null) {
            holder.tvStatus.visibility = View.VISIBLE
            holder.tvStatus.text = item.status
        } else {
            holder.tvStatus.visibility = View.GONE
        }

        holder.rbSelect.isChecked = item.applRefNo == selectedAppRefNo

        holder.rbSelect.setOnClickListener {
            selectedAppRefNo = item.applRefNo
            notifyDataSetChanged()
            onItemSelected(item)
        }
    }

    override fun getItemCount() = items.size
    
    fun updateList(newList: List<KYCItem>) {
        items = newList
        notifyDataSetChanged()
    }
}
