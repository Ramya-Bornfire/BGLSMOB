package com.example.bgls.DataModels

import com.google.gson.annotations.SerializedName

data class Branch(
    val srlNo: Int,
    val branchCode: String,
    val branchName: String,
    val swiftCode: String,
    val branchHead: String,
    val designation: String?,
    val remarks: String?,
    val landline: String?,
    val fax: String?,
    val mobile: String?,
    val contactPerson: String?,
    val website: String?,
    val email: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val zipCode: String?
)

//data class BranchDto(
//
//    @SerializedName("branch_code")
//    val branchCode: String?,
//
//    @SerializedName("branch_name")
//    val branchName: String?,
//
//    @SerializedName("swift_code")
//    val swiftCode: String?,
//
//    @SerializedName("branch_head")
//    val branchHead: String?
//)
data class BranchDto(
    @SerializedName("branch_code") val branchCode: String?,
    @SerializedName("branch_name") val branchName: String?,
    @SerializedName("branch_head") val branchHead: String?,
    @SerializedName("designation") val designation: String?,
    @SerializedName("swift_code") val swiftCode: String?,
    @SerializedName("remarks") val remarks: String?,
    @SerializedName("land_line") val landline: String?,
    @SerializedName("fax") val fax: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("cont_person") val contactPerson: String?,
    @SerializedName("website") val website: String?,
    @SerializedName("mail_id") val email: String?,
    @SerializedName("add_1") val address1: String?,
    @SerializedName("add_2") val address2: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("zip_code") val zip: String?
)