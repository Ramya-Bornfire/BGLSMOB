sed -i '' '/private fun saveAccountDetails() {/,/^\    }/c\
    private fun saveAccountDetails() {\
        val progressDialog = android.app.ProgressDialog(this).apply {\
            setMessage("Saving Account Details...")\
            setCancelable(false)\
            show()\
        }\
        \
        lifecycleScope.launch {\
            try {\
                val appRefNo = intent.getStringExtra("app_ref_no") ?: "ARN0936"\
                \
                val formData = mutableMapOf<String, Any>()\
                val scheme = binding.spSchemeType.selectedItem.toString()\
                val isDeposit = scheme == "FIXED DEPOSIT" || scheme == "DEPOSIT ACCOUNT"\
                val schemetype = if (isDeposit) "TD" else "LA"\
                formData["schemetype"] = schemetype\
                formData["schemecode"] = if (isDeposit) "TDFIXED" else "LSRET"\
                formData["currency"] = "SCR"\
                formData["prisolid"] = binding.etPrimaryBranch.text.toString()\
                formData["branch_desc"] = binding.etBranchDesc.text.toString()\
                formData["certificate_registration"] = binding.etCertReg.text.toString()\
                formData["business_registration"] = binding.etBusReg.text.toString()\
                formData["date_incorporation"] = binding.etDateIncorp.text.toString()\
                formData["countryOrigin"] = binding.spCountryOperation.selectedItem.toString()\
                \
                fun formatDateForBackend(s: String): String {\
                    return try {\
                        val parts = s.split("-")\
                        if (parts.size == 3 && parts[2].length == 4) "${parts[2]}-${parts[1]}-${parts[0]}" else s\
                    } catch (e: Exception) { s }\
                }\
                fun String?.takeIfNotEmpty(): String? = if (this.isNullOrBlank()) null else this\
                \
                if (isDeposit) {\
                    formData["account_no"] = binding.etDepositAccountNo.text?.toString() ?: ""\
                    formData["deposit_account_no"] = binding.etDepositAccountNo.text?.toString() ?: ""\
                    formData["td_deposit_accountno"] = binding.etDepositAccountNo.text?.toString() ?: ""\
                    formData["gl_code"] = schemeGlCode\
                    formData["gl_desc"] = schemeGlDesc\
                    formData["glsh_code"] = schemeGlshCode\
                    formData["glsh_desc"] = schemeGlshDesc\
                    formData["deposit_date"]      = binding.etDateOfDeposit.text?.toString() ?: ""\
                    formData["deposit_amt"]       = binding.etDepositAmount.text?.toString()?.replace(",", "") ?: ""\
                    formData["deposit_period"]    = binding.etDepositPeriod.text?.toString() ?: ""\
                    formData["maturity_date"]     = formatDateForBackend(binding.etMaturityDate.text?.toString() ?: "")\
                    formData["rate_of_int"]       = binding.etRateOfInterest.text?.toString() ?: ""\
                    formData["int_amt"]           = binding.etInterestAmount.text?.toString() ?: ""\
                    formData["maturity_amt"]      = binding.etMaturityAmount.text?.toString() ?: ""\
                    formData["deposit_type"]      = "Fixed"\
                    formData["frequency"]         = "Monthly"\
                } else {\
                    formData["account_no"] = generatedAccountNo\
                    formData["loan_accountno"]    = generatedAccountNo\
                    formData["la_loan_accountno"] = generatedAccountNo\
                    formData["date_of_loan"]      = formatDateForBackend(binding.etDateOfLoan.text?.toString() ?: "")\
                    formData["gl_code"] = schemeGlCode\
                    formData["gl_desc"] = schemeGlDesc\
                    formData["glsh_code"] = schemeGlshCode\
                    formData["glsh_desc"] = schemeGlshDesc\
                    formData["gl_code_loan"] = schemeGlCode\
                    formData["gl_desc_loan"] = schemeGlDesc\
                    formData["glsh_code_loan"] = schemeGlshCode\
                    formData["glsh_desc_loan"] = schemeGlshDesc\
                    formData["loan_sanctioned"]   = binding.etLoanSanctioned.text?.toString()?.replace(",", "") ?: ""\
                    formData["effective_interest_rate"] = binding.etInterestRate.text?.toString() ?: ""\
                    formData["effective_fees_rate"]= binding.etFeesRate.text?.toString() ?: ""\
                    formData["recovery_method"]   = binding.spRecoveryMethod.selectedItem?.toString() ?: ""\
                    formData["inst_start_dt"]     = ""\
                    formData["loan_period"]       = binding.etLoanPeriod.text?.toString() ?: ""\
                }\
                \
                val finalAccountNo = if (isDeposit) binding.etDepositAccountNo.text?.toString() ?: "" else generatedAccountNo\
                val body = mapOf(\
                    "formData" to formData,\
                    "loanAccountNo" to finalAccountNo,\
                    "accountNo" to finalAccountNo,\
                    "scheduleList" to emptyList<Any>()\
                )\
                \
                val response = withContext(Dispatchers.IO) {\
                    RetrofitClient.api.saveAccountDetails(appRefNo, body)\
                }\
                \
                progressDialog.dismiss()\
                if (response.isSuccessful) {\
                    if (isDeposit) {\
                        val depositReq = com.example.bgls.Retrofit.DepositEntityRequest(\
                            depo_actno = binding.etDepositAccountNo.text?.toString()?.takeIfNotEmpty(),\
                            deposit_date = formatDateForBackend(binding.etDateOfDeposit.text?.toString() ?: "").takeIfNotEmpty(),\
                            deposit_amt = binding.etDepositAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                            currency = "SCR",\
                            deposit_period = binding.etDepositPeriod.text?.toString()?.takeIfNotEmpty(),\
                            maturity_date = formatDateForBackend(binding.etMaturityDate.text?.toString() ?: "").takeIfNotEmpty(),\
                            rate_of_int = binding.etRateOfInterest.text?.toString()?.takeIfNotEmpty(),\
                            int_amt = binding.etInterestAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                            maturity_amt = binding.etMaturityAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                            deposit_type = null,\
                            frequency = null,\
                            gl_code = schemeGlCode,\
                            gl_desc = schemeGlDesc,\
                            glsh_code = schemeGlshCode,\
                            glsh_desc = schemeGlshDesc,\
                            cust_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),\
                            cust_name = binding.etCorporateName.text?.toString()?.takeIfNotEmpty(),\
                            scheme_code = if (isDeposit) "TDFIXED" else "LSRET",\
                            branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),\
                            branch_desc = binding.etBranchDesc.text?.toString()?.takeIfNotEmpty(),\
                            deposit_frequency = null,\
                            interest_type = null\
                        )\
                        withContext(Dispatchers.IO) {\
                            RetrofitClient.api.depositAddCust(depositReq)\
                        }\
                    } else {\
                        val leaseReq = com.example.bgls.Retrofit.LeaseDataRequest(\
                            loanDetails = com.example.bgls.Retrofit.LoanDetailsRequest(\
                                customer_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),\
                                customer_name = binding.etCorporateName.text?.toString()?.takeIfNotEmpty(),\
                                branch_name = binding.etBranchDesc.text?.toString()?.takeIfNotEmpty(),\
                                branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),\
                                loan_type = schemetype.takeIfNotEmpty(),\
                                loan_accountno = generatedAccountNo.takeIfNotEmpty(),\
                                date_of_loan = formatDateForBackend(binding.etDateOfLoan.text?.toString() ?: "").takeIfNotEmpty(),\
                                loan_sanctioned = binding.etLoanSanctioned.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                margin_limit = binding.etMargin.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                drawing_limit = binding.etDrawingLimit.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                loan_currency = "SCR",\
                                disbursement = binding.etLoanSanctioned.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                loan_outstanding = binding.etLoanSanctioned.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                loan_period = binding.etLoanPeriod.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                expiry_date = formatDateForBackend(binding.etExpiryDate.text?.toString() ?: "").takeIfNotEmpty(),\
                                repayment_terms = null,\
                                recovery_method = binding.spRecoveryMethod.selectedItem?.toString()?.takeIfNotEmpty(),\
                                effective_interest_rate = binding.etInterestRate.text?.toString()?.takeIfNotEmpty(),\
                                effective_fees_rate = binding.etFeesRate.text?.toString()?.takeIfNotEmpty(),\
                                gl_code = schemeGlCode,\
                                gl_desc = schemeGlDesc,\
                                glsh_code = schemeGlshCode,\
                                glsh_desc = schemeGlshDesc\
                            ),\
                            repaymentDetails = com.example.bgls.Retrofit.RepaymentDetailsRequest(\
                                customer_id = intent.getStringExtra("cif_id")?.takeIfNotEmpty(),\
                                branch_id = binding.etPrimaryBranch.text?.toString()?.takeIfNotEmpty(),\
                                account_no = generatedAccountNo.takeIfNotEmpty(),\
                                acid = generatedAccountNo.takeIfNotEmpty(),\
                                inst_id = "1",\
                                inst_start_dt = "",\
                                inst_freq = "",\
                                inst_amount = binding.etInstallmentAmount.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                no_of_inst = binding.etLoanPeriod.text?.toString()?.replace(",", "")?.takeIfNotEmpty(),\
                                inst_pct = "",\
                                interest_frequency = "",\
                                maturity_flg = "Y"\
                            )\
                        )\
                        withContext(Dispatchers.IO) {\
                            RetrofitClient.api.addLeaseAccount(leaseReq)\
                        }\
                    }\
                    \
                    val msg = response.body()?.string()?.takeIf { it.isNotBlank() } ?: "Account Details Saved"\
                    android.app.AlertDialog.Builder(this@CorporateCustomerAccountOpeningActivity)\
                        .setMessage(msg)\
                        .setPositiveButton("Okay") { dialog, _ ->\
                            dialog.dismiss()\
                            binding.tabLayout.getTabAt(2)?.select()\
                        }\
                        .setCancelable(false)\
                        .show()\
                } else {\
                    android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Failed to save details: ${response.code()}", android.widget.Toast.LENGTH_SHORT).show()\
                }\
            } catch (e: Exception) {\
                progressDialog.dismiss()\
                android.widget.Toast.makeText(this@CorporateCustomerAccountOpeningActivity, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()\
            }\
        }\
    }\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CorporateCustomerAccountOpeningActivity.kt
