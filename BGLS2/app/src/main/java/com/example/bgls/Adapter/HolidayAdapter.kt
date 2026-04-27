package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.HolidayModel
import com.example.bgls.R

class HolidayAdapter(private val list: List<HolidayModel>) :
    RecyclerView.Adapter<HolidayAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val year: TextView = view.findViewById(R.id.txtsYear)
        val month: TextView = view.findViewById(R.id.txtsMonth)
        val date: TextView = view.findViewById(R.id.txtsDate)
        val holiday: TextView = view.findViewById(R.id.txtHoliday)
        val remarks: TextView = view.findViewById(R.id.txtRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_holiday, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.year.text = item.year
        holder.month.text = item.month
        holder.date.text = item.date
        holder.holiday.text = item.description
        holder.remarks.text = item.remarks
    }

    override fun getItemCount() = list.size
}