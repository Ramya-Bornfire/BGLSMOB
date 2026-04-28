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

    // ─── Interface for Action callbacks ───
    interface OnActionClickListener {
        fun onView(employee: EmployeeProfile)
        fun onEdit(employee: EmployeeProfile)
        fun onDelete(employee: EmployeeProfile, position: Int)
    }

    // ─── ViewHolder ───
    inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSrlNo: TextView = itemView.findViewById(R.id.tvSrlNo)
        val tvEmployeeId: TextView = itemView.findViewById(R.id.tvEmployeeId)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDesignation: TextView = itemView.findViewById(R.id.tvDesignation)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvMobile: TextView = itemView.findViewById(R.id.tvMobile)
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvProfileStatus: TextView = itemView.findViewById(R.id.tvProfileStatus)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_employee_profile, parent, false)
        return EmployeeViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employeeList[position]

        holder.tvSrlNo.text = employee.srlNo
        holder.tvEmployeeId.text = employee.employeeId
        holder.tvName.text = employee.name
        holder.tvDesignation.text = employee.designation
        holder.tvCategory.text = employee.category
        holder.tvMobile.text = employee.mobile
        holder.tvEmail.text = employee.email
        holder.tvProfileStatus.text = employee.profileStatus

        // Status color
        holder.tvProfileStatus.setTextColor(
            if (employee.profileStatus == "Verified")
                context.getColor(android.R.color.holo_green_dark)
            else
                context.getColor(android.R.color.holo_orange_dark)
        )

        // Click Employee ID → View
        holder.tvEmployeeId.setOnClickListener {
            listener.onView(employee)
        }

        // Action dropdown
        holder.tvAction.setOnClickListener { anchor ->
            showPopupMenu(anchor, employee, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = employeeList.size

    private fun showPopupMenu(anchor: View, employee: EmployeeProfile, position: Int) {
        val popup = PopupMenu(context, anchor)
        popup.menu.add(0, 1, 0, "View")
//        popup.menu.add(0, 2, 1, "Edit")
//        popup.menu.add(0, 3, 2, "Delete")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> { listener.onView(employee); true }
                2 -> { listener.onEdit(employee); true }
                3 -> { listener.onDelete(employee, position); true }
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
}