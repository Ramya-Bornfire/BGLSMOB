package com.example.bgls.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bgls.R
import androidx.lifecycle.lifecycleScope
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch

class HeadOfficeFragment : Fragment(R.layout.fragment_head_office) {

    private lateinit var etOrgName: EditText
    private lateinit var etOrgType: EditText
    private lateinit var etDate: EditText
    private lateinit var etCert: EditText
    private lateinit var etBusiness: EditText
    private lateinit var etVat: EditText
    private lateinit var etEmp: EditText
    private lateinit var etAsOn: EditText
    private lateinit var etReg1: EditText
    private lateinit var etReg2: EditText
    private lateinit var etCorp1: EditText
    private lateinit var etCorp2: EditText
    private lateinit var etWebsite: EditText
    private lateinit var etEmail: EditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        etOrgName = view.findViewById(R.id.etOrganizationName)
        etOrgType = view.findViewById(R.id.etOrganizationType)
        etDate = view.findViewById(R.id.etDateOfRegistration)
        etCert = view.findViewById(R.id.etCertificateReg)
        etBusiness = view.findViewById(R.id.etBusinessRegCard)
        etVat = view.findViewById(R.id.etVatReference)
        etEmp = view.findViewById(R.id.etNoOfEmployees)
        etAsOn = view.findViewById(R.id.etAsOn)
        etReg1 = view.findViewById(R.id.etRegOfficeAddr1)
        etReg2 = view.findViewById(R.id.etRegOfficeAddr2)
        etCorp1 = view.findViewById(R.id.etCorpOfficeAddr1)
        etCorp2 = view.findViewById(R.id.etCorpOfficeAddr2)
        etWebsite = view.findViewById(R.id.etWebsite)
        etEmail = view.findViewById(R.id.etEmail)

        loadHeadOffice()
    }

    private fun loadHeadOffice() {

        lifecycleScope.launch {

            try {
                val response = RetrofitClient.api.getOrganizationDetails("add")

                if (response.isSuccessful) {

                    val org = response.body()?.organization

                    org?.let {

                        etOrgName.setText(it.org_name ?: "")
                        etOrgType.setText(it.org_type ?: "")
                        etDate.setText(it.date_of_regn ?: "")
                        etCert.setText(it.reg_no ?: "")
                        etBusiness.setText(it.pan_card ?: "")
                        etVat.setText(it.gst_ref ?: "")
                        etEmp.setText(it.no_of_emp ?: "")
                        etAsOn.setText(it.as_on ?: "")
                        etReg1.setText(it.reg_addr_1 ?: "")
                        etReg2.setText(it.reg_addr_2 ?: "")
                        etCorp1.setText(it.corp_addr_1 ?: "")
                        etCorp2.setText(it.cor_addr_2 ?: "")
                        etWebsite.setText(it.web_site ?: "")
                        etEmail.setText(it.email ?: "")

                    }

                } else {
                    Toast.makeText(requireContext(), "API Error", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
