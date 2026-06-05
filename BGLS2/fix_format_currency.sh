sed -i '' '/private fun setupCalculations() {/a\
\
        fun formatCurrency(value: Double): String {\
            val formatter = java.text.DecimalFormat("#,##0.00")\
            return formatter.format(value)\
        }\
' app/src/main/java/com/example/bgls/CustomerOnBoarding/CorporateCustomerAccountOpeningActivity.kt
