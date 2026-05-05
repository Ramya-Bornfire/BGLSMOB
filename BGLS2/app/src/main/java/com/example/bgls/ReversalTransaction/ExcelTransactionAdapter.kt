package com.example.bgls.ReversalTransaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.ExcelTransactionModel
import com.example.bgls.R

class ExcelTransactionAdapter(
    private val list: List<ExcelTransactionModel>,
    private val onAccountsClick: (Int) -> Unit,
    private val onValuesClick: (Int) -> Unit
) : RecyclerView.Adapter<ExcelTransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTranId: TextView = view.findViewById(R.id.tvTranId)
        val etNames: android.widget.EditText = view.findViewById(R.id.etNames)
        val etReference: android.widget.EditText = view.findViewById(R.id.etReference)
        val etMobile: android.widget.EditText = view.findViewById(R.id.etMobile)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvAllocatedAmt: TextView = view.findViewById(R.id.tvAllocatedAmt)
        val tvTransTime: TextView = view.findViewById(R.id.tvTransTime)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbAccounts: RadioButton = view.findViewById(R.id.rbAccounts)
        val rbValues: RadioButton = view.findViewById(R.id.rbValues)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_excel_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.tvTranId.text = item.tranId
        holder.etNames.setText(item.names)
        holder.etReference.setText(item.reference)
        holder.etMobile.setText(item.mobileNumber)
        holder.tvAmount.text = item.amount
        holder.tvAllocatedAmt.text = item.allocatedAmount
        holder.tvTransTime.text = item.transTime
        holder.tvStatus.text = item.status
        
        holder.rbAccounts.isChecked = item.isAccountsSelected
        holder.rbValues.isChecked = item.isValuesSelected

        // Handle edits (Simple way: update model on bind, ideally use TextWatcher)
        // But for mock, simple setText is fine. If you need it persistent:
        /*
        holder.etNames.doAfterTextChanged { item.names = it.toString() }
        */

        holder.rbAccounts.setOnClickListener {
            item.isAccountsSelected = true
            onAccountsClick(position)
            notifyItemChanged(position)
        }

        holder.rbValues.setOnClickListener {
            item.isValuesSelected = true
            onValuesClick(position)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = list.size
}
