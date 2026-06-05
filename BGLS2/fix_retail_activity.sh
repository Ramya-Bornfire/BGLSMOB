sed -i '' '/formData\["la_loan_accountno"\]/a\
                    formData["date_of_loan"]      = formatDateForBackend(binding.etDateOfLoan.text?.toString() ?: "")\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CustomerAccountOpeningActivity.kt
