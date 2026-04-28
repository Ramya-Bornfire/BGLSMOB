package com.example.bgls.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import com.example.bgls.data.model.BusinessActivity

class BusinessActivityAdapter(
    private val context: Context,
    private val activityList: List<BusinessActivity>
) : RecyclerView.Adapter<BusinessActivityAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_business_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = activityList[position]

        holder.tvAuditDate.text = item.auditDate
        holder.tvTableName.text = item.tableName
        holder.tvFunction.text = item.function
        holder.tvEntryUser.text = item.entryUser
        holder.tvEntryTime.text = item.entryTime
        holder.tvAuthorizer.text = item.authorizer
        holder.tvAuthorizerTime.text = item.authorizerTime
        holder.tvRemarks.text = item.remarks
    }

    override fun getItemCount(): Int = activityList.size
}