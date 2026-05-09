package com.example.bgls

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
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
    data class Module(val title: String, val icon: String, val color: String, val subItems: List<String> = emptyList())

    private val modules = listOf(
        Module("Admin",                 "⚙️",  "#1565C0", listOf("Organization Details","User Control","Parameters","Audit Trail")),
        Module("Migration",             "🔄",  "#00695C", listOf("Customer Master","Loan Master","Loan Schedule","Transaction")),
        Module("Chart of Accounts",     "📊",  "#6A1B9A", listOf("Chart of Accounts")),
        Module("Customer Onboarding",   "👤",  "#E65100", listOf("Minimal Data","Approval","Disbursement","KYC Compliance","Compliance Department","Hold and Reject")),
        Module("Customer Maintenance",  "🛠️", "#2E7D32", listOf("Customer Maintenance")),
        Module("Loan Maintenance",      "📋",  "#1976D2", listOf("Loan Maintenance")),
        Module("Loan Operation",        "💼",  "#AD1457", listOf("Loan Operation","Loan Closure")),
        Module("Deposit Maintenance",   "🏦",  "#00838F", listOf("Deposit Maintenance")),
        Module("Wallet Maintenance",    "👛",  "#4527A0", listOf("Wallet Maintanance","Wallet Inquries")),
        Module("Transaction Maintenance",    "💳",  "#558B2F", listOf("Journal Entries","Account Ledger Positing","Account Leader","Trial Balance","Profile and Loss Account")),
        Module("Reversal Transactions", "↩️",  "#BF360C", listOf("Transaction Reversal","Recovery Reversal","Failed Reversal")),
        Module("Collection Process",    "📥",  "#0277BD", listOf("Loan Collection")),
        Module("Batch Job",             "⚡",  "#37474F", listOf("Batch Job")),
        Module("Reports",               "📄",  "#4E342E", listOf("Credit Facility Report","End Of Month Report","DAB Reports","Consolidated Loan Reports","Transaction Reports","Recovery Report","Demand generation Report","Interest Accrual Report","Penalty Accrual Report")),
        Module("Transaction Inquiries", "🔍",  "#283593", listOf("Account Balances","Interest Summary","Journal Book","Account Ledger","Trial Balance","Profile and Loss Account","Balance Sheet","Balancing Report"))
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

            recyclerView.adapter = ModuleAdapter(modules, cellW, cellH) { module ->
                onModuleClicked(module)
            }
        }
    }

    // ── Module click → show sub-items dialog or navigate directly ───────────
    private fun onModuleClicked(module: Module) {
        when {
            module.subItems.isEmpty() -> {
                Toast.makeText(this, "${module.title} coming soon", Toast.LENGTH_SHORT).show()
            }
            module.subItems.size == 1 -> {
                navigate(module.subItems.first(), module.title)
            }
            else -> showSubMenu(module)
        }
    }

    // ── Professional Sub-Menu Dialog with Blue Gradient Header ─────────────────
    private fun showSubMenu(module: Module) {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_sub_menu)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT)

        // Get all dialog views
        val tvTitle    = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val container  = dialog.findViewById<LinearLayout>(R.id.llDialogItems)
        val btnClose   = dialog.findViewById<ImageView>(R.id.ivDialogClose)
        val tvIcon     = dialog.findViewById<TextView>(R.id.tvDialogIcon)
        val flIcon     = dialog.findViewById<FrameLayout>(R.id.flDialogIcon)
        val headerLayout = dialog.findViewById<LinearLayout>(R.id.llDialogHeader)
        val scrollView = dialog.findViewById<ScrollView>(R.id.scrollViewItems)

        // ✅ Header gets BLUE GRADIENT
        headerLayout?.background = getHeaderGradientBackground()

        // White text for header
        tvTitle.setTextColor(Color.WHITE)

        // "BANKING SERVICES" label in semi-transparent white
       // val lblBankingService = dialog.findViewById<TextView>(R.id.lblBankingService)
      //  lblBankingService?.setTextColor(Color.parseColor("#CCFFFFFF"))

        // Close icon in white
        btnClose.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)

        // ✅ Sub-items area stays WHITE
        scrollView?.setBackgroundColor(Color.WHITE)

        tvTitle.text = module.title
        tvIcon.text = module.icon

        // Icon background - white with module color border
        flIcon.background = getRoundedCircleBgWhite(module.color)
        tvIcon.setTextColor(Color.parseColor(module.color))

        // Clear existing views
        container.removeAllViews()

        // Add each sub-item
        module.subItems.forEachIndexed { index, item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(14), dp(16), dp(14))

                background = getWhiteCardBg()
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    dialog.dismiss()
                    navigate(item, module.title)
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(dp(12), if (index == 0) dp(12) else dp(6), dp(12), if (index == module.subItems.size - 1) dp(12) else dp(6))
                }
                layoutParams = params
            }

            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(8), dp(8)).apply {
                    marginEnd = dp(14)
                }
                background = getRoundedDot(module.color)
            }

            val tv = TextView(this).apply {
                text = item
                textSize = 16f
                setTextColor(Color.parseColor("#1A1A1A"))
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val arrow = TextView(this).apply {
                text = "›"
                textSize = 22f
                setTextColor(Color.parseColor("#CCCCCC"))
                setPadding(dp(8), 0, 0, 0)
            }

            row.addView(dot)
            row.addView(tv)
            row.addView(arrow)
            container.addView(row)

            // Add divider between items
            if (index < module.subItems.size - 1) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(1))
                    setBackgroundColor(Color.parseColor("#F0F0F0"))
                }
                container.addView(divider)
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
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
            cornerRadius = dp(12).toFloat()
            setStroke(dp(1), Color.parseColor("#E0E0E0"))
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
            "Account Ledger"            -> Intent(this, TransAccountLedgerActivity::class.java)
            "Balance Sheet"             -> Intent(this, BalanceSheetActivity::class.java)
            "Balancing Report"          -> Intent(this, BalancingReportActivity::class.java)
            "Batch Job"                 -> Intent(this, BatchJobActivity::class.java)
            "Minimal Data"              -> Intent(this, MinimalDataActivity::class.java)
            "Approval"                  -> Intent(this, ApprovalActivity::class.java)
            "Disbursement"              -> Intent(this, DisbursementActivity::class.java)
            "KYC Compliance"            -> Intent(this, KYCComplianceActivity::class.java)
            "Compliance Department"     -> Intent(this, ComplianceDepartmentActivity::class.java)
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