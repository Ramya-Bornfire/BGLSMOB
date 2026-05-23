package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserProfile(
    @SerializedName("userid") val userId: String?,
    @SerializedName("username") val userName: String?,
    @SerializedName("user_status") val userStatus: String?,
    @SerializedName("auth_flg") val authFlg: String? = "Y",
    @SerializedName("disable_flg") val disableFlg: String? = null,
    @SerializedName("entity_flg") val entityFlg: String? = "Y",
    @SerializedName("login_status") val loginStatus: String? = "Active",
    @SerializedName("modify_flg") val modifyFlg: String? = "N",
    @SerializedName("user_locked_flg") val userLockedFlg: String? = "N",
    @SerializedName("login_flg") val loginFlg: String? = "N",

    @SerializedName("mob_number") val mobileNumber: String? = null,
    @SerializedName("email_id") val emailId: String? = null,
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("branch_des") val branchDes: String? = null,

    @SerializedName("disable_start_date") val disableStartDate: Any? = null,
    @SerializedName("disable_end_date") val disableEndDate: Any? = null,
    @SerializedName("pass_exp_date") val passwordExpiryDate: Any? = null,
    @SerializedName("acc_exp_date") val accountExpiryDate: Any? = null,

    @SerializedName("role_id") val roleId: String? = null,
    @SerializedName("role_desc") val roleDesc: String? = null,
    @SerializedName("permissions") val permissions: String? = null,
    @SerializedName("work_class") val workClass: String? = null,
    @SerializedName("acct_access_code") val acctAccessCode: String? = "ALL",
    @SerializedName("doc_access_code") val docAccessCode: String? = "ALL",
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("password") val password: String? = null
)

data class UserProfileResponse(
    val formmode: String?,
    val userProfiles: List<UserProfile>?
)

data class SingleUserResponse(
    val formmode: String?,
    val userProfile: UserProfile?
)