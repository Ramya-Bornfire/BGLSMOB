sed -i '' '/private fun setupSpinners() {/i\
    private fun setupCalculations() {\
        fun calculateLoanMath() {\
            try {\
                val s = binding.etLoanSanctioned.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0\
                val m = binding.etMargin.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0\
                if (s > 0 && m >= 0) {\
                    val d = s - (s * m / 100)\
                    binding.etDrawingLimit.setText(formatCurrency(d))\
                } else {\
                    binding.etDrawingLimit.setText("")\
                }\
                val i = binding.etInterestRate.text.toString().toDoubleOrNull() ?: 0.0\
                val p = binding.etLoanPeriod.text.toString().toIntOrNull() ?: 0\
                if (s > 0 && i > 0 && p > 0) {\
                    val r = i / (12 * 100)\
                    val emi = (s * r * Math.pow(1 + r, p.toDouble())) / (Math.pow(1 + r, p.toDouble()) - 1)\
                    binding.etInstallmentAmount.setText(formatCurrency(emi))\
                } else {\
                    binding.etInstallmentAmount.setText("")\
                }\
            } catch (e: Exception) {}\
        }\
        binding.etLoanSanctioned.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }\
        })\
        binding.etMargin.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }\
        })\
        binding.etInterestRate.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }\
        })\
        binding.etLoanPeriod.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateLoanMath() }\
        })\
        fun calculateDepositMath() {\
            try {\
                val p = binding.etDepositAmount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0\
                val r = binding.etRateOfInterest.text.toString().toDoubleOrNull() ?: 0.0\
                val t = binding.etDepositPeriod.text.toString().toIntOrNull() ?: 0\
                if (p > 0 && r > 0 && t > 0) {\
                    val interest = (p * r * t) / (12 * 100)\
                    val maturity = p + interest\
                    binding.etInterestAmount.setText(formatCurrency(interest))\
                    binding.etMaturityAmount.setText(formatCurrency(maturity))\
                } else {\
                    binding.etInterestAmount.setText("")\
                    binding.etMaturityAmount.setText("")\
                }\
            } catch (e: Exception) {}\
        }\
        binding.etDepositAmount.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }\
        })\
        binding.etRateOfInterest.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }\
        })\
        binding.etDepositPeriod.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) { calculateDepositMath() }\
        })\
        binding.etAnnualIncome.addTextChangedListener(object : android.text.TextWatcher {\
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}\
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}\
            override fun afterTextChanged(s: android.text.Editable?) {\
                try {\
                    val annual = s?.toString()?.replace(",", "")?.toDoubleOrNull() ?: 0.0\
                    val monthly = annual / 12\
                    binding.etMonthlyIncome.setText(formatCurrency(monthly))\
                } catch (e: Exception) {}\
            }\
        })\
    }\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CorporateCustomerAccountOpeningActivity.kt

sed -i '' '/setupSpinners()/a\
        setupCalculations()\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CorporateCustomerAccountOpeningActivity.kt
