package com.example.bgls.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.databinding.DialogDepositFlowBinding
import com.example.bgls.databinding.ItemFlowRowBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class DepositFlowDialog : DialogFragment() {

    private var _binding: DialogDepositFlowBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDepositFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerViewFlow.layoutManager = LinearLayoutManager(requireContext())
        val args = arguments ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getDepositFlow(
                    depositType = args.getString("deposit_type", ""),
                    depoActNo = args.getString("depo_actno", ""),
                    depositDate = args.getString("deposit_date", "").replace("/", "-"),
                    depositAmt = args.getString("deposit_amt", ""),
                    currency = "SCR",
                    depositPeriod = args.getString("deposit_period", ""),
                    maturityDate = "",
                    branchId = "",
                    branchName = "",
                    depositFrequency = args.getString("deposit_frequency", ""),
                    interestType = "",
                    intAmt = "",
                    rateOfInt = args.getString("rate_of_int", "")
                )
                
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    binding.recyclerViewFlow.adapter = FlowAdapter(data)
                    
                    // Calculate totals
                    var totalInterest = 0.0
                    for (item in data) {
                        val flowAmtStr = item["flow_amt"]?.toString() ?: "0"
                        totalInterest += flowAmtStr.toDoubleOrNull() ?: 0.0
                    }
                    val maturityAmount = if (data.isNotEmpty()) {
                        data.last()["flow_amt"]?.toString() ?: "0"
                    } else "0"
                    
                    binding.tvTotalInterest.text = totalInterest.toString()
                    binding.tvMaturityAmount.text = maturityAmount
                } else {
                    Toast.makeText(requireContext(), "Failed to load flow", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    // --- Adapter ---
    inner class FlowAdapter(private val items: List<Map<String, Any>>) : 
        RecyclerView.Adapter<FlowAdapter.ViewHolder>() {
        
        inner class ViewHolder(val itemBinding: ItemFlowRowBinding) : RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            ViewHolder(ItemFlowRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.itemBinding.apply {
                val flowCode = item["flow_code"]?.toString() ?: ""
                tvFlowId.text = item["flow_id"]?.toString() ?: ""
                tvFlowCode.text = flowCode
                
                // Set tooltip for flow code
                val tooltipText = when (flowCode) {
                    "PI" -> "Principal Inflow"
                    "II" -> "Interest Inflow"
                    "IO" -> "Interest Outflow"
                    "PO" -> "Principal Outflow"
                    else -> "Total Outflow"
                }
                TooltipCompat.setTooltipText(tvFlowCode, tooltipText)
                
                tvFlowDate.text = item["flow_date"]?.toString() ?: ""
                tvFlowAmt.text = item["flow_amt"]?.toString() ?: ""
                tvOutstanding.text = item["clr_bal_amt"]?.toString() ?: ""
            }
        }
        override fun getItemCount() = items.size
    }

    companion object {
        fun newInstance(args: Bundle) = DepositFlowDialog().apply { arguments = args }
    }
}