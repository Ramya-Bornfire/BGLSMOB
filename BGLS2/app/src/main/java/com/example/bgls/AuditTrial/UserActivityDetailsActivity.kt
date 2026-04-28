package com.example.bgls.AuditTrial

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.AuditTrial.UserActivityAdapter
import com.example.bgls.R
import com.example.bgls.data.model.UserActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserActivityDetailsActivity : AppCompatActivity() {

    private lateinit var tvCurrentDate: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnHome: Button
    private lateinit var btnBack: ImageView

    private lateinit var adapter: UserActivityAdapter

    // ── Dummy data — replace with API call later ──
    private val activityList = mutableListOf(
        UserActivity(
            "24-04-2026",
            "BGLS_USER_PROFILE_TABLE",
            "LOGIN",
            "EMP04",
            "11:32 am",
            "",
            "",
            "LOGGED IN SUCCESSFULLY"
        ),
        UserActivity(
            "24-04-2026",
            "BGLS_USER_PROFILE_TABLE",
            "LOGIN",
            "EMP04",
            "10:37 am",
            "",
            "",
            "LOGGED IN SUCCESSFULLY"
        ),
        UserActivity(
            "24-04-2026",
            "BGLS_USER_PROFILE_TABLE",
            "LOGIN",
            "EMP04",
            "10:15 am",
            "",
            "",
            "LOGGED IN SUCCESSFULLY"
        ),
        UserActivity(
            "24-04-2026",
            "BGLS_USER_PROFILE_TABLE",
            "LOGIN",
            "EMP04",
            "10:03 am",
            "",
            "",
            "LOGGED IN SUCCESSFULLY"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        initViews()
        setCurrentDate()
        setupRecyclerView()
        setupButtons()
    }

    private fun initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        recyclerView = findViewById(R.id.recyclerViewUserActivity)
      //  btnHome = findViewById(R.id.btnHome)
        btnBack = findViewById(R.id.btnBack)
    }

    // ── Show today's date in top right ──
    private fun setCurrentDate() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvCurrentDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = UserActivityAdapter(this, activityList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
//        btnHome.setOnClickListener {
//            // TODO: startActivity(Intent(this, HomeActivity::class.java))
//            Toast.makeText(this, "Navigate to Home", Toast.LENGTH_SHORT).show()
//        }
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}