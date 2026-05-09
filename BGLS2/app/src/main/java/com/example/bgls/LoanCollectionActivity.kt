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
    private lateinit var btnBulkAdd: Button
    private lateinit var btnBulkUpload: Button

    // ✅ Weights — must match XML header exactly
    // TranId=1.2, Names=1.5, Ref=1.2, Mobile=1.5,
    // Amount=1.0, AllocAmt=1.2, Time=1.5, Status=1.2,
    // AllocRB=0.8, Delete=0.6
    private val W_TRAN_ID   = 1.2f
    private val W_NAMES     = 1.5f
    private val W_REF       = 1.2f
    private val W_MOBILE    = 1.5f
    private val W_AMOUNT    = 1.0f
    private val W_ALLOC_AMT = 1.2f
    private val W_TIME      = 1.5f
    private val W_STATUS    = 1.2f
    private val W_ALLOC_RB  = 0.8f
    private val W_DELETE    = 0.6f

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val path = it.path ?: ""
                val fileName = if (path.contains("/"))
                    path.substring(path.lastIndexOf("/") + 1)
                else "Selected File"
                Toast.makeText(this, "Selected: $fileName", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_collection)

        layoutBulkCollection = findViewById(R.id.layoutBulkCollection)
        llBulkRows            = findViewById(R.id.llBulkRows)
        btnBulkAdd            = findViewById(R.id.btnBulkAdd)
        btnBulkUpload         = findViewById(R.id.btnBulkUpload)

        layoutBulkCollection.visibility = LinearLayout.VISIBLE

        btnBulkAdd.setOnClickListener { addRow() }
        btnBulkUpload.setOnClickListener { filePickerLauncher.launch("*/*") }
    }

    private fun addRow() {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(40))
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.WHITE)
        }

        // Column definitions: Pair(weight, defaultText)
        val columns = listOf(
            Pair(W_TRAN_ID,   ""),
            Pair(W_NAMES,     ""),
            Pair(W_REF,       ""),
            Pair(W_MOBILE,    ""),
            Pair(W_AMOUNT,    ""),
            Pair(W_ALLOC_AMT, "0"),
            Pair(W_TIME,      "01-05-2026 13:00"),
            Pair(W_STATUS,    "UNALLOCATED")
        )

        // ✅ EditText cells — width=0dp + weight so they fill proportionally
        for ((weight, defaultText) in columns) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 9f
                setPadding(dp(4), dp(2), dp(4), dp(2))
                setText(defaultText)
                gravity = Gravity.CENTER
            }
            row.addView(et)
        }

        // ✅ RadioButton cell
        val rbCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, W_ALLOC_RB)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
            addView(RadioButton(this@LoanCollectionActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                minHeight = 0
                minWidth  = 0
            })
        }
        row.addView(rbCell)

        // ✅ Delete cell — same weight as header "Del" column
        val deleteCell = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, W_DELETE)
            setBackgroundResource(R.drawable.table_cell_bg)
            gravity = Gravity.CENTER
            addView(ImageView(this@LoanCollectionActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dp(20), dp(20))
                setImageResource(android.R.drawable.ic_menu_delete)
                setPadding(dp(2), dp(2), dp(2), dp(2))
                setColorFilter(Color.RED)
                setOnClickListener {
                    llBulkRows.removeView(row)
                }
            })
        }
        row.addView(deleteCell)

        llBulkRows.addView(row)
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}