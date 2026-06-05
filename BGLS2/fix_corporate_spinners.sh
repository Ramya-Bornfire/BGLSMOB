sed -i '' '/binding.spCountryOperation.adapter = countryOpAdapter/a\
\
        val schemeTypes = listOf("SELECT", "CURRENT ACCOUNT", "DEPOSIT ACCOUNT", "LOAN ACCOUNT")\
        val schAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, schemeTypes)\
        schAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)\
        binding.spSchemeType.adapter = schAdapter\
\
        binding.spSchemeType.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {\
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {\
                val selected = schemeTypes[position]\
                val isDeposit = selected == "DEPOSIT ACCOUNT" || selected == "FIXED DEPOSIT"\
                if (isDeposit) {\
                    binding.etLoanSanctioned.visibility = android.view.View.GONE\
                    binding.etMargin.visibility = android.view.View.GONE\
                    binding.etDrawingLimit.visibility = android.view.View.GONE\
                    binding.etLoanPeriod.visibility = android.view.View.GONE\
                    binding.etInterestRate.visibility = android.view.View.GONE\
                    binding.etFeesRate.visibility = android.view.View.GONE\
                    binding.spRecoveryMethod.visibility = android.view.View.GONE\
                    binding.etInstallmentAmount.visibility = android.view.View.GONE\
                    binding.etDateOfLoan.visibility = android.view.View.GONE\
                    binding.etDepositAccountNo.visibility = android.view.View.VISIBLE\
                    binding.etDateOfDeposit.visibility = android.view.View.VISIBLE\
                    binding.etDepositAmount.visibility = android.view.View.VISIBLE\
                    binding.etDepositPeriod.visibility = android.view.View.VISIBLE\
                    binding.etRateOfInterest.visibility = android.view.View.VISIBLE\
                    binding.etMaturityDate.visibility = android.view.View.VISIBLE\
                    binding.etInterestAmount.visibility = android.view.View.VISIBLE\
                    binding.etMaturityAmount.visibility = android.view.View.VISIBLE\
                } else {\
                    binding.etLoanSanctioned.visibility = android.view.View.VISIBLE\
                    binding.etMargin.visibility = android.view.View.VISIBLE\
                    binding.etDrawingLimit.visibility = android.view.View.VISIBLE\
                    binding.etLoanPeriod.visibility = android.view.View.VISIBLE\
                    binding.etInterestRate.visibility = android.view.View.VISIBLE\
                    binding.etFeesRate.visibility = android.view.View.VISIBLE\
                    binding.spRecoveryMethod.visibility = android.view.View.VISIBLE\
                    binding.etInstallmentAmount.visibility = android.view.View.VISIBLE\
                    binding.etDateOfLoan.visibility = android.view.View.VISIBLE\
                    binding.etDepositAccountNo.visibility = android.view.View.GONE\
                    binding.etDateOfDeposit.visibility = android.view.View.GONE\
                    binding.etDepositAmount.visibility = android.view.View.GONE\
                    binding.etDepositPeriod.visibility = android.view.View.GONE\
                    binding.etRateOfInterest.visibility = android.view.View.GONE\
                    binding.etMaturityDate.visibility = android.view.View.GONE\
                    binding.etInterestAmount.visibility = android.view.View.GONE\
                    binding.etMaturityAmount.visibility = android.view.View.GONE\
                }\
            }\
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}\
        }\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CorporateCustomerAccountOpeningActivity.kt
