package com.example.bgls

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.widget.TextView

class BGLSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                // Try to find the user info views in the activity's layout (check both ID naming conventions)
                val txtUserId = activity.findViewById<TextView>(R.id.txtUserId) ?: activity.findViewById<TextView>(R.id.txtUserIdInfo)
                val txtUserName = activity.findViewById<TextView>(R.id.txtUserName) ?: activity.findViewById<TextView>(R.id.txtUserNameInfo)
                val txtLoginTime = activity.findViewById<TextView>(R.id.txtLoginTime) ?: activity.findViewById<TextView>(R.id.txtLoginTimeInfo)

                // If any of them exist, populate them with data from SharedPreferences
                if (txtUserId != null || txtUserName != null || txtLoginTime != null) {
                    val prefs = activity.getSharedPreferences("ASPIRA_PREFS", Context.MODE_PRIVATE)
                    
                    val userid = prefs.getString("userid", "")
                    val username = prefs.getString("username", "")
                    val loginTime = prefs.getString("loginTime", "")

                    txtUserId?.text = userid
                    txtUserName?.text = username
                    txtLoginTime?.text = loginTime
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
