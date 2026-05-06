package com.example.bgls.TransactionMaintenance

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.MassEntryModel
import com.example.bgls.DataModels.MassEntryRequest
import com.example.bgls.R

class MassEntryAdapter(
    private val list: MutableList<MassEntryModel>,
    private val onAccountSearchRequested: (Int) -> Unit,
    private val onTotalCalculated: (Double, Double) -> Unit
) : RecyclerView.Adapter<MassEntryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etTranId: EditText = view.findViewById(R.id.etTranId)
        val etPartTranId: EditText = view.findViewById(R.id.etPartTranId)
        val etAccountNo: EditText = view.findViewById(R.id.etAccountNo)
        val etAccountName: EditText = view.findViewById(R.id.etAccountName)
        val spinnerPartTranType: Spinner = view.findViewById(R.id.spinnerPartTranType)
        val etTranAmount: EditText = view.findViewById(R.id.etTranAmount)
        val etTranParticular: EditText = view.findViewById(R.id.etTranParticular)
        val etRemarks: EditText = view.findViewById(R.id.etRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mass_entry_row, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        
        holder.etTranId.setText(item.tran_id)
        holder.etPartTranId.setText(item.part_tran_id)
        holder.etAccountNo.setText(item.acct_num)
        holder.etAccountName.setText(item.acct_name)
        holder.etTranAmount.setText(if (item.tran_amt == 0.0) "" else item.tran_amt.toString())
        holder.etTranParticular.setText(item.tran_particular)
        holder.etRemarks.setText(item.tran_remarks)

        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, arrayOf("Credit", "Debit"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerPartTranType.adapter = adapter
        holder.spinnerPartTranType.setSelection(if (item.part_tran_type == "Credit") 0 else 1)

        // Account Search click
        holder.etAccountNo.setOnClickListener { onAccountSearchRequested(position) }

        // Text Watchers to update model
        holder.etPartTranId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.part_tran_id = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.etTranAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { 
                item.tran_amt = s.toString().toDoubleOrNull() ?: 0.0
                calculateTotals()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.etTranParticular.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { item.tran_particular = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        holder.spinnerPartTranType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                item.part_tran_type = if (pos == 0) "Credit" else "Debit"
                calculateTotals()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun getItemCount() = list.size

    fun addRow(tranId: String) {
        val nextPartId = (list.size + 1).toString()
        list.add(MassEntryModel(tran_id = tranId, part_tran_id = nextPartId))
        notifyItemInserted(list.size - 1)
        calculateTotals()
    }

    fun removeRow() {
        if (list.isNotEmpty()) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
            calculateTotals()
        }
    }

    fun updateAccount(position: Int, acctNo: String, acctName: String) {
        list[position].acct_num = acctNo
        list[position].acct_name = acctName
        notifyItemChanged(position)
    }

    fun getEntries(): List<MassEntryRequest> {
        return list.map {
            MassEntryRequest(
                tran_id = it.tran_id,
                part_tran_id = it.part_tran_id,
                acct_num = it.acct_num,
                acct_name = it.acct_name,
                part_tran_type = it.part_tran_type,
                tran_amt = it.tran_amt,
                tran_particular = it.tran_particular,
                tran_remarks = it.tran_remarks,
                rate_code = null,
                rate = null,
                add_details = null
            )
        }
    }

    private fun calculateTotals() {
        var credit = 0.0
        var debit = 0.0
        list.forEach {
            if (it.part_tran_type == "Credit") credit += it.tran_amt
            else debit += it.tran_amt
        }
        onTotalCalculated(credit, debit)
    }
}
