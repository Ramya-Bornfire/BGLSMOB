package com.example.bgls.TransactionReports

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

class CreditFacilityReportActivity : AppCompatActivity() {

    private lateinit var btnDetails: Button
    private lateinit var btnSchedule: Button
    private lateinit var ivSearchAccount: ImageView
    private lateinit var etAccountNo: EditText
    private lateinit var etAccountName: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credit_facility_report)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnDetails = findViewById(R.id.btnDetails)
        btnSchedule = findViewById(R.id.btnSchedule)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        etAccountNo = findViewById(R.id.etAccountNo)
        etAccountName = findViewById(R.id.etAccountName)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupListeners() {
        btnDetails.setOnClickListener {
            setMode(true)
        }

        btnSchedule.setOnClickListener {
            setMode(false)
        }

        ivSearchAccount.setOnClickListener {
            openAccountSearchDialog()
        }

        btnSubmit.setOnClickListener {
            val accNo = etAccountNo.text.toString()
            if (accNo.isEmpty()) {
                Toast.makeText(this, "Please select an account first", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Report generated for $accNo", Toast.LENGTH_LONG).show()
            }
        }

        btnHome.setOnClickListener { finish() }
        btnBack.setOnClickListener { onBackPressed() }
    }

    private fun setMode(isDetails: Boolean) {
        if (isDetails) {
            btnDetails.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnDetails.setTextColor(Color.WHITE)
            btnSchedule.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            btnSchedule.setTextColor(Color.parseColor("#333333"))
        } else {
            btnDetails.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            btnDetails.setTextColor(Color.parseColor("#333333"))
            btnSchedule.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#007BFF"))
            btnSchedule.setTextColor(Color.WHITE)
        }
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
                    etAccountNo.setText(acc.first)
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
