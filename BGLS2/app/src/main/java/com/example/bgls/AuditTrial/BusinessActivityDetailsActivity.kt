package com.example.bgls.AuditTrial

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.BusinessActivityAdapter
import com.example.bgls.DataModels.BusinessActivityItem
import com.example.bgls.DataModels.BusinessActivityResponse
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BusinessActivityDetailsActivity : AppCompatActivity() {

    private lateinit var tvCurrentDate: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var adapter: BusinessActivityAdapter

    private val activityList = mutableListOf<BusinessActivityItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_details)

        initViews()
        setupRecyclerView()
        setupDatePicker()
        setupButtons()

        val today = getCurrentDateString()
        tvCurrentDate.text = today
        loadActivitiesForDate(today)
    }

    private fun initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        recyclerView = findViewById(R.id.recyclerViewbusinessActivity)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupRecyclerView() {
        adapter = BusinessActivityAdapter(this, activityList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupDatePicker() {
        tvCurrentDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = String.format("%02d/%02d/%d", day, month + 1, year)
                    tvCurrentDate.text = selectedDate
                    loadActivitiesForDate(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupButtons() {
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadActivitiesForDate(date: String) {

        Log.d("BusinessActivity", "Calling API for date: $date")

        RetrofitClient.api.getServiceActivities("list", date, "Y")
            .enqueue(object : Callback<BusinessActivityResponse> {

                override fun onResponse(
                    call: Call<BusinessActivityResponse>,
                    response: Response<BusinessActivityResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {

                        val body = response.body()!!
                        val auditList = body.auditList ?: emptyList()

                        Log.d("BusinessActivity", "Data size: ${auditList.size}")

                        if (auditList.isEmpty()) {
                            Toast.makeText(
                                this@BusinessActivityDetailsActivity,
                                "No data for this date",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        activityList.clear()
                        activityList.addAll(auditList)

                        adapter.notifyDataSetChanged()

                    } else {
                        Log.e("BusinessActivity", "Error: ${response.code()}")

                        Toast.makeText(
                            this@BusinessActivityDetailsActivity,
                            "Failed to load data (${response.code()})",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BusinessActivityResponse>, t: Throwable) {
                    Log.e("BusinessActivity", "Network Error", t)

                    Toast.makeText(
                        this@BusinessActivityDetailsActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
