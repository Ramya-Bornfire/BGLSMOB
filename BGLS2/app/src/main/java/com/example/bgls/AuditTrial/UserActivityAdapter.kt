package com.example.bgls.AuditTrial

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.UserActivityItem
import com.example.bgls.R
import formatAuditDateFromIso
import formatEntryTimeFromIso


class UserActivityAdapter(
    private val context: Context,
    private val activityList: List<UserActivityItem>
) : RecyclerView.Adapter<UserActivityAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvAuditDate: TextView = itemView.findViewById(R.id.tvAuditDate)
        val tvTableName: TextView = itemView.findViewById(R.id.tvTableName)
        val tvFunction: TextView = itemView.findViewById(R.id.tvFunction)
        val tvEntryUser: TextView = itemView.findViewById(R.id.tvEntryUser)
        val tvEntryTime: TextView = itemView.findViewById(R.id.tvEntryTime)
        val tvAuthorizer: TextView = itemView.findViewById(R.id.tvAuthorizer)
        val tvAuthorizerTime: TextView = itemView.findViewById(R.id.tvAuthorizerTime)
        val tvRemarks: TextView = itemView.findViewById(R.id.tvRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = activityList[position]
        holder.tvAuditDate.text = formatAuditDateFromIso(item.audit_date)
        holder.tvTableName.text = item.audit_table ?: ""
        holder.tvFunction.text = item.func_code ?: ""
        holder.tvEntryUser.text = item.entry_user ?: ""
        holder.tvEntryTime.text = formatEntryTimeFromIso(item.entry_time)
        //holder.tvEntryTime.text = item.entry_time ?: ""
        holder.tvAuthorizer.text = item.auth_user ?: ""
        holder.tvAuthorizerTime.text = item.auth_time ?: ""
        holder.tvRemarks.text = item.remarks ?: ""

        // Zebra striping
        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
    }

    override fun getItemCount(): Int = activityList.size
}

