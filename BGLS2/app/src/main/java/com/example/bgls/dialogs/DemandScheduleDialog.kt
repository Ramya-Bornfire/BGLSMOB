package com.example.bgls.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.databinding.DialogScheduleBinding
import com.example.bgls.databinding.ItemScheduleRowBinding
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class DemandScheduleDialog : DialogFragment() {

    private var _binding: DialogScheduleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerViewSchedule.layoutManager = LinearLayoutManager(requireContext())
        
        val args = arguments ?: return
        
        binding.progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getInterestDetails(
                    creationDate = args.getString("creation_Date", "").replace("/", "-"),
                    interestRate = args.getString("int_rate", ""),
                    installID = "1",
                    installStartDate = args.getString("start_date", "").replace("/", "-"),
                    pricipleFreq = args.getString("principle_frequency", ""),
                    noOfInstallment = args.getString("no_of_inst", ""),
                    installAmount = args.getString("int_amt", ""),
                    interestFreq = args.getString("interestFrequency", ""),
                    feesRate = args.getString("fees_percentage", "")
                )
                
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    binding.recyclerViewSchedule.adapter = ScheduleAdapter(data)
                } else {
                    Toast.makeText(requireContext(), "Failed to load schedule", Toast.LENGTH_SHORT).show()
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
    inner class ScheduleAdapter(private val items: List<Map<String, Any>>) : 
        RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
        
        inner class ViewHolder(val itemBinding: ItemScheduleRowBinding) : RecyclerView.ViewHolder(itemBinding.root)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = 
            ViewHolder(ItemScheduleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.itemBinding.apply {
                tvSrlNo.text = item["no_of_instalment"]?.toString() ?: ""
                tvDate.text = item["installment_date"]?.toString() ?: ""
                tvDescription.text = item["installment_description"]?.toString() ?: ""
                tvInstAmt.text = item["installment_amount"]?.toString() ?: ""
                tvPrincipalAmt.text = item["principal_amount"]?.toString() ?: ""
                tvInterestAmt.text = item["interest_amount"]?.toString() ?: ""
                tvChargesAmt.text = item["charges_amount"]?.toString() ?: ""
                tvPrincipalOut.text = item["principal_outstanding"]?.toString() ?: ""
            }
        }
        override fun getItemCount() = items.size
    }

    companion object {
        fun newInstance(args: Bundle) = DemandScheduleDialog().apply { arguments = args }
    }
}