package com.example.bgls.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.DepositAccountModel
import com.example.bgls.R

class DepositAccountAdapter(
    private val accountList: List<DepositAccountModel>,
    private val onActionClick: (DepositAccountModel) -> Unit
) : RecyclerView.Adapter<DepositAccountAdapter.AccountViewHolder>() {

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustId: TextView = view.findViewById(R.id.tvCustId)
        val tvCustName: TextView = view.findViewById(R.id.tvCustName)
        val tvActNo: TextView = view.findViewById(R.id.tvActNo)
        val tvDateOfDeposit: TextView = view.findViewById(R.id.tvDateOfDeposit)
        val tvDepositAmount: TextView = view.findViewById(R.id.tvDepositAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val spinnerAction: android.widget.Spinner = view.findViewById(R.id.spinnerAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_deposit_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = accountList[position]
        holder.tvCustId.text = account.custId
        holder.tvCustName.text = account.custName
        holder.tvActNo.text = account.actNo
        holder.tvDateOfDeposit.text = account.dateOfDeposit
        holder.tvDepositAmount.text = account.depositAmount
        holder.tvStatus.text = account.status

        if (account.status.equals("Verified", ignoreCase = true)) {
            holder.tvStatus.setTextColor(Color.parseColor("#28A745")) // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#DC3545")) // Red
        }

        val options = arrayOf("Action", "View", "Modify")
        val adapter = android.widget.ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_dropdown_item, options)
        holder.spinnerAction.adapter = adapter

        holder.spinnerAction.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos == 1) { // "View" selected
                    onActionClick(account)
                    holder.spinnerAction.setSelection(0) // Reset to "Action"
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    override fun getItemCount(): Int = accountList.size
}
