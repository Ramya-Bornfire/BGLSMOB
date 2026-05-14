package com.example.bgls.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.R
import java.text.DecimalFormat

class DepositOperationAdapter(
    private val operationList: List<List<Any?>>,
    private val onTranIdClick: (String) -> Unit
) : RecyclerView.Adapter<DepositOperationAdapter.OperationViewHolder>() {

    class OperationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranDate: TextView = view.findViewById(R.id.tvTranDate)
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val tvTranParticulars: TextView = view.findViewById(R.id.tvTranParticulars)
        val tvCurrency: TextView = view.findViewById(R.id.tvCurrency)
        val tvCredits: TextView = view.findViewById(R.id.tvCredits)
        val tvDebits: TextView = view.findViewById(R.id.tvDebits)
        val tvBalance: TextView = view.findViewById(R.id.tvBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deposit_operation, parent, false)
        return OperationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val data = operationList[position]
        val df = DecimalFormat("#,##0.00")

        // Mapping based on web controller:
        // [0]: date, [1]: tranId, [2]: particular, [3]: currency, [5]: credits, [6]: debits
        holder.tvTranDate.text = formatDate(data.getOrNull(0)?.toString())
        holder.tvTranId.text = data.getOrNull(1)?.toString() ?: ""
        holder.tvTranParticulars.text = data.getOrNull(2)?.toString() ?: ""
        holder.tvCurrency.text = data.getOrNull(3)?.toString() ?: ""
        
        val credits = data.getOrNull(5)?.toString()?.toDoubleOrNull() ?: 0.0
        val debits = data.getOrNull(6)?.toString()?.toDoubleOrNull() ?: 0.0
        
        holder.tvCredits.text = df.format(credits)
        holder.tvDebits.text = df.format(debits)
        holder.tvBalance.text = "0.00" // Balance logic not fully clear in web, usually 0.00 there too

        holder.tvTranId.setTextColor(Color.parseColor("#007BFF"))
        holder.tvTranId.setOnClickListener {
            onTranIdClick(holder.tvTranId.text.toString())
        }

        holder.itemView.setBackgroundColor(android.graphics.Color.WHITE)
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null || dateStr == "null" || dateStr.isEmpty()) return ""
        return if (dateStr.contains("T")) {
            val parts = dateStr.split("T")[0].split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else if (dateStr.contains("-") && dateStr.length >= 10) {
            val parts = dateStr.substring(0, 10).split("-")
            if (parts.size == 3 && parts[0].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else dateStr
        } else {
            dateStr
        }
    }

    override fun getItemCount(): Int = operationList.size
}


