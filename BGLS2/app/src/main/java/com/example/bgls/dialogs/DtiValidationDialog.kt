package com.example.bgls.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.bgls.databinding.DialogDtiValidationBinding

class DtiValidationDialog : DialogFragment() {

    private var _binding: DialogDtiValidationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogDtiValidationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Retrieve arguments
        val borrowerName = arguments?.getString("borrower_name") ?: ""
        val constitution = arguments?.getString("constitution") ?: ""
        val monthlyIncome = arguments?.getDouble("monthly_income") ?: 0.0
        val monthlyRepayment = arguments?.getDouble("monthly_repayment") ?: 0.0

        binding.etBorrowerName.setText(borrowerName)
        binding.etConstitution.setText(constitution)
        binding.etMonthlyIncome.setText(monthlyIncome.toString())
        binding.etMonthlyRepayment.setText(monthlyRepayment.toString())

        binding.btnCalculateDti.setOnClickListener {
            if (monthlyIncome <= 0) {
                Toast.makeText(requireContext(), "Invalid input: Monthly income must be > 0", Toast.LENGTH_SHORT).show()
                binding.tvStatus.text = "Invalid input"
                binding.tvStatus.setTextColor(android.graphics.Color.RED)
                return@setOnClickListener
            }

            val dti = (monthlyRepayment / monthlyIncome) * 100
            binding.etDtiRatio.setText(String.format("%.2f%%", dti))

            when {
                dti <= 35 -> {
                    binding.tvStatus.text = "Favorable"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Green
                }
                dti <= 49 -> {
                    binding.tvStatus.text = "Adequate"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")) // Orange
                }
                else -> {
                    binding.tvStatus.text = "Not Favorable"
                    binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#F44336")) // Red
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(name: String, constitution: String, income: Double, repayment: Double) = 
            DtiValidationDialog().apply {
                arguments = Bundle().apply {
                    putString("borrower_name", name)
                    putString("constitution", constitution)
                    putDouble("monthly_income", income)
                    putDouble("monthly_repayment", repayment)
                }
            }
    }
}