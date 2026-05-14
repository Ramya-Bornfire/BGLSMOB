package com.example.bgls.UserControl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.R

class EmployeeProfileAdapter(
    private val context: Context,
    private var employeeList: MutableList<EmployeeProfile>,
    private val listener: OnActionClickListener
) : RecyclerView.Adapter<EmployeeProfileAdapter.EmployeeViewHolder>() {

    interface OnActionClickListener {
        fun onView(employee: EmployeeProfile)
        fun onEdit(employee: EmployeeProfile)
        fun onDelete(employee: EmployeeProfile, position: Int)
    }

    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSrlNo: TextView = itemView.findViewById(R.id.tvSrlNo)
        val tvEmployeeId: TextView = itemView.findViewById(R.id.tvEmployeeId)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDesignation: TextView = itemView.findViewById(R.id.tvDesignation)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvStatus: TextView = itemView.findViewById(R.id.tvProfileStatus) // verify_flg
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_employee_profile, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val emp = employeeList[position]
        holder.tvSrlNo.text = (position + 1).toString()
        holder.tvEmployeeId.text = emp.employeeId ?: "N/A"
        holder.tvName.text = emp.employeeName ?: "Unknown"
        holder.tvDesignation.text = emp.design ?: "-"
        holder.tvCategory.text = emp.category ?: "-"
        holder.tvMobile.text = emp.mobile ?: "-"
        holder.tvEmail.text = emp.email ?: "-"
        holder.tvStatus.text = if (emp.verifyFlg == "Y") "Pending" else "Verified"
        holder.tvStatus.setTextColor(
            if (emp.verifyFlg == "N") context.getColor(android.R.color.holo_green_dark)
            else context.getColor(android.R.color.holo_orange_dark)
        )

        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)

        holder.tvEmployeeId.setOnClickListener { listener.onView(emp) }
        holder.tvAction.setOnClickListener { anchor ->
            showPopupMenu(anchor, emp, holder.adapterPosition)
        }
    }

    override fun getItemCount() = employeeList.size

    private fun showPopupMenu(anchor: View, emp: EmployeeProfile, position: Int) {
        val popup = PopupMenu(context, anchor)
        popup.menu.add(0, 1, 0, "View")
        popup.menu.add(0, 2, 1, "Edit")
        popup.menu.add(0, 3, 2, "Delete")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> { listener.onView(emp); true }
                2 -> { listener.onEdit(emp); true }
                3 -> { listener.onDelete(emp, position); true }
                else -> false
            }
        }
        popup.show()
    }

    fun removeItem(position: Int) {
        employeeList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, employeeList.size)
    }

    fun updateList(newList: MutableList<EmployeeProfile>) {
        employeeList = newList
        notifyDataSetChanged()
    }

    fun addItem(emp: EmployeeProfile) {
        employeeList.add(emp)
        notifyItemInserted(employeeList.size - 1)
    }
}

