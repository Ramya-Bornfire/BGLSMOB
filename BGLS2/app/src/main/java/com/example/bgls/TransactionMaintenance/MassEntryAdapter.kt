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
import com.example.bgls.R

data class MassEntryModel(
    var tranId: String = "TR8868",
    var partTranId: String = "",
    var accountNo: String = "",
    var accountName: String = "",
    var partTranType: String = "Select",
    var tranAmount: String = "",
    var tranParticular: String = "",
    var remarks: String = "",
    var rateCode: String = "",
    var rate: String = "",
    var additionalRemarks: String = ""
)

class MassEntryAdapter(
    private var dataList: MutableList<MassEntryModel>,
    private val onTotalCalculated: (credit: Double, debit: Double) -> Unit
) : RecyclerView.Adapter<MassEntryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etTranId: EditText = view.findViewById(R.id.etTranId)
        val etPartTranId: EditText = view.findViewById(R.id.etPartTranId)
        val etAccountNo: EditText = view.findViewById(R.id.etAccountNo)
        val etAccountName: EditText = view.findViewById(R.id.etAccountName)
        val spinnerPartTranType: Spinner = view.findViewById(R.id.spinnerPartTranType)
        val etTranAmount: EditText = view.findViewById(R.id.etTranAmount)
        val etTranParticular: EditText = view.findViewById(R.id.etTranParticular)
        val etRemarks: EditText = view.findViewById(R.id.etRemarks)
        val etRateCode: EditText = view.findViewById(R.id.etRateCode)
        val etRate: EditText = view.findViewById(R.id.etRate)
        val etAdditionalRemarks: EditText = view.findViewById(R.id.etAdditionalRemarks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mass_entry_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        // Remove previous listeners to prevent recursive loops when recycling views
        holder.etTranAmount.tag = null
        holder.spinnerPartTranType.onItemSelectedListener = null

        holder.etTranId.setText(item.tranId)
        holder.etPartTranId.setText(item.partTranId)
        holder.etAccountNo.setText(item.accountNo)
        holder.etAccountName.setText(item.accountName)
        holder.etTranAmount.setText(item.tranAmount)
        holder.etTranParticular.setText(item.tranParticular)
        holder.etRemarks.setText(item.remarks)
        holder.etRateCode.setText(item.rateCode)
        holder.etRate.setText(item.rate)
        holder.etAdditionalRemarks.setText(item.additionalRemarks)

        val spinnerOptions = arrayOf("Select", "Credit", "Debit")
        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_dropdown_item, spinnerOptions)
        holder.spinnerPartTranType.adapter = adapter
        holder.spinnerPartTranType.setSelection(spinnerOptions.indexOf(item.partTranType).takeIf { it >= 0 } ?: 0)

        // Setup Amount listener
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                item.tranAmount = s.toString()
                calculateTotals()
            }
        }
        holder.etTranAmount.addTextChangedListener(textWatcher)
        holder.etTranAmount.tag = textWatcher // Store to remove later if needed

        // Setup Spinner listener
        holder.spinnerPartTranType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                item.partTranType = spinnerOptions[pos]
                calculateTotals()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun getItemCount(): Int = dataList.size

    fun addRow() {
        val nextPartId = (dataList.size + 1).toString()
        dataList.add(MassEntryModel(partTranId = nextPartId))
        notifyItemInserted(dataList.size - 1)
        calculateTotals()
    }

    fun removeRow() {
        if (dataList.isNotEmpty()) {
            dataList.removeAt(dataList.size - 1)
            notifyItemRemoved(dataList.size)
            calculateTotals()
        }
    }

    private fun calculateTotals() {
        var totalCredit = 0.0
        var totalDebit = 0.0

        for (item in dataList) {
            val amount = item.tranAmount.toDoubleOrNull() ?: 0.0
            if (item.partTranType == "Credit") {
                totalCredit += amount
            } else if (item.partTranType == "Debit") {
                totalDebit += amount
            }
        }
        onTotalCalculated(totalCredit, totalDebit)
    }

    fun getEntries(): List<com.example.bgls.DataModels.MassEntryRequest> {
        return dataList.map { item ->
            com.example.bgls.DataModels.MassEntryRequest(
                tran_id = item.tranId,
                part_tran_id = item.partTranId,
                acct_num = item.accountNo,
                acct_name = item.accountName,
                part_tran_type = item.partTranType,
                tran_amt = item.tranAmount.toDoubleOrNull() ?: 0.0,
                tran_particular = item.tranParticular,
                tran_remarks = item.remarks,
                rate_code = item.rateCode,
                rate = item.rate.toDoubleOrNull(),
                add_details = item.additionalRemarks
            )
        }
    }
}
