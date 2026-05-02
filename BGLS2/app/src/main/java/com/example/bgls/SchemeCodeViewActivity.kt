package com.example.bgls

import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SchemeCodeViewActivity : AppCompatActivity() {

    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scheme_code_view)

        val tvProduct = findViewById<EditText>(R.id.tvProduct)
        val tvProductType = findViewById<EditText>(R.id.tvProductType)
        val tvId = findViewById<EditText>(R.id.tvId)
        val tvState = findViewById<EditText>(R.id.tvState)
        val tvProductCategory = findViewById<EditText>(R.id.tvProductCategory)
        val tvProductDescription = findViewById<EditText>(R.id.tvProductDescription)

        // Populate from intent
        intent.extras?.let { bundle: android.os.Bundle ->
            tvProduct.setText(bundle.getString("PRODUCT", "Bizna All Inclusive"))
            tvId.setText(bundle.getString("ID", "60003"))
            tvProductCategory.setText(bundle.getString("CATEGORY", "Purchase Financing"))
            tvProductType.setText(bundle.getString("TYPE", "Fixed Term Loan"))
            tvProductDescription.setText(bundle.getString("DESCRIPTION", "Loxea v3"))
            tvState.setText(bundle.getString("STATUS", "Active"))
        }

        val mode = intent.getStringExtra("MODE") ?: "VIEW"
        if (mode == "ADD") {
            isEditMode = true
            tvProduct.setText("")
            tvId.setText("")
            tvProductCategory.setText("")
            tvProductType.setText("")
            tvProductDescription.setText("")
            tvState.setText("Active")
            findViewById<Button>(R.id.btnModify).text = "Submit"
            window.decorView.post {
                setFormEnabled(findViewById(android.R.id.content), true)
            }
        }

//        findViewById<Button>(R.id.btnHome).setOnClickListener {
//            finish()
//        }

        val btnModify = findViewById<Button>(R.id.btnModify)
        btnModify.setOnClickListener {
            if (!isEditMode) {
                isEditMode = true
                btnModify.text = "Submit"
                setFormEnabled(findViewById(android.R.id.content), true)
                Toast.makeText(this, "Edit mode enabled", Toast.LENGTH_SHORT).show()
            } else {
                val message = if (mode == "ADD") "Scheme Code Added successfully" else "Data Submitted successfully"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this Scheme Code?")
                .setPositiveButton("Yes") { dialogInterface: DialogInterface, i: Int ->
                    Toast.makeText(this, "Scheme Code deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }

//        findViewById<Button>(R.id.btnBack).setOnClickListener {
//            finish()
//        }

        populateTransactionSettings()
        populateProductFees()
    }

    private fun setFormEnabled(viewGroup: ViewGroup, isEnabled: Boolean) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is EditText) {
                child.isEnabled = isEnabled
            } else if (child is CheckBox) {
                child.isEnabled = isEnabled
            } else if (child is ViewGroup) {
                setFormEnabled(child, isEnabled)
            }
        }
    }

    private fun populateTransactionSettings() {
        val container = findViewById<android.widget.LinearLayout>(R.id.transactionSettingsContainer)
        
        // Header Row
        val headerRow = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 16) }
        }
        
        val headers = listOf("", "Income A/C", "Received", "Parking", "Collection")
        headers.forEach { text ->
            headerRow.addView(TextView(this).apply {
                this.text = text
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                gravity = android.view.Gravity.CENTER
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
        }
        container.addView(headerRow)

        // Rows
        val rows = listOf("PRINCIPLE", "INTEREST", "FEES", "PENALTY")
        rows.forEach { rowName ->
            val row = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 8) }
            }
            
            // Row Label
            row.addView(TextView(this).apply {
                text = rowName
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            })

            // 4 Input fields
            for (i in 0 until 4) {
                row.addView(EditText(this).apply {
                    setBackgroundResource(R.drawable.edittext_background)
                    setPadding(16, 16, 16, 16)
                    isEnabled = isEditMode
                    layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        setMargins(8, 0, 8, 0)
                    }
                })
            }
            container.addView(row)
        }
    }

    private fun populateProductFees() {
        val container = findViewById<android.widget.LinearLayout>(R.id.feesContainer)

        val mockData = mapOf(
            1 to listOf("Motor Insurance", "None", "", "639714737", "Manual", "Flat (Ksh)", ""),
            2 to listOf("Loxea Maintenance", "None", "", "511226422", "Manual", "Flat (Ksh)", ""),
            3 to listOf("Admin", "None", "", "656038514", "Manual", "Flat (Ksh)", ""),
            4 to listOf("Other fees", "None", "", "325341291", "Manual", "Flat (Ksh)", ""),
            5 to listOf("Tracker", "None", "", "1975564643", "Manual", "Flat (Ksh)", ""),
            6 to listOf("Repairs", "None", "", "2084802350", "Manual", "Flat (Ksh)", ""),
            7 to listOf("VAT on OL", "None", "", "53561656", "Manual", "Flat (Ksh)", "")
        )

        val mode = intent.getStringExtra("MODE") ?: "VIEW"

        for (i in 1..17) {
            val data = if (mode == "ADD") listOf("", "", "", "", "", "", "") else (mockData[i] ?: listOf("", "", "", "", "", "", ""))

            val row1 = createFeeRow(
                "FEE${i}_NAME", data[0],
                "FEE${i}_ID_TYPE", data[3],
                "FEE${i}_PAYMENT", data[5]
            )
            val row2 = createFeeRow(
                "FEE${i}_AMORT_PROFILE", data[1],
                "TYPE", data[4],
                "AMOUNT", data[6]
            )
            val row3 = createFeeRow(
                "FEE${i}_APPLICATION", data[2],
                "", "",
                "", ""
            )

            container.addView(row1)
            container.addView(row2)
            container.addView(row3)

            // Add spacing between fees
            container.addView(android.view.View(this).apply {
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 24
                )
            })
        }
    }

    private fun createFeeRow(
        label1: String, val1: String,
        label2: String, val2: String,
        label3: String, val3: String
    ): android.widget.LinearLayout {
        val row = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 8) }
        }

        row.addView(createFeeField(label1, val1))
        row.addView(createFeeField(label2, val2))
        row.addView(createFeeField(label3, val3))

        return row
    }

    private fun createFeeField(label: String, value: String): android.widget.LinearLayout {
        val field = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, 16, 0)
            }
        }

        if (label.isNotEmpty()) {
            val tvLabel = TextView(this).apply {
                text = label
                textSize = 10f
                setTextColor(android.graphics.Color.parseColor("#666666"))
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
                gravity = android.view.Gravity.CENTER_VERTICAL
            }
            field.addView(tvLabel)

            val etValue = EditText(this).apply {
                setText(value)
                textSize = 12f
                isEnabled = isEditMode
                setBackgroundResource(R.drawable.edittext_background)
                setPadding(16, 16, 16, 16)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f)
            }
            field.addView(etValue)
        }

        return field
    }
}
