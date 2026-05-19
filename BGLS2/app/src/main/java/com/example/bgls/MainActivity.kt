package com.example.bgls

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.content.ContextCompat
import com.example.bgls.AuditTrial.AuditTrailDetailsActivity
import com.example.bgls.BatchJobExecution.BatchJobActivity
import com.example.bgls.ChartOfAccounts.ChartOfAccountsActivity
import com.example.bgls.CustomerMaintenance.CustomerMaintenanceActivity
import com.example.bgls.CustomerMaster.CustomerMasterActivity
import com.example.bgls.CustomerOnBoarding.*
import com.example.bgls.DepositAccount.DepositAccountMaintenanceListActivity
import com.example.bgls.LoanMaintenance.LoanMaintananceListActivity
import com.example.bgls.LoanMaster.LoanMasterActivity
import com.example.bgls.LoanOperation.LoanClosureActivity
import com.example.bgls.LoanOperation.LoanOperationActivity
import com.example.bgls.LoanSchedule.LoanScheduleActivity
import com.example.bgls.OrganizationDetails.OrganizationDetialsActivity
import com.example.bgls.ReversalTransaction.*
import com.example.bgls.Transaction.TransactionActivity
import com.example.bgls.TransactionInquiries.*
import com.example.bgls.TransactionMaintenance.*
import com.example.bgls.TransactionReports.*
import com.example.bgls.UserControl.UserControlActivity
import com.example.bgls.WalletMaintenance.*
import com.example.bgls.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // ── Module definitions ──────────────────────────────────────────────────
    data class Module(val title: String, val icon: String, val subItems: List<String> = emptyList())

    private val modules = listOf(
        Module("Admin", "⚙️", listOf("Organization Details","User Control","Parameters","Audit Trail")),
        Module("Migration", "🔄", listOf("Customer Master","Loan Master","Loan Schedule","Transaction")),
        Module("Chart of Accounts", "📊", listOf("Chart of Accounts")),
        Module("Customer Onboarding", "👤", listOf("Minimal Data","Approval","Disbursement","KYC Compliance","Compliance Department","Hold and Reject")),
        Module("Customer Maintenance", "🛠️", listOf("Customer Maintenance")),
        Module("Loan Maintenance", "📋", listOf("Loan Maintenance")),
        Module("Loan Operation", "💼", listOf("Loan Operation","Loan Closure")),
        Module("Deposit Maintenance", "🏦", listOf("Deposit Maintenance")),
        Module("Wallet Maintenance", "👛", listOf("Wallet Maintanance","Wallet Inquries")),
        Module("Transaction Maintenance", "💳", listOf("Journal Entries","Account Ledger Positing","Account Leader","Trial Balance","Profile and Loss Account")),
        Module("Reversal Transactions", "↩️", listOf("Transaction Reversal","Recovery Reversal","Failed Reversal")),
        Module("Collection Process", "📥", listOf("Loan Collection")),
        Module("Batch Job", "⚡", listOf("Batch Job")),
        Module("Reports", "📄", listOf("Credit Facility Report","End Of Month Report","DAB Reports","Consolidated Loan Reports","Transaction Reports","Recovery Report","Demand generation Report","Interest Accrual Report","Penalty Accrual Report")),
        Module("Transaction Inquiries", "🔍", listOf("Account Balances","Interest Summary","Journal Book","Account Ledger","Trial Balance","Profile and Loss Account","Balance Sheet","Balancing Report"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupDrawer()
        setupGrid()
    }

    // ── Header ──────────────────────────────────────────────────────────────
    private fun setupHeader() {
        val currentTime = java.text.SimpleDateFormat(
            "dd-MM-yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        binding.txtLoginTime.text = currentTime
    }

    // ── Drawer ──────────────────────────────────────────────────────────────
    private fun setupDrawer() {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        binding.menuIcon.setOnClickListener {
            drawerLayout.openDrawer(Gravity.START)
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home    -> Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
                R.id.nav_profile -> Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show()
                R.id.nav_logout  -> Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(Gravity.START)
            true
        }
    }

    // ── Grid ────────────────────────────────────────────────────────────────
    private fun setupGrid() {
        val recyclerView = binding.recyclerViewModules
        recyclerView?.post {
            val totalW = recyclerView.width
            val totalH = recyclerView.height

            val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val cols = if (isLandscape) 5 else 4
            val rows = Math.ceil(modules.size / cols.toDouble()).toInt()

            val cellW = totalW / cols
            val cellH = totalH / rows

            recyclerView.layoutManager = object : androidx.recyclerview.widget.GridLayoutManager(this, cols) {
                override fun canScrollVertically(): Boolean = false
                override fun canScrollHorizontally(): Boolean = false
            }

            recyclerView.adapter = ModuleAdapter(modules, cellW, cellH) { module, view ->
                onModuleClicked(module, view)
            }
        }
    }

    // ── Module click → show sub-items dialog or navigate directly ───────────
    private fun onModuleClicked(module: Module, view: View) {
        when {
            module.subItems.isEmpty() -> {
                Toast.makeText(this, "${module.title} coming soon", Toast.LENGTH_SHORT).show()
            }
            module.subItems.size == 1 -> {
                navigate(module.subItems.first(), module.title)
            }
            else -> showSubMenu(module, view)
        }
    }

    // ── Professional Sub-Menu Dialog with Blue Gradient Header ─────────────────
    // ── Professional Sub-Menu anchored to the clicked module card ─────────────────
    private fun showSubMenu(module: Module, anchorView: View) {
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.dialog_sub_menu, null)

        // Show all items without scrolling by using WRAP_CONTENT
//        val width = anchorView.width
//        val popupWindow = PopupWindow(popupView, width, LinearLayout.LayoutParams.WRAP_CONTENT, true)
// Smaller popup aligned with text/icon area
        val width = (anchorView.width * 0.82).toInt()

        val popupWindow = PopupWindow(
            popupView,
            width,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 25f
        popupWindow.animationStyle = android.R.style.Animation_Dialog

        val tvTitle    = popupView.findViewById<TextView>(R.id.tvDialogTitle)
        val container  = popupView.findViewById<LinearLayout>(R.id.llDialogItems)
        val btnClose   = popupView.findViewById<ImageView>(R.id.ivDialogClose)
        val tvIcon     = popupView.findViewById<TextView>(R.id.tvDialogIcon)
        val flIcon     = popupView.findViewById<FrameLayout>(R.id.flDialogIcon)
        val headerLayout = popupView.findViewById<LinearLayout>(R.id.llDialogHeader)

        headerLayout?.setPadding(dp(12), dp(10), dp(12), dp(10))
        headerLayout?.background = getHeaderGradientBackground()
        
       // tvTitle.text = module.title.lowercase().replaceFirstChar { it.uppercase() }
        tvTitle.text = module.title
            .lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
        tvTitle.textSize = 14f
        tvTitle.setTextColor(Color.WHITE)
        tvTitle.isAllCaps = false

        tvIcon.text = module.icon
        tvIcon.textSize = 18f
        flIcon.layoutParams.width = dp(34)
        flIcon.layoutParams.height = dp(34)
//        flIcon.background = getRoundedCircleBgWhite(module.color)
//        tvIcon.setTextColor(Color.parseColor(module.color))
        flIcon.background = getRoundedCircleBgWhite("#38A9CB")
        tvIcon.setTextColor(Color.parseColor("#38A9CB"))

        btnClose.layoutParams.width = dp(24)
        btnClose.layoutParams.height = dp(24)
        btnClose.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
        btnClose.setOnClickListener { popupWindow.dismiss() }

        container.removeAllViews()

        module.subItems.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(10), dp(4), dp(10), dp(4))
                
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    popupWindow.dismiss()
                    navigate(item, module.title)
                }

                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val tv = TextView(this).apply {
                // Sentence case: First letter capital, others lowercase
              //  text = item.lowercase().replaceFirstChar { it.uppercase() }
                text = item
                    .lowercase()
                    .split( " ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercase() }
                    }
                textSize = 11f
                setTextColor(Color.parseColor("#1A1A1A"))
                isAllCaps = false
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            row.addView(tv)
            container.addView(row)
        }

        // Show anchored to the clicked view, perfectly covering the card using absolute coordinates
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        //popupWindow.showAtLocation(anchorView, android.view.Gravity.NO_GRAVITY, location[0], location[1])
        // Shift popup slightly right so it starts from title/icon area
        val xOffset = dp(14)
        val yOffset = 0

        popupWindow.showAtLocation(
            anchorView,
            Gravity.NO_GRAVITY,
            location[0] + xOffset,
            location[1] + yOffset
        )
    }

    // ── Gradient Background for Header (Blue Gradient) ─────────────────────
    private fun getHeaderGradientBackground(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            colors = intArrayOf(
                Color.parseColor("#38A9CB"),
                Color.parseColor("#5FC3DC")
            )
            orientation = android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
            cornerRadii = floatArrayOf(
                dp(24).toFloat(), dp(24).toFloat(),
                dp(24).toFloat(), dp(24).toFloat(),
                0f, 0f,
                0f, 0f
            )
        }
    }

    // ── White Rounded Circle Background for Icon ────────────────────────────
    private fun getRoundedCircleBgWhite(hexColor: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.WHITE)
            setStroke(dp(2), Color.parseColor(hexColor))
        }
    }

    // ── White Card Background for Sub-items ─────────────────────────────────
    private fun getWhiteCardBg(): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            cornerRadius = dp(8).toFloat()
            // Removed stroke for a line-less look
        }
    }

    // ── Navigation ──────────────────────────────────────────────────────────
    private fun navigate(subItem: String, parentName: String) {
        val intent: Intent? = when (subItem) {
            "Organization Details"      -> Intent(this, OrganizationDetialsActivity::class.java)
            "User Control"              -> Intent(this, UserControlActivity::class.java)
            "Parameters"                -> Intent(this, ParameterActivity::class.java)
            "Audit Trail"               -> Intent(this, AuditTrailDetailsActivity::class.java)
            "Customer Master"           -> Intent(this, CustomerMasterActivity::class.java)
            "Loan Master"               -> Intent(this, LoanMasterActivity::class.java)
            "Loan Schedule"             -> Intent(this, LoanScheduleActivity::class.java)
            "Transaction"               -> Intent(this, TransactionActivity::class.java)
            "Chart of Accounts"         -> Intent(this, ChartOfAccountsActivity::class.java)
            "Customer Maintenance"      -> Intent(this, CustomerMaintenanceActivity::class.java)
            "Loan Maintenance"          -> Intent(this, LoanMaintananceListActivity::class.java)
            "Journal Entries"           -> Intent(this, JournalEntriesActivity::class.java)
            "Account Ledger Positing"   -> Intent(this, AccountLedgerPositingActivity::class.java)
            "Account Leader" ->
                Intent(this, ParameterActivity::class.java).apply {
                    putExtra("MODULE_NAME", "Account Ledger")
                }
            "Trial Balance"             -> Intent(this, TrialBalanceActivity::class.java)
            "Profile and Loss Account"  -> Intent(this, ProfileAndLossAccountActivity::class.java)
            "Loan Operation"            -> Intent(this, LoanOperationActivity::class.java)
            "Loan Closure"              -> Intent(this, LoanClosureActivity::class.java)
            "Wallet Maintanance"        -> Intent(this, WalletMaintenanceListActivity::class.java)
            "Wallet Inquries"           -> Intent(this, WalletInquiryActivity::class.java)
            "Loan Collection"           -> Intent(this, LoanCollectionActivity::class.java)
            "Transaction Reversal"      -> Intent(this, TransactionsReversalActivity::class.java)
            "Recovery Reversal"         -> Intent(this, RecoveryReversalActivity::class.java)
            "Failed Reversal"           -> Intent(this, FailedReversalActivity::class.java)
            "Account Balances"          -> Intent(this, AccountBalanceActivity::class.java)
            "Interest Summary"          -> Intent(this, InterestSummaryActivity::class.java)
            "Journal Book"              -> Intent(this, JournalEntriesListActivity::class.java)
            "Account Ledger"            -> Intent(this, TransInqAccountLedgerActivity::class.java)
            "Balance Sheet"             -> Intent(this, BalanceSheetActivity::class.java)
            "Balancing Report"          -> Intent(this, BalancingReportActivity::class.java)
            "Batch Job"                 -> Intent(this, BatchJobActivity::class.java)
            "Minimal Data"              -> Intent(this, MinimalDataActivity::class.java)
            "Approval"                  -> Intent(this, ApprovalActivity::class.java)
            "Disbursement"              -> Intent(this, DisbursementActivity::class.java)
            "KYC Compliance"            -> Intent(this, KYCComplianceActivity::class.java)
            "Compliance Department"     -> Intent(this, ComplianceDepartmentActivity::class.java)
            "Hold and Reject"                  -> Intent(this, HoldRejectListActivity::class.java)
            "Deposit Maintenance"       -> Intent(this, DepositAccountMaintenanceListActivity::class.java)
            "Credit Facility Report"    -> Intent(this, CreditFacilityReportActivity::class.java)
            "End Of Month Report"       -> Intent(this, EndOfMonthReportActivity::class.java)
            "DAB Reports"               -> Intent(this, DABReportActivity::class.java)
            "Consolidated Loan Reports" -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Consolidated Loan - Reports") }
            "Transaction Reports"       -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Transaction - Reports") }
            "Recovery Report"           -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Recovery - Reports")
                putExtra("SHOW_SPINNER", true) }
            "Demand generation Report"  -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Demand Generation - Reports") }
            "Interest Accrual Report"   -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Interest Accrual - Reports") }
            "Penalty Accrual Report"    -> Intent(this, GenericReportActivity::class.java).apply {
                putExtra("REPORT_TITLE", "Penalty Accrual - Reports") }
            else -> null
        }
        if (intent != null) startActivity(intent)
        else Toast.makeText(this, "$subItem coming soon", Toast.LENGTH_SHORT).show()
    }

    // ── Helper Methods ──────────────────────────────────────────────────────

    private fun getRoundedDot(hexColor: String): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(Color.parseColor(hexColor))
        }
    }

    private fun getRippleDrawable(): android.graphics.drawable.Drawable {
        return android.graphics.drawable.RippleDrawable(
            android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")),
            getWhiteCardBg(),
            getWhiteCardBg()
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}