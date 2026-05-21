package com.example.bgls.CustomerOnBoarding

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bgls.R
import com.example.bgls.databinding.ActivityMinimalDataBinding

class MinimalDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMinimalDataBinding
    private val retailCustomerTypes = listOf("SELECT", "Individual", "Joint Account")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityMinimalDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupSpinners()
        setupMandatoryLabels()
        
        binding.etDateOfBirthI.setOnClickListener { showDatePicker(binding.etDateOfBirthI, isDob = true) }
        binding.etDateIncorpC.setOnClickListener { showDatePicker(binding.etDateIncorpC) }
        
        binding.btnCustomerCheck.setOnClickListener {
            performCustomerCheck()
        }

        binding.btnProceedTop.setOnClickListener {
            if (validateMandatoryFields()) {
                val isCorporate = binding.spCustomerGroup1.selectedItem.toString() == "CORPORATE CUSTOMER"
                val intent = if (isCorporate) {
                    android.content.Intent(this, CorporateCustomerAccountOpeningActivity::class.java)
                } else {
                    android.content.Intent(this, CustomerAccountOpeningActivity::class.java)
                }
                
                intent.putExtra("app_ref_no", binding.etAppRefNo.text.toString())
                intent.putExtra("customer_group", binding.spCustomerGroup1.selectedItem.toString())
                
                if (isCorporate) {
                    intent.putExtra("customer_type", "CORPORATE")
                    intent.putExtra("primary_branch", "103") // Placeholder or extract if available
                    intent.putExtra("branch_name", "Al Salam Bank Seychelles Limited")
                    intent.putExtra("first_name", binding.etCorporateNameC.text.toString())
                    intent.putExtra("short_name", binding.etTradeNameC.text.toString())
                    intent.putExtra("full_name", binding.etCorporateNameC.text.toString())
                    intent.putExtra("dob", binding.etDateIncorpC.text.toString())
                    intent.putExtra("email_id", binding.etEmailC.text.toString())
                } else {
                    intent.putExtra("customer_type", binding.spCustomerType.selectedItem.toString())
                    intent.putExtra("primary_branch", "103")
                    intent.putExtra("branch_name", "Al Salam Bank Seychelles Limited")
                    intent.putExtra("first_name", binding.etFirstNameI.text.toString())
                    intent.putExtra("middle_name", binding.etMiddleNameI.text.toString())
                    intent.putExtra("last_name", binding.etLastNameI.text.toString())
                    intent.putExtra("short_name", binding.etShortNameI.text.toString())
                    intent.putExtra("full_name", binding.etFullNameI.text.toString())
                    intent.putExtra("dob", binding.etDateOfBirthI.text.toString())
                    intent.putExtra("mobile_no", binding.tvCountryCodeI.text.toString() + binding.etMobileI.text.toString())
                    intent.putExtra("email_id", "") // Not in minimal data for individual yet?
                }
                
                startActivity(intent)
            }
        }

