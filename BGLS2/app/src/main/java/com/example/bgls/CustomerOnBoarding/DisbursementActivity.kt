package com.example.bgls.CustomerOnBoarding

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bgls.R

class DisbursementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disbursement)

        initViews()
    }

    private fun initViews() {
        val btnBack: android.widget.ImageView = findViewById(R.id.btnBack)
        val btnHome: android.widget.ImageView = findViewById(R.id.btnHome)
        val btnFooterHome: android.widget.Button = findViewById(R.id.btnFooterHome)
        val btnFooterBack: android.widget.Button = findViewById(R.id.btnFooterBack)
        val spinnerAccountNo: android.widget.Spinner = findViewById(R.id.spinnerAccountNo)
        val rgTranAbstract: android.widget.RadioGroup = findViewById(R.id.rgTranAbstract)

        val navigationToHome = {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener { finish() }
        btnFooterBack.setOnClickListener { finish() }
        btnHome.setOnClickListener { navigationToHome() }
        btnFooterHome.setOnClickListener { navigationToHome() }

        rgTranAbstract.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCashAbstract || checkedId == R.id.rbOfficeRoutingAbstract) {
                showAbstractDialog()
            }
        }

        // Dummy data for Account No spinner
        val accounts = listOf("Select", "1100001110", "1100001120", "1100001130")
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, accounts)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAccountNo.adapter = adapter
    }

    private fun showAbstractDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_abstract)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Make dialog wider
        val layoutParams = android.view.WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        dialog.window?.attributes = layoutParams

        val btnClose: android.widget.Button = dialog.findViewById(R.id.btnDialogClose)
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}