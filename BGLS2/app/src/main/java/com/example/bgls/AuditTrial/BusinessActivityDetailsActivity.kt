package com.example.bgls.AuditTrial

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.Adapter.BusinessActivityAdapter
import com.example.bgls.R
import com.example.bgls.data.model.BusinessActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessActivityDetailsActivity : AppCompatActivity() {

    private lateinit var tvCurrentDate: TextView
    private lateinit var recyclerView: RecyclerView
   // private lateinit var btnHome: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_business_details)

        initViews()
        setCurrentDate()
        setupRecyclerView()
        setupButtons()
    }

    private fun initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        recyclerView = findViewById(R.id.recyclerViewbusinessActivity)
       // btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {

        val activityList = listOf(
            BusinessActivity(
                auditDate = "",
                tableName = "",
                function = "",
                entryUser = "",
                entryTime = "",
                authorizer = "",
                authorizerTime = "",
                FieldName = "",
                NewValue = "",
                OldValue = "",
                remarks = ""
            )

        )

        val adapter = BusinessActivityAdapter(this, activityList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

//        btnHome.setOnClickListener {
//            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
//        }
    }
}