package com.example.bgls.BatchJobExecution

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class BatchJobActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_batch_job)

        val etNextWorkingDate = findViewById<android.widget.EditText>(R.id.etNextWorkingDate)
        etNextWorkingDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePicker = android.app.DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
                etNextWorkingDate.setText(formattedDate)
            }, year, month, day)
            datePicker.show()
        }

        findViewById<View>(R.id.rowHolidayCheck).setOnClickListener {
            showHolidayCheckDialog()
        }

        findViewById<View>(R.id.rowDailyAccountBalance).setOnClickListener {
            showDabSelectionDialog()
        }

        findViewById<View>(R.id.rowConsistencyCheck).setOnClickListener {
            showConsistencyCheckDialog()
        }

        findViewById<View>(R.id.rowDateChange).setOnClickListener {
            showSuccessDialog("Date Change Operation", "Date Change Successfully")
        }

        findViewById<View>(R.id.rowGlUpdation).setOnClickListener {
            showSuccessDialog("GL Consolidation", "GL Consolidation Successful")
        }

        findViewById<View>(R.id.rowPenaltyAccrual).setOnClickListener {
            showAccrualDialog("Penalty")
        }

        findViewById<View>(R.id.rowInterestAccrual).setOnClickListener {
            showAccrualDialog("Interest")
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            // Simulate refresh logic
            android.widget.Toast.makeText(this, "Batch jobs refreshed successfully", android.widget.Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun showHolidayCheckDialog() {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_holiday_check, null)
        dialog.setView(view)

        val etFromDate = view.findViewById<android.widget.EditText>(R.id.etFromDate)
        val etToDate = view.findViewById<android.widget.EditText>(R.id.etToDate)
        val btnSubmit = view.findViewById<android.widget.Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnClose)
        val tvDialogTitle = view.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvValidationMessage = view.findViewById<android.widget.TextView>(R.id.tvValidationMessage)
        val layoutContent = view.findViewById<android.widget.LinearLayout>(R.id.layoutDialogContent)

        etFromDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePicker = android.app.DatePickerDialog(this, { _, year, month, day ->
                etFromDate.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnSubmit.setOnClickListener {
            // Change UI to validation result view as per image
            tvDialogTitle.text = "Holiday Check Validation"
            
            // Hide input rows
            for (i in 0 until layoutContent.childCount) {
                val child = layoutContent.getChildAt(i)
                if (child is android.widget.LinearLayout) {
                    child.visibility = View.GONE
                }
            }
            
            tvValidationMessage.visibility = View.VISIBLE
            btnSubmit.visibility = View.GONE
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showConsistencyCheckDialog() {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_consistency_check, null)
        dialog.setView(view)

        view.findViewById<android.widget.Button>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showSuccessDialog(title: String, message: String) {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_holiday_check, null)
        dialog.setView(view)

        val tvDialogTitle = view.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvValidationMessage = view.findViewById<android.widget.TextView>(R.id.tvValidationMessage)
        val layoutContent = view.findViewById<android.widget.LinearLayout>(R.id.layoutDialogContent)
        val btnSubmit = view.findViewById<android.widget.Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnClose)

        tvDialogTitle.text = title
        tvValidationMessage.text = message
        tvValidationMessage.visibility = View.VISIBLE
        btnSubmit.visibility = View.GONE

        // Hide date input rows
        for (i in 0 until layoutContent.childCount) {
            val child = layoutContent.getChildAt(i)
            if (child is android.widget.LinearLayout) {
                child.visibility = View.GONE
            }
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showAccrualDialog(type: String) {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_penalty_accrual, null)
        dialog.setView(view)

        val etAccrualDate = view.findViewById<android.widget.EditText>(R.id.etAccrualDate)
        val btnSubmit = view.findViewById<android.widget.Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnClose)
        val tvDialogTitle = view.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvSuccessMessage = view.findViewById<android.widget.TextView>(R.id.tvSuccessMessage)
        val layoutContent = view.findViewById<android.widget.LinearLayout>(R.id.layoutDialogContent)

        tvDialogTitle.text = "$type Accrual Run"
        tvSuccessMessage.text = "$type Accrual Runned Successfully"

        etAccrualDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePicker = android.app.DatePickerDialog(this, { _, year, month, day ->
                etAccrualDate.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
            datePicker.show()
        }

        btnSubmit.setOnClickListener {
            tvDialogTitle.text = "$type Accrual"
            
            // Hide input rows
            for (i in 0 until layoutContent.childCount) {
                val child = layoutContent.getChildAt(i)
                if (child is android.widget.LinearLayout) {
                    child.visibility = View.GONE
                }
            }
            
            tvSuccessMessage.visibility = View.VISIBLE
            btnSubmit.visibility = View.GONE
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showDabSelectionDialog() {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_dab_selection, null)
        dialog.setView(view)

        val etAccountId = view.findViewById<android.widget.EditText>(R.id.etAccountId)
        val btnSearchAccount = view.findViewById<android.widget.ImageButton>(R.id.btnSearchAccount)
        val etFromDate = view.findViewById<android.widget.EditText>(R.id.etFromDate)
        val etToDate = view.findViewById<android.widget.EditText>(R.id.etToDate)
        val btnSubmit = view.findViewById<android.widget.Button>(R.id.btnSubmit)
        val btnClose = view.findViewById<android.widget.Button>(R.id.btnClose)
        val tvDialogTitle = view.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val layoutSelectionContent = view.findViewById<android.widget.LinearLayout>(R.id.layoutSelectionContent)
        val layoutProcessing = view.findViewById<android.widget.LinearLayout>(R.id.layoutProcessing)

        etFromDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                etFromDate.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        etToDate.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            android.app.DatePickerDialog(this, { _, year, month, day ->
                etToDate.setText(String.format("%02d-%02d-%04d", day, month + 1, year))
            }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
        }

        btnSearchAccount.setOnClickListener {
            showDabAccountListDialog { selectedAccount ->
                etAccountId.setText(selectedAccount)
            }
        }

        btnSubmit.setOnClickListener {
            // Show processing and error as per image
            tvDialogTitle.text = "Daily Account Balance"
            
            // Hide input rows
            for (i in 0 until layoutSelectionContent.childCount) {
                val child = layoutSelectionContent.getChildAt(i)
                if (child is android.widget.LinearLayout && child.id != R.id.layoutProcessing) {
                    child.visibility = View.GONE
                }
            }
            
            layoutProcessing.visibility = View.VISIBLE
            btnSubmit.visibility = View.GONE
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun showDabAccountListDialog(onSelected: (String) -> Unit) {
        val dialog = android.app.AlertDialog.Builder(this).create()
        val view = layoutInflater.inflate(R.layout.dialog_dab_account_list, null)
        dialog.setView(view)

        val btnFilter = view.findViewById<android.widget.Button>(R.id.btnFilter)
        val etFilterAccountNumber = view.findViewById<android.widget.EditText>(R.id.etFilterAccountNumber)
        val etFilterAccountName = view.findViewById<android.widget.EditText>(R.id.etFilterAccountName)
        val tableAccounts = view.findViewById<android.widget.TableLayout>(R.id.tableAccounts)

        btnFilter.setOnClickListener {
            val isVisible = etFilterAccountNumber.visibility == View.VISIBLE
            etFilterAccountNumber.visibility = if (isVisible) View.GONE else View.VISIBLE
            etFilterAccountName.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        // Add click listeners to mock rows
        for (i in 0 until tableAccounts.childCount) {
            val row = tableAccounts.getChildAt(i) as? android.widget.TableRow
            row?.setOnClickListener {
                val accountNumber = (row.getChildAt(0) as? android.widget.TextView)?.text.toString()
                onSelected(accountNumber)
                dialog.dismiss()
            }
        }

        view.findViewById<android.widget.Button>(R.id.btnClose).setOnClickListener {
            dialog.dismiss()
        }
        
        view.findViewById<android.widget.Button>(R.id.btnBack).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
