package com.example.bgls.UserControl

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.EmployeeListResponse
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.DataModels.UserProfile
import com.example.bgls.DataModels.UserProfileResponse
import com.example.bgls.MainActivity
import com.example.bgls.R
import com.example.bgls.Retrofit.RetrofitClient
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserControlActivity : AppCompatActivity(),
    UserProfileAdapter.OnActionClickListener,
    EmployeeProfileAdapter.OnActionClickListener {

    // Views
    private lateinit var btnUserProfile: Button
    private lateinit var btnEmployeeProfile: Button
    private lateinit var btnCreateUser: Button
    private lateinit var layoutUserProfile: LinearLayout
    private lateinit var layoutEmployeeProfile: LinearLayout
    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var recyclerViewEmployees: RecyclerView

    // Adapters
    private lateinit var userAdapter: UserProfileAdapter
    private lateinit var employeeAdapter: EmployeeProfileAdapter

    private var isUserProfileTabActive = true


    // Launcher for user add/edit – refresh list
    private val userResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadUserProfiles()
        }
    }

    // Launcher for employee add/edit – refresh list
    private val employeeResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadEmployeeProfiles()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_control)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        initViews()
        setupRecyclerViews()
        setupToggleButtons()
        setupBottomButtons()
        loadUserProfiles()
        loadEmployeeProfiles()

        selectTab(isUserProfile = true)
    }

    private fun initViews() {
        btnUserProfile = findViewById(R.id.btnUserProfile)
        btnEmployeeProfile = findViewById(R.id.btnEmployeeProfile)
        btnCreateUser = findViewById(R.id.btnCreateUser)
        layoutUserProfile = findViewById(R.id.layoutUserProfile)
        layoutEmployeeProfile = findViewById(R.id.layoutEmployeeProfile)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
        recyclerViewEmployees = findViewById(R.id.recyclerViewEmployees)

    }

    private fun setupRecyclerViews() {
        userAdapter = UserProfileAdapter(this, mutableListOf(), this)
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter

        employeeAdapter = EmployeeProfileAdapter(this, mutableListOf(), this)
        recyclerViewEmployees.layoutManager = LinearLayoutManager(this)
        recyclerViewEmployees.adapter = employeeAdapter
    }

    private fun setupToggleButtons() {
        btnUserProfile.setOnClickListener { selectTab(isUserProfile = true) }
        btnEmployeeProfile.setOnClickListener { selectTab(isUserProfile = false) }
    }

    private fun selectTab(isUserProfile: Boolean) {
        isUserProfileTabActive = isUserProfile
        if (isUserProfile) {
            btnUserProfile.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.holo_blue_light))
            btnUserProfile.setTextColor(getColor(android.R.color.white))
            btnEmployeeProfile.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.white))
            btnEmployeeProfile.setTextColor(getColor(android.R.color.black))
            layoutUserProfile.visibility = View.VISIBLE
            layoutEmployeeProfile.visibility = View.GONE
            btnCreateUser.text = "Create User"
        } else {
            btnEmployeeProfile.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.holo_blue_light))
            btnEmployeeProfile.setTextColor(getColor(android.R.color.white))
            btnUserProfile.backgroundTintList = ColorStateList.valueOf(getColor(android.R.color.white))
            btnUserProfile.setTextColor(getColor(android.R.color.black))
            layoutUserProfile.visibility = View.GONE
            layoutEmployeeProfile.visibility = View.VISIBLE
            btnCreateUser.text = "Create Employee"
        }
    }

    private fun setupBottomButtons() {
        btnCreateUser.setOnClickListener {
            if (isUserProfileTabActive) {
                val intent = Intent(this, UserProfileAddActivity::class.java).apply {
                    putExtra(UserProfileAddActivity.EXTRA_MODE, UserProfileAddActivity.MODE_ADD)
                }
                userResultLauncher.launch(intent)
            } else {
                val intent = Intent(this, EmployeProfileAddActivity::class.java).apply {
                    putExtra(EmployeProfileAddActivity.EXTRA_MODE, EmployeProfileAddActivity.MODE_ADD)
                }
                employeeResultLauncher.launch(intent)
            }
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val btnHome = findViewById<ImageView>(R.id.btnHome)
        btnHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    // ────────── User Profile API calls ──────────
    private fun loadUserProfiles() {
        RetrofitClient.api.getUserProfiles("list").enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    val users = response.body()?.userProfiles ?: emptyList()
                    userAdapter.updateList(users.toMutableList())
                    Log.d("API_DEBUG", "User count: ${users.size}")
                } else {
                    val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                    Log.e("API_DEBUG", "User list failed: $err")
                    Toast.makeText(this@UserControlActivity, "Failed to load users: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // ────────── Employee Profile API calls ──────────
    private fun loadEmployeeProfiles() {
        RetrofitClient.api.getEmployeeProfiles("list").enqueue(object : Callback<EmployeeListResponse> {
            override fun onResponse(call: Call<EmployeeListResponse>, response: Response<EmployeeListResponse>) {
                if (response.isSuccessful) {
                    val employees = response.body()?.EmployeeList ?: emptyList()
                    employeeAdapter.updateList(employees.toMutableList())
                    Log.d("API_DEBUG", "Employee count: ${employees.size}")
                } else {
                    Toast.makeText(this@UserControlActivity, "Failed to load employees", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<EmployeeListResponse>, t: Throwable) {
                Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ────────── User Profile Actions ──────────
    override fun onView(user: UserProfile) {
        val intent = Intent(this, UserProfileAddActivity::class.java).apply {
            putExtra(UserProfileAddActivity.EXTRA_MODE, UserProfileAddActivity.MODE_VIEW)
            putExtra(UserProfileAddActivity.EXTRA_USER_ID, user.userId)
        }
        startActivity(intent)
    }

    override fun onEdit(user: UserProfile) {
        val intent = Intent(this, UserProfileAddActivity::class.java).apply {
            putExtra(UserProfileAddActivity.EXTRA_MODE, UserProfileAddActivity.MODE_EDIT)
            putExtra(UserProfileAddActivity.EXTRA_USER_ID, user.userId)
        }
        userResultLauncher.launch(intent)
    }

    override fun onDelete(user: UserProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Delete ${user.userName}?")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.api.deleteUser(user.userId!!).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            userAdapter.removeItem(position)
                            Toast.makeText(this@UserControlActivity, "User deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@UserControlActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onVerify(user: UserProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Verify User")
            .setMessage("Verify ${user.userName}?")
            .setPositiveButton("Verify") { _, _ ->
                RetrofitClient.api.verifyUser(user.userId!!).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UserControlActivity, "User verified", Toast.LENGTH_SHORT).show()
                            loadUserProfiles()
                        } else {
                            Toast.makeText(this@UserControlActivity, "Verification failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResetPassword(user: UserProfile) {
        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Reset password for ${user.userName}?")
            .setPositiveButton("Reset") { _, _ ->
                RetrofitClient.api.resetPassword(user.userId!!).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        val msg = if (response.isSuccessful) "Password reset successful" else "Reset failed"
                        Toast.makeText(this@UserControlActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ────────── Employee Actions ──────────
    override fun onView(employee: EmployeeProfile) {
        val intent = Intent(this, EmployeProfileAddActivity::class.java).apply {
            putExtra(EmployeProfileAddActivity.EXTRA_MODE, EmployeProfileAddActivity.MODE_VIEW)
            putExtra(EmployeProfileAddActivity.EXTRA_EMPLOYEE_ID, employee.employeeId)
        }
        startActivity(intent)
    }

    override fun onEdit(employee: EmployeeProfile) {
        val intent = Intent(this, EmployeProfileAddActivity::class.java).apply {
            putExtra(EmployeProfileAddActivity.EXTRA_MODE, EmployeProfileAddActivity.MODE_EDIT)
            putExtra(EmployeProfileAddActivity.EXTRA_EMPLOYEE_ID, employee.employeeId)
        }
        employeeResultLauncher.launch(intent)
    }

    override fun onVerify(employee: EmployeeProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Verify Employee")
            .setMessage("Verify ${employee.employeeName}?")
            .setPositiveButton("Verify") { _, _ ->
                RetrofitClient.api.verifyEmployee(employee.employeeId!!).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@UserControlActivity, "Employee verified", Toast.LENGTH_SHORT).show()
                            loadEmployeeProfiles()
                        } else {
                            Toast.makeText(this@UserControlActivity, "Verification failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDelete(employee: EmployeeProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Employee")
            .setMessage("Delete ${employee.employeeName}?")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.api.deleteEmployee(employee.employeeId!!).enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) {
                            employeeAdapter.removeItem(position)
                            Toast.makeText(this@UserControlActivity, "Employee deleted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@UserControlActivity, "Delete failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Toast.makeText(this@UserControlActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}