//        binding.btnProceedCheck.setOnClickListener {
//            binding.btnProceedTop.performClick()
//        }

        binding.btnRefresh.setOnClickListener {
            recreate()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnProceed.setOnClickListener {
            binding.btnProceedTop.performClick()
        }
    }

    private fun validateMandatoryFields(): Boolean {
        val isCorporate = binding.spCustomerGroup1.selectedItem.toString() == "CORPORATE CUSTOMER"
        var isValid = true

        if (isCorporate) {
            if (binding.etCorporateNameC.text.isNullOrBlank()) { binding.etCorporateNameC.error = "Required"; isValid = false }
            if (binding.etTradeNameC.text.isNullOrBlank()) { binding.etTradeNameC.error = "Required"; isValid = false }
            if (binding.etCertificateC.text.isNullOrBlank()) { binding.etCertificateC.error = "Required"; isValid = false }
            if (binding.etRegNoC.text.isNullOrBlank()) { binding.etRegNoC.error = "Required"; isValid = false }
            if (binding.etDateIncorpC.text.isNullOrBlank()) { binding.etDateIncorpC.error = "Required"; isValid = false }
            if (binding.etPostBoxC.text.isNullOrBlank()) { binding.etPostBoxC.error = "Required"; isValid = false }
            if (binding.etLandLineC.text.isNullOrBlank()) { binding.etLandLineC.error = "Required"; isValid = false }
            if (binding.etFaxC.text.isNullOrBlank()) { binding.etFaxC.error = "Required"; isValid = false }
            if (binding.etEmailC.text.isNullOrBlank()) { binding.etEmailC.error = "Required"; isValid = false }
            if (binding.etWebsiteC.text.isNullOrBlank()) { binding.etWebsiteC.error = "Required"; isValid = false }
        } else {
            if (binding.etFirstNameI.text.isNullOrBlank()) { binding.etFirstNameI.error = "Required"; isValid = false }
            if (binding.etShortNameI.text.isNullOrBlank()) { binding.etShortNameI.error = "Required"; isValid = false }
            if (binding.etFullNameI.text.isNullOrBlank()) { binding.etFullNameI.error = "Required"; isValid = false }
            if (binding.etDateOfBirthI.text.isNullOrBlank()) { binding.etDateOfBirthI.error = "Required"; isValid = false }
            if (binding.etMobileI.text.isNullOrBlank()) { binding.etMobileI.error = "Required"; isValid = false }
            if (binding.etPassportI.text.isNullOrBlank()) { binding.etPassportI.error = "Required"; isValid = false }
            if (binding.etNationalIdI.text.isNullOrBlank()) { binding.etNationalIdI.error = "Required"; isValid = false }
        }

        if (!isValid) {
            android.widget.Toast.makeText(this, "Please fill all mandatory fields", android.widget.Toast.LENGTH_SHORT).show()
        }
        return isValid
    }

    private fun performCustomerCheck() {
        if (!validateMandatoryFields()) return

        // Hide the check button as requested
        binding.btnCustomerCheck.visibility = View.GONE
        
        // Call API logic
        val idToCheck = if (binding.layoutIndividual.visibility == View.VISIBLE) 
            binding.etNationalIdI.text.toString() else binding.etRegNoC.text.toString()

        checkDuplicateApi(idToCheck) { hasDuplicate ->
            // Show the results section
            binding.layoutCustomerCheck.visibility = View.VISIBLE
            
            if (hasDuplicate) {
                // Show duplicates, hide top Proceed
                binding.btnProceedTop.visibility = View.GONE
                
                binding.tvDuplicateListResult.text = "Potential Duplicate: Customer already exists."
                binding.tvDuplicateListResult.visibility = View.VISIBLE
                binding.tvDuplicateListResult.setTextColor(android.graphics.Color.RED)
            } else {
                // No duplicates, show top Proceed
                binding.btnProceedTop.visibility = View.VISIBLE
                
                binding.tvDuplicateListResult.text = "No Result found on Customer Check"
                binding.tvDuplicateListResult.visibility = View.VISIBLE
                binding.tvDuplicateListResult.setTextColor(android.graphics.Color.parseColor("#666666"))
            }

            // Always show no results for these in mock
            binding.tvBlackListResult.text = "No Result found on Customer Check"
            binding.tvNegativeListResult.text = "No Result found on Customer Check"
            
            android.widget.Toast.makeText(this, "Customer check completed", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * API integration point. Replace mock logic with real API call.
     */
    private fun checkDuplicateApi(id: String, callback: (Boolean) -> Unit) {
        // Mock logic: duplicates for specific values
        val isDuplicate = (id == "44833" || id == "SAN")
        
        // Simulate network delay if desired, but here we just callback
        callback(isDuplicate)
    }

    private fun showDatePicker(editText: android.widget.EditText, isDob: Boolean = false) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%02d-%02d-%d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(date)
        }, year, month, day)

        if (isDob) {
            // Restrict to 18 years old or older
            val maxCalendar = java.util.Calendar.getInstance()
            maxCalendar.add(java.util.Calendar.YEAR, -18)
            datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis
        } else {
            // For other dates, maybe just restrict to today as max
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        }
        
        datePickerDialog.show()
    }

    private fun setupMandatoryLabels() {
        val labels = listOf(
            binding.tvLabelCustomerType,
            binding.tvLabelPrimaryBranchI,
            binding.tvLabelHolderTypeI,
            binding.tvLabelRelationshipI,
            binding.tvLabelCustomerGroupI,
            binding.tvLabelFirstNameI,
            binding.tvLabelShortNameI,
            binding.tvLabelFullNameI,
            binding.tvLabelDateOfBirthI,
            binding.tvLabelMobileI,
            binding.tvLabelPassportI,
            binding.tvLabelNationalIdI,
            binding.tvLabelPrimaryBranchC,
            binding.tvLabelConstitutionC,
            binding.tvLabelCorporateNameC,
            binding.tvLabelTradeNameC,
            binding.tvLabelCertificateC,
            binding.tvLabelRegNoC,
            binding.tvLabelDateIncorpC,
            binding.tvLabelPostBoxC,
            binding.tvLabelLandLineC,
            binding.tvLabelFaxC,
            binding.tvLabelEmailC,
            binding.tvLabelWebsiteC
        )

        labels.forEach { textView ->
            val text = textView.text.toString()
            if (text.endsWith("*")) {
                val labelText = text.substring(0, text.length - 1).trim()
                textView.text = Html.fromHtml("$labelText <font color='#FF0000'>*</font>")
            }
        }
    }

    private fun setupSpinners() {
        // Customer Type: SELECT, Individual, Joint Account
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, retailCustomerTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCustomerType.adapter = typeAdapter

        // Customer Group: RETAIL, CORPORATE CUSTOMER
        val customerGroups = listOf("RETAIL", "CORPORATE CUSTOMER")
        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, customerGroups)
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCustomerGroup1.adapter = groupAdapter

        // Constitution: SELECT, PRP, COM, PAR, ASS, TRT, CLB
        val constitutions = listOf("SELECT", "PRP", "COM", "PAR", "ASS", "TRT", "CLB")
        val constAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, constitutions)
        constAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spConstitution.adapter = constAdapter

        // Logic for Switching Layouts based on Customer Group
        binding.spCustomerGroup1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = customerGroups[position]
                if (selected == "CORPORATE CUSTOMER") {
                    switchToCorporate()
                } else {
                    switchToIndividual()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Logic for No of Persons based on Customer Type
        binding.spCustomerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = binding.spCustomerType.selectedItem.toString()
                if (binding.spCustomerGroup1.selectedItem.toString() != "CORPORATE CUSTOMER") {
                    updateNoOfPersonsOptions(selected)
                    
                    // Show/Hide Joint Account specific fields
                    if (selected == "Joint Account") {
                        binding.rowHolderType.visibility = View.VISIBLE
                    } else {
                        binding.rowHolderType.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Relationship: SELECT, Spouse, Father, Mother, Daughter, Son, Others
        val relationships = listOf("SELECT", "Spouse", "Father", "Mother", "Daughter", "Son", "Others")
        val relAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, relationships)
        relAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRelationship.adapter = relAdapter
    }

    private fun switchToCorporate() {
        layoutIndividualVisible(false)
        
        binding.etAppRefNo.setText("ARN0916")
        
        // Hide spinner, show ReadOnly EditText for Customer Type
        binding.spCustomerType.visibility = View.GONE
        binding.etCustomerTypeReadOnly.visibility = View.VISIBLE

        // Hide Joint Account fields for Corporate
        binding.rowHolderType.visibility = View.GONE

        // No of Persons for Corporate (selectable 2-10)
        updateNoOfPersonsOptions("CORPORATE")
    }

    private fun switchToIndividual() {
        layoutIndividualVisible(true)
        
        binding.etAppRefNo.setText("ARN0915")
        
        // Show spinner, hide ReadOnly EditText for Customer Type
        binding.spCustomerType.visibility = View.VISIBLE
        binding.etCustomerTypeReadOnly.visibility = View.GONE
        
        val selectedType = binding.spCustomerType.selectedItem.toString()
        updateNoOfPersonsOptions(selectedType)
        
        // Handle Holder Type visibility
        if (selectedType == "Joint Account") {
            binding.rowHolderType.visibility = View.VISIBLE
        } else {
            binding.rowHolderType.visibility = View.GONE
        }
    }

    private fun layoutIndividualVisible(visible: Boolean) {
        if (visible) {
            binding.layoutIndividual.visibility = View.VISIBLE
            binding.layoutCorporate.visibility = View.GONE
        } else {
            binding.layoutIndividual.visibility = View.GONE
            binding.layoutCorporate.visibility = View.VISIBLE
        }
    }

    private fun updateNoOfPersonsOptions(customerType: String) {
        val options = when (customerType) {
            "Individual" -> listOf("1")
            "Joint Account", "CORPORATE" -> (2..10).map { it.toString() }
            else -> listOf("0")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spNoOfPersons.adapter = adapter
        
        if (customerType == "Individual") {
            binding.spNoOfPersons.isEnabled = false
            binding.spNoOfPersons.setBackgroundResource(R.drawable.readonly_background)
        } else {
            binding.spNoOfPersons.isEnabled = true
            binding.spNoOfPersons.setBackgroundResource(R.drawable.spinner_with_arrow)
        }
    }
}