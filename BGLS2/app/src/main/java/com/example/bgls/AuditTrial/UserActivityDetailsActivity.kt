package com.example.bgls.AuditTrial

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.UserActivityItem
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class UserActivityDetailsActivity : AppCompatActivity() {

    private lateinit var tvCurrentDate: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserActivityAdapter   // change adapter to use UserActivityItem
    private val activityList = mutableListOf<UserActivityItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)
        setContentView(R.layout.activity_user_details)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
        initViews()
        setupDatePicker()
        setupRecyclerView()
        //setupButtons()
        loadActivitiesForDate(getCurrentDateString())
    }

    private fun initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        recyclerView = findViewById(R.id.recyclerViewUserActivity)
        //btnBack = findViewById(R.id.btnBack)

    }

    private fun setupDatePicker() {
        tvCurrentDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                    tvCurrentDate.text = selectedDate
                    loadActivitiesForDate(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadActivitiesForDate(date: String) {
        RetrofitClient.api.getUserActivities("list", date, "Y")
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        val auditList = body["AuditList"] as? List<Map<String, Any>> ?: emptyList()
                        activityList.clear()
                        for (item in auditList) {
                            activityList.add(
                                UserActivityItem(
                                    audit_date = item["audit_date"] as? String ?: "",
                                    audit_table = item["audit_table"] as? String ?: "",
                                    func_code = item["func_code"] as? String ?: "",
                                    entry_user = item["entry_user"] as? String ?: "",
                                    entry_time = item["entry_time"] as? String ?: "",
                                    auth_user = item["auth_user"] as? String ?: "",
                                    auth_time = item["auth_time"] as? String ?: "",
                                    remarks = item["remarks"] as? String ?: ""
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@UserActivityDetailsActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@UserActivityDetailsActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setupRecyclerView() {
        adapter = UserActivityAdapter(this, activityList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

//    private fun setupButtons() {
//        btnBack.setOnClickListener { onBackPressed() }
//    }
}