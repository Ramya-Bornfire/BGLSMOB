package com.example.bgls.UserControl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.UserProfile
import com.example.bgls.R

class UserProfileAdapter(
    private val context: Context,
    private var userList: MutableList<UserProfile>,
    private val listener: OnActionClickListener
) : RecyclerView.Adapter<UserProfileAdapter.UserViewHolder>() {

    // ─── Interface for Action callbacks ───
    interface OnActionClickListener {
        fun onView(user: UserProfile)
        fun onEdit(user: UserProfile)
        fun onDelete(user: UserProfile, position: Int)
    }

    // ─── ViewHolder ───
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_user_profile, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        holder.tvUserId.text = user.userId
        holder.tvUserName.text = user.userName
        holder.tvStatus.text = user.status

        // Status color
        holder.tvStatus.setTextColor(
            if (user.status == "Verified")
                context.getColor(android.R.color.holo_green_dark)
            else
                context.getColor(android.R.color.holo_orange_dark)
        )

        // Clicking USER ID → View details
        holder.tvUserId.setOnClickListener {
            listener.onView(user)
        }

        // Action dropdown menu
        holder.tvAction.setOnClickListener { anchorView ->
            showPopupMenu(anchorView, user, holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = userList.size

    // ─── PopupMenu with View / Edit / Delete ───
    private fun showPopupMenu(anchor: View, user: UserProfile, position: Int) {
        val popup = PopupMenu(context, anchor)
        popup.menu.add(0, 1, 0, "View")
        popup.menu.add(0, 2, 1, "Edit")
        popup.menu.add(0, 3, 2, "Delete")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> { listener.onView(user); true }
                2 -> { listener.onEdit(user); true }
                3 -> { listener.onDelete(user, position); true }
                else -> false
            }
        }
        popup.show()
    }

    // ─── Call this to remove a deleted item from list ───
    fun removeItem(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, userList.size)
    }

    // ─── Call this to swap data when toggling tabs ───
    fun updateList(newList: MutableList<UserProfile>) {
        userList = newList
        notifyDataSetChanged()
    }
}