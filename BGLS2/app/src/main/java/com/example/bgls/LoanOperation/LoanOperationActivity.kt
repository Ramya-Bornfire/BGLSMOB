package com.example.bgls.LoanOperation

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.R

class LoanOperationActivity : AppCompatActivity() {

    private lateinit var rgOperationType: RadioGroup
    private lateinit var rgCollectionType: RadioGroup
    private lateinit var layoutCollectionType: View
    private lateinit var tvTranDateLabel: TextView
    private lateinit var etTranDate: EditText
    private lateinit var tvFromDateLabel: TextView
    private lateinit var etFromDate: EditText
    private lateinit var tvToDateLabel: TextView
    private lateinit var etToDate: EditText
    private lateinit var tvAcctBalanceLabel: TextView
    private lateinit var etAcctBalance: EditText
    private lateinit var tvRecoveryPaidLabel: TextView
    private lateinit var etRecoveryPaid: EditText
    private lateinit var tvRoutingAcctLabel: TextView
    private lateinit var etRoutingAcct: EditText
    private lateinit var tvColTranAmt: TextView
    //private lateinit var btnSubmit: Button
    private lateinit var btnHome: Button
    private lateinit var btnBack: Button
    private lateinit var ivSearchAccount: ImageView
    
    // Upload mode views
    private lateinit var layoutStandardOperation: View
    private lateinit var layoutFileUpload: View
    private lateinit var layoutTableArea: View
    private lateinit var btnChooseFile: Button
    private lateinit var tvFileName: TextView
    private lateinit var btnList: Button
    private lateinit var btnList1: Button
    private lateinit var btnUpload: Button

    // Bulk Collection views
    private lateinit var layoutBulkCollection: View
    private lateinit var llBulkRows: LinearLayout
    private lateinit var btnBulkUpload: Button
    private lateinit var btnBulkAdd: Button
    private lateinit var btnBulkSubmit: Button
    private lateinit var btnBulkHome: Button
    private lateinit var btnBulkBack: Button

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
        setContentView(R.layout.activity_loan_operation)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        rgOperationType = findViewById(R.id.rgOperationType)
        rgCollectionType = findViewById(R.id.rgCollectionType)
        layoutCollectionType = findViewById(R.id.layoutCollectionType)
        
        tvTranDateLabel = findViewById(R.id.tvTranDateLabel)
        etTranDate = findViewById(R.id.etTranDate)
        tvFromDateLabel = findViewById(R.id.tvFromDateLabel)
        etFromDate = findViewById(R.id.etFromDate)
        tvToDateLabel = findViewById(R.id.tvToDateLabel)
        etToDate = findViewById(R.id.etToDate)
        
        tvAcctBalanceLabel = findViewById(R.id.tvAcctBalanceLabel)
        etAcctBalance = findViewById(R.id.etAcctBalance)
        tvRecoveryPaidLabel = findViewById(R.id.tvRecoveryPaidLabel)
        etRecoveryPaid = findViewById(R.id.etRecoveryPaid)
        
        tvRoutingAcctLabel = findViewById(R.id.tvRoutingAcctLabel)
        etRoutingAcct = findViewById(R.id.etRoutingAcct)
        tvColTranAmt = findViewById(R.id.tvColTranAmt)
        
       // btnSubmit = findViewById(R.id.btnSubmit)
        btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
        ivSearchAccount = findViewById(R.id.ivSearchAccount)
        
        layoutStandardOperation = findViewById(R.id.layoutStandardOperation)
        layoutFileUpload = findViewById(R.id.layoutFileUpload)
        layoutTableArea = findViewById(R.id.layoutTableArea)
        btnChooseFile = findViewById(R.id.btnChooseFile)
        tvFileName = findViewById(R.id.tvFileName)
        btnList = findViewById(R.id.btnList)
        btnList1 = findViewById(R.id.btnList1)
        btnUpload = findViewById(R.id.btnUpload)

