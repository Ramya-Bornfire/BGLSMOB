package com.example.bgls

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class LoanCollectionActivity : AppCompatActivity() {

    private lateinit var layoutBulkCollection: LinearLayout
    private lateinit var llBulkRows: LinearLayout

    private lateinit var tvFileName: TextView

    private lateinit var btnBulkAdd: Button
    //private lateinit var btnBulkSubmit: Button

    private lateinit var btnBulkUpload: Button
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val path = it.path ?: ""
            val fileName = if (path.contains("/")) path.substring(path.lastIndexOf("/") + 1) else "Selected File"
            tvFileName.text = fileName
            Toast.makeText(this, "Selected: $fileName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_collection)

        // View Binding (findViewById)
        layoutBulkCollection = findViewById(R.id.layoutBulkCollection)
        llBulkRows = findViewById(R.id.llBulkRows)

        btnBulkAdd = findViewById(R.id.btnBulkAdd)
        //btnBulkSubmit = findViewById(R.id.btnBulkSubmit)

        btnBulkUpload = findViewById(R.id.btnBulkUpload)


        // Show layout
        layoutBulkCollection.visibility = LinearLayout.VISIBLE

        // ➕ Add row
        btnBulkAdd.setOnClickListener {
            addRow()
        }

        // 📤 Upload click
        btnBulkUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        // ✅ Submit
//        btnBulkSubmit.setOnClickListener {
//            Toast.makeText(this, "Submitted", Toast.LENGTH_SHORT).show()
//        }


    }

    // 🔥 Dynamic Row Add
    private fun addRow() {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.HORIZONTAL
        }

        // Weights: 1, 1.5, 1, 1.2, 1, 1, 1.2, 1.2, 0.8
        val fields = listOf(
            Triple(1f, "", Gravity.CENTER), Triple(1.5f, "", Gravity.CENTER),
            Triple(1f, "", Gravity.CENTER), Triple(1.2f, "", Gravity.CENTER),
            Triple(1f, "", Gravity.CENTER), Triple(1f, "0", Gravity.CENTER),
            Triple(1.2f, "01-05-2026 13:00", Gravity.CENTER), Triple(1.2f, "UNALLOCATED", Gravity.CENTER)
        )

        for (field in fields) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), field.first)
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 10f
                setPadding(4.dpToPx(), 10.dpToPx(), 4.dpToPx(), 4.dpToPx())
                setText(field.second)
                gravity = field.third
            }
            row.addView(et)
        }

        // RadioButton for Allocated
        val rb = RadioButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), 0.8f)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
        }
        row.addView(rb)

        // Delete button (Trash icon)
        val ivDelete = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(40.dpToPx(), 40.dpToPx())
            setImageResource(android.R.drawable.ic_menu_delete)
            setPadding(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
            setColorFilter(Color.RED)
            setOnClickListener {
                llBulkRows.removeView(row)
            }
        }
        row.addView(ivDelete)

        llBulkRows.addView(row)
    }
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}