package com.example.bgls

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bgls.DataModels.EmployeeProfile
import com.example.bgls.DataModels.UserProfile

class UserControlActivity : AppCompatActivity(),
    UserProfileAdapter.OnActionClickListener,
    EmployeeProfileAdapter.OnActionClickListener {

    // ─── Views ───
    private lateinit var btnUserProfile: Button
    private lateinit var btnEmployeeProfile: Button
    private lateinit var btnCreateUser: Button

    private lateinit var layoutUserProfile: LinearLayout
    private lateinit var layoutEmployeeProfile: LinearLayout

    private lateinit var recyclerViewUsers: RecyclerView
    private lateinit var recyclerViewEmployees: RecyclerView

    // ─── Adapters ───
    private lateinit var userAdapter: UserProfileAdapter
    private lateinit var employeeAdapter: EmployeeProfileAdapter

    // ─── Track active tab ───
    private var isUserProfileTabActive = true

    // ══════════════════════════════════════════
    // ✅ FIXED: createUserLauncher INSIDE class
    // ══════════════════════════════════════════
    private val createUserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data

            val userId   = data?.getStringExtra("newUserId") ?: ""
            val userName = data?.getStringExtra("newUserName") ?: ""
            val status   = data?.getStringExtra("newUserStatus") ?: "Active"

            // Add new user to list and refresh RecyclerView
            val newUser = UserProfile(userId, userName, status)
            userProfileList.add(newUser)
            userAdapter.notifyItemInserted(userProfileList.size - 1)

            // Scroll to bottom to show new user
            recyclerViewUsers.scrollToPosition(userProfileList.size - 1)

            Toast.makeText(this, "$userName added to list!", Toast.LENGTH_SHORT).show()
        }
    }
    private val createEmployeeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val newEmployee = EmployeeProfile(
                srlNo     = data?.getStringExtra("newSrlNo") ?: "",
                employeeId = data?.getStringExtra("newEmployeeId") ?: "",
                name       = data?.getStringExtra("newName") ?: "",
                designation = data?.getStringExtra("newDesignation") ?: "",
                category   = data?.getStringExtra("newCategory") ?: "",
                mobile     = data?.getStringExtra("newMobile") ?: "",
                email      = data?.getStringExtra("newEmail") ?: "",
                profileStatus = data?.getStringExtra("newProfileStatus") ?: "Verified"
            )
            employeeProfileList.add(newEmployee)
            employeeAdapter.notifyItemInserted(employeeProfileList.size - 1)
            recyclerViewEmployees.scrollToPosition(employeeProfileList.size - 1)
            Toast.makeText(this, "${newEmployee.name} added!", Toast.LENGTH_SHORT).show()
        }
    }

    // ─── Dummy Data ───
    private val userProfileList = mutableListOf(
        UserProfile("EMP01", "SIDHAIYAN", "Verified"),
        UserProfile("EMP02", "Dylan Doolun", "Verified"),
        UserProfile("EMP03", "Tejas Babbea", "Verified"),
        UserProfile("EMP04", "MANIVANAN", "Verified"),
        UserProfile("EMP05", "Suchindra Devalam", "Verified"),
        UserProfile("EMP06", "Sonam Jhugdamby", "Verified"),
        UserProfile("EMP07", "Anthony Kithaka", "Verified"),
        UserProfile("EMP08", "Marie-Christine John Chuan", "Verified"),
        UserProfile("EMP09", "Pooja Reedye", "Verified"),
        UserProfile("EMP10", "Anishta Gungadin", "Verified")
    )

    private val employeeProfileList = mutableListOf(
        EmployeeProfile("1","EMP01","SIDHAIYAN SIR","CEO","Full Time","+230 5000 0001","sidhaiyan@cim.mu","Verified"),
        EmployeeProfile("2","EMP02","KALIDASS SIR","CTO","Full Time","+230 5000 0002","dylan@cim.mu","Verified"),
        EmployeeProfile("3","EMP03","Tejas Babbea","Analyst","Part Time","+230 5000 0003","tejas@cim.mu","Verified"),
        EmployeeProfile("4","EMP04","MANIVANAN","HR Executive","Full Time","+230 5000 0004","mani@cim.mu","Verified"),
        EmployeeProfile("5","EMP05","Suchindra Devalam","Accountant","Full Time","+230 5000 0005","suchi@cim.mu","Pending"),
        EmployeeProfile("6","EMP06","Sonam Jhugdamby","Tester","Contract","+230 5000 0006","sonam@cim.mu","Verified"),
        EmployeeProfile("7","EMP07","Anthony Kithaka","Team Lead","Full Time","+230 5000 0007","anthony@cim.mu","Verified"),
        EmployeeProfile("8","EMP08","Marie-Christine John Chuan","Designer","Full Time","+230 5000 0008","marie@cim.mu","Verified"),
        EmployeeProfile("9","EMP09","Pooja Reedye","Support","Part Time","+230 5000 0009","pooja@cim.mu","Pending"),
        EmployeeProfile("10","EMP10","Anishta Gungadin","Finance","Full Time","+230 5000 0010","anishta@cim.mu","Verified")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_control)

        initViews()
        setupRecyclerViews()
        setupToggleButtons()
        setupBottomButtons()

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
        userAdapter = UserProfileAdapter(this, userProfileList, this)
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter

        employeeAdapter = EmployeeProfileAdapter(this, employeeProfileList, this)
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
            btnUserProfile.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_blue_light))
            btnUserProfile.setTextColor(getColor(android.R.color.white))
            btnEmployeeProfile.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.white))
            btnEmployeeProfile.setTextColor(getColor(android.R.color.black))

            layoutUserProfile.visibility = View.VISIBLE
            layoutEmployeeProfile.visibility = View.GONE
            btnCreateUser.text = "Create User"

        } else {
            btnEmployeeProfile.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.holo_blue_light))
            btnEmployeeProfile.setTextColor(getColor(android.R.color.white))
            btnUserProfile.backgroundTintList =
                android.content.res.ColorStateList.valueOf(getColor(android.R.color.white))
            btnUserProfile.setTextColor(getColor(android.R.color.black))

            layoutUserProfile.visibility = View.GONE
            layoutEmployeeProfile.visibility = View.VISIBLE
            btnCreateUser.text = "Create Employee"
        }
    }

    private fun setupBottomButtons() {
        btnCreateUser.setOnClickListener {
            if (isUserProfileTabActive) {
                // ✅ Use launcher so new user comes back to list
                createUserLauncher.launch(
                    Intent(this, UserProfileAddActivity::class.java)
                )
            } else {
                createEmployeeLauncher.launch(
                    Intent(this, EmployeProfileAddActivity::class.java))
            }
        }
    }

    // ─── User Profile Actions ───
    override fun onView(user: UserProfile) {
        Toast.makeText(this, "View: ${user.userId} - ${user.userName}", Toast.LENGTH_SHORT).show()
    }
    override fun onEdit(user: UserProfile) {
        Toast.makeText(this, "Edit: ${user.userId} - ${user.userName}", Toast.LENGTH_SHORT).show()
    }
    override fun onDelete(user: UserProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Delete ${user.userName}?")
            .setPositiveButton("Delete") { _, _ -> userAdapter.removeItem(position) }
            .setNegativeButton("Cancel", null).show()
    }

    // ─── Employee Profile Actions ───
    override fun onView(employee: EmployeeProfile) {
        Toast.makeText(this, "View: ${employee.employeeId} - ${employee.name}", Toast.LENGTH_SHORT).show()
    }
    override fun onEdit(employee: EmployeeProfile) {
        Toast.makeText(this, "Edit: ${employee.employeeId} - ${employee.name}", Toast.LENGTH_SHORT).show()
    }
    override fun onDelete(employee: EmployeeProfile, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Employee")
            .setMessage("Delete ${employee.name}?")
            .setPositiveButton("Delete") { _, _ -> employeeAdapter.removeItem(position) }
            .setNegativeButton("Cancel", null).show()
    }
}