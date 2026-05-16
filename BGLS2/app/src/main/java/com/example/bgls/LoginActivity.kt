package com.example.bgls

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bgls.DataModels.LoginRequest
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.username)
        val etPassword = findViewById<EditText>(R.id.password)
        val eyeIcon    = findViewById<ImageView>(R.id.eyeIcon)
        val btnLogin   = findViewById<Button>(R.id.btnLogin)

        // ── Auto uppercase username (Improved version) ────────────────────
        etUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // No implementation needed here
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed here
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentText = s?.toString() ?: ""
                val upperText = currentText.uppercase()

                // Only update if text has actually changed to prevent infinite loop
                if (currentText != upperText) {
                    etUsername.removeTextChangedListener(this)
                    etUsername.setText(upperText)
                    etUsername.setSelection(upperText.length)
                    etUsername.addTextChangedListener(this)
                }
            }
        })

        // ── Password visibility toggle ────────────────────────────────────
        var isPasswordVisible = false
        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye)
            } else {
                etPassword.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeIcon.setImageResource(R.drawable.ic_eye_off)
            }
            etPassword.setSelection(etPassword.text.length)
            isPasswordVisible = !isPasswordVisible
        }

        // ── Optional: Convert existing text when activity starts ──────────
        etUsername.setText(etUsername.text.toString().uppercase())

        // ── Login button ──────────────────────────────────────────────────
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim().uppercase() // Extra uppercase for safety
            val password = etPassword.text.toString().trim()

            if (username.isEmpty()) {
                etUsername.error = "Enter username"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Enter password"
                return@setOnClickListener
            }

            callLoginApi(username, password, btnLogin)
        }
    }

    // ── API Call via Retrofit ─────────────────────────────────────────────
    private fun callLoginApi(username: String, password: String, btnLogin: Button) {
        btnLogin.isEnabled = false
        btnLogin.text      = "Logging in..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.login(
                    LoginRequest(userid = username, password = password)
                )

                // ✅ Log raw response for debugging
                val rawBody = response.errorBody()?.string()
                    ?: response.body()?.toString()
                    ?: "NULL BODY"

                android.util.Log.d("LOGIN_DEBUG", "HTTP Code   : ${response.code()}")
                android.util.Log.d("LOGIN_DEBUG", "isSuccessful: ${response.isSuccessful}")
                android.util.Log.d("LOGIN_DEBUG", "Status      : ${response.body()?.status}")
                android.util.Log.d("LOGIN_DEBUG", "Message     : ${response.body()?.message}")
                android.util.Log.d("LOGIN_DEBUG", "User        : ${response.body()?.user}")
                android.util.Log.d("LOGIN_DEBUG", "Raw/Error   : $rawBody")

                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Login"

                    val body = response.body()

                    if (response.isSuccessful && body != null && body.status == "SUCCESS") {
                        val user  = body.user
                        val perms = body.permissions

                        val prefs = getSharedPreferences("ASPIRA_PREFS", MODE_PRIVATE).edit()

                        prefs.putString("userid",     user?.userid     ?: "")
                        prefs.putString("username",   user?.username   ?: "")
                        prefs.putString("branchId",   user?.branchId   ?: "")
                        prefs.putString("branchName", user?.branchName ?: "")
                        prefs.putString("loginTime",  user?.loginTime  ?: "")

                        prefs.putBoolean("perm_admin",                    perms?.admin                    == "Y")
                        prefs.putBoolean("perm_orgnaizationDetails",      perms?.orgnaizationDetails      == "Y")
                        prefs.putBoolean("perm_userControls",             perms?.userControls             == "Y")
                        prefs.putBoolean("perm_referenceCodeMaintenance", perms?.referenceCodeMaintenance == "Y")
                        prefs.putBoolean("perm_auditTrail",               perms?.auditTrail               == "Y")
                        prefs.putBoolean("perm_dayEndOperation",          perms?.dayEndOperation          == "Y")
                        prefs.putBoolean("perm_customerMaintenance",      perms?.customerMaintenance      == "Y")
                        prefs.putBoolean("perm_loanMaintenance",          perms?.loanMaintenance          == "Y")
                        prefs.putBoolean("perm_migration",                perms?.migration                == "Y")
                        prefs.putBoolean("perm_customerMaster",           perms?.customerMaster           == "Y")
                        prefs.putBoolean("perm_loanMaster",               perms?.loanMaster               == "Y")
                        prefs.putBoolean("perm_loanScheduleMigration",    perms?.loanScheduleMigration    == "Y")
                        prefs.putBoolean("perm_transactionMigration",     perms?.transactionMigration     == "Y")
                        prefs.putBoolean("perm_loanOperation",            perms?.loanOperation            == "Y")
                        prefs.putBoolean("perm_loanOperationLs",          perms?.loanOperationLs          == "Y")
                        prefs.putBoolean("perm_loanClosure",              perms?.loanClosure              == "Y")
                        prefs.putBoolean("perm_transactionMaintenance",   perms?.transactionMaintenance   == "Y")
                        prefs.putBoolean("perm_journalEntries",           perms?.journalEntries           == "Y")
                        prefs.putBoolean("perm_accountLedgerPosting",     perms?.accountLedgerPosting     == "Y")
                        prefs.putBoolean("perm_accountLedger",            perms?.accountLedger            == "Y")
                        prefs.putBoolean("perm_trialBalanceT",            perms?.trialBalanceT            == "Y")
                        prefs.putBoolean("perm_profitAndLossAccountT",    perms?.profitAndLossAccountT    == "Y")
                        prefs.putBoolean("perm_collectionProcess",        perms?.collectionProcess        == "Y")
                        prefs.putBoolean("perm_participatingBanks",       perms?.participatingBanks       == "Y")
                        prefs.putBoolean("perm_loanCollecting",           perms?.loanCollecting           == "Y")
                        prefs.putBoolean("perm_batchJobExecution",        perms?.batchJobExecution        == "Y")
                        prefs.putBoolean("perm_batchJob",                 perms?.batchJob                 == "Y")
                        prefs.putBoolean("perm_inquiriesAndReports",      perms?.inquiriesAndReports      == "Y")
                        prefs.putBoolean("perm_accountBalanceInq",        perms?.accountBalanceInq        == "Y")
                        prefs.putBoolean("perm_intersetSummaryInq",       perms?.intersetSummaryInq       == "Y")
                        prefs.putBoolean("perm_journalBook",              perms?.journalBook              == "Y")
                        prefs.putBoolean("perm_accountLedgersI",          perms?.accountLedgersI          == "Y")
                        prefs.putBoolean("perm_trialBalanceI",            perms?.trialBalanceI            == "Y")
                        prefs.putBoolean("perm_generalLedger",            perms?.generalLedger            == "Y")
                        prefs.putBoolean("perm_profitAndLossAccountI",    perms?.profitAndLossAccountI    == "Y")
                        prefs.putBoolean("perm_balanceSheet",             perms?.balanceSheet             == "Y")
                        prefs.putBoolean("perm_balanceSheets",            perms?.balanceSheets            == "Y")
                        prefs.putBoolean("perm_creditFacilityReport",     perms?.creditFacilityReport     == "Y")
                        prefs.putBoolean("perm_endOfMonthReport",         perms?.endOfMonthReport         == "Y")
                        prefs.putBoolean("perm_dab",                      perms?.dab                      == "Y")
                        prefs.putBoolean("perm_consolidatedReport",       perms?.consolidatedReport       == "Y")
                        prefs.putBoolean("perm_transactionReport",        perms?.transactionReport        == "Y")
                        prefs.putBoolean("perm_interestAccrualReport",    perms?.interestAccrualReport    == "Y")
                        prefs.putBoolean("perm_penaltyAccrualReport",     perms?.penaltyAccrualReport     == "Y")
                        prefs.putBoolean("perm_recoveryReport",           perms?.recoveryReport           == "Y")
                        prefs.putBoolean("perm_demandGeneration",         perms?.demandGeneration         == "Y")
                        prefs.putBoolean("perm_transactionAccounts",      perms?.transactionAccounts      == "Y")
                        prefs.putBoolean("perm_transactionReversal",      perms?.transactionReversal      == "Y")
                        prefs.putBoolean("perm_notificationReports",      perms?.notificationReports      == "Y")

                        prefs.apply()

                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome ${user?.username}",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()

                    } else {
                        // ✅ Show exact server message in toast for debugging
                        val serverMsg = body?.message ?: "No message from server"
                        val serverStatus = body?.status ?: "null status"
                        Toast.makeText(
                            this@LoginActivity,
                            "Status: $serverStatus\nMsg: $serverMsg\nCode: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Login"
                    android.util.Log.e("LOGIN_DEBUG", "Exception: ${e.message}", e)
                    Toast.makeText(
                        this@LoginActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}