        layoutBulkCollection = findViewById(R.id.layoutBulkCollection)
        llBulkRows = findViewById(R.id.llBulkRows)
        btnBulkUpload = findViewById(R.id.btnBulkUpload)
        btnBulkAdd = findViewById(R.id.btnBulkAdd)
        btnBulkSubmit = findViewById(R.id.btnBulkSubmit)
        btnBulkHome = findViewById(R.id.btnBulkHome)
        btnBulkBack = findViewById(R.id.btnBulkBack)
    }

    private fun setupListeners() {
        rgOperationType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCollection) {
                // SHOW COLLECTION FIELDS
                layoutCollectionType.visibility = View.VISIBLE
                tvTranDateLabel.visibility = View.VISIBLE
                etTranDate.visibility = View.VISIBLE
                tvFromDateLabel.visibility = View.GONE
                etFromDate.visibility = View.GONE
                tvToDateLabel.visibility = View.GONE
                etToDate.visibility = View.GONE
                tvAcctBalanceLabel.visibility = View.VISIBLE
                etAcctBalance.visibility = View.VISIBLE
                tvRecoveryPaidLabel.visibility = View.VISIBLE
                etRecoveryPaid.visibility = View.VISIBLE
                tvColTranAmt.visibility = View.VISIBLE
                
                // Reset sub-mode visibility based on collection type
                updateCollectionSubMode(rgCollectionType.checkedRadioButtonId)
            } else {
                // SHOW INTEREST/FEES/PENALTY FIELDS (Matches images)
                layoutCollectionType.visibility = View.GONE
                tvTranDateLabel.visibility = View.GONE
                etTranDate.visibility = View.GONE
                tvFromDateLabel.visibility = View.VISIBLE
                etFromDate.visibility = View.VISIBLE
                tvToDateLabel.visibility = View.VISIBLE
                etToDate.visibility = View.VISIBLE
                tvAcctBalanceLabel.visibility = View.GONE
                etAcctBalance.visibility = View.GONE
                tvRecoveryPaidLabel.visibility = View.GONE
                etRecoveryPaid.visibility = View.GONE
                tvColTranAmt.visibility = View.GONE
                
                // Always show standard layout for these modes
                layoutStandardOperation.visibility = View.VISIBLE
                layoutFileUpload.visibility = View.GONE
                layoutBulkCollection.visibility = View.GONE
                layoutTableArea.visibility = View.VISIBLE
            }
        }

        rgCollectionType.setOnCheckedChangeListener { _, checkedId ->
            updateCollectionSubMode(checkedId)
        }

        btnChooseFile.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        btnBulkUpload.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }

        btnBulkAdd.setOnClickListener {
            addBulkRow()
        }

        btnBulkSubmit.setOnClickListener {
            Toast.makeText(this, "Bulk collection submitted successfully", Toast.LENGTH_LONG).show()
        }

        btnBulkHome.setOnClickListener { finish() }
        btnBulkBack.setOnClickListener { onBackPressed() }

        ivSearchAccount.setOnClickListener {
            openAccountSearchDialog()
        }

//        btnSubmit.setOnClickListener {
//            Toast.makeText(this, "Operation submitted successfully", Toast.LENGTH_LONG).show()
//        }

        btnHome.setOnClickListener {
            finish()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnUpload.setOnClickListener {
            if (tvFileName.text == "No file chosen") {
                Toast.makeText(this, "Please choose a file first", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "File ${tvFileName.text} uploaded successfully", Toast.LENGTH_LONG).show()
            }
        }

        btnList.setOnClickListener {
            Toast.makeText(this, "Showing List", Toast.LENGTH_SHORT).show()
        }

        btnList1.setOnClickListener {
            Toast.makeText(this, "Showing List 1", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCollectionSubMode(checkedId: Int) {
        if (rgOperationType.checkedRadioButtonId != R.id.rbCollection) return

        // Default visibility (Cash mode)
        tvRoutingAcctLabel.visibility = View.GONE
        etRoutingAcct.visibility = View.GONE
        layoutStandardOperation.visibility = View.VISIBLE
        layoutFileUpload.visibility = View.GONE
        layoutBulkCollection.visibility = View.GONE
        layoutTableArea.visibility = View.VISIBLE

        when (checkedId) {
            R.id.rbOfficeRouting -> {
                tvRoutingAcctLabel.visibility = View.VISIBLE
                etRoutingAcct.visibility = View.VISIBLE
            }
            R.id.rbStandingInstruction -> {
                layoutStandardOperation.visibility = View.GONE
                layoutFileUpload.visibility = View.VISIBLE
                layoutTableArea.visibility = View.GONE
            }
            R.id.rbMultipleEntries -> {
                layoutStandardOperation.visibility = View.GONE
                layoutFileUpload.visibility = View.GONE
                layoutBulkCollection.visibility = View.VISIBLE
                layoutTableArea.visibility = View.GONE
            }
        }
    }

    private fun addBulkRow() {
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
                setPadding(4.dpToPx(), 4.dpToPx(), 4.dpToPx(), 4.dpToPx())
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

    private fun openAccountSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_account_search, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etSearchAccNo = dialogView.findViewById<EditText>(R.id.etSearchAccNo)
        val etSearchAccName = dialogView.findViewById<EditText>(R.id.etSearchAccName)
        val btnFilter = dialogView.findViewById<Button>(R.id.btnFilter)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)
        val tlAccounts = dialogView.findViewById<TableLayout>(R.id.tlAccounts)

        val accounts = listOf(
            Pair("BFM190701417", "SPEARS HUMBEG INVESTMENTS LIMITED LTD"),
            Pair("BFM190701451", "IZONE AFRICA LIMITED"),
            Pair("BFM190701931", "STELLA NYAKERU KARIMI"),
            Pair("BFM190702838", "THE IGNATION GROUP LIMITED"),
            Pair("BFM190706075", "LEVITICUS VENTURES LIMITED"),
            Pair("BFM190709009", "IRON BRIDGE LTD"),
            Pair("BFM190709177", "CORNERSTONE ACADEMY LIMITED"),
            Pair("BFM190714580", "AUTO SPARKLE"),
            Pair("BFM190715452", "ROYALSTONE CROSS ENTERTAINMENT SOUNDS"),
            Pair("BFM190722628", "AIRPORT VIEW INVESTMENT LIMITED")
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
                    findViewById<EditText>(R.id.etAccountNo).setText(acc.first)
                    findViewById<EditText>(R.id.etAcctName).setText(acc.second)
                    dialog.dismiss()
                }
                tlAccounts.addView(row)
            }
        }

        populateTable(accounts)

        btnFilter.setOnClickListener {
            etSearchAccNo.requestFocus()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
