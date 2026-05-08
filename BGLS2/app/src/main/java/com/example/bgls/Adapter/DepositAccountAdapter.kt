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

    private var selectedPosition = -1

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCustId: TextView = view.findViewById(R.id.tvCustId)
        val tvCustName: TextView = view.findViewById(R.id.tvCustName)
        val tvActNo: TextView = view.findViewById(R.id.tvActNo)
        val tvDateOfDeposit: TextView = view.findViewById(R.id.tvDateOfDeposit)
        val tvDepositAmount: TextView = view.findViewById(R.id.tvDepositAmount)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val rbSelect: android.widget.RadioButton = view.findViewById(R.id.rbSelect)
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

        holder.rbSelect.isChecked = position == selectedPosition

        holder.rbSelect.setOnClickListener {
            selectedPosition = holder.adapterPosition
            notifyDataSetChanged()
            onActionClick(account)
        }

        holder.tvCustId.setOnClickListener {
            onActionClick(account)
        }
    }

    override fun getItemCount(): Int = accountList.size
}
