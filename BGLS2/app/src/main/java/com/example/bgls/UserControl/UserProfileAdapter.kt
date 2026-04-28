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

    interface OnActionClickListener {
        fun onView(user: UserProfile)
        fun onEdit(user: UserProfile)
        fun onDelete(user: UserProfile, position: Int)
        fun onVerify(user: UserProfile, position: Int)
        fun onResetPassword(user: UserProfile)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserId: TextView = itemView.findViewById(R.id.tvUserId)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvAction: TextView = itemView.findViewById(R.id.tvAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_user_profile, parent, false)
    )

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUserId.text = user.userId ?: "N/A"
        holder.tvUserName.text = user.userName ?: "Unknown"
        holder.tvStatus.text = when (user.authFlg) {
            "N" -> "Verified"
            "Y" -> "Unverified"
            else -> "Unknown"
        }
        holder.tvStatus.setTextColor(
            if (user.authFlg == "N") context.getColor(android.R.color.holo_green_dark)
            else context.getColor(android.R.color.holo_orange_dark)
        )

        holder.tvUserId.setOnClickListener { listener.onView(user) }
        holder.tvAction.setOnClickListener { anchor ->
            showPopupMenu(anchor, user, position)
        }
    }

    override fun getItemCount() = userList.size

    private fun showPopupMenu(anchor: View, user: UserProfile, position: Int) {
        val popup = PopupMenu(context, anchor)
        popup.menu.add(0, 1, 0, "View")
        popup.menu.add(0, 2, 1, "Edit")
        popup.menu.add(0, 3, 2, "Delete")
        if (user.authFlg == "Y") {
            popup.menu.add(0, 4, 3, "Verify")
        }
        popup.menu.add(0, 5, 4, "Reset Password")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> listener.onView(user)
                2 -> listener.onEdit(user)
                3 -> listener.onDelete(user, position)
                4 -> listener.onVerify(user, position)
                5 -> listener.onResetPassword(user)
            }
            true
        }
        popup.show()
    }

    fun removeItem(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, userList.size)
    }

    fun updateList(newList: MutableList<UserProfile>) {
        userList = newList
        notifyDataSetChanged()
    }

    fun updateItem(position: Int, newUser: UserProfile) {
        userList[position] = newUser
        notifyItemChanged(position)
    }
}