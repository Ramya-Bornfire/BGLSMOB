package com.example.bgls.CustomerMaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.LoanSchedule.LoanScheduleActivity
import com.example.bgls.R

class LoanScheduleViewActivity : AppCompatActivity() {

    private lateinit var recyclerViewLoanSchedule: RecyclerView
    private lateinit var adapter: LoanScheduleAdapter
    private lateinit var btnAccount: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_schedule_view)
        
        btnAccount = findViewById(R.id.btnAccount)
        recyclerViewLoanSchedule = findViewById(R.id.recyclerViewLoanSchedule)

        setupTable()
        setupButtons()
    }

    private fun setupButtons() {
        val fromLoanSchedule = intent.getBooleanExtra("is_from_loan_schedule", false)
        
        btnAccount.setOnClickListener {
            if (fromLoanSchedule) {
                // Navigate to next screen (LoanScheduleActivity)
                val intent = Intent(this, LoanMasterViewActivity::class.java)
                startActivity(intent)
            } else {
                // Acts as back button
                finish()
            }
        }
    }

    private fun setupTable() {
        val dummyData = listOf(
            LoanScheduleItem("19-06-2023", "10,607.00", "7,001.00", "535.00", "0.00", "19-06-2023", "10,607.00", "7,001.00", "535.00", "0.00", "0.00"),
            LoanScheduleItem("19-07-2023", "10,607.00", "7,001.00", "535.00", "0.00", "02-08-2023", "10,607.00", "7,001.00", "535.00", "0.00", "0.00"),
            LoanScheduleItem("18-08-2023", "10,607.00", "7,001.00", "535.00", "2,378.00", "09-11-2023", "10,607.00", "7,001.00", "535.00", "2,378.00", "0.00"),
            LoanScheduleItem("17-09-2023", "10,607.00", "7,001.00", "3,662.00", "6,821.00", "25-03-2024", "10,607.00", "7,001.00", "3,662.00", "6,821.00", "0.00"),
            LoanScheduleItem("17-10-2023", "10,607.00", "7,001.00", "3,659.00", "15,993.00", "05-11-2024", "10,607.00", "7,001.00", "3,659.00", "15,993.00", "0.00"),
            LoanScheduleItem("16-11-2023", "10,607.00", "7,001.00", "3,659.00", "30,185.00", "", "0.00", "0.00", "3,659.00", "4,069.30", "43,723.70"),
            LoanScheduleItem("16-12-2023", "10,607.00", "7,001.00", "3,659.00", "28,865.00", "", "0.00", "0.00", "0.00", "0.00", "50,132.00"),
            LoanScheduleItem("15-01-2024", "10,607.00", "7,001.00", "3,659.00", "27,545.00", "", "0.00", "0.00", "0.00", "0.00", "48,812.00")
        )

        recyclerViewLoanSchedule.layoutManager = LinearLayoutManager(this)
        adapter = LoanScheduleAdapter(this, dummyData)
        recyclerViewLoanSchedule.adapter = adapter
    }
}
