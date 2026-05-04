package com.example.bgls.LoanOperation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import com.example.bgls.R

class LoanClosureActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvAccountLabel: TextView
    private lateinit var tvBalanceLabel: TextView
    private lateinit var btnScheduler: Button
    private lateinit var btnLedger: Button
    private lateinit var btnPreClosureMode: Button
    private lateinit var btnClosureMode: Button
    private lateinit var btnSubmit: Button
    private lateinit var ivSearchAccount: ImageView
    private lateinit var etAccountId: EditText
    private lateinit var etAccountName: EditText
    private lateinit var llRows: LinearLayout
    private lateinit var ivAddRow: ImageView
    private lateinit var ivRemoveRow: ImageView
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_closure)

        initViews()
        setupListeners()
        setMode(true) // Start with Pre-Closure mode
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tvTitle)
        tvAccountLabel = findViewById(R.id.tvAccountLabel)
        tvBalanceLabel = findViewById(R.id.tvBalanceLabel)
        btnScheduler = findViewById(R.id.btnScheduler)
        btnLedger = findViewById(R.id.btnLedger)
        btnPreClosureMode = findViewById(R.id.btnPreClosureMode)
        btnClosureMode = findViewById(R.id.btnClosureMode)
        btnSubmit = findViewById(R.id.btnSubmit)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        etAccountId = findViewById(R.id.etAccountId)
        etAccountName = findViewById(R.id.etAccountName)
        llRows = findViewById(R.id.llRows)
        ivAddRow = findViewById(R.id.ivAddRow)
        ivRemoveRow = findViewById(R.id.ivRemoveRow)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnScheduler.setOnClickListener {
            Toast.makeText(this, "Opening Scheduler...", Toast.LENGTH_SHORT).show()
        }

        btnLedger.setOnClickListener {
            Toast.makeText(this, "Opening Ledger...", Toast.LENGTH_SHORT).show()
        }

        btnPreClosureMode.setOnClickListener { setMode(true) }
        btnClosureMode.setOnClickListener { setMode(false) }

        ivSearchAccount.setOnClickListener {
            openAccountSearchDialog()
        }

        ivAddRow.setOnClickListener {
            addNewRow()
        }

        ivRemoveRow.setOnClickListener {
            if (llRows.childCount > 0) {
                llRows.removeViewAt(llRows.childCount - 1)
            }
        }

        btnSubmit.setOnClickListener {
            Toast.makeText(this, "Loan operation submitted successfully", Toast.LENGTH_LONG).show()
        }

        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun setMode(isPreClosure: Boolean) {
        if (isPreClosure) {
            tvTitle.text = "LOAN PRE - CLOSURE"
            tvAccountLabel.text = "Account ID"
            tvBalanceLabel.text = "Loan Balance"
            btnPreClosureMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#0056b3"))
            btnClosureMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
        } else {
            tvTitle.text = "LOAN CLOSURE"
            tvAccountLabel.text = "Account No"
            tvBalanceLabel.text = "Account Balance"
            btnPreClosureMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnClosureMode.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#0056b3"))
        }
    }

    private fun addNewRow() {
        val row = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
        }

        // Weights: 1, 1, 1.2, 1.2, 1.2, 1.5
        val weights = listOf(1f, 1f, 1.2f, 1.2f, 1.2f, 1.5f)
        for ((index, w) in weights.withIndex()) {
            val et = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, 40.dpToPx(), w)
                setBackgroundResource(R.drawable.table_cell_bg)
                textSize = 10f
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
                gravity = if (index >= 2) Gravity.END else Gravity.CENTER
            }
            row.addView(et)
        }

        llRows.addView(row)
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun openAccountSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etSearchAccNo = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val btnFilter = dialogView.findViewById<Button>(R.id.btnFilter)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val tlAccounts = dialogView.findViewById<TableLayout>(R.id.tlAccounts)

        val accounts = listOf(
            Pair("BFM190701417", "SPEARS HUMBEG INVESTMENTS LIMITED LTD"),
            Pair("BFM190701451", "IZONE AFRICA LIMITED"),
            Pair("BFM190701931", "STELLA NYAKERU KARIMI"),
            Pair("BFM190702838", "THE IGNATION GROUP LIMITED"),
            Pair("BFM190706075", "LEVITICUS VENTURES LIMITED")
        )

        fun populateTable(list: List<Pair<String, String>>) {
            tlAccounts.removeAllViews()
            for (acc in list) {
                val row = TableRow(this)
                val tvNo = TextView(this).apply {
                    text = acc.first
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvName = TextView(this).apply {
                    text = acc.second
                    textSize = 10f
                    setPadding(16, 16, 16, 16)
                    setBackgroundResource(R.drawable.table_cell_bg)
                    layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                }
                row.addView(tvNo)
                row.addView(tvName)
                
                row.setOnClickListener {
                    etAccountId.setText(acc.first)
                    etAccountName.setText(acc.second)
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        populateTable(accounts)
        btnFilter.setOnClickListener { etSearchAccNo.requestFocus() }
        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}
