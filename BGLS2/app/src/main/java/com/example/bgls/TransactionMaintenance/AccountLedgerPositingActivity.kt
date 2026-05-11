package com.example.bgls.TransactionMaintenance

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.AccountLedgerPostingModel
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import kotlinx.coroutines.launch
import android.widget.ImageView

class AccountLedgerPositingActivity : AppCompatActivity() {

    private lateinit var rvAccountLedger: RecyclerView
    private lateinit var adapter: AccountLedgerPositingAdapter
    private lateinit var progressBar: ProgressBar
    private var postingList = mutableListOf<AccountLedgerPostingModel>()
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account_ledger_positing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        loadDataFromAPI()
    }

    private fun initViews() {
        rvAccountLedger = findViewById(R.id.rvAccountLedger)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        btnHome = findViewById(R.id.btnHome)

        btnBack.setOnClickListener { finish() }
        btnHome.setOnClickListener {
            val intent = android.content.Intent(this, com.example.bgls.MainActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // ✅ Fix 1: Disable item change animation — prevents visual left-shift on selection
        rvAccountLedger.itemAnimator = null

        // ✅ Fix 2: Fixed size prevents full relayout when only checked state changes
        rvAccountLedger.setHasFixedSize(true)

        // ✅ Fix 3: Disable nested scrolling — stops RecyclerView fighting HorizontalScrollView
        rvAccountLedger.isNestedScrollingEnabled = false

        val layoutManager = LinearLayoutManager(this)
        rvAccountLedger.layoutManager = layoutManager

        adapter = AccountLedgerPositingAdapter(postingList) { position ->
            val item = postingList[position]
            val intent = android.content.Intent(this, AccountLedgerDetailActivity::class.java)
            intent.putExtra("tranId", item.tranId)
            intent.putExtra("partTranId", item.partTranId)
            intent.putExtra("acctId", item.acctId)
            intent.putExtra("acctName", item.acctName)
            intent.putExtra("amount", item.amount)
            intent.putExtra("tranDate", item.tranDate)
            intent.putExtra("partTranType", item.partTranType)
            intent.putExtra("currency", item.currency)
            intent.putExtra("tranParticular", item.tranParticular)
            intent.putExtra("status", item.status)
            startActivity(intent)
        }
        rvAccountLedger.adapter = adapter
    }

    private fun loadDataFromAPI() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getAccountLedgerPostingList()
                if (response.isSuccessful && response.body() != null) {
                    val jour = response.body()?.jour ?: emptyList()
                    val newData = jour.filter { it.tran_status == "ENTERED" }.map { item ->
                        AccountLedgerPostingModel(
                            tranDate = item.tran_date ?: "",
                            tranId = item.tran_id ?: "",
                            partTranId = item.part_tran_id?.toString() ?: "",
                            partTranType = item.part_tran_type ?: "",
                            currency = item.acct_crncy ?: "",
                            amount = String.format("%.2f", item.tran_amt ?: 0.0),
                            acctId = item.acct_num ?: "",
                            acctName = item.acct_name ?: "",
                            tranParticular = item.tran_particular ?: "",
                            status = item.tran_status ?: ""
                        )
                    }
                    postingList.clear()
                    postingList.addAll(newData)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@AccountLedgerPositingActivity, "Failed to load ledger data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AccountLedgerPositingActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}