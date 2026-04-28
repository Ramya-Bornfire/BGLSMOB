package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName
import java.util.Date

data class EmployeeProfile(
    @SerializedName("branch_desc") val branchDesc: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("employee_id") val employeeId: String? = null,
    @SerializedName("employee_name") val employeeName: String? = null,
    @SerializedName("bank") val bank: String? = null,
    @SerializedName("bank_act_no") val bankActNo: String? = null,
    @SerializedName("doj") val doj: String? = null,          // date as string (ISO or timestamp)
    @SerializedName("dob") val dob: String? = null,
    @SerializedName("branch_id") val branchId: String? = null,
    @SerializedName("department") val department: String? = null,
    @SerializedName("design") val design: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("qual") val qual: String? = null,
    @SerializedName("addl_qual") val addlQual: String? = null,
    @SerializedName("passport") val passport: String? = null,
    @SerializedName("driving_license") val drivingLicense: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("blood_group") val bloodGroup: String? = null,
    @SerializedName("marital_status") val maritalStatus: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("alt_mobile") val altMobile: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("addr1") val addr1: String? = null,
    @SerializedName("addr2") val addr2: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("postal_code") val postalCode: String? = null,
    @SerializedName("emer_contact_person") val emerContactPerson: String? = null,
    @SerializedName("emer_contact_num") val emerContactNum: String? = null,
    @SerializedName("employee_Remarks") val employeeRemarks: String? = null,
    @SerializedName("employee_Photo") val employeePhoto: String? = null,  // base64 string
    // Audit fields (optional for display)
    @SerializedName("entry_user") val entryUser: String? = null,
    @SerializedName("entry_time") val entryTime: String? = null,
    @SerializedName("modify_user") val modifyUser: String? = null,
    @SerializedName("modify_time") val modifyTime: String? = null,
    @SerializedName("verify_flg") val verifyFlg: String? = null
)

data class EmployeeListResponse(
    val formmode: String?,
    val EmployeeList: List<EmployeeProfile>?
)

data class SingleEmployeeResponse(
    val formmode: String?,
    val employee: EmployeeProfile?
)