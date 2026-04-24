package com.example.bgls.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.CalendarModel
import com.example.bgls.R

class CalendarAdapter(
    private val list: List<CalendarModel>,
    private val onClick: (String) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    var selectedPosition = -1

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val year = view.findViewById<TextView>(R.id.txtYear)
        val month = view.findViewById<TextView>(R.id.txtMonth)
        val radio = view.findViewById<RadioButton>(R.id.radioSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.year.text = item.year
        holder.month.text = item.month

        holder.radio.isChecked = position == selectedPosition

        holder.radio.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()

            // 🔥 MAIN LOGIC
            onClick(item.month)
        }

        // 👉 optional (row click)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()

            onClick(item.month)
        }
    }

    override fun getItemCount() = list.size
}